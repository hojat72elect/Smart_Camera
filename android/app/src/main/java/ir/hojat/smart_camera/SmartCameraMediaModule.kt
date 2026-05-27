package ir.hojat.smart_camera

import android.content.ContentValues
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import java.io.IOException

class SmartCameraMediaModule(reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext) {

  override fun getName(): String = "SmartCameraMedia"

  @ReactMethod
  fun saveJpegToDcimSmartCamera(sourceUriString: String, promise: Promise) {
    try {
      val context = reactApplicationContext
      val resolver = context.contentResolver
      val sourceUri = Uri.parse(sourceUriString)

      val displayName = "SC_${System.currentTimeMillis()}.jpg"
      val relativePath = Environment.DIRECTORY_DCIM + "/SmartCamera"

      val values = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, displayName)
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
          put(MediaStore.Images.Media.RELATIVE_PATH, relativePath)
          put(MediaStore.Images.Media.IS_PENDING, 1)
        }
      }

      val collection: Uri =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
          MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
          MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

      val itemUri = resolver.insert(collection, values)
        ?: throw IOException("Failed to create MediaStore record")

      resolver.openInputStream(sourceUri).use { input ->
        if (input == null) throw IOException("Failed to open source uri: $sourceUriString")
        resolver.openOutputStream(itemUri).use { output ->
          if (output == null) throw IOException("Failed to open output stream")
          input.copyTo(output)
        }
      }

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val pendingOff = ContentValues().apply {
          put(MediaStore.Images.Media.IS_PENDING, 0)
        }
        resolver.update(itemUri, pendingOff, null, null)
      }

      promise.resolve(itemUri.toString())
    } catch (e: Exception) {
      promise.reject("SAVE_FAILED", e.message, e)
    }
  }
}
