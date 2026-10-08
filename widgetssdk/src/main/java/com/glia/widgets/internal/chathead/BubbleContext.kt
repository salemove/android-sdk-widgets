package com.glia.widgets.internal.chathead

/**
 * Where a bubble would appear right now — the rendering environment, not the bubble state.
 */
internal sealed interface BubbleContext {
    /** The app is in the foreground, showing [visibleScreen]. */
    data class InApp(val visibleScreen: VisibleScreen) : BubbleContext

    /** No Glia-visible activity is resumed — only a system overlay could be seen. */
    data object AppInBackground : BubbleContext
}

/**
 * Raw lifecycle events pushed by the bubble hosts; the controller derives [BubbleContext] from them.
 */
internal sealed interface BubbleContextEvent {
    /**
     * The topmost resumed screen changed to [screen], or to `null` when nothing is resumed.
     *
     * Reported for every resume *and* pause: which activity is on top is resolved by the watcher's
     * resumed-activity stack, which is what makes the translucent CallActivity case come out right.
     * The controller only stores what it is told.
     */
    data class ScreenChanged(val screen: VisibleScreen?) : BubbleContextEvent

    /** The whole app went to the background, so no screen can host a bubble. */
    data object AppBackgrounded : BubbleContextEvent
}
