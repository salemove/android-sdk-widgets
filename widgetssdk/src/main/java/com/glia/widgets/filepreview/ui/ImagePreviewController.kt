package com.glia.widgets.filepreview.ui

import android.net.Uri
import com.glia.widgets.filepreview.domain.usecase.GetImageFileFromCacheUseCase
import com.glia.widgets.filepreview.domain.usecase.GetImageFileFromDownloadsUseCase
import com.glia.widgets.filepreview.domain.usecase.PutImageFileToDownloadsUseCase
import io.reactivex.rxjava3.disposables.CompositeDisposable

internal class ImagePreviewController @JvmOverloads constructor(
    private val getImageFileFromDownloadsUseCase: GetImageFileFromDownloadsUseCase,
    private val getImageFileFromCacheUseCase: GetImageFileFromCacheUseCase,
    private val putImageFileToDownloadsUseCase: PutImageFileToDownloadsUseCase,
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
            view?.shareImageFile(state.localImageUri ?: return)
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

    override fun onLocalImageReceived(uri: Uri) {
        setState(state.withLocalImage(uri))
    }
}
