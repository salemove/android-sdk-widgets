package com.glia.widgets.core.queue.domain

import com.glia.androidsdk.GliaException
import com.glia.androidsdk.queuing.Queue
import com.glia.widgets.di.GliaCore
import java.util.function.Consumer

internal interface UnsubscribeFromQueueUpdatesUseCase {
    operator fun invoke(onError: Consumer<GliaException>?, callback: Consumer<Queue>)
}

internal class UnsubscribeFromQueueUpdatesUseCaseImpl(
    private val gliaCore: GliaCore
) : UnsubscribeFromQueueUpdatesUseCase {
    override fun invoke(onError: Consumer<GliaException>?, callback: Consumer<Queue>) {
        gliaCore.unsubscribeFromQueueUpdates(onError, callback)
    }
}
