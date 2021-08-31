package com.glia.widgets.filepreview.data;

import android.graphics.Bitmap;

import com.glia.widgets.filepreview.data.source.local.InAppBitmapCache;
import com.glia.widgets.filepreview.data.source.local.DownloadsFolderDataSource;
import com.glia.widgets.filepreview.domain.exception.CacheFileNotFoundException;

import io.reactivex.Completable;
import io.reactivex.Maybe;

public class GliaFileRepositoryImpl implements GliaFileRepository {
    private final InAppBitmapCache bitmapCache;
    private final DownloadsFolderDataSource downloadsFolderDataSource;

    public GliaFileRepositoryImpl(InAppBitmapCache bitmapCache, DownloadsFolderDataSource downloadsFolderDataSource) {
        this.bitmapCache = bitmapCache;
        this.downloadsFolderDataSource = downloadsFolderDataSource;
    }

    @Override
    public Maybe<Bitmap> loadFromDownloads(String fileName) {
        return downloadsFolderDataSource.getImageFromDownloads(fileName);
    }

    @Override
    public Maybe<Bitmap> loadFromCache(String fileName) {
        return Maybe.create(emitter -> {
            Bitmap bitmap = bitmapCache.getBitmapById(fileName);
            if (bitmap != null) emitter.onSuccess(bitmap);
            else emitter.onError(new CacheFileNotFoundException());
        });
    }

    @Override
    public Maybe<Bitmap> loadFromNetwork(String fileName) {
        return Maybe.error(new RuntimeException("Load from network failed"));
    }

    @Override
    public Completable putToCache(String fileName, Bitmap bitmap) {
        return Completable.error(new RuntimeException("Save to cache failed"));
    }

    @Override
    public Completable putToDownloads(String fileName, Bitmap bitmap) {
        return downloadsFolderDataSource.putImageToDownloads(fileName, bitmap);
    }
}
