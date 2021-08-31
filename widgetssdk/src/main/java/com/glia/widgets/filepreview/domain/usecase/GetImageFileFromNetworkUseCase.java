package com.glia.widgets.filepreview.domain.usecase;

import android.graphics.Bitmap;

import com.glia.widgets.filepreview.data.GliaFileRepository;
import com.glia.widgets.filepreview.domain.exception.FileNameMissingException;

import io.reactivex.Maybe;

public class GetImageFileFromNetworkUseCase {
    private final GliaFileRepository gliaFileRepository;

    public GetImageFileFromNetworkUseCase(GliaFileRepository gliaFileRepository) {
        this.gliaFileRepository = gliaFileRepository;
    }

    public Maybe<Bitmap> execute(String fileName) {
        if (fileName == null || fileName.isEmpty())
            return Maybe.error(new FileNameMissingException());
        return gliaFileRepository
                .loadFromNetwork(fileName)
                .flatMap(bitmap ->
                        gliaFileRepository.putToCache(fileName, bitmap)
                                .andThen(Maybe.just(bitmap))
                );
    }
}
