package com.glia.widgets.fileupload.domain;

import com.glia.widgets.fileupload.FileAttachmentRepository;
import com.glia.widgets.fileupload.model.FileAttachment;

public class RemoveFileAttachmentUseCase {
    private final FileAttachmentRepository repository;

    public RemoveFileAttachmentUseCase(FileAttachmentRepository repository) {
        this.repository = repository;
    }

    public void execute(FileAttachment attachment) {
        repository.detachFile(attachment);
    }
}
