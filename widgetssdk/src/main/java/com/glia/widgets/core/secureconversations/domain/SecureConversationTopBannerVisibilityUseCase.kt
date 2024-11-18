package com.glia.widgets.core.secureconversations.domain

import com.glia.androidsdk.Engagement.MediaType
import com.glia.androidsdk.queuing.Queue
import com.glia.androidsdk.queuing.QueueState
import com.glia.widgets.core.queue.QueueRepository
import com.glia.widgets.core.queue.QueuesState
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Flowable

/**
 * Should show top banner in secure conversations chat only if both conditions are met
 * - There is an open queue with text, audio or video media
 * - Has pending secure conversations
 */
internal class SecureConversationTopBannerVisibilityUseCase(
    private val queueRepository: QueueRepository,
    private val hasOngoingInteraction: HasOngoingSecureConversationUseCase
) {

    operator fun invoke(): Flowable<Result<Boolean>> = getPendingSecureConversationState()
        .flatMap { hasPendingSecureConversation ->
            when (hasPendingSecureConversation) {
                true -> checkAvailableQueues()
                else -> Flowable.just(false)
            }
        }
        .map { Result.success(it) }
        .observeOn(AndroidSchedulers.mainThread())
        .onErrorReturn { Result.failure(it) }
        .distinctUntilChanged()

    private fun getPendingSecureConversationState() = hasOngoingInteraction.invoke().toFlowable(BackpressureStrategy.LATEST)

    private fun checkAvailableQueues() = queueRepository.queuesState
            .filter { it !is QueuesState.Loading }
            .map { receivedState ->
                when(receivedState) {
                    is QueuesState.Error -> throw receivedState.error
                    else -> receivedState.queuesOrEmpty()
                }
            }
            .map(::hasOpenQueueWithLiveMedia)

    private fun hasOpenQueueWithLiveMedia(queues: List<Queue>): Boolean = queues.asSequence()
        .filter { it.state.status == QueueState.Status.OPEN }
        .any { it.state.medias.hasLiveMediaType() }


    private fun Array<MediaType>.hasLiveMediaType() = asSequence()
        .any { it == MediaType.TEXT || it == MediaType.AUDIO || it == MediaType.VIDEO }
}
