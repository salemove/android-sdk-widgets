package com.glia.widgets.lifecycle

import android.annotation.SuppressLint
import com.glia.widgets.engagement.domain.LifecycleEventUseCase
import io.reactivex.rxjava3.disposables.Disposable

internal interface LifecycleEvents {
    fun subscribe(listener: OnLifecycleEvent)
    fun unsubscribe(listener: OnLifecycleEvent)
}

internal class LifecycleEventsImpl(
    private val lifecycleEventUseCase: LifecycleEventUseCase
) : LifecycleEvents {
    internal val subscriptions: MutableMap<Int, Disposable> = mutableMapOf()

    @SuppressLint("CheckResult")
    override fun subscribe(listener: OnLifecycleEvent) {
        if (subscriptions.containsKey(listener.hashCode())) {
            // Already subscribed
            return
        }
        subscriptions[listener.hashCode()] = lifecycleEventUseCase().subscribe(listener::onEvent)
    }

    override fun unsubscribe(listener: OnLifecycleEvent) {
        subscriptions.remove(listener.hashCode())?.dispose()
    }
}
