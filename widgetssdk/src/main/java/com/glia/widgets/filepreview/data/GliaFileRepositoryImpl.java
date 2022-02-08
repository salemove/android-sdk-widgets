package com.glia.widgets.filepreview.data;

import android.graphics.Bitmap;

import com.glia.androidsdk.chat.AttachmentFile;
import com.glia.widgets.chat.helper.FileHelper;
import com.glia.widgets.di.Dependencies;
import com.glia.widgets.filepreview.data.source.local.InAppBitmapCache;
import com.glia.widgets.filepreview.data.source.local.DownloadsFolderDataSource;
import com.glia.widgets.filepreview.domain.exception.CacheFileNotFoundException;

import java.io.InputStream;

import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

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
        return Maybe.<InputStream>create(emitter -> Dependencies.glia().fetchFile(attachmentFile, (fileInputStream, gliaException) -> {
            if (gliaException != null) emitter.onError(gliaException);
            else emitter.onSuccess(fileInputStream);
        }))
                .flatMap(inputStream -> FileHelper.decodeSampledBitmapFromInputStream(inputStream)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread()));
    }

    @Override
    public Completable downloadFileFromNetwork(AttachmentFile attachmentFile) {
        return
                Maybe.<InputStream>create(emitter -> Dependencies.glia().fetchFile(attachmentFile, (fileInputStream, gliaException) -> {
                    if (gliaException != null) emitter.onError(gliaException);
                    else emitter.onSuccess(fileInputStream);
                }))
                        .flatMapCompletable(inputStream ->
                                downloadsFolderDataSource.downloadFileToDownloads(
                                        FileHelper.getFileName(attachmentFile),
                                        attachmentFile.getContentType(),
                                        inputStream
                                )
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .doOnComplete(inputStream::close)
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
