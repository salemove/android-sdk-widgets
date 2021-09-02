package com.glia.widgets.filepreview.domain.usecase;

import android.graphics.Bitmap;

import com.glia.androidsdk.chat.AttachmentFile;
import com.glia.widgets.filepreview.data.GliaFileRepository;
import com.glia.widgets.filepreview.domain.exception.FileNameMissingException;
import com.glia.widgets.filepreview.domain.exception.RemoteImageIsDeletedException;
import com.glia.widgets.helper.Logger;

import io.reactivex.Maybe;

public class GetImageFileFromNetworkUseCase {
    private final GliaFileRepository gliaFileRepository;

    public GetImageFileFromNetworkUseCase(GliaFileRepository gliaFileRepository) {
        this.gliaFileRepository = gliaFileRepository;
    }

    public Maybe<Bitmap> execute(AttachmentFile file) {
        if (file == null || file.getName().isEmpty())
            return Maybe.error(new FileNameMissingException());
        if (file.isDeleted())
            return Maybe.error(new RemoteImageIsDeletedException());

        return gliaFileRepository
                .loadFromNetwork(file)
                .flatMap(bitmap ->
                        gliaFileRepository.putToCache(file.getId() + "." + file.getName(), bitmap)
                                .andThen(Maybe.just(bitmap))
                );
    }
}
