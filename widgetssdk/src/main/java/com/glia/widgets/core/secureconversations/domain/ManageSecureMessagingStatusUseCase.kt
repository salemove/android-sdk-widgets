package com.glia.widgets.core.secureconversations.domain

import com.glia.widgets.engagement.EngagementRepository

internal class ManageSecureMessagingStatusUseCase(private val engagementRepository: EngagementRepository) {

    val shouldUseSecureMessagingEndpoints: Boolean
        get() = engagementRepository.isSecureMessagingRequested
            && !engagementRepository.isQueueingOrLiveEngagement
            && !engagementRepository.isTransferredSecureConversation

    val shouldBehaveAsSecureMessaging: Boolean
        get() = engagementRepository.isSecureMessagingRequested || engagementRepository.isTransferredSecureConversation

    fun updateSecureMessagingStatus(isRequested: Boolean) = engagementRepository.updateIsSecureMessagingRequested(isRequested)
}
