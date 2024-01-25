package com.glia.widgets.filepreview.ui

import android.graphics.Bitmap
import com.glia.widgets.helper.toFileName

internal data class State(
    val imageLoadingState: ImageLoadingState = ImageLoadingState.INITIAL,
    val isShowShareButton: Boolean = false,
    val isShowDownloadButton: Boolean = false,
    val imageIdName: String = "",
    val imageName: String = "",
    val imageId: String = "",
    val loadedImage: Bitmap? = null
) {
    enum class ImageLoadingState {
        INITIAL,
        LOADING_FROM_DOWNLOADS,
        LOADING_FROM_CACHE,
        SUCCESS_FROM_DOWNLOADS,
        SUCCESS_FROM_CACHE,
        FAILURE
    }

    fun reset(): State = State()

    fun imageLoadingFiled(): State = copy(imageLoadingState = ImageLoadingState.FAILURE)

    fun withImageData(id: String, name: String): State = copy(
        imageName = id,
        imageId = name,
        imageIdName = toFileName(id, name)
    )

    @JvmOverloads
    fun withImageLoadedFromDownloads(image: Bitmap? = null): State = copy(
        imageLoadingState = ImageLoadingState.SUCCESS_FROM_DOWNLOADS,
        isShowShareButton = true,
        isShowDownloadButton = false,
        loadedImage = image ?: loadedImage
    )

    fun withImageLoadedFromCache(image: Bitmap): State = copy(
        imageLoadingState = ImageLoadingState.SUCCESS_FROM_CACHE,
        isShowShareButton = false,
        isShowDownloadButton = true,
        loadedImage = image
    )

    fun loadingImageFromDownloads(): State = copy(
        imageLoadingState = ImageLoadingState.LOADING_FROM_DOWNLOADS,
        isShowShareButton = false,
        isShowDownloadButton = false
    )

    fun loadingImageFromCache(): State = copy(
        imageLoadingState = ImageLoadingState.LOADING_FROM_CACHE,
        isShowShareButton = false,
        isShowDownloadButton = false
    )
}
