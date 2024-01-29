package com.glia.widgets

internal object Constants {
    /**
     * Used by the sdk to differentiate between CHAT and CALL and CALL and IMAGE_PREVIEW activities.
     */
    const val CHAT_ACTIVITY = "chat_activity"

    /**
     * Used by the sdk to differentiate between CHAT and CALL and CALL and IMAGE_PREVIEW activities.
     */
    const val CALL_ACTIVITY = "call_activity"

    /**
     * Used by the sdk to differentiate between CHAT and CALL and IMAGE_PREVIEW activities.
     */
    const val IMAGE_PREVIEW_ACTIVITY = "image_preview_activity"

    /**
     * Global media timer delay value
     */
    const val CALL_TIMER_DELAY = 0

    /**
     * Global media timer interval value
     */
    const val CALL_TIMER_INTERVAL_VALUE = 1000

    /**
     * Needed to overlap existing app bar in existing view with this view's app bar.
     */
    const val WIDGETS_SDK_LAYER_ELEVATION = 100f
}
