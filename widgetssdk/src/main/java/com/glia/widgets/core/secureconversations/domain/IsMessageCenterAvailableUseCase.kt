package com.glia.widgets.core.secureconversations.domain

import com.glia.androidsdk.GliaException
import com.glia.androidsdk.RequestCallback
import io.reactivex.rxjava3.disposables.CompositeDisposable

internal class IsMessageCenterAvailableUseCase(
    private val queueIds: List<String>,
    private val isMessagingAvailableUseCase: IsMessagingAvailableUseCase
) {
    private val disposable = CompositeDisposable()

    operator fun invoke(callback: RequestCallback<Boolean>) {
        disposable.add(
            isMessagingAvailableUseCase(queueIds).subscribe(
                { callback.onResult(it, null) },
                { callback.onResult(null, GliaException.from(it)) }
            )
        )
    }

    fun dispose() {
        disposable.clear()
    }
}
