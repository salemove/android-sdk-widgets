package com.glia.widgets.filepreview.ui

import com.glia.widgets.filepreview.domain.usecase.GetImageFileFromCacheUseCase
import com.glia.widgets.filepreview.domain.usecase.GetImageFileFromDownloadsUseCase
import com.glia.widgets.filepreview.domain.usecase.PutImageFileToDownloadsUseCase
import io.reactivex.disposables.CompositeDisposable

internal class FilePreviewController @JvmOverloads constructor(
    private val getImageFileFromDownloadsUseCase: GetImageFileFromDownloadsUseCase,
    private val getImageFileFromCacheUseCase: GetImageFileFromCacheUseCase,
    private val putImageFileToDownloadsUseCase: PutImageFileToDownloadsUseCase,
    private var state: State = State(),
    private val disposables: CompositeDisposable = CompositeDisposable()
) : FilePreviewContract.Controller {
    private var view: FilePreviewContract.View? = null

    fun setView(view: FilePreviewContract.View) {
        this.view = view
        state.reset()
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
        view?.shareImageFile(state.imageIdName)
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
        disposables.clear()
        state.reset()
    }
}
