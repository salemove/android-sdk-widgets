package com.glia.widgets.core.fileupload.domain;

import static com.glia.widgets.core.fileupload.domain.AddFileToAttachmentAndUploadUseCase.SUPPORTED_FILE_COUNT;

import com.glia.widgets.core.fileupload.FileAttachmentRepository;

public class SupportedFileCountCheckUseCase {
    private final FileAttachmentRepository repository;

    public SupportedFileCountCheckUseCase(FileAttachmentRepository repository) {
        this.repository = repository;
    }

    public boolean execute() {
        return repository.getFileAttachments().size() <= SUPPORTED_FILE_COUNT;
    }
}
