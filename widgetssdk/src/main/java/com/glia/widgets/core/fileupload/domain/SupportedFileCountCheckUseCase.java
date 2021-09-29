package com.glia.widgets.core.fileupload.domain;

import com.glia.widgets.core.fileupload.FileAttachmentRepository;

public class SupportedFileCountCheckUseCase {
    private final static long SUPPORTED_FILE_COUNT = 25;

    private final FileAttachmentRepository repository;

    public SupportedFileCountCheckUseCase(FileAttachmentRepository repository) {
        this.repository = repository;
    }

    public boolean execute() {
        return repository.getFileAttachments().size() <= SUPPORTED_FILE_COUNT;
    }
}
