package ca.hojat.smartcamera.feature_gallery

import android.content.Intent
import android.media.MediaScannerConnection
import android.os.Build
import android.view.LayoutInflater
import android.webkit.MimeTypeMap
import androidx.appcompat.app.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.FileProvider
import androidx.fragment.app.FragmentActivity
import androidx.navigation.Navigation
import ca.hojat.smartcamera.BuildConfig
import ca.hojat.smartcamera.R
import ca.hojat.smartcamera.core.extensions.padWithDisplayCutout
import ca.hojat.smartcamera.core.extensions.showImmersive
import ca.hojat.smartcamera.databinding.FragmentGalleryBinding
import java.io.File

@Composable
fun GalleryScreen(
    mediaList: MutableList<File>,
    onBackClick: () -> Unit,
    onShareClick: (File) -> Unit,
    onDeleteClick: (File) -> Unit
) {
    AndroidView(
        factory = { context ->

            val binding = FragmentGalleryBinding.inflate(LayoutInflater.from(context), null, false)

            //Checking media files list
            if (mediaList.isEmpty()) {
                binding.deleteButton.isEnabled = false
                binding.shareButton.isEnabled = false
            }

            // Populate the ViewPager and implement a cache of two media items
            binding.photoViewPager.apply {
                offscreenPageLimit = 2
                adapter = MediaPagerAdapter(context as FragmentActivity, mediaList)
            }

            // Make sure that the cutout "safe area" avoids the screen notch if any
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                // Use extension method to pad "inside" view containing UI using display cutout's bounds
                binding.cutoutSafeArea.padWithDisplayCutout()
            }

            // Handle back button press
            binding.backButton.setOnClickListener {
                Navigation.findNavController(context as FragmentActivity, R.id.fragment_container).navigateUp()
            }

            // Handle share button press
            binding.shareButton.setOnClickListener {

                mediaList.getOrNull(binding.photoViewPager.currentItem)
                    ?.let { mediaFile ->

                        // Create a sharing intent
                        val intent = Intent().apply {
                            // Infer media type from file extension
                            val mediaType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(mediaFile.extension)
                            // Get URI from our FileProvider implementation
                            val uri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", mediaFile)
                            // Set the appropriate intent extra, type, action and flags
                            putExtra(Intent.EXTRA_STREAM, uri)
                            type = mediaType
                            action = Intent.ACTION_SEND
                            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                        }

                        // Launch the intent letting the user choose which app to share with
                        context.startActivity(Intent.createChooser(intent, context.getString(R.string.share_hint)))
                    }
            }

            // Handle delete button press
            binding.deleteButton.setOnClickListener {

                mediaList.getOrNull(binding.photoViewPager.currentItem)
                    ?.let { mediaFile ->

                        AlertDialog.Builder(context, android.R.style.Theme_Material_Dialog)
                            .setTitle(context.getString(R.string.delete_title))
                            .setMessage(context.getString(R.string.delete_dialog))
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setPositiveButton(android.R.string.yes) { _, _ ->

                                // Delete current photo
                                mediaFile.delete()

                                // Send relevant broadcast to notify other apps of deletion
                                MediaScannerConnection.scanFile(context, arrayOf(mediaFile.absolutePath), null, null)

                                // Notify our view pager
                                mediaList.removeAt(binding.photoViewPager.currentItem)
                                binding.photoViewPager.adapter?.notifyDataSetChanged()

                                // If all photos have been deleted, return to camera
                                if (mediaList.isEmpty()) {
                                    Navigation.findNavController(
                                        context as FragmentActivity,
                                        R.id.fragment_container
                                    ).navigateUp()
                                }

                            }

                            .setNegativeButton(android.R.string.no, null)
                            .create().showImmersive()
                    }
            }


            binding.root
        },
    )
}