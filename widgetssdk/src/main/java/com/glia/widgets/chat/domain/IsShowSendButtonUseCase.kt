package com.glia.widgets.chat.domain

import com.glia.widgets.core.fileupload.EngagementFileAttachmentRepository
import com.glia.widgets.core.secureconversations.domain.ManageSecureMessagingStatusUseCase
import com.glia.widgets.engagement.domain.IsQueueingOrLiveEngagementUseCase

internal class IsShowSendButtonUseCase(
    private val isQueueingOrLiveEngagementUseCase: IsQueueingOrLiveEngagementUseCase,
    private val fileAttachmentRepository: EngagementFileAttachmentRepository,
    private val manageSecureMessagingStatusUseCase: ManageSecureMessagingStatusUseCase
) {
    operator fun invoke(message: String?): Boolean {
        return when {
            manageSecureMessagingStatusUseCase.shouldBehaveAsSecureMessaging -> hasText(message) || hadReadyToSendUnsentAttachments()
            else -> hasText(message) || hasEngagementOngoingAndReadyToSendUnsentAttachments()
        }
    }

    private fun hasText(message: String?): Boolean {
        return !message.isNullOrEmpty()
    }

    private fun hadReadyToSendUnsentAttachments(): Boolean {
        return fileAttachmentRepository.getReadyToSendFileAttachments().isNotEmpty()
    }

    private fun hasEngagementOngoingAndReadyToSendUnsentAttachments(): Boolean {
        return isQueueingOrLiveEngagementUseCase.hasOngoingLiveEngagement && hadReadyToSendUnsentAttachments()
    }
}
