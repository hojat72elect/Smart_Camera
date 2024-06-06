package ca.on.sudbury.hojat.smartcamera.utils.shared

import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import kotlinx.coroutines.flow.Flow


abstract class BaseFragment() : Fragment() {


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

    /**
     * Convenient extension to observe [LiveData] which uses root view's lifecycle as owner.
     * In most cases lifecycle is same as fragment other than fragment detached scenarios.
     *
     * If using default [getViewLifecycleOwner], it should only be called after view is initialized ie. [onViewInitialized]
     */
    fun <T> LiveData<T>.observe(
        owner: LifecycleOwner = viewLifecycleOwner,
        onChanged: (T) -> Unit
    ) = observe(owner, Observer(onChanged))

    fun <T> LiveEvent<T>.observeEvent(
        owner: LifecycleOwner = viewLifecycleOwner,
        onEvent: (T) -> Unit
    ) = observe(owner, EventObserver(onEvent))

    /**
     * The [Flow] will be converted into a [LiveData] which is valid through the lifecycle
     * of this Fragment.
     */
    fun <T> Flow<T>.asViewBoundLiveData(): LiveData<T> =
        viewLifecycleOwnerLiveData.switchMap {
            asLiveData(it.lifecycleScope.coroutineContext)
        }
}
