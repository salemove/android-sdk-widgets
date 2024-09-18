package com.glia.widgets.filepreview.ui

import android.graphics.Bitmap
import com.glia.widgets.filepreview.domain.exception.FileNameMissingException
import com.glia.widgets.filepreview.domain.usecase.GetImageFileFromCacheUseCase
import com.glia.widgets.filepreview.domain.usecase.GetImageFileFromDownloadsUseCase
import com.glia.widgets.filepreview.domain.usecase.PutImageFileToDownloadsUseCase
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ImagePreviewControllerTest {
    private lateinit var view: ImagePreviewContract.View
    private lateinit var getImageFileFromDownloadsUseCase: GetImageFileFromDownloadsUseCase
    private lateinit var getImageFileFromCacheUseCase: GetImageFileFromCacheUseCase
    private lateinit var putImageFileToDownloadsUseCase: PutImageFileToDownloadsUseCase
    private lateinit var filePreviewController: FilePreviewController

    @Before
    fun setUp() {
        view = mock()
        getImageFileFromDownloadsUseCase = mock()
        getImageFileFromCacheUseCase = mock()
        putImageFileToDownloadsUseCase = mock()
        filePreviewController = FilePreviewController(
            getImageFileFromDownloadsUseCase,
            getImageFileFromCacheUseCase,
            putImageFileToDownloadsUseCase,
            mock()
        )
        imagePreviewController.setView(view)
    }

    @Test
    fun onImageDataReceived_updatesState_whenNonNullArguments() {
        val argument = argumentCaptor<State>()
        filePreviewController.onImageDataReceived(BITMAP_ID, BITMAP_NAME)
        verify(view).onStateUpdated(argument.capture())
        assertEquals(BITMAP_ID, argument.lastValue.imageName)
        assertEquals(BITMAP_NAME, argument.lastValue.imageId)
    }

    @Test
    fun onImageRequested_updatesState_whenLoadingFromDownloadsSuccess() {
        whenever(getImageFileFromDownloadsUseCase(any())) doReturn Maybe.just(BITMAP)
        val argument = argumentCaptor<State>()
        imagePreviewController.onImageRequested()
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
        whenever(getImageFileFromDownloadsUseCase(any())) doReturn Maybe.error(EXCEPTION)
        whenever(getImageFileFromCacheUseCase(any())) doReturn Maybe.just(BITMAP)

        imagePreviewController.onImageRequested()
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

        whenever(getImageFileFromDownloadsUseCase(any())) doReturn Maybe.error(EXCEPTION)
        whenever(getImageFileFromCacheUseCase(any())) doReturn Maybe.error(EXCEPTION)
        imagePreviewController.onImageRequested()
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
        filePreviewController.onImageDataReceived(BITMAP_ID, BITMAP_NAME)
        filePreviewController.onSharePressed()
        verify(view).shareImageFile(BITMAP_ID)
    }

    @Test
    fun onDownloadPressed_updatesState_whenDownloadSuccessful() {
        whenever(getImageFileFromDownloadsUseCase(any())) doReturn Maybe.just(BITMAP)
        filePreviewController.onImageRequested()
        val argument = argumentCaptor<State>()

        whenever(putImageFileToDownloadsUseCase(any(), any())) doReturn Completable.complete()

        imagePreviewController.onDownloadPressed()
        verify(view).showOnImageSaveSuccess()
        verify(view, times(3)).onStateUpdated(argument.capture())
        val (imageLoadingState, isShowShareButton, isShowDownloadButton) = argument.lastValue
        assertEquals(State.ImageLoadingState.SUCCESS_FROM_DOWNLOADS, imageLoadingState)
        assertTrue(isShowShareButton)
        assertFalse(isShowDownloadButton)
    }

    @Test
    fun onDownloadPressed_callsShowOnImageSaveFailed_whenDownloadFails() {
        whenever(getImageFileFromDownloadsUseCase(any())) doReturn Maybe.just(BITMAP)
        filePreviewController.onImageRequested()
        whenever(putImageFileToDownloadsUseCase(any(), any())) doReturn Completable.error(EXCEPTION)
        imagePreviewController.onDownloadPressed()
        verify(view).showOnImageSaveFailed()
    }

    companion object {
        private const val BITMAP_ID = "BITMAP_ID"
        private const val BITMAP_NAME = "BITMAP_NAME"
        private val BITMAP: Bitmap = mock()
        private val EXCEPTION: FileNameMissingException = mock()
    }
}
