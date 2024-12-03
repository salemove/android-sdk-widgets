package com.glia.widgets.core.secureconversations.domain

import com.glia.widgets.core.secureconversations.SecureConversationsRepository
import com.glia.widgets.helper.rx.Schedulers
import io.reactivex.rxjava3.core.Flowable

internal class ObserveUnreadMessagesCountUseCase(
    private val repository: SecureConversationsRepository,
    private val schedulers: Schedulers
) {

    operator fun invoke(): Flowable<Int> = repository.unreadMessagesCountObservable
        .subscribeOn(schedulers.computationScheduler)
        .observeOn(schedulers.mainScheduler)

}
