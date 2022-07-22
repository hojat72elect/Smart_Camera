package ca.on.sudbury.hojat.smartcamera.utils.shared

import androidx.lifecycle.Observer

/**
 * An [Observer] for [Event]s, simplifying the pattern of checking if the [Event]'s content has
 * already been handled.
 *
 * [onEvent] is *only* called if the [Event]'s contents has not been handled.
 */
class EventObserver<T>(private val onEvent: (T) -> Unit) : Observer<Event<T>> {

    override fun onChanged(event: Event<T>?) {
        event?.getContent()?.let { value ->
            onEvent(value)
        }
    }
}
