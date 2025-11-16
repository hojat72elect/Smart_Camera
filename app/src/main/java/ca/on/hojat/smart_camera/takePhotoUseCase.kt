package ca.on.hojat.smart_camera

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.Locale

fun takePhotoUseCase(
    context: Context,
    scope: CoroutineScope,
    imageCapture: ImageCapture,
    onPhotoCaptured: () -> Unit,
    onSuccess: (Uri) -> Unit,
    onError: (ImageCaptureException) -> Unit
) {

    imageCapture.takePicture(
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                onPhotoCaptured()
                scope.launch(Dispatchers.IO) {
                    val buffer: ByteBuffer = image.planes[0].buffer
                    val bytes = ByteArray(buffer.remaining())
                    buffer.get(bytes)

                    val name =
                        SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(System.currentTimeMillis())
                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, name)
                        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Smart-Camera")
                        }
                    }

                    var uri: Uri? = null
                    try {
                        val resolver = context.contentResolver
                        uri = resolver.insert(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            contentValues
                        )
                        if (uri == null) {
                            throw Exception("Failed to create new MediaStore entry.")
                        }
                        resolver.openOutputStream(uri)?.use {
                            it.write(bytes)
                        }
                        withContext(Dispatchers.Main) {
                            onSuccess(uri)
                        }
                    } catch (exc: Exception) {
                        uri?.let { context.contentResolver.delete(it, null, null) }
                        withContext(Dispatchers.Main) {
                            onError(
                                ImageCaptureException(
                                    ImageCapture.ERROR_FILE_IO,
                                    "Failed to save image",
                                    exc
                                )
                            )
                        }
                    } finally {
                        image.close()
                    }
                }
            }

            override fun onError(exception: ImageCaptureException) {
                onError(exception)
            }
        }
    )
}