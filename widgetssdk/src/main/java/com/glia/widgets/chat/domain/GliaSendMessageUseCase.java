package com.glia.widgets.chat.domain;

import com.glia.androidsdk.GliaException;
import com.glia.androidsdk.chat.FilesAttachment;
import com.glia.androidsdk.chat.OperatorMessage;
import com.glia.androidsdk.chat.SingleChoiceAttachment;
import com.glia.androidsdk.chat.VisitorMessage;
import com.glia.androidsdk.engagement.EngagementFile;
import com.glia.widgets.chat.data.GliaChatRepository;
import com.glia.widgets.core.engagement.GliaEngagementStateRepository;
import com.glia.widgets.core.fileupload.FileAttachmentRepository;
import com.glia.widgets.core.fileupload.model.FileAttachment;

import java.util.List;

public class GliaSendMessageUseCase {
    public interface Listener {
        void messageSent(VisitorMessage message);

        void onCardMessageUpdated(OperatorMessage message);

        void onMessageValidated();

        void errorOperatorNotOnline(String message);

        void errorMessageInvalid();

        void error(GliaException ex);
    }

    private final GliaChatRepository chatRepository;
    private final FileAttachmentRepository fileAttachmentRepository;
    private final GliaEngagementStateRepository engagementStateRepository;

    public GliaSendMessageUseCase(
            GliaChatRepository chatRepository,
            FileAttachmentRepository fileAttachmentRepository,
            GliaEngagementStateRepository engagementStateRepository
    ) {
        this.chatRepository = chatRepository;
        this.fileAttachmentRepository = fileAttachmentRepository;
        this.engagementStateRepository = engagementStateRepository;
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

    public void execute(String cardMessageId, String text, String value, Listener listener) {
        chatRepository.sendResponse(cardMessageId, text, value, listener);
    }

    private boolean isOperatorOnline() {
        return engagementStateRepository.isOperatorPresent();
    }

    private boolean canSendMessage(String message, int numOfAttachment) {
        return message.length() > 0 || numOfAttachment > 0;
    }
}
