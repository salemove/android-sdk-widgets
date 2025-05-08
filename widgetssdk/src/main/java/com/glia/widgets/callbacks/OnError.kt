package com.glia.widgets.callbacks

import com.glia.androidsdk.RequestCallback
import com.glia.widgets.GliaWidgetsException
import com.glia.widgets.toCoreType

/**
 * Error callback used in some requests to Glia Widgets SDK.
 */
fun interface OnError {
    /**
     * Function that is fired if request to Glia Widgets SDK fails
     *
     * @param exception GliaWidgetsException returned if the request failed
     */
    fun onError(exception: GliaWidgetsException)
}

internal fun RequestCallback<Void>.toOnError(): OnError {
    return OnError { onResult(null, it.toCoreType()) }
}
