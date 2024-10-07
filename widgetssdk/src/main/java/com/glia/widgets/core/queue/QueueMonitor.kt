package com.glia.widgets.core.queue

import com.glia.androidsdk.queuing.Queue
import com.glia.widgets.core.queue.domain.SubscribeToQueueUpdatesUseCase
import com.glia.widgets.core.queue.domain.UnsubscribeFromQueueUpdatesUseCase
import com.glia.widgets.helper.Data
import com.glia.widgets.helper.unSafeSubscribe
import com.glia.widgets.launcher.ConfigurationManager
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
    private val configurationManager: ConfigurationManager,
    private val gliaQueueRepository: GliaQueueRepository,
    private val subscribeToQueueUpdatesUseCase: SubscribeToQueueUpdatesUseCase,
    private val unsubscribeFromQueueUpdatesUseCase: UnsubscribeFromQueueUpdatesUseCase
) {

    private val _observableIntegratorQueues: BehaviorProcessor<QueueMonitorState> = BehaviorProcessor.createDefault(QueueMonitorState.Loading)
    val observableIntegratorQueues = _observableIntegratorQueues.hide()

    private val queueUpdateCallback: Consumer<Queue> = Consumer {
        updateQueuesList(it)
    }

    init {
        subscribeToQueueUpdates()
    }

    private fun subscribeToQueueUpdates() {
        Flowable.combineLatest(configurationManager.queueIdsObservable, gliaQueueRepository.fetchQueues()) { queueIds, queuesState ->
            queueIds to queuesState
        }.unSafeSubscribe { (queueIds, queuesState) ->
            when (queuesState) {
                is FetchQueuesState.Queues -> {
                    updateQueuesList(mapCurrentQueues(queuesState.queues, queueIds))
                }

                FetchQueuesState.Loading -> {
                    _observableIntegratorQueues.onNext(QueueMonitorState.Loading)
                }

                FetchQueuesState.Error -> {
                    _observableIntegratorQueues.onNext(QueueMonitorState.Error)
                }
            }
        }
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
