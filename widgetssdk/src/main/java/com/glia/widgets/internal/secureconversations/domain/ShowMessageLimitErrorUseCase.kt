package com.glia.widgets.internal.secureconversations.domain

import com.glia.widgets.helper.rx.Schedulers
import com.glia.widgets.internal.secureconversations.SendMessageRepository
import io.reactivex.rxjava3.core.Observable

internal class ShowMessageLimitErrorUseCase(
    private val sendMessageRepository: SendMessageRepository,
    private val schedulers: Schedulers
) {

    operator fun invoke(): Observable<Boolean> = sendMessageRepository.observable
        .map { it.count() > MAX_MESSAGE_LENGTH }
        .subscribeOn(schedulers.computationScheduler)
        .observeOn(schedulers.mainScheduler)

    companion object {
        private const val MAX_MESSAGE_LENGTH = 10000
    }
}
