package com.glia.widgets.core.secureconversations.domain

import androidx.annotation.VisibleForTesting
import com.glia.androidsdk.queuing.Queue
import com.glia.androidsdk.queuing.QueueState
import com.glia.widgets.core.queue.GliaQueueRepository
import com.glia.widgets.helper.rx.Schedulers
import com.glia.widgets.helper.supportMessaging
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.BehaviorSubject

internal class IsMessagingAvailableUseCase(
    private val queueRepository: GliaQueueRepository,
    private val schedulers: Schedulers
) {
    private val queueIdsSubject: BehaviorSubject<Array<String>> = BehaviorSubject.create()

    private val isMessagingAvailable: Observable<Boolean> by lazy {
        queueIdsSubject.distinctUntilChanged { arr1, arr2 -> arr1.contentEquals(arr2) }
            .flatMapSingle { mapQueues(it) }
            .subscribeOn(schedulers.computationScheduler)
            .observeOn(schedulers.mainScheduler)
    }

    operator fun invoke(queueIds: Array<String>): Observable<Boolean> {
        queueIdsSubject.onNext(queueIds)
        return isMessagingAvailable
    }

    private fun mapQueues(queueIds: Array<String>) =
        queueRepository.queues.map { containsMessagingQueue(queueIds, it) }

    @VisibleForTesting
    fun containsMessagingQueue(queueIds: Array<String>, queues: Array<Queue>): Boolean = queues
        .asSequence()
        .filter { queueIds.contains(it.id) }
        .filterNot { it.state.status == QueueState.Status.CLOSED }
        .filterNot { it.state.status == QueueState.Status.UNKNOWN }
        .any { it.supportMessaging() }
}
