package com.glia.widgets.filepreview.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.graphics.Bitmap;

import com.glia.widgets.core.engagement.domain.GliaOnEngagementEndUseCase;
import com.glia.widgets.filepreview.domain.exception.FileNameMissingException;
import com.glia.widgets.filepreview.domain.usecase.GetImageFileFromCacheUseCase;
import com.glia.widgets.filepreview.domain.usecase.GetImageFileFromDownloadsUseCase;
import com.glia.widgets.filepreview.domain.usecase.PutImageFileToDownloadsUseCase;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import io.reactivex.Completable;
import io.reactivex.Maybe;

public class FilePreviewControllerTest {

    private FilePreviewContract.View view;

    private GetImageFileFromDownloadsUseCase getImageFileFromDownloadsUseCase;
    private GetImageFileFromCacheUseCase getImageFileFromCacheUseCase;
    private PutImageFileToDownloadsUseCase putImageFileToDownloadsUseCase;
    private GliaOnEngagementEndUseCase onEngagementEndUseCase;
    private FilePreviewController filePreviewController;

    @Before
    public void setUp() {
        this.view = mock(FilePreviewContract.View.class);
        this.getImageFileFromDownloadsUseCase = mock(GetImageFileFromDownloadsUseCase.class);
        this.getImageFileFromCacheUseCase = mock(GetImageFileFromCacheUseCase.class);
        this.putImageFileToDownloadsUseCase = mock(PutImageFileToDownloadsUseCase.class);
        this.onEngagementEndUseCase = mock(GliaOnEngagementEndUseCase.class);
        filePreviewController = new FilePreviewController(
                getImageFileFromDownloadsUseCase,
                getImageFileFromCacheUseCase,
                putImageFileToDownloadsUseCase,
                onEngagementEndUseCase
        );
        filePreviewController.setView(view);
    }

    @Test
    public void setView_endsEngagement() {
        verify(onEngagementEndUseCase).execute(filePreviewController);
    }

    @Test
    public void onImageDataReceived_updatesState_whenNonNullArguments() {
        ArgumentCaptor<State> argument = ArgumentCaptor.forClass(State.class);

        filePreviewController.onImageDataReceived(BITMAP_ID, BITMAP_NAME);

        verify(view).onStateUpdated(argument.capture());
        assertEquals(BITMAP_ID, argument.getValue().getImageName());
        assertEquals(BITMAP_NAME, argument.getValue().getImageId());
    }

    @Test
    public void onImageDataReceived_updatesState_whenNullArguments() {
        ArgumentCaptor<State> argument = ArgumentCaptor.forClass(State.class);

        filePreviewController.onImageDataReceived(null, null);

        verify(view).onStateUpdated(argument.capture());
        assertNull(argument.getValue().getImageName());
        assertNull(argument.getValue().getImageId());
    }

    @Test
    public void onImageRequested_updatesState_whenLoadingFromDownloadsSuccess() {
        when(getImageFileFromDownloadsUseCase.execute(any()))
                .thenReturn(Maybe.just(BITMAP));
        ArgumentCaptor<State> argument = ArgumentCaptor.forClass(State.class);

        filePreviewController.onImageRequested();

        verify(view, times(2)).onStateUpdated(argument.capture());

        State state1 = argument.getAllValues().get(0);
        assertEquals(
                State.ImageLoadingState.LOADING_FROM_DOWNLOADS,
                state1.getImageLoadingState()
        );
        assertFalse(state1.getIsShowShareButton());
        assertFalse(state1.getIsShowDownloadButton());

        State state2 = argument.getAllValues().get(1);
        assertEquals(
                State.ImageLoadingState.SUCCESS_FROM_DOWNLOADS,
                state2.getImageLoadingState()
        );
        assertTrue(state2.getIsShowShareButton());
        assertFalse(state2.getIsShowDownloadButton());
        assertEquals(BITMAP, state2.getLoadedImage());
    }

    @Test
    public void onImageRequested_updatesState_whenLoadingFromCacheSuccess() {
        ArgumentCaptor<State> argument = ArgumentCaptor.forClass(State.class);

        when(getImageFileFromDownloadsUseCase.execute(any()))
                .thenReturn(Maybe.error(EXCEPTION));
        when(getImageFileFromCacheUseCase.execute(any()))
                .thenReturn(Maybe.just(BITMAP));

        filePreviewController.onImageRequested();

        verify(view, times(3)).onStateUpdated(argument.capture());

        State state1 = argument.getAllValues().get(0);
        assertEquals(
                State.ImageLoadingState.LOADING_FROM_DOWNLOADS,
                state1.getImageLoadingState()
        );
        assertFalse(state1.getIsShowShareButton());
        assertFalse(state1.getIsShowDownloadButton());

        State state2 = argument.getAllValues().get(1);
        assertEquals(
                State.ImageLoadingState.LOADING_FROM_CACHE,
                state2.getImageLoadingState()
        );
        assertFalse(state2.getIsShowShareButton());
        assertFalse(state2.getIsShowDownloadButton());

        State state3 = argument.getAllValues().get(2);
        assertEquals(
                State.ImageLoadingState.SUCCESS_FROM_CACHE,
                state3.getImageLoadingState()
        );
        assertFalse(state3.getIsShowShareButton());
        assertTrue(state3.getIsShowDownloadButton());
        assertEquals(BITMAP, state3.getLoadedImage());
    }

    @Test
    public void onImageRequested_updatesState_whenLoadingFails() {
        ArgumentCaptor<State> argument = ArgumentCaptor.forClass(State.class);

        when(getImageFileFromDownloadsUseCase.execute(any()))
                .thenReturn(Maybe.error(EXCEPTION));
        when(getImageFileFromCacheUseCase.execute(any()))
                .thenReturn(Maybe.error(EXCEPTION));

        filePreviewController.onImageRequested();

        verify(view, times(3)).onStateUpdated(argument.capture());
        verify(view).showOnImageLoadingFailed();

        State state1 = argument.getAllValues().get(0);
        assertEquals(
                State.ImageLoadingState.LOADING_FROM_DOWNLOADS,
                state1.getImageLoadingState()
        );
        assertFalse(state1.getIsShowShareButton());
        assertFalse(state1.getIsShowDownloadButton());

        State state2 = argument.getAllValues().get(1);
        assertEquals(
                State.ImageLoadingState.LOADING_FROM_CACHE,
                state2.getImageLoadingState()
        );
        assertFalse(state2.getIsShowShareButton());
        assertFalse(state2.getIsShowDownloadButton());

        State state3 = argument.getAllValues().get(2);
        assertEquals(
                State.ImageLoadingState.FAILURE,
                state3.getImageLoadingState()
        );
    }

    @Test
    public void onSharePressed_callsShareImageFile_whenValidArguments() {
        filePreviewController.onImageDataReceived(BITMAP_ID, BITMAP_NAME);
        filePreviewController.onSharePressed();
        verify(view).shareImageFile(BITMAP_ID);
    }

    @Test
    public void onSharePressed_callsShareImageFile_whenBitmapIdArgumentNull() {
        filePreviewController.onImageDataReceived(null, BITMAP_NAME);
        filePreviewController.onSharePressed();
        verify(view).shareImageFile("null");
    }

    @Test
    public void onSharePressed_callsShareImageFile_whenBitmapNameArgumentNull() {
        filePreviewController.onImageDataReceived(BITMAP_ID, null);
        filePreviewController.onSharePressed();
        verify(view).shareImageFile(BITMAP_ID);
    }

    @Test
    public void onSharePressed_callsShareImageFile_whenArgumentsNull() {
        filePreviewController.onImageDataReceived(null, null);
        filePreviewController.onSharePressed();
        verify(view).shareImageFile("null");
    }

    @Test
    public void onDownloadPressed_updatesState_whenDownloadSuccessful() {
        ArgumentCaptor<State> argument = ArgumentCaptor.forClass(State.class);
        when(putImageFileToDownloadsUseCase.execute(any(), any()))
                .thenReturn(Completable.complete());

        filePreviewController.onDownloadPressed();

        verify(view).showOnImageSaveSuccess();
        verify(view).onStateUpdated(argument.capture());
        State state = argument.getValue();
        assertEquals(
                State.ImageLoadingState.SUCCESS_FROM_DOWNLOADS,
                state.getImageLoadingState()
        );
        assertTrue(state.getIsShowShareButton());
        assertFalse(state.getIsShowDownloadButton());
    }

    @Test
    public void onDownloadPressed_callsShowOnImageSaveFailed_whenDownloadFails() {
        when(putImageFileToDownloadsUseCase.execute(any(), any()))
                .thenReturn(Completable.error(EXCEPTION));
        filePreviewController.onDownloadPressed();
        verify(view).showOnImageSaveFailed();
    }

    @Test
    public void onDestroy_endsEngagement() {
        filePreviewController.onDestroy();
        verify(onEngagementEndUseCase).unregisterListener(any());
    }

    @Test
    public void engagementEnded_callsEngagementEnded() {
        filePreviewController.engagementEnded();
        verify(view).engagementEnded();
    }

    private final static String BITMAP_ID = "BITMAP_ID";
    private final static String BITMAP_NAME = "BITMAP_NAME";
    private final static Bitmap BITMAP = mock(Bitmap.class);
    private final static FileNameMissingException EXCEPTION = mock(FileNameMissingException.class);
}
