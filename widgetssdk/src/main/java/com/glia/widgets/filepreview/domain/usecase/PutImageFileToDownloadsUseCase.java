package com.glia.widgets.filepreview.domain.usecase;

import android.graphics.Bitmap;

import com.glia.widgets.filepreview.data.GliaFileRepository;
import com.glia.widgets.filepreview.domain.exception.FileNameMissingException;

import io.reactivex.Completable;

public class PutImageFileToDownloadsUseCase {
    private final GliaFileRepository gliaFileRepository;

    public PutImageFileToDownloadsUseCase(GliaFileRepository gliaFileRepository) {
        this.gliaFileRepository = gliaFileRepository;
    }

    public Completable execute(String fileName, Bitmap bitmap) {
        if (fileName == null || fileName.isEmpty()) {
            return Completable.error(new FileNameMissingException());
        }

        return gliaFileRepository
                .putImageToDownloads(fileName, bitmap);
    }
}
