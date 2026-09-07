package com.glia.widgets.lifecycle

internal interface GliaLifecycleEvents {
    fun subscribe(listener: OnGliaEvent)
    fun unsubscribe(listener: OnGliaEvent)
}

internal class GliaLifecycleEventsImpl : GliaLifecycleEvents {
    internal val listeners: MutableMap<Int, OnGliaEvent> = mutableMapOf()

    override fun subscribe(listener: OnGliaEvent) {
        listeners[listener.hashCode()] = listener
    }

    override fun unsubscribe(listener: OnGliaEvent) {
        listeners.remove(listener.hashCode())
    }
}
