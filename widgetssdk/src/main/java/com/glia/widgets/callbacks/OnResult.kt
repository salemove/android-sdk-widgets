package com.glia.widgets.callbacks

import com.glia.androidsdk.RequestCallback

/**
 * Callback used in some requests to Glia Widgets SDK.
 *
 * @param <T> object expected to be returned by the request
</T> */
fun interface OnResult<T> {
    /**
     * Function that is fired if request succeeds with a result
     *
     * @param result result returned if the request succeeded
     */
    fun onResult(result: T)
}

internal fun <T> RequestCallback<T>.toOnResult(): OnResult<T> {
    return OnResult { onResult(it, null) }
}
