package com.glia.widgets.engagement.domain

import com.glia.androidsdk.Engagement
import com.glia.widgets.core.secureconversations.SecureConversationsRepository
import com.glia.widgets.engagement.EngagementRepository

internal interface EnqueueForEngagementUseCase {
    operator fun invoke(mediaType: Engagement.MediaType = Engagement.MediaType.TEXT)
}

internal class EnqueueForEngagementUseCaseImpl(
    private val engagementRepository: EngagementRepository,
    private val secureConversationsRepository: SecureConversationsRepository
) : EnqueueForEngagementUseCase {
    override fun invoke(mediaType: Engagement.MediaType) {
        val replaceExisting = secureConversationsRepository.hasPendingSecureConversations || engagementRepository.isTransferredSecureConversation
        engagementRepository.queueForEngagement(mediaType, replaceExisting)
    }
}
