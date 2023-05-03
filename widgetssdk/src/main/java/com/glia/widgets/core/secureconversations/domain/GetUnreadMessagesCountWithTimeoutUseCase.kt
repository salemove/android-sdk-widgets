package com.glia.widgets.core.secureconversations.domain

import androidx.annotation.VisibleForTesting
import com.glia.widgets.core.secureconversations.SecureConversationsRepository
import com.glia.widgets.helper.Logger
import io.reactivex.Single
import java.util.concurrent.TimeUnit

private const val TAG = "GetUnreadMessagesCountUseCase"

/**
 * Timeout for the [SecureConversationsRepository.getUnreadMessagesCount]
 *
 * This timeout is not related to the timeout for marking the messages read.
 *
 * @see [GetUnreadMessagesCountWithTimeoutUseCase.invoke]
 */
@VisibleForTesting
const val TIMEOUT_SEC = 3L

internal const val NO_UNREAD_MESSAGES = 0

internal class GetUnreadMessagesCountWithTimeoutUseCase(private val repository: SecureConversationsRepository) {

    /**
     * @return [NO_UNREAD_MESSAGES] if the current socket doesn't signal a success value within the specified [TIMEOUT_SEC] window.
     *
     * This is combined with the chat transcript result to avoid "Jumping UI" when adding new messages'
     * divider [SecureConversationsRepository.getUnreadMessagesCount] is a socket call and
     * there is no warranty that it will return anything so timeout is added to make sure that it will return [NO_UNREAD_MESSAGES]
     * after the timeout if there is no answer from socket yet.
     */
    operator fun invoke(): Single<Int> = Single.create {
        repository.getUnreadMessagesCount { count, exception ->
            if (it.isDisposed) return@getUnreadMessagesCount
            if (exception != null) {
                it.tryOnError(exception)
                Logger.e(TAG, "Failed to get unread messages count", exception)
            } else {
                it.onSuccess(count ?: NO_UNREAD_MESSAGES)
            }
        }
    }.timeout(TIMEOUT_SEC, TimeUnit.SECONDS).onErrorReturnItem(NO_UNREAD_MESSAGES)
}