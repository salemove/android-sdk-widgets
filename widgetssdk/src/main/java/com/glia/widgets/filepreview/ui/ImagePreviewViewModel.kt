package com.glia.widgets.filepreview.ui

import android.graphics.Bitmap
import android.net.Uri
import com.glia.widgets.base.BaseViewModel
import com.glia.widgets.filepreview.domain.usecase.GetImageFileFromCacheUseCase
import com.glia.widgets.filepreview.domain.usecase.GetImageFileFromDownloadsUseCase
import com.glia.widgets.filepreview.domain.usecase.PutImageFileToDownloadsUseCase
import com.glia.widgets.helper.toFileName
import io.reactivex.rxjava3.disposables.CompositeDisposable

/**
 * ViewModel for ImagePreview screen implementing MVI pattern.
 *
 * Handles:
 * - Loading images from downloads or cache
 * - Sharing images
 * - Downloading images to device storage
 */
internal class ImagePreviewViewModel(
    private val getImageFileFromDownloadsUseCase: GetImageFileFromDownloadsUseCase,
    private val getImageFileFromCacheUseCase: GetImageFileFromCacheUseCase,
    private val putImageFileToDownloadsUseCase: PutImageFileToDownloadsUseCase
) : BaseViewModel<ImagePreviewUiState, ImagePreviewIntent, ImagePreviewEffect>(ImagePreviewUiState()) {

    private val disposables: CompositeDisposable = CompositeDisposable()

    private var imageId: String = ""
    private var imageName: String = ""
    private var imageIdName: String = ""
    private var isLocalImage: Boolean = false

    override suspend fun handleIntent(intent: ImagePreviewIntent) {
        when (intent) {
            is ImagePreviewIntent.InitializeWithRemoteImage -> handleInitializeWithRemoteImage(intent.imageId, intent.imageName)
            is ImagePreviewIntent.InitializeWithLocalImage -> handleInitializeWithLocalImage(intent.uri)
            ImagePreviewIntent.LoadImage -> handleLoadImage()
            ImagePreviewIntent.ShareImage -> handleShareImage()
            ImagePreviewIntent.DownloadImage -> handleDownloadImage()
            ImagePreviewIntent.CloseScreen -> handleCloseScreen()
        }
    }

    private fun handleInitializeWithRemoteImage(id: String, name: String) {
        imageId = id
        imageName = name
        imageIdName = toFileName(id, name)
        isLocalImage = false
        updateState { copy(title = name) }
    }

    private fun handleInitializeWithLocalImage(uri: Uri) {
        isLocalImage = true
        updateState {
            copy(
                localImageUri = uri,
                isShowShareButton = true,
                isShowDownloadButton = false
            )
        }
    }

    private fun handleLoadImage() {
        if (isLocalImage) return

        updateState { copy(isLoading = true, isShowShareButton = false, isShowDownloadButton = false) }
        loadImageFromDownloads()
    }

    private fun loadImageFromDownloads() {
        disposables.add(
            getImageFileFromDownloadsUseCase(imageIdName)
                .subscribe(
                    { bitmap -> onImageLoadedFromDownloads(bitmap) },
                    { loadImageFromCache() }
                )
        )
    }

    private fun onImageLoadedFromDownloads(bitmap: Bitmap) {
        updateState {
            copy(
                loadedBitmap = bitmap,
                isShowShareButton = true,
                isShowDownloadButton = false,
                isLoading = false
            )
        }
    }

    private fun loadImageFromCache() {
        disposables.add(
            getImageFileFromCacheUseCase(imageIdName)
                .subscribe(
                    { bitmap -> onImageLoadedFromCache(bitmap) },
                    { onImageLoadingFailed() }
                )
        )
    }

    private fun onImageLoadedFromCache(bitmap: Bitmap) {
        updateState {
            copy(
                loadedBitmap = bitmap,
                isShowShareButton = false,
                isShowDownloadButton = true,
                isLoading = false
            )
        }
    }

    private fun onImageLoadingFailed() {
        updateState { copy(isLoading = false) }
        sendEffect(ImagePreviewEffect.ShowImageLoadingFailed)
    }

    private fun handleShareImage() {
        if (isLocalImage) {
            currentState.localImageUri?.let { uri ->
                sendEffect(ImagePreviewEffect.ShareLocalImage(uri))
            }
        } else {
            sendEffect(ImagePreviewEffect.ShareRemoteImage(imageIdName))
        }
    }

    private fun handleDownloadImage() {
        val bitmap = currentState.loadedBitmap ?: return

        disposables.add(
            putImageFileToDownloadsUseCase(imageIdName, bitmap)
                .subscribe(
                    {
                        updateState { copy(isShowShareButton = true, isShowDownloadButton = false) }
                        sendEffect(ImagePreviewEffect.ShowImageSaveSuccess)
                    },
                    { sendEffect(ImagePreviewEffect.ShowImageSaveFailed) }
                )
        )
    }

    private fun handleCloseScreen() {
        sendEffect(ImagePreviewEffect.Dismiss)
    }

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }
}
