package com.glia.widgets.lifecycle

/**
 * Callback invoked with Widgets SDK lifecycle events.
 *
 * @see com.glia.widgets.GliaWidgets.subscribeToEvents
 */
fun interface OnLifecycleEvent {
    /**
     * Function that is fired when a lifecycle event occurs.
     *
     * @param event the [LifecycleEvent] that occurred
     */
    fun onEvent(event: LifecycleEvent)
}
