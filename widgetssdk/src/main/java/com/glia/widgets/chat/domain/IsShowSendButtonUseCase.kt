package com.glia.widgets.chat.domain

import com.glia.widgets.core.fileupload.FileAttachmentRepository
import com.glia.widgets.core.secureconversations.domain.IsSecureEngagementUseCase
import com.glia.widgets.engagement.IsQueueingOrEngagementUseCase

internal class IsShowSendButtonUseCase(
    private val isQueueingOrEngagementUseCase: IsQueueingOrEngagementUseCase,
    private val fileAttachmentRepository: FileAttachmentRepository,
    private val isSecureEngagementUseCase: IsSecureEngagementUseCase
) {
    operator fun invoke(message: String?): Boolean {
        return when {
            isSecureEngagementUseCase() -> hasText(message) || hadReadyToSendUnsentAttachments()
            else -> hasText(message) || hasEngagementOngoingAndReadyToSendUnsentAttachments()
        }
    }

    private fun hasText(message: String?): Boolean {
        return !message.isNullOrEmpty()
    }

    private fun hadReadyToSendUnsentAttachments(): Boolean {
        return fileAttachmentRepository.readyToSendFileAttachments.isNotEmpty()
    }

    private fun hasEngagementOngoingAndReadyToSendUnsentAttachments(): Boolean {
        return isQueueingOrEngagementUseCase.hasOngoingEngagement && hadReadyToSendUnsentAttachments()
    }
}
