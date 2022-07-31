package ca.on.sudbury.hojat.smartcamera.camera

import android.content.Context
import android.content.pm.PackageManager
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.window.WindowManager
import ca.on.sudbury.hojat.smartcamera.R
import ca.on.sudbury.hojat.smartcamera.utils.CameraTimer
import ca.on.sudbury.hojat.smartcamera.utils.Constants
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService

class CameraViewModel(private val useCase: CameraUseCase) : ViewModel() {

    lateinit var windowManager: WindowManager

    lateinit var broadcastManager: LocalBroadcastManager

    // fragment sets and gets data into this variable
    var lensFacing: Int = CameraSelector.LENS_FACING_BACK // default back lens of the camera

    var preview: Preview? = null

    var imageCapture: ImageCapture? = null

    // fragment needs to set and get the value of this image analyzer
    var imageAnalyzer: ImageAnalysis? = null

    var camera: Camera? = null

    var cameraProvider: ProcessCameraProvider? = null

    // Fragment can change this timer value
    var selectedTimer = CameraTimer.OFF

    /** Blocking camera operations are performed using this executor */
    lateinit var cameraExecutor: ExecutorService

    /** Returns true if the device has an available back camera.
     * False otherwise
     **/
    fun hasBackCamera() = cameraProvider?.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA) ?: false

    /** Returns true if the device has an available
     * front camera. False otherwise
     **/
    fun hasFrontCamera() = cameraProvider?.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA) ?: false

    // represents the connection between ViewModel
    // and UseCase (don't need it for now).
    fun sayHello() = "${useCase.sayHello()}\nwas performed in $this"

    /**
     * Fragment calls this function in order to ask user for granting required permissions.
     */
    fun askPermissions(context: Context, requestSource: Fragment, requestCode: Int) {
        if (!hasPermissions(context)) {
            // If permissions have already been granted, proceed
            // Request camera-related permissions
            requestSource.requestPermissions(
                Constants.PERMISSIONS_REQUIRED,
                requestCode
            )
        }
    }

    companion object {
        /** Helper function used to create a timestamped file */
        fun createFile(baseFolder: File, format: String, extension: String) =
            File(
                baseFolder, SimpleDateFormat(format, Locale.US)
                    .format(System.currentTimeMillis()) + extension
            )

        /** Use external media if it is available, our app's file directory otherwise */
        fun getOutputDirectory(context: Context): File {
            val appContext = context.applicationContext
            val mediaDir = context.externalMediaDirs.firstOrNull()?.let {
                File(it, appContext.resources.getString(R.string.app_name)).apply { mkdirs() }
            }
            return if (mediaDir != null && mediaDir.exists())
                mediaDir else appContext.filesDir
        }

        /** Convenience method used to check if all permissions required by this app are granted */
        fun hasPermissions(context: Context) = Constants.PERMISSIONS_REQUIRED.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }
}