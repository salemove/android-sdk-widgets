package com.glia.widgets.filepreview.ui

import android.net.Uri
import com.glia.widgets.base.BaseController
import com.glia.widgets.base.BaseView

internal interface ImagePreviewContract {
    interface Controller : BaseController {
        fun onSharePressed()
        fun onDownloadPressed()
        fun onImageRequested()
        fun onImageDataReceived(bitmapId: String, bitmapName: String)
        fun setView(view: View)
        fun onLocalImageReceived(uri: Uri)
    }

    interface View : BaseView<Controller> {
        fun onStateUpdated(state: State)
        fun shareImageFile(fileName: String)
        fun showOnImageSaveSuccess()
        fun showOnImageSaveFailed()
        fun showOnImageLoadingFailed()
        fun shareImageFile(uri: Uri)
    }
}
