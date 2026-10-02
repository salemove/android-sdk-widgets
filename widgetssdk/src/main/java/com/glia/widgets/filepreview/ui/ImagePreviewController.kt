package com.glia.widgets.filepreview.ui

import com.glia.widgets.filepreview.domain.usecase.GetImageFileFromCacheUseCase
import com.glia.widgets.filepreview.domain.usecase.GetImageFileFromDownloadsUseCase
import com.glia.widgets.filepreview.domain.usecase.PutImageFileToDownloadsUseCase
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.TAG
import com.glia.widgets.internal.fileupload.domain.GetShareableLocalAttachmentUriUseCase
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable

internal class ImagePreviewController @JvmOverloads constructor(
    private val getImageFileFromDownloadsUseCase: GetImageFileFromDownloadsUseCase,
    private val getImageFileFromCacheUseCase: GetImageFileFromCacheUseCase,
    private val putImageFileToDownloadsUseCase: PutImageFileToDownloadsUseCase,
    private val getShareableLocalAttachmentUriUseCase: GetShareableLocalAttachmentUriUseCase,
    private val disposables: CompositeDisposable = CompositeDisposable()
) : ImagePreviewContract.Controller {
    private var view: ImagePreviewContract.View? = null
    private var state: State = State()

    override fun setView(view: ImagePreviewContract.View) {
        this.view = view
        state = State()
    }

    @Synchronized
    private fun setState(state: State) {
        this.state = state
        view?.onStateUpdated(state)
    }

    override fun onImageDataReceived(bitmapId: String, bitmapName: String) {
        setState(state.withImageData(bitmapId, bitmapName))
    }

    override fun onImageRequested() {
        setState(state.loadingImageFromDownloads())

        disposables.add(
            getImageFileFromDownloadsUseCase(state.imageIdName)
                .subscribe(
                    { setState(state.withImageLoadedFromDownloads(it)) }
                ) { onRequestImageFromCache() }
        )
    }

    override fun onSharePressed() {
        if (state.imageLoadingState == State.ImageLoadingState.LOCAL) {
            shareLocalImage(state.localImage ?: return)
        } else {
            view?.shareImageFile(state.imageIdName)
        }
    }

    override fun onDownloadPressed() {
        disposables.add(
            putImageFileToDownloadsUseCase(state.imageIdName, state.loadedImage ?: return)
                .subscribe(
                    {
                        setState(state.withImageLoadedFromDownloads())
                        view?.showOnImageSaveSuccess()
                    }
                ) {
                    view?.showOnImageSaveFailed()
                }
        )
    }

    private fun onRequestImageFromCache() {
        setState(state.loadingImageFromCache())

        disposables.add(
            getImageFileFromCacheUseCase(state.imageIdName)
                .subscribe({
                    setState(state.withImageLoadedFromCache(it))
                }) {
                    view?.showOnImageLoadingFailed()
                    setState(state.imageLoadingFiled())
                }
        )
    }

    override fun onDestroy() {
        view = null
        disposables.clear()
        state = State()
    }

    private fun shareLocalImage(image: LocalImagePreview) {
        disposables.add(
            getShareableLocalAttachmentUriUseCase(image.uri, image.source, image.fileId, image.displayName, image.size)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { view?.shareImageFile(it, image.mimeType) }
                ) {
                    Logger.w(TAG, "Failed to prepare local image for sharing: ${it.javaClass.simpleName}")
                    view?.showOnImageShareFailed()
                }
        )
    }

    override fun onLocalImageReceived(image: LocalImagePreview) {
        setState(state.withLocalImage(image))
    }
}
