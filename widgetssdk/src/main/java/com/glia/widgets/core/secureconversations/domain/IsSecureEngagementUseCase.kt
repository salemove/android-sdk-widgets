package com.glia.widgets.core.secureconversations.domain

import com.glia.widgets.chat.ChatType
import com.glia.widgets.core.engagement.GliaEngagementConfigRepository

class IsSecureEngagementUseCase(private val engagementConfigRepository: GliaEngagementConfigRepository) {
    operator fun invoke(): Boolean {
        return engagementConfigRepository.chatType == ChatType.SECURE_MESSAGING
    }
}