package com.glia.widgets.callbacks

import com.glia.widgets.GliaWidgetsException

/**
 * Error callback used in some requests to Glia Widgets SDK.
 */
fun interface OnError {
    /**
     * Function that is fired if request to Glia Widgets SDK fails
     *
     * @param exception GliaWidgetsException returned if the request failed
     */
    fun onError(exception: GliaWidgetsException?)
}
