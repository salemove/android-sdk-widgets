package com.glia.widgets.callvisualizer

import com.glia.androidsdk.omnibrowse.VisitorCode
import com.glia.widgets.base.BaseController
import com.glia.widgets.base.BaseView

internal interface VisitorCodeContract {
    interface Controller : BaseController {
        fun setView(view: View, isClosable: Boolean)
        fun onCloseButtonClicked()
        fun onRefreshButtonClicked()
        fun onLoadVisitorCode()
    }

    interface View : BaseView<Controller> {
        fun notifySetupComplete()
        fun startLoading()
        fun showError(throwable: Throwable)
        fun showVisitorCode(visitorCode: VisitorCode)
        fun setTimer(duration: Long)
        fun destroyTimer()
    }
}
