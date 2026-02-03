package com.glia.widgets.filepreview.ui

import android.graphics.Bitmap
import android.net.Uri
import app.cash.turbine.test
import com.glia.widgets.filepreview.domain.usecase.GetImageFileFromCacheUseCase
import com.glia.widgets.filepreview.domain.usecase.GetImageFileFromDownloadsUseCase
import com.glia.widgets.filepreview.domain.usecase.PutImageFileToDownloadsUseCase
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class ImagePreviewViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var getImageFileFromDownloadsUseCase: GetImageFileFromDownloadsUseCase
    private lateinit var getImageFileFromCacheUseCase: GetImageFileFromCacheUseCase
    private lateinit var putImageFileToDownloadsUseCase: PutImageFileToDownloadsUseCase
    private lateinit var viewModel: ImagePreviewViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        getImageFileFromDownloadsUseCase = mock()
        getImageFileFromCacheUseCase = mock()
        putImageFileToDownloadsUseCase = mock()
        viewModel = ImagePreviewViewModel(
            getImageFileFromDownloadsUseCase,
            getImageFileFromCacheUseCase,
            putImageFileToDownloadsUseCase
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // region Initialize tests

    @Test
    fun `initializeWithRemoteImage sets title from imageName`() = runTest {
        viewModel.processIntent(ImagePreviewIntent.InitializeWithRemoteImage(IMAGE_ID, IMAGE_NAME))
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(IMAGE_NAME, state.title)
        assertFalse(state.isShowShareButton)
        assertFalse(state.isShowDownloadButton)
    }

    @Test
    fun `initializeWithLocalImage sets localImageUri and shows share button`() = runTest {
        val uri: Uri = mock()

        viewModel.processIntent(ImagePreviewIntent.InitializeWithLocalImage(uri))
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(uri, state.localImageUri)
        assertTrue(state.isShowShareButton)
        assertFalse(state.isShowDownloadButton)
    }

    // endregion

    // region LoadImage tests

    @Test
    fun `loadImage from downloads success shows share button`() = runTest {
        val bitmap: Bitmap = mock()
        whenever(getImageFileFromDownloadsUseCase(any())).thenReturn(Maybe.just(bitmap))

        viewModel.processIntent(ImagePreviewIntent.InitializeWithRemoteImage(IMAGE_ID, IMAGE_NAME))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.processIntent(ImagePreviewIntent.LoadImage)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(bitmap, state.loadedBitmap)
        assertTrue(state.isShowShareButton)
        assertFalse(state.isShowDownloadButton)
        assertFalse(state.isLoading)
    }

    @Test
    fun `loadImage falls back to cache when downloads fails`() = runTest {
        val bitmap: Bitmap = mock()
        whenever(getImageFileFromDownloadsUseCase(any())).thenReturn(Maybe.error(RuntimeException("Not found")))
        whenever(getImageFileFromCacheUseCase(any())).thenReturn(Maybe.just(bitmap))

        viewModel.processIntent(ImagePreviewIntent.InitializeWithRemoteImage(IMAGE_ID, IMAGE_NAME))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.processIntent(ImagePreviewIntent.LoadImage)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(bitmap, state.loadedBitmap)
        assertFalse(state.isShowShareButton)
        assertTrue(state.isShowDownloadButton)
        assertFalse(state.isLoading)
    }

    @Test
    fun `loadImage emits ShowImageLoadingFailed when both downloads and cache fail`() = runTest {
        whenever(getImageFileFromDownloadsUseCase(any())).thenReturn(Maybe.error(RuntimeException("Not found")))
        whenever(getImageFileFromCacheUseCase(any())).thenReturn(Maybe.error(RuntimeException("Not found")))

        viewModel.processIntent(ImagePreviewIntent.InitializeWithRemoteImage(IMAGE_ID, IMAGE_NAME))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.effect.test {
            viewModel.processIntent(ImagePreviewIntent.LoadImage)
            testDispatcher.scheduler.advanceUntilIdle()

            assertEquals(ImagePreviewEffect.ShowImageLoadingFailed, awaitItem())
        }
    }

    @Test
    fun `loadImage does nothing for local images`() = runTest {
        val uri: Uri = mock()

        viewModel.processIntent(ImagePreviewIntent.InitializeWithLocalImage(uri))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.processIntent(ImagePreviewIntent.LoadImage)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(getImageFileFromDownloadsUseCase, never()).invoke(any())
        verify(getImageFileFromCacheUseCase, never()).invoke(any())
    }

    // endregion

    // region ShareImage tests

    @Test
    fun `shareImage emits ShareRemoteImage for remote images`() = runTest {
        val bitmap: Bitmap = mock()
        whenever(getImageFileFromDownloadsUseCase(any())).thenReturn(Maybe.just(bitmap))

        viewModel.processIntent(ImagePreviewIntent.InitializeWithRemoteImage(IMAGE_ID, IMAGE_NAME))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.processIntent(ImagePreviewIntent.LoadImage)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.effect.test {
            viewModel.processIntent(ImagePreviewIntent.ShareImage)
            testDispatcher.scheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertTrue(effect is ImagePreviewEffect.ShareRemoteImage)
        }
    }

    @Test
    fun `shareImage emits ShareLocalImage for local images`() = runTest {
        val uri: Uri = mock()

        viewModel.processIntent(ImagePreviewIntent.InitializeWithLocalImage(uri))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.effect.test {
            viewModel.processIntent(ImagePreviewIntent.ShareImage)
            testDispatcher.scheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertTrue(effect is ImagePreviewEffect.ShareLocalImage)
            assertEquals(uri, (effect as ImagePreviewEffect.ShareLocalImage).uri)
        }
    }

    // endregion

    // region DownloadImage tests

    @Test
    fun `downloadImage success emits ShowImageSaveSuccess and updates state`() = runTest {
        val bitmap: Bitmap = mock()
        whenever(getImageFileFromDownloadsUseCase(any())).thenReturn(Maybe.error(RuntimeException("Not found")))
        whenever(getImageFileFromCacheUseCase(any())).thenReturn(Maybe.just(bitmap))
        whenever(putImageFileToDownloadsUseCase(any(), any())).thenReturn(Completable.complete())

        viewModel.processIntent(ImagePreviewIntent.InitializeWithRemoteImage(IMAGE_ID, IMAGE_NAME))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.processIntent(ImagePreviewIntent.LoadImage)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.effect.test {
            viewModel.processIntent(ImagePreviewIntent.DownloadImage)
            testDispatcher.scheduler.advanceUntilIdle()

            assertEquals(ImagePreviewEffect.ShowImageSaveSuccess, awaitItem())
        }

        val state = viewModel.state.value
        assertTrue(state.isShowShareButton)
        assertFalse(state.isShowDownloadButton)
    }

    @Test
    fun `downloadImage failure emits ShowImageSaveFailed`() = runTest {
        val bitmap: Bitmap = mock()
        whenever(getImageFileFromDownloadsUseCase(any())).thenReturn(Maybe.error(RuntimeException("Not found")))
        whenever(getImageFileFromCacheUseCase(any())).thenReturn(Maybe.just(bitmap))
        whenever(putImageFileToDownloadsUseCase(any(), any())).thenReturn(Completable.error(RuntimeException("Save failed")))

        viewModel.processIntent(ImagePreviewIntent.InitializeWithRemoteImage(IMAGE_ID, IMAGE_NAME))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.processIntent(ImagePreviewIntent.LoadImage)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.effect.test {
            viewModel.processIntent(ImagePreviewIntent.DownloadImage)
            testDispatcher.scheduler.advanceUntilIdle()

            assertEquals(ImagePreviewEffect.ShowImageSaveFailed, awaitItem())
        }
    }

    @Test
    fun `downloadImage does nothing when no bitmap loaded`() = runTest {
        viewModel.processIntent(ImagePreviewIntent.InitializeWithRemoteImage(IMAGE_ID, IMAGE_NAME))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.effect.test {
            viewModel.processIntent(ImagePreviewIntent.DownloadImage)
            testDispatcher.scheduler.advanceUntilIdle()

            expectNoEvents()
        }

        verify(putImageFileToDownloadsUseCase, never()).invoke(any(), any())
    }

    // endregion

    // region CloseScreen tests

    @Test
    fun `closeScreen emits Dismiss effect`() = runTest {
        viewModel.effect.test {
            viewModel.processIntent(ImagePreviewIntent.CloseScreen)
            testDispatcher.scheduler.advanceUntilIdle()

            assertEquals(ImagePreviewEffect.Dismiss, awaitItem())
        }
    }

    // endregion

    companion object {
        private const val IMAGE_ID = "image_123"
        private const val IMAGE_NAME = "screenshot.png"
    }
}