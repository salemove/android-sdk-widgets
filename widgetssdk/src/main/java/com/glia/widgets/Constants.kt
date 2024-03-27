package com.glia.widgets

internal object Constants {
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

    /**
     * MIME type for ActivityResultLauncher to show only images
     */
    const val MIME_TYPE_IMAGES = "image/*"

    /**
     * MIME type for ActivityResultLauncher to show all files
     */
    const val MIME_TYPE_ALL = "*/*"

    /**
     * Delay for WebView initialization
     */
    const val WEB_VIEW_INITIALIZATION_DELAY = 100L
}
