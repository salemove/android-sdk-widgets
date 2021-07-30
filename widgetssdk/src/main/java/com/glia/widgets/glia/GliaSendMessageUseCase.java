package com.glia.widgets.glia;

import com.glia.androidsdk.GliaException;
import com.glia.androidsdk.chat.FilesAttachment;
import com.glia.androidsdk.chat.SingleChoiceAttachment;
import com.glia.androidsdk.chat.VisitorMessage;
import com.glia.androidsdk.engagement.EngagementFile;
import com.glia.widgets.core.engagement.GliaEngagementRepository;
import com.glia.widgets.fileupload.FileAttachmentRepository;
import com.glia.widgets.fileupload.model.FileAttachment;
import com.glia.widgets.model.GliaChatRepository;

import java.util.List;

public class GliaSendMessageUseCase {
    public interface Listener {
        void messageSent(VisitorMessage message);

        void onMessageValidated();

        void errorOperatorNotOnline(String message);

        void errorMessageInvalid();

        void error(GliaException ex);
    }

    private final GliaChatRepository chatRepository;
    private final FileAttachmentRepository fileAttachmentRepository;
    private final GliaEngagementRepository engagementRepository;

    public GliaSendMessageUseCase(
            GliaChatRepository chatRepository,
            FileAttachmentRepository fileAttachmentRepository,
            GliaEngagementRepository engagementRepository
    ) {
        this.chatRepository = chatRepository;
        this.fileAttachmentRepository = fileAttachmentRepository;
        this.engagementRepository = engagementRepository;
    }

    private boolean hasFileAttachments(List<FileAttachment> fileAttachments) {
        return fileAttachments.size() > 0;
    }

    private void sendMessageWithAttachments(String message, List<FileAttachment> fileAttachments, Listener listener) {
        EngagementFile[] engagementFiles =
                fileAttachments
                        .stream()
                        .map(FileAttachment::getEngagementFile)
                        .toArray(EngagementFile[]::new);

        if (!message.isEmpty()) {
            chatRepository.sendMessageWithAttachment(message, FilesAttachment.from(engagementFiles), listener);
        } else {
            chatRepository.sendMessageAttachment(FilesAttachment.from(engagementFiles), listener);
        }
        fileAttachmentRepository.detachFiles(fileAttachments);
    }

    private void sendMessage(String message, Listener listener) {
        chatRepository.sendMessage(message, listener);
    }

    public void execute(String message, Listener listener) {
        List<FileAttachment> fileAttachments = fileAttachmentRepository.getReadyToSendFileAttachments();
        if (canSendMessage(message, fileAttachments.size())) {
            listener.onMessageValidated();
            if (isOperatorOnline()) {
                if (hasFileAttachments(fileAttachments)) {
                    sendMessageWithAttachments(message, fileAttachments, listener);
                } else {
                    sendMessage(message, listener);
                }
            } else {
                listener.errorOperatorNotOnline(message);
            }
        } else {
            listener.errorMessageInvalid();
        }
    }

    public void execute(SingleChoiceAttachment singleChoiceAttachment, Listener listener) {
        chatRepository.sendMessageSingleChoice(singleChoiceAttachment, listener);
    }

    private boolean isOperatorOnline() {
        return engagementRepository.isOperatorOnline();
    }

    private boolean canSendMessage(String message, int numOfAttachment) {
        return message.length() > 0 || numOfAttachment > 0;
    }
}
