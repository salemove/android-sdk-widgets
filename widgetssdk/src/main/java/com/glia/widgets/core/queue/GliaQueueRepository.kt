package com.glia.widgets.core.queue

import com.glia.androidsdk.queuing.Queue
import com.glia.widgets.di.GliaCore
import io.reactivex.Single

class GliaQueueRepository(private val gliaCore: GliaCore) {
    /**
     * Emits a list of all Queues
     */
    val queues: Single<Array<Queue>>
        get() = Single.create {
            gliaCore.getQueues { queues, exception ->
                exception?.apply { it.onError(this) } ?: it.onSuccess(queues)
            }
        }
}
