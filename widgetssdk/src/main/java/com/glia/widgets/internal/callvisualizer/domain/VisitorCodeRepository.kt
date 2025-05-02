package com.glia.widgets.internal.callvisualizer.domain

import com.glia.androidsdk.GliaException
import com.glia.androidsdk.omnibrowse.VisitorCode
import com.glia.widgets.di.GliaCore
import io.reactivex.rxjava3.core.Observable

internal class VisitorCodeRepository(private val gliaCore: GliaCore) {

    fun getVisitorCode(): Observable<VisitorCode> {
        if (!gliaCore.isInitialized) {
            return Observable.error(
                GliaException(
                    "Widgets SDK is not initialized",
                    GliaException.Cause.INVALID_INPUT
                )
            )
        }
        return Observable.create { emitter ->
            gliaCore.callVisualizer.getVisitorCode { response, exception ->
                if (response != null && response.code != null) {
                    emitter.onNext(response)
                } else if (exception != null) {
                    emitter.onError(exception)
                } else {
                    emitter.onError(IllegalArgumentException("No value presented"))
                }
                emitter.onComplete()
            }
        }
    }
}
