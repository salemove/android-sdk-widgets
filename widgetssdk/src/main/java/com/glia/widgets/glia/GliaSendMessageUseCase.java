package com.glia.widgets.glia;

import com.glia.androidsdk.GliaException;
import com.glia.androidsdk.RequestCallback;
import com.glia.androidsdk.chat.FilesAttachment;
import com.glia.androidsdk.chat.SingleChoiceAttachment;
import com.glia.androidsdk.chat.VisitorMessage;
import com.glia.androidsdk.engagement.EngagementFile;
import com.glia.widgets.fileupload.FileAttachmentRepository;
import com.glia.widgets.fileupload.model.FileAttachment;
import com.glia.widgets.model.GliaChatRepository;

import java.util.List;

public class GliaSendMessageUseCase implements RequestCallback<VisitorMessage> {

    public interface Listener {
        void messageSent(VisitorMessage message, GliaException exception);
    }

    private final GliaChatRepository repository;
    private final FileAttachmentRepository fileAttachmentRepository;
    private Listener listener;

    public GliaSendMessageUseCase(GliaChatRepository repository, FileAttachmentRepository fileAttachmentRepository) {
        this.repository = repository;
        this.fileAttachmentRepository = fileAttachmentRepository;
    }

    public void execute(String message, Listener listener) {
        this.listener = listener;
        List<FileAttachment> fileAttachments = fileAttachmentRepository.getReadyToSendFileAttachments();

        if (fileAttachments.size() > 0) {
            EngagementFile[] engagementFiles =
                    fileAttachments
                            .stream()
                            .map(FileAttachment::getEngagementFile)
                            .toArray(EngagementFile[]::new);
            fileAttachmentRepository.detachFiles(fileAttachments);
            repository.sendMessage(message, FilesAttachment.from(engagementFiles), this);
        } else {
            repository.sendMessage(message, this);
        }
    }

    public void execute(SingleChoiceAttachment singleChoiceAttachment, Listener listener) {
        repository.sendMessage(singleChoiceAttachment, this);
    }

    public void execute(FilesAttachment filesAttachment, Listener listener) {
        this.listener = listener;
        repository.sendMessage(filesAttachment, this);
    }

    public void execute(String message, FilesAttachment filesAttachment, Listener listener) {
        this.listener = listener;
        repository.sendMessage(message, filesAttachment, this);
    }

    public void unregisterListener(Listener listener) {
        if (this.listener == listener) {
            this.listener = null;
        }
    }

    @Override
    public void onResult(VisitorMessage visitorMessage, GliaException e) {
        if (this.listener != null) {
            listener.messageSent(visitorMessage, e);
        }
    }
}
