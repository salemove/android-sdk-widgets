package com.glia.widgets.core.secureconversations.domain

import com.glia.androidsdk.GliaException
import com.glia.androidsdk.RequestCallback
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

    /**
     * This function provides a default value instead of an error.
     * If error handling is needed, use the repository function directly.
     *
     * @return [NO_UNREAD_MESSAGES] if the current socket doesn't signal a success value within the specified [TIMEOUT_SEC] window.
     *
     * This is combined with the chat transcript result to avoid "Jumping UI" when adding new messages'
     * divider [SecureConversationsRepository.getUnreadMessagesCount] is a socket call and
     * there is no warranty that it will return anything so timeout is added to make sure that it will return [NO_UNREAD_MESSAGES]
     * after the timeout if there is no answer from socket yet.
     */
    operator fun invoke(): Observable<Int> {
        val observeNoUnreadMessages = Observable.just(NO_UNREAD_MESSAGES)
            .subscribeOn(schedulers.computationScheduler)
            .observeOn(schedulers.mainScheduler)

        return when {
            core.isInitialized.not() -> observeNoUnreadMessages
            isAuthenticatedUseCase().not() -> observeNoUnreadMessages

            else -> observeUnreadMessagesCount(repository)
        }
    }

    private fun observeUnreadMessagesCount(repository: SecureConversationsRepository): Observable<Int> {
        return Observable.create { emitter ->
            repository.getUnreadMessagesCount(object : RequestCallback<Int> {
                override fun onResult(count: Int?, exception: GliaException?) {
                    if (emitter.isDisposed) return
                    if (exception != null) {
                        emitter.tryOnError(exception)
                    } else {
                        emitter.onNext(count ?: NO_UNREAD_MESSAGES)
                    }
                }
            })
        }
            .subscribeOn(schedulers.computationScheduler)
            .observeOn(schedulers.mainScheduler)
            .timeout(TIMEOUT_SEC, TimeUnit.SECONDS).onErrorReturnItem(NO_UNREAD_MESSAGES)
    }
}
