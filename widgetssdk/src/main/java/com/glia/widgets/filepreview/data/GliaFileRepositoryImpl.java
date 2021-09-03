package com.glia.widgets.filepreview.data;

import android.graphics.Bitmap;

import com.glia.androidsdk.Glia;
import com.glia.androidsdk.chat.AttachmentFile;
import com.glia.widgets.chat.helper.FileHelper;
import com.glia.widgets.filepreview.data.source.local.InAppBitmapCache;
import com.glia.widgets.filepreview.data.source.local.DownloadsFolderDataSource;
import com.glia.widgets.filepreview.domain.exception.CacheFileNotFoundException;
import com.glia.widgets.helper.Logger;

import java.io.InputStream;

import io.reactivex.Completable;
import io.reactivex.Maybe;

public class GliaFileRepositoryImpl implements GliaFileRepository {
    private static final String TAG = GliaFileRepositoryImpl.class.getSimpleName();

    private final InAppBitmapCache bitmapCache;
    private final DownloadsFolderDataSource downloadsFolderDataSource;

    public GliaFileRepositoryImpl(InAppBitmapCache bitmapCache, DownloadsFolderDataSource downloadsFolderDataSource) {
        this.bitmapCache = bitmapCache;
        this.downloadsFolderDataSource = downloadsFolderDataSource;
    }

    @Override
    public Maybe<Bitmap> loadImageFromDownloads(String fileName) {
        return downloadsFolderDataSource.getImageFromDownloadsFolder(fileName);
    }

    @Override
    public Maybe<Bitmap> loadImageFromCache(String fileName) {
        return Maybe.create(emitter -> {
            Bitmap bitmap = bitmapCache.getBitmapById(fileName);
            if (bitmap != null) emitter.onSuccess(bitmap);
            else emitter.onError(new CacheFileNotFoundException());
        });
    }

    @Override
    public Maybe<Bitmap> loadImageFileFromNetwork(AttachmentFile attachmentFile) {
        return Maybe.create(emitter -> Glia.fetchFile(attachmentFile, (fileInputStream, gliaException) -> {
            if (gliaException != null) {
                emitter.onError(gliaException);
            } else {
                try {
                    Logger.d(TAG, "Image decode starting");
                    Bitmap bitmap = FileHelper.decodeSampledBitmapFromInputStream(fileInputStream);
                    Logger.d(TAG, "Image decode success");
                    emitter.onSuccess(bitmap);
                } catch (Exception e) {
                    Logger.e(TAG, "Image decode failed: " + e.getMessage());
                    emitter.onError(e);
                }
            }
        }));
    }

    @Override
    public Completable downloadFileFromNetwork(AttachmentFile attachmentFile) {
        return
                Maybe.<InputStream>create(emitter -> {
                    Glia.fetchFile(attachmentFile, (fileInputStream, gliaException) -> {
                        if (gliaException != null) emitter.onError(gliaException);
                        else emitter.onSuccess(fileInputStream);
                    });
                }).flatMapCompletable(inputStream ->
                        downloadsFolderDataSource.downloadFileToDownloads(
                                FileHelper.getFileName(attachmentFile),
                                attachmentFile.getContentType(),
                                inputStream
                        ).doOnComplete(inputStream::close)
                );
    }

    @Override
    public Completable putImageToCache(String fileName, Bitmap bitmap) {
        return Completable.create(emitter -> {
            try {
                bitmapCache.putBitmap(fileName, bitmap);
                emitter.onComplete();
            } catch (Exception ex) {
                emitter.onError(ex);
            }
        });
    }

    @Override
    public Completable putImageToDownloads(String fileName, Bitmap bitmap) {
        return downloadsFolderDataSource.putImageToDownloads(fileName, bitmap);
    }
}
