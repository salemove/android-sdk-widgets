package com.glia.widgets.chat.domain

import com.glia.widgets.internal.fileupload.FileAttachmentRepository
import com.glia.widgets.internal.secureconversations.domain.ManageSecureMessagingStatusUseCase
import com.glia.widgets.engagement.domain.IsQueueingOrLiveEngagementUseCase

internal class IsSendButtonEnableUseCase(
    private val isQueueingOrLiveEngagementUseCase: IsQueueingOrLiveEngagementUseCase,
    private val fileAttachmentRepository: FileAttachmentRepository,
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
