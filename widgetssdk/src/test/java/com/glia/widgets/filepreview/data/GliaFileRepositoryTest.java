package com.glia.widgets.filepreview.data;

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
import com.glia.widgets.filepreview.domain.exception.CacheFileNotFoundException;

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
    public void loadImageFromCache_emitsBitmap_whenImageWasCached() {
        when(bitmapCache.getBitmapById(any())).thenReturn(BITMAP);
        Maybe<Bitmap> result = gliaFileRepository.loadImageFromCache(FILENAME);
        result.test().assertResult(BITMAP);
    }

    @Test
    public void loadImageFromCache_emitsCacheFileNotFoundException_whenImageWasNotCached() {
        when(bitmapCache.getBitmapById(any())).thenReturn(null);
        Maybe<Bitmap> result = gliaFileRepository.loadImageFromCache(FILENAME);
        result.test().assertError(CacheFileNotFoundException.class);
    }

    @Test
    public void loadImageFromCache_emitsCacheFileNotFoundException_whenNullArgument() {
        when(bitmapCache.getBitmapById(any())).thenReturn(null);
        Maybe<Bitmap> result = gliaFileRepository.loadImageFromCache(null);
        result.test().assertError(CacheFileNotFoundException.class);
    }

    @Test
    public void putImageToCache_completesSuccessfully_whenValidArguments() {
        Completable result = gliaFileRepository.putImageToCache(FILENAME, BITMAP);
        result.test().assertComplete();
    }

    @Test
    public void putImageToCache_completesSuccessfully_whenFileNameArgumentNull() {
        Completable result = gliaFileRepository.putImageToCache(null, BITMAP);
        result.test().assertComplete();
    }

    @Test
    public void putImageToCache_completesSuccessfully_whenBitmapArgumentNull() {
        Completable result = gliaFileRepository.putImageToCache(FILENAME, null);
        result.test().assertComplete();
    }

    @Test
    public void putImageToCache_completesSuccessfully_whenArgumentsNull() {
        Completable result = gliaFileRepository.putImageToCache(null, null);
        result.test().assertComplete();
    }

    @Test
    public void putImageToCache_emitsException_whenSomethingGoesWrong() {
        doThrow(RUNTIME_EXCEPTION).when(bitmapCache).putBitmap(any(), any());
        Completable result = gliaFileRepository.putImageToCache(FILENAME, BITMAP);
        result.test().assertError(RuntimeException.class);
    }

    @Test
    public void loadImageFileFromNetwork_emitsBitmap_whenValidArguments() {
        when(fileHelper.decodeSampledBitmapFromInputStream(any()))
                .thenReturn(MAYBE_BITMAP);
        doAnswer(invocation -> {
            RequestCallback<InputStream> callback = invocation.getArgument(1);
            callback.onResult(INPUT_STREAM, null);
            return null;
        }).when(gliaCore).fetchFile(any(), any());
        gliaFileRepository.loadImageFileFromNetwork(ATTACHMENT_FILE)
                .test().assertResult(BITMAP);
    }

    @Test
    public void loadImageFileFromNetwork_emitsGliaException_whenGliaCoreReturnsException() {
        doAnswer(invocation -> {
            RequestCallback<InputStream> callback = invocation.getArgument(1);
            callback.onResult(INPUT_STREAM, GLIA_EXCEPTION);
            return null;
        }).when(gliaCore).fetchFile(any(), any());
        gliaFileRepository.loadImageFileFromNetwork(ATTACHMENT_FILE)
                .test().assertError(GLIA_EXCEPTION);
    }

    @Test
    public void loadImageFileFromNetwork_emitsException_whenBitmapDecodingThrows() {
        when(fileHelper.decodeSampledBitmapFromInputStream(any()))
                .thenReturn(MAYBE_EXCEPTION);
        doAnswer(invocation -> {
            RequestCallback<InputStream> callback = invocation.getArgument(1);
            callback.onResult(INPUT_STREAM, null);
            return null;
        }).when(gliaCore).fetchFile(any(), any());
        gliaFileRepository.loadImageFileFromNetwork(ATTACHMENT_FILE)
                .test().assertError(EXCEPTION.getClass());
    }

    @Test
    public void downloadFileFromNetwork_completesSuccessfully_whenValidArguments() {
        when(downloadsFolderDataSource.downloadFileToDownloads(any(), any(), any()))
                .thenReturn(COMPLETABLE);
        doAnswer(invocation -> {
            RequestCallback<InputStream> callback = invocation.getArgument(1);
            callback.onResult(INPUT_STREAM, null);
            return null;
        }).when(gliaCore).fetchFile(any(), any());
        gliaFileRepository.downloadFileFromNetwork(ATTACHMENT_FILE)
                .test().assertComplete();
    }

    @Test
    public void downloadFileFromNetwork_emitsGliaException_whenGliaCoreReturnsException() {
        doAnswer(invocation -> {
            RequestCallback<InputStream> callback = invocation.getArgument(1);
            callback.onResult(INPUT_STREAM, GLIA_EXCEPTION);
            return null;
        }).when(gliaCore).fetchFile(any(), any());
        gliaFileRepository.downloadFileFromNetwork(ATTACHMENT_FILE)
                .test().assertError(GLIA_EXCEPTION.getClass());
    }

    private static final String FILENAME = "FILENAME";
    private static final Completable COMPLETABLE = Completable.complete();
    private static final AttachmentFile ATTACHMENT_FILE = mock(AttachmentFile.class);
    private static final InputStream INPUT_STREAM = mock(InputStream.class);
    private static final Bitmap BITMAP = mock(Bitmap.class);
    private static final Maybe<Bitmap> MAYBE_BITMAP = Maybe.just(BITMAP);
    private static final RuntimeException RUNTIME_EXCEPTION = new RuntimeException();
    private static final GliaException GLIA_EXCEPTION = mock(GliaException.class);
    private static final Exception EXCEPTION = mock(Exception.class);
    private static final Maybe<Bitmap> MAYBE_EXCEPTION = Maybe.error(EXCEPTION);
}
