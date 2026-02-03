package com.glia.widgets.filepreview.ui

import android.graphics.Bitmap
import android.net.Uri
import com.glia.widgets.base.UiEffect
import com.glia.widgets.base.UiIntent
import com.glia.widgets.base.UiState

/**
 * UI state for ImagePreview screen.
 */
internal data class ImagePreviewUiState(
    val title: String = "",
    val isShowShareButton: Boolean = false,
    val isShowDownloadButton: Boolean = false,
    val loadedBitmap: Bitmap? = null,
    val localImageUri: Uri? = null,
    val isLoading: Boolean = false
) : UiState

/**
 * User intents for ImagePreview screen.
 */
internal sealed interface ImagePreviewIntent : UiIntent {
    data class InitializeWithRemoteImage(val imageId: String, val imageName: String) : ImagePreviewIntent
    data class InitializeWithLocalImage(val uri: Uri) : ImagePreviewIntent
    data object LoadImage : ImagePreviewIntent
    data object ShareImage : ImagePreviewIntent
    data object DownloadImage : ImagePreviewIntent
    data object CloseScreen : ImagePreviewIntent
}

/**
 * One-time effects for ImagePreview screen.
 */
internal sealed interface ImagePreviewEffect : UiEffect {
    data object Dismiss : ImagePreviewEffect
    data class ShareRemoteImage(val fileName: String) : ImagePreviewEffect
    data class ShareLocalImage(val uri: Uri) : ImagePreviewEffect
    data object ShowImageSaveSuccess : ImagePreviewEffect
    data object ShowImageSaveFailed : ImagePreviewEffect
    data object ShowImageLoadingFailed : ImagePreviewEffect
}
