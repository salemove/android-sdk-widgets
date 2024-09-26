package com.glia.widgets.core.queue

import com.glia.androidsdk.queuing.Queue
import com.glia.widgets.di.GliaCore
import io.reactivex.rxjava3.core.Single

internal class GliaQueueRepository(private val gliaCore: GliaCore) {
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
