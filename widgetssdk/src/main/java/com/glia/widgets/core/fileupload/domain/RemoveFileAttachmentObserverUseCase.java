package com.glia.widgets.core.fileupload.domain;

import com.glia.widgets.core.fileupload.FileAttachmentRepository;

import java.util.Observer;

public class RemoveFileAttachmentObserverUseCase {
    private final FileAttachmentRepository repository;

    public RemoveFileAttachmentObserverUseCase(FileAttachmentRepository repository) {
        this.repository = repository;
    }

    public void execute(Observer observer) {
        this.repository.removeObserver(observer);
    }
}
