package com.glia.widgets.lifecycle

import android.annotation.SuppressLint
import com.glia.widgets.engagement.domain.GliaLifecycleEventUseCase
import io.reactivex.rxjava3.disposables.Disposable

internal interface GliaLifecycleEvents {
    fun subscribe(listener: OnEvent)
    fun unsubscribe(listener: OnEvent)
}

internal class GliaLifecycleEventsImpl(
    private val gliaLifecycleEventUseCase: GliaLifecycleEventUseCase
) : GliaLifecycleEvents {
    internal val subscriptions: MutableMap<Int, Disposable> = mutableMapOf()

    @SuppressLint("CheckResult")
    override fun subscribe(listener: OnEvent) {
        if (subscriptions.containsKey(listener.hashCode())) {
            // Already subscribed
            return
        }
        subscriptions[listener.hashCode()] = gliaLifecycleEventUseCase().subscribe(listener::onEvent)
    }

    override fun unsubscribe(listener: OnEvent) {
        subscriptions.remove(listener.hashCode())?.dispose()
    }
}
