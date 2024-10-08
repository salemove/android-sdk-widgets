package com.glia.widgets.core.engagement.domain

import com.glia.widgets.chat.ChatType
import com.glia.widgets.core.engagement.GliaEngagementConfigRepository

internal class SetEngagementConfigUseCase(private val engagementConfigRepository: GliaEngagementConfigRepository) {
    operator fun invoke(chatType: ChatType, queueIds: List<String>) {
        engagementConfigRepository.chatType = chatType
        engagementConfigRepository.queueIds = queueIds
    }
}
