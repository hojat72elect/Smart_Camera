package ca.on.sudbury.hojat.smartcamera.core.shared

import java.util.concurrent.atomic.AtomicBoolean


/**
 * Used as a wrapper for data that is exposed via a LiveData that represents an event.
 */
open class Event<out T>(private val content: T) {

    private val isHandled = AtomicBoolean(false)

    /**
     * Returns the content and prevents its use again.
     */
    fun getContent(): T? = if (isHandled.compareAndSet(false, true)) content else null

}
