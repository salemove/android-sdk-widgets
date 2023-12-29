package com.glia.widgets.view.floatingvisitorvideoview

import com.glia.androidsdk.comms.MediaState
import com.glia.widgets.base.BaseController
import com.glia.widgets.base.BaseView

interface FloatingVisitorVideoContract {
    interface Controller : BaseController {
        fun setView(view: View)
    }

    interface View : BaseView<Controller> {
        fun show(state: MediaState?)
        fun hide()
        fun onResume()
        fun onPause()
        fun showOnHold()
        fun hideOnHold()
    }
}
