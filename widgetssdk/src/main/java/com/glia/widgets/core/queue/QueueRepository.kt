package com.glia.widgets.core.queue

import com.glia.androidsdk.queuing.Queue
import com.glia.widgets.di.GliaCore
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

internal interface QueueRepository {
    val queuesState: Flowable<QueuesState>
    val relevantQueueIds: List<String>
}

internal class QueueRepositoryImpl(private val gliaCore: GliaCore, private val configurationManager: ConfigurationManager) : QueueRepository {

    private val queueUpdateCallback: Consumer<Queue> = Consumer { updateQueues(it) }
    private val siteQueues: BehaviorProcessor<Result<List<Queue>>> = BehaviorProcessor.create()

    private val _queuesState: BehaviorProcessor<QueuesState> = BehaviorProcessor.createDefault(QueuesState.Loading)
    override val queuesState = _queuesState.hide()
        .doOnSubscribe { fetchQueues() }
        .distinctUntilChanged()

    override val relevantQueueIds: List<String>
        get() = _queuesState.value
            ?.let { it as? QueuesState.Queues }
            ?.run { queues.map { it.id } }
            .orEmpty()

    init {
        subscribeToQueues()
        subscribeToQueueUpdates()
    }

    private fun fetchQueues() {
        if (siteQueues.value?.isSuccess != true) {
            _queuesState.onNext(QueuesState.Loading)
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
        _queuesState
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
                onSuccess = {
                    Logger.i(TAG, "Setting up queues. site has ${it.count()} queues")
                    onQueuesReceived(integratorQueueIds, it)
                },
                onFailure = {
                    Logger.e(TAG, "Setting up queues. Failed to get site queues", it)
                    _queuesState.onNext(QueuesState.Error(it))
                }
            )
        }
    }

    private fun onQueuesReceived(queueIds: List<String>, siteQueues: List<Queue>) {
        when {
            siteQueues.isEmpty() -> {
                Logger.i(TAG, "Setting up queues. Site has no queues")
                _queuesState.onNext(QueuesState.Empty)
            }

            queueIds.isEmpty() -> {
                Logger.i(TAG, "Setting up queues. Integrator queues are empty")
                setDefaultQueues(siteQueues)
            }

            else -> {
                Logger.i(TAG, "Setting up queues. Matching queues")
                matchQueues(queueIds, siteQueues)
            }
        }
    }

    private fun setDefaultQueues(siteQueues: List<Queue>) {
        Logger.i(TAG, "Setting up queues. Falling back to default queues")

        val defaultQueues = siteQueues.filter { it.isDefault == true }

        if (defaultQueues.isEmpty()) {
            Logger.w(TAG, "Setting up queues. No default queues")
            _queuesState.onNext(QueuesState.Empty)
        } else {
            Logger.i(TAG, "Setting up queues. Using default ${defaultQueues.count()} queues")
            _queuesState.onNext(QueuesState.Queues(defaultQueues))
        }
    }

    private fun updateQueues(queue: Queue) {
        val currentQueues = _queuesState.value
            ?.let { it as? QueuesState.Queues }
            ?.queues
            ?.toMutableList() ?: return

        val index = currentQueues.indexOfFirst { it.id == queue.id }.takeIf { it != -1 } ?: return

        currentQueues[index] = queue

        _queuesState.onNext(QueuesState.Queues(currentQueues.toList()))
    }

    private fun matchQueues(queueIds: List<String>, siteQueues: List<Queue>) {
        val matchedQueues = siteQueues.filter { queueIds.contains(it.id) }

        if (matchedQueues.isEmpty()) {
            setDefaultQueues(siteQueues)
        } else {
            Logger.i(
                TAG,
                "Setting up queues. ${matchedQueues.count()} out of ${queueIds.count()} queues provided by an integrator match with site queues."
            )
            _queuesState.onNext(QueuesState.Queues(matchedQueues))
        }
    }

}
