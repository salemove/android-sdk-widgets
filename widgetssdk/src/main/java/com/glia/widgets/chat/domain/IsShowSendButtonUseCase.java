package com.glia.widgets.chat.domain;

import com.glia.widgets.core.engagement.GliaEngagementRepository;
import com.glia.widgets.fileupload.FileAttachmentRepository;

public class IsShowSendButtonUseCase {
    private final GliaEngagementRepository engagementRepository;
    private final FileAttachmentRepository fileAttachmentRepository;

    public IsShowSendButtonUseCase(
            GliaEngagementRepository engagementRepository,
            FileAttachmentRepository fileAttachmentRepository
    ) {
        this.engagementRepository = engagementRepository;
        this.fileAttachmentRepository = fileAttachmentRepository;
    }

    public boolean execute(String message) {
        return hasText(message) || hasEngagementOngoingAndReadyToSendUnsentAttachments();
    }

    private boolean hasText(String message) {
        return message != null && !message.isEmpty();
    }

    private boolean hasEngagementOngoingAndReadyToSendUnsentAttachments() {
        return engagementRepository.hasOngoingEngagement() &&
                fileAttachmentRepository.getReadyToSendFileAttachments().size() > 0;
    }
}
