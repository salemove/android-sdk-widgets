package com.glia.widgets.filepreview.domain.usecase;

import com.glia.androidsdk.chat.AttachmentFile;
import com.glia.widgets.filepreview.data.GliaFileRepository;
import com.glia.widgets.filepreview.domain.exception.FileNameMissingException;
import com.glia.widgets.filepreview.domain.exception.RemoteFileIsDeletedException;

import io.reactivex.Completable;

public class DownloadFileUseCase {
    private final GliaFileRepository fileRepository;

    public DownloadFileUseCase(GliaFileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }

    public Completable execute(AttachmentFile file) {
        if (file == null || file.getName().isEmpty())
            return Completable.error(new FileNameMissingException());
        if (file.isDeleted())
            return Completable.error(new RemoteFileIsDeletedException());

        return this.fileRepository
                .downloadFileFromNetwork(file);
    }
}
