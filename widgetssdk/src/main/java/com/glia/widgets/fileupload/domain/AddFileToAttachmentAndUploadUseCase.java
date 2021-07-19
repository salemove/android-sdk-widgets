package com.glia.widgets.fileupload.domain;

import android.net.Uri;

import com.glia.widgets.fileupload.FileAttachmentRepository;
import com.glia.widgets.fileupload.exception.RemoveBeforeReUploadingException;

public class AddFileToAttachmentAndUploadUseCase {
    private final FileAttachmentRepository repository;

    public AddFileToAttachmentAndUploadUseCase(FileAttachmentRepository repository) {
        this.repository = repository;
    }

    public void execute(Uri fileUri, Listener listener) {
        if (repository.isFileAttached(fileUri)) {
            listener.onError(new RemoveBeforeReUploadingException());
            return;
        }
        repository.attachFile(fileUri);
        repository.uploadFile(fileUri);
        listener.onSuccess();
    }

    public interface Listener {
        void onSuccess();

        void onError(Exception ex);
    }
}
