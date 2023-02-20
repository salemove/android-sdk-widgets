package com.glia.widgets.callvisualizer

import com.glia.widgets.base.BaseController
import com.glia.widgets.base.BaseView

interface EndScreenSharingContract {

    interface Controller : BaseController {
        fun setView(view: View)
        fun onBackArrowClicked()
        fun onEndScreenSharingButtonClicked()
    }

    interface View : BaseView<Controller> {
        fun finish()
        fun stopScreenSharing()
    }
}
