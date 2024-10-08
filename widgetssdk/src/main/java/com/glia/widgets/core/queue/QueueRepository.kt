package com.glia.widgets.core.queue

import com.glia.androidsdk.queuing.Queue
import com.glia.widgets.di.GliaCore
import com.glia.widgets.helper.Data
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.TAG
import com.glia.widgets.helper.unSafeSubscribe
import com.glia.widgets.launcher.ConfigurationManager
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.processors.BehaviorProcessor
import java.util.function.Consumer

internal sealed interface QueuesState {
    object Loading : QueuesState
    object Empty : QueuesState
    data class Queues(val queues: List<Queue>) : QueuesState
    data class Error(val error: Throwable) : QueuesState
}

internal class QueueRepository(private val gliaCore: GliaCore, private val configurationManager: ConfigurationManager) {

    private val queueUpdateCallback: Consumer<Queue> = Consumer { updateQueues(it) }
    private val siteQueues: BehaviorProcessor<Result<List<Queue>>> = BehaviorProcessor.create()

    private val _observableIntegratorQueues: BehaviorProcessor<QueuesState> = BehaviorProcessor.createDefault(QueuesState.Loading)
    val observableIntegratorQueues = _observableIntegratorQueues.hide()
        .doOnSubscribe { fetchQueues() }
        .distinctUntilChanged()

    val integratorQueueIds: List<String>
        get() = _observableIntegratorQueues.value
            ?.let { it as? QueuesState.Queues }
            ?.run { queues.map { it.id } }
            .orEmpty()

    init {
        subscribeToQueues()
        subscribeToQueueUpdates()
    }

    private fun fetchQueues() {
        if (siteQueues.value?.isSuccess != true) {
            _observableIntegratorQueues.onNext(QueuesState.Loading)
            gliaCore.getQueues { queues, exception ->
                when {
                    queues != null -> siteQueues.onNext(Result.success(queues.toList()))
                    else -> {
                        siteQueues.onNext(Result.failure(exception ?: RuntimeException("Fetching queues failed")))
                        Logger.e(TAG, "Fetching queues failed", exception)
                    }
                }
            }
        }
    }

    private fun subscribeToQueueUpdates() {
        _observableIntegratorQueues
            .filter { it is QueuesState.Queues }
            .map { it as QueuesState.Queues }
            .map { it.queues }
            .map { queues -> queues.map { it.id } }
            .map { it.toTypedArray() }
            .unSafeSubscribe { gliaCore.subscribeToQueueStateUpdates(it, {}, queueUpdateCallback) }
    }

    private fun subscribeToQueues() {
        Flowable.combineLatest(configurationManager.queueIdsObservable, siteQueues) { queueIds, queuesState ->
            queueIds to queuesState
        }.unSafeSubscribe { (integratorQueueIds, siteQueueResult) ->
            siteQueueResult.fold(
                onSuccess = { updateQueues(mapCurrentQueues(it, integratorQueueIds)) },
                onFailure = { _observableIntegratorQueues.onNext(QueuesState.Error(it)) }
            )
        }
    }

    private fun updateQueues(queues: List<Queue>) {
        if (queues.isEmpty()) {
            _observableIntegratorQueues.onNext(QueuesState.Empty)
            return
        }

        _observableIntegratorQueues.onNext(QueuesState.Queues(queues))
    }

    private fun updateQueues(queue: Queue) {
        val currentQueues = _observableIntegratorQueues.value
            ?.let { it as? QueuesState.Queues }
            ?.queues
            ?.toMutableList() ?: return

        val index = currentQueues.indexOfFirst { it.id == queue.id }.takeIf { it != -1 } ?: return

        currentQueues[index] = queue

        _observableIntegratorQueues.onNext(QueuesState.Queues(currentQueues.toList()))
    }

    private fun mapCurrentQueues(siteQueues: List<Queue>, queueIds: Data<List<String>>): List<Queue> = when (queueIds) {
        is Data.Value -> siteQueues.filter { queueIds.result.contains(it.id) }
        else -> siteQueues.filter { it.isDefault == true }
    }
}
