package com.glia.widgets.callvisualizer

import com.glia.widgets.base.BaseController
import com.glia.widgets.base.BaseView

internal interface EndScreenSharingContract {

    interface Controller : BaseController {
        fun setView(view: View)
        fun onActivityCreate()
        fun onBackArrowClicked()
        fun onEndScreenSharingButtonClicked()
        fun onForceStopScreenSharing()
    }

    interface View : BaseView<Controller> {
        fun finish()
        fun stopScreenSharing()
    }
}
