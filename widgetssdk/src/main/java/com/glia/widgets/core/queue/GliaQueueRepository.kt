package com.glia.widgets.core.queue

import com.glia.androidsdk.queuing.Queue
import com.glia.widgets.di.GliaCore
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.TAG
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.processors.BehaviorProcessor

internal sealed interface FetchQueuesState {
    object Loading : FetchQueuesState
    data class Queues(val queues: List<Queue>) : FetchQueuesState
    object Error : FetchQueuesState
}

internal class GliaQueueRepository(private val gliaCore: GliaCore) {

    private val _queues: BehaviorProcessor<FetchQueuesState> = BehaviorProcessor.createDefault(FetchQueuesState.Loading)

    fun fetchQueues(): Flowable<FetchQueuesState> {
        if (_queues.value !is FetchQueuesState.Queues) {
            _queues.onNext(FetchQueuesState.Loading)
            gliaCore.getQueues { queues, exception ->
                when {
                    queues != null -> _queues.onNext(FetchQueuesState.Queues(queues.toList()))
                    else -> {
                        _queues.onNext(FetchQueuesState.Error)
                        Logger.e(TAG, "Fetching queues failed", exception)
                    }
                }
            }
        }
        return _queues.hide()
    }

    /**
     * Emits a list of all Queues
     */
    val queues: Single<Array<Queue>>
        get() = Single.create { emitter ->
            gliaCore.getQueues { queues, exception ->
                when {
                    exception != null -> emitter.onError(exception)
                    queues != null -> emitter.onSuccess(queues)
                    else -> emitter.onError(RuntimeException("Fetching queues failed"))
                }
            }
        }
}
