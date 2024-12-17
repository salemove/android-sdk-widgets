package com.glia.widgets.core.secureconversations.domain

import androidx.annotation.VisibleForTesting
import com.glia.widgets.core.secureconversations.SecureConversationsRepository
import com.glia.widgets.helper.Logger
import io.reactivex.rxjava3.core.Completable
import java.util.concurrent.TimeUnit

private const val TAG = "MarkMessagesReadUseCase"

@VisibleForTesting
const val DELAY_SEC = 6L

internal class MarkMessagesReadWithDelayUseCase(private val repository: SecureConversationsRepository) {

    operator fun invoke(): Completable = repository.isLeaveSecureConversationDialogVisibleObservable
        .filter { !it }
        .take(1)
        .delay(DELAY_SEC, TimeUnit.SECONDS)
        .ignoreElements()
        .andThen(markMessagesRead())
        .doOnComplete { Logger.d(TAG, "Messages successfully marked as read") }

    private fun markMessagesRead(): Completable = Completable.create {
        if (it.isDisposed) return@create

        repository.markMessagesRead call@{ _, exception ->
            if (it.isDisposed) return@call
            if (exception != null) {
                it.onError(exception)
            } else {
                it.onComplete()
            }
        }
    }
}
