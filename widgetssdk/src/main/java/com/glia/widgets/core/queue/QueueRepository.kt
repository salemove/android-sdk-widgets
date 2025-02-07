package com.glia.widgets.core.queue

import com.glia.androidsdk.GliaException
import com.glia.widgets.di.GliaCore
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.TAG
import com.glia.widgets.helper.unSafeSubscribe
import com.glia.widgets.launcher.ConfigurationManager
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.processors.BehaviorProcessor
import java.util.function.Consumer
import com.glia.androidsdk.queuing.Queue as CoreSdkQueue

internal sealed interface QueuesState {
    fun queuesOrEmpty(): List<Queue> = when (this) {
        is Queues -> queues
        else -> emptyList()
    }

    data object Loading : QueuesState
    data object Empty : QueuesState
    data class Queues(val queues: List<Queue>) : QueuesState
    data class Error(val error: Throwable) : QueuesState
}

internal interface QueueRepository {
    val queuesState: Flowable<QueuesState>
    val relevantQueueIds: Single<List<String>>
    fun initialize()
    fun fetchQueues()
}

internal class QueueRepositoryImpl(private val gliaCore: GliaCore, private val configurationManager: ConfigurationManager) : QueueRepository {

    private val queueUpdateCallback: Consumer<CoreSdkQueue> = Consumer { updateQueues(it) }
    private val siteQueues: BehaviorProcessor<List<Queue>> = BehaviorProcessor.create()

    private val _queuesState: BehaviorProcessor<QueuesState> = BehaviorProcessor.create()
    override val queuesState = _queuesState.hide()
        .doOnSubscribe { fetchQueues() }
        .distinctUntilChanged()

    private val _relevantQueueIds: Flowable<List<String>>
        get() = queuesState
            .filter { it !is QueuesState.Loading }
            .map { it.queuesOrEmpty() }
            .map { queues -> queues.map { it.id } }

    override val relevantQueueIds: Single<List<String>>
        get() = _relevantQueueIds.firstOrError()

    override fun initialize() {
        fetchQueues()
    }

    override fun fetchQueues() {
        if (gliaCore.isInitialized && siteQueues.value == null) {
            _queuesState.onNext(QueuesState.Loading)

            gliaCore.getQueues(::siteQueuesReceived, ::reportGetSiteQueuesError)
        }
    }

    private fun siteQueuesReceived(queues: Array<CoreSdkQueue>) {
        siteQueues.onNext(queues.map { it.asLocalQueue() })
        subscribeToQueues()
        subscribeToQueueUpdates()
    }

    private fun reportGetSiteQueuesError(exception: GliaException?) {
        val ex = exception ?: RuntimeException("Fetching queues failed: queues were null")
        Logger.e(TAG, "Setting up queues. Failed to get site queues.", ex)
        _queuesState.onNext(QueuesState.Error(ex))
    }

    private fun subscribeToQueueUpdates() {
        _relevantQueueIds
            .filter { it.isNotEmpty() }
            .distinctUntilChanged()
            .unSafeSubscribe { gliaCore.subscribeToQueueStateUpdates(it, {}, queueUpdateCallback) }
    }

    private fun subscribeToQueues() {
        Flowable.combineLatest(configurationManager.queueIdsObservable, siteQueues, ::Pair)
            .distinctUntilChanged()
            .unSafeSubscribe { (integratorQueueIds, siteQueues) ->
                Logger.d(TAG, "Setting up queues. Site has ${siteQueues.count()} queues.")
                onQueuesReceived(integratorQueueIds, siteQueues)
            }
    }

    private fun onQueuesReceived(queueIds: List<String>, siteQueues: List<Queue>) {
        when {
            siteQueues.isEmpty() -> {
                Logger.w(TAG, "Setting up queues. Site has no queues.")
                _queuesState.onNext(QueuesState.Empty)
            }

            queueIds.isEmpty() -> {
                Logger.i(TAG, "Setting up queues. Integrator specified an empty list of queues.")
                setDefaultQueues(siteQueues)
            }

            else -> {
                matchQueues(queueIds, siteQueues)
            }
        }
    }

    private fun setDefaultQueues(siteQueues: List<Queue>) {
        val defaultQueues = siteQueues.filter { it.isDefault == true }

        Logger.i(TAG, "Setting up queues. Using ${defaultQueues.count()} default queues.")
        if (defaultQueues.isEmpty()) {
            _queuesState.onNext(QueuesState.Empty)
        } else {
            _queuesState.onNext(QueuesState.Queues(defaultQueues))
        }
    }

    private fun updateQueues(coreSdkQueue: CoreSdkQueue) {
        val currentQueues = _queuesState.value
            ?.let { it as? QueuesState.Queues }
            ?.queues
            ?.toMutableList() ?: return

        val index = currentQueues.indexOfFirst { it.id == coreSdkQueue.id }.takeIf { it != -1 } ?: return

        val currentQueue = currentQueues[index]

        if (currentQueue.lastUpdatedMillis > coreSdkQueue.lastUpdatedMillis) {
            // Skip if the current queue is newer then the received one
            return
        }

        currentQueues[index] = currentQueue merge coreSdkQueue.asLocalQueue()

        _queuesState.onNext(QueuesState.Queues(currentQueues.toList()))
    }

    private fun matchQueues(queueIds: List<String>, siteQueues: List<Queue>) {
        val matchedQueues = siteQueues.filter { queueIds.contains(it.id) }
        Logger.i(
            TAG,
            "Setting up queues. ${matchedQueues.count()} out of ${queueIds.count()} queues provided by an integrator match with site queues."
        )
        if (matchedQueues.isEmpty()) {
            setDefaultQueues(siteQueues)
        } else {
            _queuesState.onNext(QueuesState.Queues(matchedQueues))
        }
    }

}
