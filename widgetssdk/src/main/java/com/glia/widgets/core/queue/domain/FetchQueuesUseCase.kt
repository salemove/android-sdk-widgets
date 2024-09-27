package com.glia.widgets.core.queue.domain

import com.glia.androidsdk.RequestCallback
import com.glia.androidsdk.queuing.Queue
import com.glia.widgets.di.GliaCore

internal interface FetchQueuesUseCase {
    operator fun invoke(callback: RequestCallback<Array<Queue>?>)
}

internal class FetchQueuesUseCaseImpl(private val gliaCore: GliaCore): FetchQueuesUseCase {
    override fun invoke(callback: RequestCallback<Array<Queue>?>) {
        gliaCore.getQueues(callback)
    }
}
