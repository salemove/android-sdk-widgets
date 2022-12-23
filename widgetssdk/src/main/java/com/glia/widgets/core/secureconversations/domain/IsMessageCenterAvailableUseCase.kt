package com.glia.widgets.core.secureconversations.domain

import com.glia.androidsdk.GliaException
import com.glia.androidsdk.RequestCallback
import io.reactivex.disposables.CompositeDisposable

internal class IsMessageCenterAvailableUseCase(
    private val queueId: String,
    private val isMessagingAvailableUseCase: IsMessagingAvailableUseCase
) {
    private val disposable = CompositeDisposable()

    operator fun invoke(callback: RequestCallback<Boolean>) {
        disposable.add(
            isMessagingAvailableUseCase(arrayOf(queueId)).subscribe(
                { callback.onResult(it, null) },
                { callback.onResult(null, GliaException.from(it)) }
            )
        )
    }

    fun dispose() {
        disposable.clear()
    }
}
