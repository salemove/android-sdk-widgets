package com.glia.widgets.callvisualizer

import com.glia.androidsdk.omnibrowse.VisitorCode
import com.glia.widgets.base.BaseController
import com.glia.widgets.base.BaseView

interface VisitorCodeContract {
    interface Controller : BaseController {
        fun setView(view: View)
        fun onCloseButtonClicked()
        fun onLoadVisitorCode()
    }

    interface View : BaseView<Controller> {
        fun onSetupComplete()
        fun startLoading()
        fun onError(throwable: Throwable)
        fun onVisitorCode(visitorCode: VisitorCode)
    }
}