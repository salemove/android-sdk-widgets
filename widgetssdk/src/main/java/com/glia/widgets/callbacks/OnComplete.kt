package com.glia.widgets.callbacks

import com.glia.androidsdk.RequestCallback

/**
 * Callback used in some requests to Glia Widgets SDK.
 *
</T> */
fun interface OnComplete {
    /**
     * Function that is fired if request completes
     */
    fun onComplete()
}

internal fun RequestCallback<Void>.toOnComplete(): OnComplete {
    return OnComplete { onResult(null, null) }
}
