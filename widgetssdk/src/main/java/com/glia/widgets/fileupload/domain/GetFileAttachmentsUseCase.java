package com.glia.widgets.fileupload.domain;

import com.glia.widgets.fileupload.FileAttachmentRepository;
import com.glia.widgets.fileupload.model.FileAttachment;

import java.util.List;

public class GetFileAttachmentsUseCase {
    private final FileAttachmentRepository repository;

    public GetFileAttachmentsUseCase(FileAttachmentRepository repository) {
        this.repository = repository;
    }

    public List<FileAttachment> execute() {
        return repository.getFileAttachments();
    }
}
