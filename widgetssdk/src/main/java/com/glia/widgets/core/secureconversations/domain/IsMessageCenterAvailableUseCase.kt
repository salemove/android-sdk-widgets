package com.glia.widgets.core.secureconversations.domain

import androidx.annotation.VisibleForTesting
import com.glia.androidsdk.Engagement
import com.glia.androidsdk.GliaException
import com.glia.androidsdk.RequestCallback
import com.glia.androidsdk.queuing.Queue
import com.glia.androidsdk.queuing.QueueState
import com.glia.widgets.core.queue.GliaQueueRepository
import com.glia.widgets.helper.rx.Schedulers
import io.reactivex.disposables.CompositeDisposable

class IsMessageCenterAvailableUseCase(
    private val queueId: String,
    private val queueRepository: GliaQueueRepository,
    private val schedulers: Schedulers
) {
    private val disposable = CompositeDisposable()

    fun execute(callback: RequestCallback<Boolean>) {
        disposable.add(
            queueRepository.queues
                .subscribeOn(schedulers.computationScheduler)
                .observeOn(schedulers.mainScheduler)
                .subscribe(
                    { queues ->
                        callback.onResult(containsMessagingQueue(queues), null)
                    },
                    { error -> callback.onResult(null, GliaException.from(error)) })
        )
    }

    @VisibleForTesting
    fun containsMessagingQueue(queues: Array<Queue>): Boolean {
        val messagingQueues = queues
            .filter { queue -> queue.id == queueId }
            .filterNot { queue -> queue.state.status == QueueState.Status.CLOSED }
            .filterNot { queue -> queue.state.status == QueueState.Status.UNKNOWN }
            .filter { queue -> queue.state.medias.any { media -> media == Engagement.MediaType.MESSAGING } }
        return messagingQueues.isNotEmpty()
    }

    fun dispose() {
        disposable.clear()
    }
}
