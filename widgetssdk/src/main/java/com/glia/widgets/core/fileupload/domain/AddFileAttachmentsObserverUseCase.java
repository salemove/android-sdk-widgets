package com.glia.widgets.core.fileupload.domain;

import com.glia.widgets.core.fileupload.FileAttachmentRepository;

import java.util.Observer;

public class AddFileAttachmentsObserverUseCase {
    private final FileAttachmentRepository repository;

    public AddFileAttachmentsObserverUseCase(FileAttachmentRepository repository) {
        this.repository = repository;
    }

    public void execute(Observer observer) {
        this.repository.addObserver(observer);
    }
}
