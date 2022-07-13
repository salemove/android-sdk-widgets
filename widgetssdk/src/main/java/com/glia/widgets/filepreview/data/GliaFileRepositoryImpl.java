package com.glia.widgets.filepreview.data;

import android.graphics.Bitmap;

import com.glia.androidsdk.chat.AttachmentFile;
import com.glia.widgets.chat.helper.FileHelper;
import com.glia.widgets.di.GliaCore;
import com.glia.widgets.filepreview.data.source.local.DownloadsFolderDataSource;
import com.glia.widgets.filepreview.data.source.local.InAppBitmapCache;
import com.glia.widgets.filepreview.domain.exception.CacheFileNotFoundException;

import java.io.InputStream;

import io.reactivex.Completable;
import io.reactivex.Maybe;

public class GliaFileRepositoryImpl implements GliaFileRepository {

    private final InAppBitmapCache bitmapCache;
    private final DownloadsFolderDataSource downloadsFolderDataSource;
    private final GliaCore gliaCore;
    private final FileHelper fileHelper;

    public GliaFileRepositoryImpl(
            InAppBitmapCache bitmapCache,
            DownloadsFolderDataSource downloadsFolderDataSource,
            GliaCore gliaCore,
            FileHelper fileHelper
    ) {
        this.bitmapCache = bitmapCache;
        this.downloadsFolderDataSource = downloadsFolderDataSource;
        this.gliaCore = gliaCore;
        this.fileHelper = fileHelper;
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
    public Maybe<Bitmap> loadImageFromDownloads(String fileName) {
        return downloadsFolderDataSource.getImageFromDownloadsFolder(fileName);
    }

    @Override
    public Completable putImageToDownloads(String fileName, Bitmap bitmap) {
        return downloadsFolderDataSource.putImageToDownloads(fileName, bitmap);
    }

    @Override
    public Maybe<Bitmap> loadImageFileFromNetwork(AttachmentFile attachmentFile) {
        return Maybe.<InputStream>create(emitter ->
                        gliaCore.fetchFile(
                                attachmentFile,
                                (fileInputStream, gliaException) -> {
                                    if (gliaException != null) {
                                        emitter.onError(gliaException);
                                    } else {
                                        emitter.onSuccess(fileInputStream);
                                    }
                                }
                        )
                )
                .flatMap(fileHelper::decodeSampledBitmapFromInputStream);
    }

    @Override
    public Completable downloadFileFromNetwork(AttachmentFile attachmentFile) {
        return Maybe.<InputStream>create(emitter ->
                        gliaCore.fetchFile(
                                attachmentFile,
                                (fileInputStream, gliaException) -> {
                                    if (gliaException != null) {
                                        emitter.onError(gliaException);
                                    } else {
                                        emitter.onSuccess(fileInputStream);
                                    }
                                }
                        )
                )
                .flatMapCompletable(inputStream ->
                        downloadsFolderDataSource
                                .downloadFileToDownloads(
                                        FileHelper.getFileName(attachmentFile),
                                        attachmentFile.getContentType(),
                                        inputStream
                                )
                                .doOnComplete(inputStream::close)
                );
    }
}
