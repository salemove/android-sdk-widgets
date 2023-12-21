package com.glia.widgets.core.secureconversations.domain

import com.glia.widgets.chat.ChatType
import com.glia.widgets.core.engagement.GliaEngagementConfigRepository
import com.glia.widgets.engagement.IsQueueingOrEngagementUseCase

internal class IsSecureEngagementUseCase(
    private val engagementConfigRepository: GliaEngagementConfigRepository,
    private val isQueueingOrEngagementUseCase: IsQueueingOrEngagementUseCase
) {
    operator fun invoke(): Boolean {
        return engagementConfigRepository.chatType == ChatType.SECURE_MESSAGING && !isQueueingOrEngagementUseCase()
    }
}
