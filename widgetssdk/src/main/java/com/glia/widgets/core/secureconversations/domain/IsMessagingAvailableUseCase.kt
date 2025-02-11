package com.glia.widgets.core.secureconversations.domain

import com.glia.androidsdk.Engagement.MediaType
import com.glia.androidsdk.queuing.QueueState
import com.glia.widgets.core.queue.Queue
import com.glia.widgets.core.queue.QueueRepository
import com.glia.widgets.core.queue.QueuesState
import com.glia.widgets.engagement.EngagementRepository
import com.glia.widgets.engagement.State
import io.reactivex.rxjava3.core.Flowable

internal class IsMessagingAvailableUseCase(private val queueRepository: QueueRepository, private val engagementRepository: EngagementRepository) {

    operator fun invoke(): Flowable<Boolean> =
        Flowable.combineLatest(queueRepository.queuesState, engagementRepository.engagementState) { queueState, engagementState ->
            mapResult(queueState, engagementState)
        }.distinctUntilChanged()

    private fun mapResult(queuesState: QueuesState, engagementState: State): Boolean = when {
        engagementState is State.TransferredToSecureConversation -> true
        queuesState is QueuesState.Queues -> isMessagingAvailable(queuesState.queues)
        else -> false
    }

    private fun isMessagingAvailable(queues: List<Queue>): Boolean = queues.asSequence()
        .filterNot { it.status == QueueState.Status.CLOSED }
        .filterNot { it.status == QueueState.Status.UNKNOWN }
        .any { it.medias.contains(MediaType.MESSAGING) } // Support messaging
}
