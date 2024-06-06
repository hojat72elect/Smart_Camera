package ca.hojat.smartcamera.core

import android.Manifest
import android.view.View

object Constants {

    /** Combination of all flags required to put activity into immersive mode */
    const val FLAGS_FULLSCREEN =
        View.SYSTEM_UI_FLAG_LOW_PROFILE or
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION

    /** Milliseconds used for UI animations */
    const val ANIMATION_FAST_MILLIS = 50L
    const val ANIMATION_SLOW_MILLIS = 100L

    const val FILENAME = "yyyy-MM-dd-HH-mm-ss-SSS"
    const val PHOTO_EXTENSION = ".jpg"
    const val RATIO_4_3_VALUE = 4.0 / 3.0
    const val RATIO_16_9_VALUE = 16.0 / 9.0

    // All the required permissions for this app.
    val PERMISSIONS_REQUIRED = arrayOf(Manifest.permission.CAMERA)


}