package com.glia.widgets.core.secureconversations.domain

import com.glia.widgets.chat.ChatType
import com.glia.widgets.core.engagement.GliaEngagementConfigRepository
import com.glia.widgets.core.queue.GliaQueueRepository
import com.glia.widgets.core.queue.model.GliaQueueingState
import com.glia.widgets.engagement.HasOngoingEngagementUseCase

internal class IsSecureEngagementUseCase(
    private val engagementConfigRepository: GliaEngagementConfigRepository,
    private val hasOngoingEngagementUseCase: HasOngoingEngagementUseCase,
    private val queueRepository: GliaQueueRepository
) {
    operator fun invoke(): Boolean {
        return engagementConfigRepository.chatType == ChatType.SECURE_MESSAGING &&
            !hasOngoingEngagementUseCase() &&
            queueRepository.queueingState is GliaQueueingState.None
    }
}
