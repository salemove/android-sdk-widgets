package com.glia.widgets.chat.domain

import com.glia.widgets.core.engagement.GliaEngagementConfigRepository
import com.glia.widgets.core.secureconversations.domain.IsMessagingAvailableUseCase
import io.reactivex.Observable

internal class IsSecureConversationsChatAvailableUseCase(
    private val engagementConfigRepository: GliaEngagementConfigRepository,
    private val isMessagingAvailableUseCase: IsMessagingAvailableUseCase
) {
    operator fun invoke(): Observable<Boolean> =
        isMessagingAvailableUseCase(engagementConfigRepository.queueIds)
}
