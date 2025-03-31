package com.glia.widgets.callbacks

/**
 * Callback used in some requests to Glia Widgets SDK.
 *
 * @param <T> object expected to be returned by the request
</T> */
fun interface OnSuccess<T> {
    /**
     * Function that is fired if request succeeds with a result
     *
     * @param result result returned if the request succeeded
     */
    fun onSuccess(result: T?)
}
