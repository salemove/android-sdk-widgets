package com.glia.widgets.core.secureconversations.domain

import com.glia.androidsdk.queuing.Queue
import com.glia.androidsdk.queuing.QueueState
import com.glia.widgets.helper.supportMessaging

internal class IsMessagingAvailableUseCase {

    operator fun invoke(queues: List<Queue>): Boolean = queues
        .asSequence()
        .filterNot { it.state.status == QueueState.Status.CLOSED }
        .filterNot { it.state.status == QueueState.Status.UNKNOWN }
        .any { it.supportMessaging() }
}
