package com.glia.widgets.core.queue

import android.annotation.SuppressLint
import com.glia.androidsdk.RequestCallback
import com.glia.androidsdk.queuing.Queue
import com.glia.widgets.core.queue.domain.FetchQueuesUseCase
import com.glia.widgets.core.queue.domain.SubscribeToQueueUpdatesUseCase
import com.glia.widgets.core.queue.domain.UnsubscribeFromQueueUpdatesUseCase
import com.glia.widgets.helper.Data
import com.glia.widgets.launcher.ConfigurationManager
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.processors.BehaviorProcessor
import java.util.function.Consumer

internal sealed interface QueueMonitorState {
    object Loading : QueueMonitorState
    data class Queues(val queues: List<Queue>) : QueueMonitorState
    object Empty : QueueMonitorState
    object Error : QueueMonitorState
}

internal class QueueMonitor(
    configurationManager: ConfigurationManager,
    private val fetchQueuesUseCase: FetchQueuesUseCase,
    private val subscribeToQueueUpdatesUseCase: SubscribeToQueueUpdatesUseCase,
    private val unsubscribeFromQueueUpdatesUseCase: UnsubscribeFromQueueUpdatesUseCase
) {

    // Queues received from an integrator or default queues
    private val _integratorQueues: Flowable<List<Queue>> = configurationManager.queueIdsObservable
        .flatMap(::findIntegratorQueues)

    private val _observableIntegratorQueues: BehaviorProcessor<QueueMonitorState> = BehaviorProcessor.createDefault(QueueMonitorState.Loading)
    val observableIntegratorQueues = _observableIntegratorQueues.hide()

    private val queueUpdateCallback: Consumer<Queue> = Consumer {
        updateQueuesList(it)
    }

    init {
        subscribeToQueueUpdates()
    }

    private fun findIntegratorQueues(queueIds: Data<List<String>>): Flowable<List<Queue>> =
        fetchSiteQueues()
            .map { mapCurrentQueues(it, queueIds) }

    private fun fetchSiteQueues(): Flowable<List<Queue>> = Flowable.create({
        fetchQueuesUseCase(RequestCallback { queues, exception ->
            if (exception == null) {
                it.onNext(queues?.toList().orEmpty())
            } else {
                it.onError(exception)
            }
        })
    }, BackpressureStrategy.LATEST)

    @SuppressLint("CheckResult")
    private fun subscribeToQueueUpdates() {
        _integratorQueues.subscribe({
            updateQueuesList(it)
        }, {
            _observableIntegratorQueues.onNext(QueueMonitorState.Error)
        })
    }

    private fun updateQueuesList(queues: List<Queue>) {
        unsubscribeFromQueueUpdatesUseCase(null, queueUpdateCallback)
        if (queues.isEmpty()) {
            _observableIntegratorQueues.onNext(QueueMonitorState.Empty)
            return
        }

        _observableIntegratorQueues.onNext(QueueMonitorState.Queues(queues))
        subscribeToQueueUpdatesUseCase(queues.map { it.id }.toTypedArray(), {}, queueUpdateCallback)
    }

    private fun updateQueuesList(queue: Queue) {
        val updatedQueues = _observableIntegratorQueues.value
            ?.let { it as? QueueMonitorState.Queues }
            ?.run { queues.map { if (it.id == queue.id) queue else it } } ?: listOf(queue)

        _observableIntegratorQueues.onNext(QueueMonitorState.Queues(updatedQueues))
    }

    private fun mapCurrentQueues(siteQueues: List<Queue>, queueIds: Data<List<String>>): List<Queue> {
        return when (queueIds) {
            is Data.Value -> {
                siteQueues.filter { queueIds.result.contains(it.id) }
            }

            else -> {
                siteQueues.filter { it.isDefault == true }
            }
        }
    }
}
