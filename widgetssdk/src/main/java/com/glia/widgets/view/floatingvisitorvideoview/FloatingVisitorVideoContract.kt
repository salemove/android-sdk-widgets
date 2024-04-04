package com.glia.widgets.view.floatingvisitorvideoview

import com.glia.androidsdk.comms.MediaState
import com.glia.widgets.base.BaseController
import com.glia.widgets.base.BaseView

internal interface FloatingVisitorVideoContract {

    interface View {
        fun show(state: MediaState?)
        fun hide()
        fun onResume()
        fun onPause()
        fun showOnHold()
        fun hideOnHold()
    }
}
