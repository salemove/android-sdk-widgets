package com.glia.widgets.filepreview.ui

import android.graphics.Bitmap
import com.glia.widgets.core.engagement.domain.GliaOnEngagementEndUseCase
import com.glia.widgets.filepreview.domain.exception.FileNameMissingException
import com.glia.widgets.filepreview.domain.usecase.GetImageFileFromCacheUseCase
import com.glia.widgets.filepreview.domain.usecase.GetImageFileFromDownloadsUseCase
import com.glia.widgets.filepreview.domain.usecase.PutImageFileToDownloadsUseCase
import io.reactivex.Completable
import io.reactivex.Maybe
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class FilePreviewControllerTest {
    private lateinit var view: FilePreviewContract.View
    private lateinit var getImageFileFromDownloadsUseCase: GetImageFileFromDownloadsUseCase
    private lateinit var getImageFileFromCacheUseCase: GetImageFileFromCacheUseCase
    private lateinit var putImageFileToDownloadsUseCase: PutImageFileToDownloadsUseCase
    private lateinit var onEngagementEndUseCase: GliaOnEngagementEndUseCase
    private lateinit var filePreviewController: FilePreviewController
    private lateinit var state: State

    @Before
    fun setUp() {
        view = mock()
        getImageFileFromDownloadsUseCase = mock()
        getImageFileFromCacheUseCase = mock()
        putImageFileToDownloadsUseCase = mock()
        onEngagementEndUseCase = mock()
        state = mock()
        mockState()
        filePreviewController = FilePreviewController(
            getImageFileFromDownloadsUseCase,
            getImageFileFromCacheUseCase,
            putImageFileToDownloadsUseCase,
            onEngagementEndUseCase,
            state,
            mock()
        )
        filePreviewController.setView(view)
    }

    @After
    fun tearDown() {
        Mockito.reset(state)
    }

    @Test
    fun setView_endsEngagement() {
        verify(onEngagementEndUseCase).execute(filePreviewController)
    }

    @Test
    fun onImageDataReceived_updatesState_whenNonNullArguments() {
        val argument = argumentCaptor<State>()
        whenever(state.withImageData(any(), any())) doReturn State().withImageData(
            BITMAP_ID,
            BITMAP_NAME
        )
        filePreviewController.onImageDataReceived(BITMAP_ID, BITMAP_NAME)
        verify(view).onStateUpdated(argument.capture())
        assertEquals(BITMAP_ID, argument.lastValue.imageName)
        assertEquals(BITMAP_NAME, argument.lastValue.imageId)
    }

    @Test
    fun onImageRequested_updatesState_whenLoadingFromDownloadsSuccess() {
        whenever(state.loadingImageFromDownloads()) doReturn State().loadingImageFromDownloads()
        whenever(getImageFileFromDownloadsUseCase(any())) doReturn Maybe.just(BITMAP)
        val argument = argumentCaptor<State>()
        filePreviewController.onImageRequested()
        verify(view, times(2)).onStateUpdated(argument.capture())
        val (imageLoadingState, isShowShareButton, isShowDownloadButton) = argument.allValues[0]
        assertEquals(State.ImageLoadingState.LOADING_FROM_DOWNLOADS, imageLoadingState)
        assertFalse(isShowShareButton)
        assertFalse(isShowDownloadButton)
        val (imageLoadingState1, isShowShareButton1, isShowDownloadButton1, _, _, _, loadedImage) = argument.allValues[1]
        assertEquals(State.ImageLoadingState.SUCCESS_FROM_DOWNLOADS, imageLoadingState1)
        assertTrue(isShowShareButton1)
        assertFalse(isShowDownloadButton1)
        assertEquals(BITMAP, loadedImage)
    }

    @Test
    fun onImageRequested_updatesState_whenLoadingFromCacheSuccess() {
        val argument = argumentCaptor<State>()
        whenever(state.loadingImageFromDownloads()) doReturn State().loadingImageFromDownloads()
        whenever(getImageFileFromDownloadsUseCase(any())) doReturn Maybe.error(EXCEPTION)
        whenever(getImageFileFromCacheUseCase(any())) doReturn Maybe.just(BITMAP)

        filePreviewController.onImageRequested()
        verify(view, Mockito.times(3)).onStateUpdated(argument.capture())
        val (imageLoadingState, isShowShareButton, isShowDownloadButton) = argument.allValues[0]
        assertEquals(State.ImageLoadingState.LOADING_FROM_DOWNLOADS, imageLoadingState)
        assertFalse(isShowShareButton)
        assertFalse(isShowDownloadButton)
        val (imageLoadingState1, isShowShareButton1, isShowDownloadButton1) = argument.allValues[1]
        assertEquals(State.ImageLoadingState.LOADING_FROM_CACHE, imageLoadingState1)
        assertFalse(isShowShareButton1)
        assertFalse(isShowDownloadButton1)
        val (imageLoadingState2, isShowShareButton2, isShowDownloadButton2, _, _, _, loadedImage) = argument.allValues[2]
        assertEquals(State.ImageLoadingState.SUCCESS_FROM_CACHE, imageLoadingState2)
        assertFalse(isShowShareButton2)
        assertTrue(isShowDownloadButton2)
        assertEquals(BITMAP, loadedImage)
    }

    @Test
    fun onImageRequested_updatesState_whenLoadingFails() {
        val argument = argumentCaptor<State>()
        whenever(state.loadingImageFromDownloads()) doReturn State().loadingImageFromDownloads()

        whenever(getImageFileFromDownloadsUseCase(any())) doReturn Maybe.error(EXCEPTION)
        whenever(getImageFileFromCacheUseCase(any())) doReturn Maybe.error(EXCEPTION)
        filePreviewController.onImageRequested()
        verify(view, times(3)).onStateUpdated(argument.capture())
        verify(view).showOnImageLoadingFailed()
        val (imageLoadingState, isShowShareButton, isShowDownloadButton) = argument.allValues[0]
        assertEquals(State.ImageLoadingState.LOADING_FROM_DOWNLOADS, imageLoadingState)
        assertFalse(isShowShareButton)
        assertFalse(isShowDownloadButton)
        val (imageLoadingState1, isShowShareButton1, isShowDownloadButton1) = argument.allValues[1]
        assertEquals(State.ImageLoadingState.LOADING_FROM_CACHE, imageLoadingState1)
        assertFalse(isShowShareButton1)
        assertFalse(isShowDownloadButton1)
        val (imageLoadingState2) = argument.allValues[2]
        assertEquals(State.ImageLoadingState.FAILURE, imageLoadingState2)
    }

    @Test
    fun onSharePressed_callsShareImageFile_whenValidArguments() {
        whenever(state.withImageData(any(), any())) doReturn State().withImageData(
            BITMAP_ID,
            BITMAP_NAME
        )
        filePreviewController.onImageDataReceived(BITMAP_ID, BITMAP_NAME)
        filePreviewController.onSharePressed()
        verify(view).shareImageFile(BITMAP_ID)
    }

    @Test
    fun onDownloadPressed_updatesState_whenDownloadSuccessful() {
        val argument = argumentCaptor<State>()
        whenever(state.withImageLoadedFromDownloads()) doReturn State().withImageLoadedFromDownloads()

        whenever(putImageFileToDownloadsUseCase(any(), any())) doReturn Completable.complete()

        filePreviewController.onDownloadPressed()
        verify(view).showOnImageSaveSuccess()
        verify(view).onStateUpdated(argument.capture())
        val (imageLoadingState, isShowShareButton, isShowDownloadButton) = argument.lastValue
        assertEquals(State.ImageLoadingState.SUCCESS_FROM_DOWNLOADS, imageLoadingState)
        assertTrue(isShowShareButton)
        assertFalse(isShowDownloadButton)
    }

    @Test
    fun onDownloadPressed_callsShowOnImageSaveFailed_whenDownloadFails() {
        whenever(putImageFileToDownloadsUseCase(any(), any())) doReturn Completable.error(EXCEPTION)
        filePreviewController.onDownloadPressed()
        verify(view).showOnImageSaveFailed()
    }

    @Test
    fun onDestroy_endsEngagement() {
        filePreviewController.onDestroy()
        verify(onEngagementEndUseCase).unregisterListener(any())
    }

    @Test
    fun engagementEnded_callsEngagementEnded() {
        filePreviewController.engagementEnded()
        Mockito.verify(view).engagementEnded()
    }

    private fun mockState() {
        whenever(state.loadedImage) doReturn BITMAP
        whenever(state.imageIdName) doReturn ""
    }

    companion object {
        private const val BITMAP_ID = "BITMAP_ID"
        private const val BITMAP_NAME = "BITMAP_NAME"
        private val BITMAP: Bitmap = mock()
        private val EXCEPTION: FileNameMissingException = mock()
    }
}
