package com.glia.widgets.core.secureconversations.domain

import androidx.annotation.VisibleForTesting
import com.glia.widgets.chat.data.ChatScreenRepository
import com.glia.widgets.core.secureconversations.SecureConversationsRepository
import com.glia.widgets.helper.Logger
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import java.util.concurrent.TimeUnit

private const val TAG = "MarkMessagesReadUseCase"

@VisibleForTesting
const val DELAY_SEC = 6L

/**
 * This use case is responsible for marking the messages as read with a delay.
 * The messages should be marked as read if the chat screen is open and the leave dialog is not visible.
 */
internal class MarkMessagesReadWithDelayUseCase(
    private val secureConversationsRepository: SecureConversationsRepository,
    private val chatScreenRepository: ChatScreenRepository
) {

    operator fun invoke(delay: Long = DELAY_SEC): Completable = Flowable.combineLatest(
        chatScreenOpen(delay),
        leaveDialogVisible(delay),
        ::Pair
    )
        .filter {
            val isChatScreenOpen = it.first
            val isLeaveDialogVisible = it.second
            return@filter isChatScreenOpen && !isLeaveDialogVisible
        }
        .take(1)
        .ignoreElements()
        .andThen(markMessagesRead())
        .doOnComplete { Logger.d(TAG, "Messages successfully marked as read") }

    private fun chatScreenOpen(delay: Long) = chatScreenRepository.isChatScreenOpenObservable
        .switchMap { isChatScreenOpen ->
            return@switchMap delayIf(isChatScreenOpen, delay) { isChatScreenOpen && delay > 0 }
        }

    private fun leaveDialogVisible(delay: Long) = secureConversationsRepository.isLeaveSecureConversationDialogVisibleObservable
        .switchMap { isLeaveDialogVisible ->
            return@switchMap delayIf(isLeaveDialogVisible, delay) { !isLeaveDialogVisible && delay > 0 }
        }

    private fun<T: Any> delayIf(value: T, delay: Long, condition: () -> Boolean): Flowable<T> {
        val flowable = Flowable.just(value)
        if (condition()) {
            return flowable.delay(delay, TimeUnit.SECONDS)
        }
        return flowable
    }

    private fun markMessagesRead(): Completable = Completable.create {
        if (it.isDisposed) return@create

        secureConversationsRepository.markMessagesRead call@{ _, exception ->
            if (it.isDisposed) return@call
            if (exception != null) {
                it.onError(exception)
            } else {
                it.onComplete()
            }
        }
    }
}
