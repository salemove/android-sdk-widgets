package com.glia.widgets.filepreview.domain.usecase;

import android.graphics.Bitmap;

import com.glia.androidsdk.chat.AttachmentFile;
import com.glia.widgets.chat.helper.FileHelper;
import com.glia.widgets.filepreview.data.GliaFileRepository;
import com.glia.widgets.filepreview.domain.exception.FileNameMissingException;
import com.glia.widgets.filepreview.domain.exception.RemoteFileIsDeletedException;

import io.reactivex.Maybe;

public class GetImageFileFromNetworkUseCase {
    private final GliaFileRepository gliaFileRepository;

    public GetImageFileFromNetworkUseCase(GliaFileRepository gliaFileRepository) {
        this.gliaFileRepository = gliaFileRepository;
    }

    public Maybe<Bitmap> execute(AttachmentFile file) {
        if (file == null || file.getName().isEmpty()) {
            return Maybe.error(new FileNameMissingException());
        }
        if (file.isDeleted()) {
            return Maybe.error(new RemoteFileIsDeletedException());
        }

        return gliaFileRepository
                .loadImageFileFromNetwork(file)
                .flatMap(bitmap ->
                        gliaFileRepository
                                .putImageToCache(FileHelper.getFileName(file), bitmap)
                                .andThen(Maybe.just(bitmap))
                );
    }
}
