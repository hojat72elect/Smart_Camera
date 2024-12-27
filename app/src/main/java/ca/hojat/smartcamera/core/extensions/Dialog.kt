package ca.hojat.smartcamera.core.extensions

import android.app.Dialog
import android.view.WindowManager
import ca.hojat.smartcamera.core.Constants

/** Same as show() on a dialog but also sets immersive mode in the dialog's window */
fun Dialog.showImmersive() {
    // Set the dialog to not focusable
    window?.setFlags(
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
    )

    // Make sure that the dialog's window is in full screen
    window?.decorView?.systemUiVisibility = Constants.FLAGS_FULLSCREEN

    // Show the dialog while still in immersive mode
    show()

    // Set the dialog to focusable again
    window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
}