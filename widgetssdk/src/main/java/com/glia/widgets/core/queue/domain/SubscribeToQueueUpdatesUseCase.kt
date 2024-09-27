package com.glia.widgets.core.queue.domain

import com.glia.androidsdk.GliaException
import com.glia.androidsdk.queuing.Queue
import com.glia.widgets.di.GliaCore
import java.util.function.Consumer

internal interface SubscribeToQueueUpdatesUseCase {
    operator fun invoke(queueIds: Array<String>, onError: Consumer<GliaException>, callback: Consumer<Queue>)
}

internal class SubscribeToQueueUpdatesUseCaseImpl(
    private val gliaCore: GliaCore
) : SubscribeToQueueUpdatesUseCase {
    override fun invoke(queueIds: Array<String>, onError: Consumer<GliaException>, callback: Consumer<Queue>) {
        gliaCore.subscribeToQueueStateUpdates(queueIds, onError, callback)
    }
}
