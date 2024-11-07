package com.glia.widgets.core.secureconversations.domain

import com.glia.widgets.engagement.EngagementRepository
import com.glia.widgets.engagement.domain.IsQueueingOrEngagementUseCase

internal class ManageSecureMessagingStatusUseCase(
    private val isQueueingOrEngagementUseCase: IsQueueingOrEngagementUseCase,
    private val engagementRepository: EngagementRepository
) {
    //TODO this function should be changed for GVA -> SC transfer
    fun shouldUseSecureMessagingEndpoints(): Boolean {
        return engagementRepository.isSecureMessagingRequested && !isQueueingOrEngagementUseCase()
    }

    //TODO this function should be changed for GVA -> SC transfer
    fun shouldBehaveAsSecureMessaging(): Boolean {
        return engagementRepository.isSecureMessagingRequested
    }

    fun updateSecureMessagingStatus(isRequested: Boolean) = engagementRepository.updateIsSecureMessagingRequested(isRequested)
}
