package com.glia.widgets.callvisualizer

import com.glia.widgets.base.BaseController
import com.glia.widgets.base.BaseView

interface VisitorCodeContract {
    interface Controller : BaseController {
        fun setView(view: View)
        fun onCloseButtonClicked()
    }

    interface View : BaseView<Controller>
}