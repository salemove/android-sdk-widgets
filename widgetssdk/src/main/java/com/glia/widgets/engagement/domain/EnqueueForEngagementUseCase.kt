package com.glia.widgets.engagement.domain

import com.glia.widgets.engagement.MediaType
import com.glia.widgets.internal.secureconversations.SecureConversationsRepository
import com.glia.widgets.engagement.EngagementRepository

internal interface EnqueueForEngagementUseCase {
    operator fun invoke(mediaType: MediaType = MediaType.TEXT)
}

internal class EnqueueForEngagementUseCaseImpl(
    private val engagementRepository: EngagementRepository,
    private val secureConversationsRepository: SecureConversationsRepository
) : EnqueueForEngagementUseCase {
    override fun invoke(mediaType: MediaType) {
        val replaceExisting = secureConversationsRepository.hasPendingSecureConversations || engagementRepository.isTransferredSecureConversation
        engagementRepository.queueForEngagement(mediaType, replaceExisting)
    }
}
