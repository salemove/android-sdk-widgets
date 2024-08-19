package com.glia.widgets.core.secureconversations.domain

import com.glia.widgets.engagement.EngagementRepository

internal class IsTransferredSecureEngagementUseCase(private val engagementRepository: EngagementRepository) {
    operator fun invoke(): Boolean = engagementRepository.isTransferredSecureConversation
}
