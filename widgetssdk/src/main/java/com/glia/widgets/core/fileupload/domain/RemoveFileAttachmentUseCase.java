package com.glia.widgets.core.fileupload.domain;

import com.glia.widgets.core.fileupload.FileAttachmentRepository;
import com.glia.widgets.core.fileupload.model.FileAttachment;

public class RemoveFileAttachmentUseCase {
    private final FileAttachmentRepository repository;

    public RemoveFileAttachmentUseCase(FileAttachmentRepository repository) {
        this.repository = repository;
    }

    public void execute(FileAttachment attachment) {
        repository.detachFile(attachment);
    }
}
