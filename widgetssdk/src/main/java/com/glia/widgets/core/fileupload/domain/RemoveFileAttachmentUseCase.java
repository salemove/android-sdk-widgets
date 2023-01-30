package com.glia.widgets.core.fileupload.domain;

import androidx.annotation.NonNull;

import com.glia.widgets.core.fileupload.FileAttachmentRepository;
import com.glia.widgets.core.fileupload.model.FileAttachment;

public class RemoveFileAttachmentUseCase {
    private final FileAttachmentRepository repository;

    public RemoveFileAttachmentUseCase(FileAttachmentRepository repository) {
        this.repository = repository;
    }

    public void execute(@NonNull FileAttachment attachment) {
        repository.detachFile(attachment);
    }
}
