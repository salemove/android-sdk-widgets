package com.glia.widgets.filepreview.data;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.graphics.Bitmap;

import com.glia.androidsdk.GliaException;
import com.glia.androidsdk.RequestCallback;
import com.glia.androidsdk.chat.AttachmentFile;
import com.glia.androidsdk.secureconversations.SecureConversations;
import com.glia.widgets.chat.ChatType;
import com.glia.widgets.core.engagement.GliaEngagementConfigRepository;
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
    private GliaEngagementConfigRepository engagementConfigRepository;
    private SecureConversations secureConversations;

    @Before
    public void setUp() {
        bitmapCache = mock(InAppBitmapCache.class);
        downloadsFolderDataSource = mock(DownloadsFolderDataSource.class);
        gliaCore = mock(GliaCore.class);
        engagementConfigRepository = mock(GliaEngagementConfigRepository.class);
        secureConversations = mock(SecureConversations.class);
        when(gliaCore.getSecureConversations()).thenReturn(secureConversations);
        gliaFileRepository = new GliaFileRepositoryImpl(bitmapCache, downloadsFolderDataSource, gliaCore, engagementConfigRepository);
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
    public void putImageToCache_completesSuccessfully_whenValidArguments() {
        when(bitmapCache.getBitmapById(FILENAME)).thenReturn(BITMAP);
        gliaFileRepository.loadImageFromCache(FILENAME).test().assertResult(BITMAP);
    }

    @Test
    public void loadImageFileFromNetwork_emitsInputStream_whenValidArguments() {
        doAnswer(invocation -> {
            RequestCallback<InputStream> callback = invocation.getArgument(1);
            callback.onResult(INPUT_STREAM, null);
            return null;
        }).when(gliaCore).fetchFile(any(), any());
        gliaFileRepository.loadImageFileFromNetwork(ATTACHMENT_FILE)
                .test().assertResult(INPUT_STREAM);
    }

    @Test
    public void loadImageFileFromNetwork_emitsGliaException_whenGliaCoreReturnsException() {
        doAnswer(invocation -> {
            RequestCallback<InputStream> callback = invocation.getArgument(1);
            callback.onResult(INPUT_STREAM, GLIA_EXCEPTION);
            return null;
        }).when(gliaCore).fetchFile(any(), any());
        gliaFileRepository.loadImageFileFromNetwork(ATTACHMENT_FILE).test().assertError(GLIA_EXCEPTION);
    }



    @Test
    public void loadImageFileFromNetwork_emitsBitmap_whenValidArgumentsForSecureMessaging() {
        when(engagementConfigRepository.getChatType()).thenReturn(ChatType.SECURE_MESSAGING);
        doAnswer(invocation -> {
            RequestCallback<InputStream> callback = invocation.getArgument(1);
            callback.onResult(INPUT_STREAM, null);
            return null;
        }).when(secureConversations).fetchFile(any(), any());
        gliaFileRepository.loadImageFileFromNetwork(ATTACHMENT_FILE).test().assertResult(INPUT_STREAM);
    }

    @Test
    public void loadImageFileFromNetwork_emitsGliaException_whenGliaCoreReturnsExceptionForSecureMessaging() {
        when(engagementConfigRepository.getChatType()).thenReturn(ChatType.SECURE_MESSAGING);
        doAnswer(invocation -> {
            RequestCallback<InputStream> callback = invocation.getArgument(1);
            callback.onResult(INPUT_STREAM, GLIA_EXCEPTION);
            return null;
        }).when(secureConversations).fetchFile(any(), any());
        gliaFileRepository.loadImageFileFromNetwork(ATTACHMENT_FILE).test().assertError(GLIA_EXCEPTION);
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
        gliaFileRepository.downloadFileFromNetwork(ATTACHMENT_FILE).test().assertComplete();
    }

    @Test
    public void downloadFileFromNetwork_emitsGliaException_whenGliaCoreReturnsException() {
        doAnswer(invocation -> {
            RequestCallback<InputStream> callback = invocation.getArgument(1);
            callback.onResult(INPUT_STREAM, GLIA_EXCEPTION);
            return null;
        }).when(gliaCore).fetchFile(any(), any());
        gliaFileRepository.downloadFileFromNetwork(ATTACHMENT_FILE).test().assertError(GLIA_EXCEPTION.getClass());
    }

    @Test
    public void downloadFileFromNetwork_completesSuccessfully_whenValidArgumentsForSecureMessaging() {
        when(engagementConfigRepository.getChatType()).thenReturn(ChatType.SECURE_MESSAGING);
        when(downloadsFolderDataSource.downloadFileToDownloads(any(), any(), any()))
                .thenReturn(COMPLETABLE);
        doAnswer(invocation -> {
            RequestCallback<InputStream> callback = invocation.getArgument(1);
            callback.onResult(INPUT_STREAM, null);
            return null;
        }).when(secureConversations).fetchFile(any(), any());
        gliaFileRepository.downloadFileFromNetwork(ATTACHMENT_FILE).test().assertComplete();
    }

    @Test
    public void downloadFileFromNetwork_emitsGliaException_whenGliaCoreReturnsExceptionForSecureMessaging() {
        when(engagementConfigRepository.getChatType()).thenReturn(ChatType.SECURE_MESSAGING);
        doAnswer(invocation -> {
            RequestCallback<InputStream> callback = invocation.getArgument(1);
            callback.onResult(INPUT_STREAM, GLIA_EXCEPTION);
            return null;
        }).when(secureConversations).fetchFile(any(), any());
        gliaFileRepository.downloadFileFromNetwork(ATTACHMENT_FILE).test().assertError(GLIA_EXCEPTION.getClass());
    }

    private static final String FILENAME = "FILENAME";
    private static final Completable COMPLETABLE = Completable.complete();
    private static final AttachmentFile ATTACHMENT_FILE = mock(AttachmentFile.class);
    private static final InputStream INPUT_STREAM = mock(InputStream.class);
    private static final Bitmap BITMAP = mock(Bitmap.class);
    private static final GliaException GLIA_EXCEPTION = mock(GliaException.class);
}
