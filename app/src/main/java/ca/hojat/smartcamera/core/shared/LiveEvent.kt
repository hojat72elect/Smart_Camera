package ca.hojat.smartcamera.core.shared

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer


abstract class LiveEvent<T>(value: T) : LiveData<Event<T>>(Event(value)) {

    override fun observeForever(observer: Observer<in Event<T>>) {
        requireNoObserverRegistered()
        super.observeForever(observer)
    }

    override fun observe(owner: LifecycleOwner, observer: Observer<in Event<T>>) {
        requireNoObserverRegistered()
        super.observe(owner, observer)
    }

    private fun requireNoObserverRegistered() {
        require(!hasActiveObservers()) {
            """LiveEvent must only have a single active observer at any given time.
                | If there are multiple observers,
                | there is no guarantee which observer will be able to process the event first,
                | after which no other observer will get invoked as the event would
                | already be marked as `handled` by the first observer.
            """.trimMargin()
        }
    }
}
