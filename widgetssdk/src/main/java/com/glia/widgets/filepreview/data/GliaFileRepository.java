package com.glia.widgets.filepreview.data;

import android.graphics.Bitmap;

import com.glia.androidsdk.chat.AttachmentFile;

import io.reactivex.Completable;
import io.reactivex.Maybe;

public interface GliaFileRepository {
    Maybe<Bitmap> loadImageFromCache(String fileName);

    Completable putImageToCache(String fileName, Bitmap bitmap);

    Maybe<Bitmap> loadImageFromDownloads(String fileName);

    Completable putImageToDownloads(String fileName, Bitmap bitmap);

    Maybe<Bitmap> loadImageFileFromNetwork(AttachmentFile file);

    Completable downloadFileFromNetwork(AttachmentFile file);
}
