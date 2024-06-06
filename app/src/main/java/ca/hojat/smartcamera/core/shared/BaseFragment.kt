package ca.hojat.smartcamera.core.shared

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment


abstract class BaseFragment : Fragment() {


    final override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (savedInstanceState == null) {
            onViewInitialized(view)
            onInitializeObservers()
        }
    }

    final override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        if (savedInstanceState == null) return
        val view = view ?: return
        onViewInitialized(view)
        onRestoreViewState(view, savedInstanceState)
        onInitializeObservers()
    }

    /**
     * Called when view is completely created and view state restored.
     * Views can be setup at this point.
     */
    open fun onViewInitialized(view: View) {}

    /**
     * Called when Android finishes initializing view and restoring view state.
     * At this point any custom view state saved in [onSaveInstanceState] can be accessed and used.
     */
    open fun onRestoreViewState(view: View, savedInstanceState: Bundle) {}

    /**
     * Initialize any reactive data observers.
     * It is called after initializing view and restoring state ie. after [onViewInitialized] and [onRestoreViewState]
     * This callback is specific to MVVM pattern.
     */
    @Deprecated("Using this for registering listeners with Flows will cause data leaks.")
    open fun onInitializeObservers() {}

}
