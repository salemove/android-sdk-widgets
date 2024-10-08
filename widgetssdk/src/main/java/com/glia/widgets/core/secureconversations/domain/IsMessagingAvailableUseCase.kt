package com.glia.widgets.core.secureconversations.domain

import com.glia.androidsdk.queuing.Queue
import com.glia.androidsdk.queuing.QueueState
import com.glia.widgets.core.queue.QueueRepository
import com.glia.widgets.core.queue.QueuesState
import com.glia.widgets.helper.supportMessaging
import io.reactivex.rxjava3.core.Flowable

internal class IsMessagingAvailableUseCase(private val queueRepository: QueueRepository) {

    operator fun invoke(): Flowable<Result<Boolean>> = queueRepository.observableIntegratorQueues
        .filter { it !is QueuesState.Loading }
        .map { mapResult(it) }
        .distinctUntilChanged()

    private fun mapResult(queueMonitorState: QueuesState): Result<Boolean> = when (queueMonitorState) {
        is QueuesState.Error -> Result.failure(queueMonitorState.error)
        is QueuesState.Queues -> Result.success(isMessagingAvailable(queueMonitorState.queues))
        else -> Result.success(false)
    }

    private fun isMessagingAvailable(queues: List<Queue>): Boolean = queues.asSequence()
        .filterNot { it.state.status == QueueState.Status.CLOSED }
        .filterNot { it.state.status == QueueState.Status.UNKNOWN }
        .any { it.supportMessaging() }
}
