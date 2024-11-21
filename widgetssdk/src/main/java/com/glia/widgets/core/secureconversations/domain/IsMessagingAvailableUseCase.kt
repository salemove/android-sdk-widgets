package com.glia.widgets.core.secureconversations.domain

import com.glia.androidsdk.queuing.Queue
import com.glia.androidsdk.queuing.QueueState
import com.glia.widgets.core.queue.QueueRepository
import com.glia.widgets.core.queue.QueuesState
import com.glia.widgets.engagement.EngagementRepository
import com.glia.widgets.engagement.State
import com.glia.widgets.helper.supportMessaging
import io.reactivex.rxjava3.core.Flowable

internal class IsMessagingAvailableUseCase(private val queueRepository: QueueRepository, private val engagementRepository: EngagementRepository) {

    operator fun invoke(): Flowable<Result<Boolean>> =
        Flowable.combineLatest(queueRepository.queuesState, engagementRepository.engagementState) { queueState, engagementState ->
            mapResult(queueState, engagementState)
        }.distinctUntilChanged()

    private fun mapResult(queuesState: QueuesState, engagementState: State): Result<Boolean> = when {
        engagementState is State.TransferredToSecureConversation -> Result.success(true)
        queuesState is QueuesState.Queues -> Result.success(isMessagingAvailable(queuesState.queues))
        queuesState is QueuesState.Error -> Result.failure(queuesState.error)
        else -> Result.success(false)
    }

    private fun isMessagingAvailable(queues: List<Queue>): Boolean = queues.asSequence()
        .filterNot { it.state.status == QueueState.Status.CLOSED }
        .filterNot { it.state.status == QueueState.Status.UNKNOWN }
        .any { it.supportMessaging() }
}
