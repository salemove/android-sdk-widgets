package com.glia.widgets.filepreview.data;


import android.graphics.Bitmap;

import io.reactivex.Completable;
import io.reactivex.Maybe;

public interface GliaFileRepository {
    Maybe<Bitmap> loadFromDownloads(String fileName);

    Maybe<Bitmap> loadFromCache(String fileName);

    Maybe<Bitmap> loadFromNetwork(String fileName);

    Completable putToCache(String fileName, Bitmap bitmap);

    Completable putToDownloads(String fileName, Bitmap bitmap);
}
