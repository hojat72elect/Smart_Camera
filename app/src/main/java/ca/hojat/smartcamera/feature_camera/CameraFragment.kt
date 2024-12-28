package ca.hojat.smartcamera.feature_camera

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.hardware.display.DisplayManager
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.camera.core.CameraInfoUnavailableException
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCapture.Metadata
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.net.toFile
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.Navigation
import androidx.window.WindowManager
import ca.hojat.smartcamera.KEY_EVENT_ACTION
import ca.hojat.smartcamera.KEY_EVENT_EXTRA
import ca.hojat.smartcamera.R
import ca.hojat.smartcamera.core.CameraTimer
import ca.hojat.smartcamera.core.Constants
import ca.hojat.smartcamera.core.Constants.ANIMATION_FAST_MILLIS
import ca.hojat.smartcamera.core.Constants.ANIMATION_SLOW_MILLIS
import ca.hojat.smartcamera.core.analyzer.LuminosityAnalyzer
import ca.hojat.smartcamera.core.extensions.simulateClick
import ca.hojat.smartcamera.databinding.FragmentCameraBinding
import ca.hojat.smartcamera.feature_gallery.EXTENSION_WHITELIST
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import java.io.File
import java.util.Locale
import java.util.concurrent.Executors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber


/**
 * Main fragment for this app. Implements all camera operations including:
 * - Viewfinder
 * - Photo taking
 * - Image analysis
 */
class CameraFragment : Fragment() {

    private var _fragmentCameraBinding: FragmentCameraBinding? = null
    private val binding get() = _fragmentCameraBinding!!

    private lateinit var outputDirectory: File

    private var displayId: Int = -1

    private val displayManager by lazy {
        requireContext().getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
    } // this will remain in fragment

    /** Volume down button receiver used to trigger shutter */
    private val volumeDownReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.getIntExtra(KEY_EVENT_EXTRA, KeyEvent.KEYCODE_UNKNOWN)) {
                // When the volume down button is pressed, simulate a shutter button click
                KeyEvent.KEYCODE_VOLUME_DOWN -> {
                    binding.cameraCaptureButton.simulateClick()
                }
            }
        }
    } // this will remain in fragment

    /**
     * We need a display listener for orientation changes that do not trigger a configuration
     * change, for example if we choose to override config change in manifest or for 180-degree
     * orientation changes.
     */
    private val displayListener = object : DisplayManager.DisplayListener {
        override fun onDisplayAdded(displayId: Int) = Unit
        override fun onDisplayRemoved(displayId: Int) = Unit
        override fun onDisplayChanged(displayId: Int) = view?.let { view ->
            if (displayId == this@CameraFragment.displayId) {
                Timber.d("Rotation changed: ${view.display.rotation}")
                vm.imageCapture?.targetRotation = view.display.rotation
                vm.imageAnalyzer?.targetRotation = view.display.rotation
            }
        } ?: Unit
    } // this will remain in fragment

    private val vm: CameraViewModel by viewModel()

    override fun onResume() {
        super.onResume()
        // Make sure that all permissions are still present, since the
        // user could have removed them while the app was in paused state.
        if (!CameraViewModel.hasPermissions(requireContext())) {
            vm.askPermissions(requireContext(), this, PERMISSIONS_REQUEST_CODE)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _fragmentCameraBinding = null

        // Shut down our background executor
        vm.cameraExecutor.shutdown()

        // Unregister the broadcast receivers and listeners
        vm.broadcastManager.unregisterReceiver(volumeDownReceiver)// this will remain in fragment
        displayManager.unregisterDisplayListener(displayListener)// this will remain in fragment
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        vm.askPermissions(requireContext(), this, PERMISSIONS_REQUEST_CODE)

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _fragmentCameraBinding = FragmentCameraBinding.inflate(inflater, container, false)
        return binding.root
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (PackageManager.PERMISSION_GRANTED != grantResults.firstOrNull()) {
                // user has not granted the permissions
                Toast.makeText(
                    requireContext(),
                    "Permissions are required for the app to work correctly",
                    Toast.LENGTH_SHORT
                ).show()
                vm.askPermissions(requireContext(), this, PERMISSIONS_REQUEST_CODE)

            }
        }

    }

    private fun setGalleryThumbnail(uri: Uri) {
        // Run the operations in the view's thread
        binding.photoViewButton.let { photoViewButton ->
            photoViewButton.post {
                // Remove thumbnail padding
                photoViewButton.setPadding(resources.getDimension(R.dimen.stroke_small).toInt())

                // Load thumbnail into circular button using Glide
                Glide.with(photoViewButton)
                    .load(uri)
                    .apply(RequestOptions.circleCropTransform())
                    .into(photoViewButton)
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.fragment = this

        // Initialize our background executor
        vm.cameraExecutor = Executors.newSingleThreadExecutor()

        vm.broadcastManager = LocalBroadcastManager.getInstance(view.context)

        // Set up the intent filter that will receive events from our main activity
        val filter = IntentFilter().apply { addAction(KEY_EVENT_ACTION) }
        vm.broadcastManager.registerReceiver(
            volumeDownReceiver,
            filter
        )// this will remain in fragment

        // Every time the orientation of device changes, update rotation for use cases
        displayManager.registerDisplayListener(displayListener, null)// this will remain in fragment

        //Initialize WindowManager to retrieve display metrics
        vm.windowManager = WindowManager(view.context)

        // Determine the output directory
        outputDirectory = CameraViewModel.getOutputDirectory(requireContext())

        // Wait for the views to be properly laid out
        binding.viewFinder.post {

            // Keep track of the display in which this view is attached
            displayId = binding.viewFinder.display.displayId

            // Build UI controls
            updateCameraUi()

            // Set up the camera and its use cases
            setUpCamera()
        }
    }

    /**
     * Inflate camera controls and update the UI manually upon config changes to avoid removing
     * and re-adding the view finder from the view hierarchy; this provides a seamless rotation
     * transition on devices that support it.
     *
     * NOTE: The flag is supported starting in Android 8 but there still is a small flash on the
     * screen for devices that run Android 9 or below.
     */
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        // Rebind the camera with the updated display metrics
        bindCameraUseCases()

        // Enable or disable switching between cameras
        updateCameraSwitchButton()
    }

    /** Initialize CameraX, and prepare to bind the camera use cases  */
    private fun setUpCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({

            // CameraProvider
            vm.cameraProvider = cameraProviderFuture.get()

            // Select lensFacing depending on the available cameras
            vm.lensFacing = when {
                vm.hasBackCamera() -> CameraSelector.LENS_FACING_BACK
                vm.hasFrontCamera() -> CameraSelector.LENS_FACING_FRONT
                else -> throw IllegalStateException("Back and front camera are unavailable")
            }

            // Enable or disable switching between cameras
            updateCameraSwitchButton()

            // Build and bind the camera use cases
            bindCameraUseCases()
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    /** Declare and bind preview, capture and analysis use cases */
    private fun bindCameraUseCases() {

        val screenAspectRatio = vm.getAspectRatio()
        val displayRotation = binding.viewFinder.display.rotation

        // CameraProvider
        val cameraProvider = vm.cameraProvider
            ?: throw IllegalStateException("Camera initialization failed.")

        // CameraSelector
        val cameraSelector = CameraSelector.Builder().requireLensFacing(vm.lensFacing).build()

        // Preview
        vm.preview = Preview.Builder()
            // We request aspect ratio but no resolution
            .setTargetAspectRatio(screenAspectRatio)
            // Set initial target rotation
            .setTargetRotation(displayRotation)
            .build()

        // ImageCapture
        vm.imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            // We request aspect ratio but no resolution to match preview config, but letting
            // CameraX optimize for whatever specific resolution best fits our use cases
            .setTargetAspectRatio(screenAspectRatio)
            // Set initial target rotation, we will have to call this again if rotation changes
            // during the lifecycle of this use case
            .setTargetRotation(displayRotation)
            .build()

        // ImageAnalysis
        vm.imageAnalyzer = ImageAnalysis.Builder()
            // We request aspect ratio but no resolution
            .setTargetAspectRatio(screenAspectRatio)
            // Set initial target rotation, we will have to call this again if rotation changes
            // during the lifecycle of this use case
            .setTargetRotation(displayRotation)
            .build()
            // The analyzer can then be assigned to the instance
            .also {
                it.setAnalyzer(vm.cameraExecutor, LuminosityAnalyzer { luma ->
                    // Values returned from our analyzer are passed to the attached listener
                    // We log image analysis results here - you should do something useful
                    // instead!
                    Timber.d("Average luminosity: $luma")
                })
            }

        // Must unbind the use-cases before rebinding them
        cameraProvider.unbindAll()

        try {
            // A variable number of use-cases can be passed here -
            // camera provides access to CameraControl & CameraInfo
            vm.camera = cameraProvider.bindToLifecycle(
                this, cameraSelector, vm.preview, vm.imageCapture, vm.imageAnalyzer
            )

            // Attach the viewfinder's surface provider to preview use case
            vm.preview?.setSurfaceProvider(binding.viewFinder.surfaceProvider)
        } catch (e: Exception) {
            Timber.e("Use case binding failed ${e.message}")
        }
    }

    /** Method used to re-draw the camera UI controls, called every time configuration changes. */
    private fun updateCameraUi() {
        lifecycleScope.launch(Dispatchers.IO) {
            outputDirectory.listFiles { file ->
                EXTENSION_WHITELIST.contains(file.extension.uppercase(Locale.ROOT))
            }?.maxOrNull()?.let {
                setGalleryThumbnail(Uri.fromFile(it))
            }
        }

        binding.cameraSwitchButton.isEnabled = false
    }

    fun takePicture() {
        lifecycleScope.launch(Dispatchers.Main) {
            when (vm.selectedTimer) {
                CameraTimer.SEC3 -> for (i in 3 downTo 1) {
                    binding.countdown.text = i.toString()
                    delay(1000)
                }

                CameraTimer.SEC10 -> for (i in 10 downTo 1) {
                    binding.countdown.text = i.toString()
                    delay(1000)
                }

                else -> binding.countdown.text = ""
            }
            binding.countdown.text = ""
            imagePhoto()
        }
    }

    private fun imagePhoto() {
        // Get a stable reference of the modifiable image
        // capture use case
        vm.imageCapture?.let { imageCapture ->

            // Create output file to hold the image
            val photoFile =
                CameraViewModel.createFile(
                    outputDirectory,
                    Constants.FILENAME,
                    Constants.PHOTO_EXTENSION
                )

            // Setup image capture metadata
            val metadata = Metadata().apply {

                // Mirror image when using the front camera
                isReversedHorizontal = vm.lensFacing == CameraSelector.LENS_FACING_FRONT
            }

            // Create output options object which contains file + metadata
            val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile)
                .setMetadata(metadata)
                .build()

            // Setup image capture listener which is triggered after photo has been taken
            imageCapture.takePicture(
                outputOptions, vm.cameraExecutor, object : ImageCapture.OnImageSavedCallback {
                    override fun onError(e: ImageCaptureException) {
                        Timber.e("Photo capture failed: ${e.message}")
                    }

                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                        val savedUri = output.savedUri ?: Uri.fromFile(photoFile)
                        Timber.d("Photo capture succeeded: $savedUri")

                        // We can only change the foreground Drawable using API level 23+ API
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            // Update the gallery thumbnail with latest picture taken
                            setGalleryThumbnail(savedUri)
                        }

                        // Implicit broadcasts will be ignored for devices running API level >= 24
                        // so if you only target API level 24+ you can remove this statement
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                            requireActivity().sendBroadcast(
                                Intent(android.hardware.Camera.ACTION_NEW_PICTURE, savedUri)
                            )
                        }

                        // If the folder selected is an external media directory, this is
                        // unnecessary but otherwise other apps will not be able to access our
                        // images unless we scan them using [MediaScannerConnection]
                        val mimeType = MimeTypeMap.getSingleton()
                            .getMimeTypeFromExtension(savedUri.toFile().extension)
                        MediaScannerConnection.scanFile(
                            context,
                            arrayOf(savedUri.toFile().absolutePath),
                            arrayOf(mimeType)
                        ) { _, uri ->
                            Timber.d("Image capture scanned into media store: $uri")
                        }
                    }
                })

            // We can only change the foreground Drawable using API level 23+ API
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                // Display flash animation to indicate that photo was captured
                binding.root.postDelayed({
                    binding.root.foreground = ColorDrawable(Color.WHITE)
                    binding.root.postDelayed(
                        { binding.root.foreground = null }, ANIMATION_FAST_MILLIS
                    )
                }, ANIMATION_SLOW_MILLIS)
            }
        }
    }

    fun switchCamera() {
        vm.lensFacing = if (CameraSelector.LENS_FACING_FRONT == vm.lensFacing) {
            binding.cameraSwitchButton.setImageResource(R.drawable.ic_camera_front)
            CameraSelector.LENS_FACING_BACK
        } else {
            binding.cameraSwitchButton.setImageResource(R.drawable.ic_camera_rear)
            CameraSelector.LENS_FACING_FRONT
        }
        bindCameraUseCases()
    }

    fun showGallery() {
        if (true == outputDirectory.listFiles()?.isNotEmpty()) {
            Navigation.findNavController(
                requireActivity(), R.id.fragment_container
            ).navigate(
                CameraFragmentDirections.actionCameraToGallery(
                    outputDirectory.absolutePath
                )
            )
        }
    }

    fun showTimerOptions() {
        binding.timerConteiner.visibility = View.VISIBLE
    }

    fun closeTimerAndSelect(timer: CameraTimer) {
        vm.selectedTimer = timer
        binding.timerConteiner.visibility = View.GONE
        binding.timerButton.setImageResource(setImageDrawableSelect(timer))
    }

    private fun setImageDrawableSelect(timer: CameraTimer) = when (timer) {
        CameraTimer.OFF -> R.drawable.ic_timer_off
        CameraTimer.SEC3 -> R.drawable.ic_timer_3_sec
        CameraTimer.SEC10 -> R.drawable.ic_timer_10_sec
    }

    fun showFlashOptions() {
        binding.flashConteiner.visibility = View.VISIBLE
    }

    fun closeFlashOptionsAndSelect(flashMode: Int) {
        vm.imageCapture?.flashMode = flashMode
        binding.flashConteiner.visibility = View.GONE
        binding.flashButton.setImageResource(setImageDrawableFlashMode(flashMode))
    }

    private fun setImageDrawableFlashMode(flashMode: Int) = when (flashMode) {
        ImageCapture.FLASH_MODE_OFF -> R.drawable.ic_flash_off
        ImageCapture.FLASH_MODE_AUTO -> R.drawable.ic_flash_auto
        ImageCapture.FLASH_MODE_ON -> R.drawable.ic_flash_on
        else -> R.drawable.ic_flash_auto
    }

    /**
     * Enable or disable the button to switch cameras depending on the available cameras on the device.
     * For example, on a ChromeBook, we usually just have one camera.
     */
    private fun updateCameraSwitchButton() {
        try {
            binding.cameraSwitchButton.isEnabled =
                vm.hasBackCamera() && vm.hasFrontCamera()
        } catch (exception: CameraInfoUnavailableException) {
            binding.cameraSwitchButton.isEnabled = false
        }
    }


    companion object {
        private const val PERMISSIONS_REQUEST_CODE = 10
    }

}
