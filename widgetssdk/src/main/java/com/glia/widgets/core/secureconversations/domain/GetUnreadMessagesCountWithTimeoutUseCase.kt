package com.glia.widgets.core.secureconversations.domain

import com.glia.widgets.chat.domain.IsAuthenticatedUseCase
import com.glia.widgets.core.secureconversations.SecureConversationsRepository
import com.glia.widgets.di.GliaCore
import io.reactivex.rxjava3.core.Single
import java.util.concurrent.TimeUnit

/**
 * Timeout for the [SecureConversationsRepository.unreadMessagesCountObservable]
 *
 * This timeout is not related to the timeout for marking the messages read.
 *
 * @see [GetUnreadMessagesCountWithTimeoutUseCase.invoke]
 */
internal const val TIMEOUT_SEC = 3L

internal const val NO_UNREAD_MESSAGES = 0

internal class GetUnreadMessagesCountWithTimeoutUseCase(
    private val repository: SecureConversationsRepository,
    private val isAuthenticatedUseCase: IsAuthenticatedUseCase,
    private val core: GliaCore
) {

    private val getUnreadMessagesCountWithTimeout: Single<Int>
        get() = repository.unreadMessagesCountObservable
            .firstOrError()
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
    operator fun invoke(): Single<Int> = when {
        core.isInitialized.not() -> Single.just(NO_UNREAD_MESSAGES)
        isAuthenticatedUseCase().not() -> Single.just(NO_UNREAD_MESSAGES)
        else -> getUnreadMessagesCountWithTimeout
    }
}
