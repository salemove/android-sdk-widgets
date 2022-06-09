package com.glia.widgets.filepreview.data;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.graphics.Bitmap;

import com.glia.androidsdk.GliaException;
import com.glia.androidsdk.RequestCallback;
import com.glia.androidsdk.chat.AttachmentFile;
import com.glia.widgets.chat.helper.FileHelper;
import com.glia.widgets.di.GliaCore;
import com.glia.widgets.filepreview.data.source.local.DownloadsFolderDataSource;
import com.glia.widgets.filepreview.data.source.local.InAppBitmapCache;

import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;

import io.reactivex.Completable;
import io.reactivex.Maybe;

public class GliaFileRepositoryTest {

    private GliaFileRepository gliaFileRepository;
    private InAppBitmapCache bitmapCache;
    private DownloadsFolderDataSource downloadsFolderDataSource;
    private GliaCore gliaCore;
    private FileHelper fileHelper;

    @Before
    public void setUp() {
        bitmapCache = mock(InAppBitmapCache.class);
        downloadsFolderDataSource = mock(DownloadsFolderDataSource.class);
        gliaCore = mock(GliaCore.class);
        fileHelper = mock(FileHelper.class);
        gliaFileRepository = new GliaFileRepositoryImpl(
                bitmapCache,
                downloadsFolderDataSource,
                gliaCore,
                fileHelper
        );
    }

    @Test
    public void loadImageFromCache_returnsMaybeOnSuccess() {
        when(bitmapCache.getBitmapById(FILENAME)).thenReturn(BITMAP);
        Maybe<Bitmap> result = gliaFileRepository.loadImageFromCache(FILENAME);
        result.test().assertResult(BITMAP);
    }

    @Test
    public void loadImageFromCache_returnsMaybeOnError() {
        doThrow(RUNTIME_EXCEPTION).when(bitmapCache).getBitmapById(FILENAME);
        Maybe<Bitmap> result = gliaFileRepository.loadImageFromCache(FILENAME);
        result.test().assertError(RUNTIME_EXCEPTION);
    }

    @Test
    public void putImageToCache_returnsCompletableOnComplete() {
        Completable result = gliaFileRepository.putImageToCache(FILENAME, BITMAP);
        result.test().assertComplete();
    }

    @Test
    public void putImageToCache_returnsCompletableOnError() {
        doThrow(RUNTIME_EXCEPTION).when(bitmapCache).putBitmap(FILENAME, BITMAP);
        Completable result = gliaFileRepository.putImageToCache(FILENAME, BITMAP);
        result.test().assertError(RUNTIME_EXCEPTION);
    }

    @Test
    public void loadImageFromDownloads_returnsMaybe() {
        when(downloadsFolderDataSource.getImageFromDownloadsFolder(FILENAME))
                .thenReturn(MAYBE_BITMAP);
        Maybe<Bitmap> result = gliaFileRepository.loadImageFromDownloads(FILENAME);
        assertEquals(MAYBE_BITMAP, result);
    }

    @Test
    public void putImageToDownloads_returnsCompletable() {
        when(downloadsFolderDataSource.putImageToDownloads(FILENAME, BITMAP))
                .thenReturn(COMPLETABLE);
        Completable result = gliaFileRepository.putImageToDownloads(FILENAME, BITMAP);
        assertEquals(COMPLETABLE, result);
    }

    @Test
    public void loadImageFileFromNetwork_returnsMaybeOnSuccess() {
        when(fileHelper.decodeSampledBitmapFromInputStream(INPUT_STREAM))
                .thenReturn(MAYBE_BITMAP);
        doAnswer(invocation -> {
            RequestCallback<InputStream> callback = invocation.getArgument(1);
            callback.onResult(INPUT_STREAM, null);
            return null;
        }).when(gliaCore).fetchFile(any(), any());
        Maybe<Bitmap> result = gliaFileRepository.loadImageFileFromNetwork(ATTACHMENT_FILE);
        result.test().assertResult(BITMAP);
    }

    @Test
    public void loadImageFileFromNetwork_returnsMaybeOnError() {
        doAnswer(invocation -> {
            RequestCallback<InputStream> callback = invocation.getArgument(1);
            callback.onResult(INPUT_STREAM, GLIA_EXCEPTION);
            return null;
        }).when(gliaCore).fetchFile(any(), any());
        Maybe<Bitmap> result = gliaFileRepository.loadImageFileFromNetwork(ATTACHMENT_FILE);
        result.test().assertError(GLIA_EXCEPTION);
    }

    @Test
    public void downloadFileFromNetwork_returnsCompletableOnComplete() {
        when(downloadsFolderDataSource.downloadFileToDownloads(any(), any(), any()))
                .thenReturn(COMPLETABLE);
        doAnswer(invocation -> {
            RequestCallback<InputStream> callback = invocation.getArgument(1);
            callback.onResult(INPUT_STREAM, null);
            return null;
        }).when(gliaCore).fetchFile(any(), any());
        Completable result = gliaFileRepository.downloadFileFromNetwork(ATTACHMENT_FILE);
        result.test().assertComplete();
    }

    @Test
    public void downloadFileFromNetwork_returnsCompletableOnError() {
        doAnswer(invocation -> {
            RequestCallback<InputStream> callback = invocation.getArgument(1);
            callback.onResult(INPUT_STREAM, GLIA_EXCEPTION);
            return null;
        }).when(gliaCore).fetchFile(any(), any());
        Completable result = gliaFileRepository.downloadFileFromNetwork(ATTACHMENT_FILE);
        result.test().assertError(GLIA_EXCEPTION);
    }

    private static final String FILENAME = "FILENAME";
    private static final Completable COMPLETABLE = Completable.complete();
    private static final AttachmentFile ATTACHMENT_FILE = mock(AttachmentFile.class);
    private static final InputStream INPUT_STREAM = mock(InputStream.class);
    private static final Bitmap BITMAP = mock(Bitmap.class);
    private static final Maybe<Bitmap> MAYBE_BITMAP = Maybe.just(BITMAP);
    private static final RuntimeException RUNTIME_EXCEPTION = new RuntimeException();
    private static final GliaException GLIA_EXCEPTION = mock(GliaException.class);
}
