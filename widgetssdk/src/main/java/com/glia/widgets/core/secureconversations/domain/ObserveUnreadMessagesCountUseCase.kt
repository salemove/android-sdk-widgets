package com.glia.widgets.core.secureconversations.domain

import com.glia.widgets.chat.domain.IsAuthenticatedUseCase
import com.glia.widgets.core.secureconversations.SecureConversationsRepository
import com.glia.widgets.di.GliaCore
import com.glia.widgets.helper.rx.Schedulers
import io.reactivex.rxjava3.core.Observable
import java.util.concurrent.TimeUnit

internal class ObserveUnreadMessagesCountUseCase(
    private val repository: SecureConversationsRepository,
    private val isAuthenticatedUseCase: IsAuthenticatedUseCase,
    private val core: GliaCore,
    private val schedulers: Schedulers
) {

    private val observeUnreadMessagesCount: Observable<Int>
        get() = when {
            core.isInitialized.not() -> Observable.just(NO_UNREAD_MESSAGES)
            isAuthenticatedUseCase().not() -> Observable.just(NO_UNREAD_MESSAGES)
            else -> unreadMessagesCountWithTimeout
        }

    private val unreadMessagesCountWithTimeout: Observable<Int>
        get() = repository.unreadMessagesCountObservable
            .timeout(TIMEOUT_SEC, TimeUnit.SECONDS)
            .onErrorReturnItem(NO_UNREAD_MESSAGES)

    /**
     * This function provides a default value instead of an error.
     * If error handling is needed, use the repository function directly.
     *
     * @return [NO_UNREAD_MESSAGES] if the current socket doesn't signal a success value within the specified [TIMEOUT_SEC] window.
     *
     * This is combined with the chat transcript result to avoid "Jumping UI" when adding new messages'
     * divider [SecureConversationsRepository.unreadMessagesCountObservable] is a socket call and
     * there is no warranty that it will return anything so timeout is added to make sure that it will return [NO_UNREAD_MESSAGES]
     * after the timeout if there is no answer from socket yet.
     */
    operator fun invoke(): Observable<Int> = observeUnreadMessagesCount
            .subscribeOn(schedulers.computationScheduler)
            .observeOn(schedulers.mainScheduler)
}
