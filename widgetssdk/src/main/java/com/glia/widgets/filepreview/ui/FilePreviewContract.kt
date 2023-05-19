package com.glia.widgets.filepreview.ui

import com.glia.widgets.base.BaseController
import com.glia.widgets.base.BaseView

internal interface FilePreviewContract {
    interface Controller : BaseController {
        fun onSharePressed()
        fun onDownloadPressed()
        fun onImageRequested()
        fun onImageDataReceived(bitmapId: String, bitmapName: String)
    }

    interface View : BaseView<FilePreviewController> {
        fun onStateUpdated(state: State)
        fun shareImageFile(fileName: String)
        fun showOnImageSaveSuccess()
        fun showOnImageSaveFailed()
        fun showOnImageLoadingFailed()
        fun engagementEnded()
    }
}
