package com.glia.widgets.view.head

import com.glia.widgets.base.BaseController
import com.glia.widgets.base.BaseView

internal interface ChatHeadLayoutContract {
    interface Controller : BaseController {
        fun onChatHeadClicked()
        fun onResume(viewName: String)
        fun setView(view: View)
        fun shouldShow(gliaOrRootViewName: String?): Boolean
        fun updateChatHeadView()
        fun onPause(viewName: String)
    }

    interface View : BaseView<Controller> {
        fun showOperatorImage(operatorImgUrl: String)
        fun showUnreadMessageCount(count: Int)
        fun showPlaceholder()
        fun showQueueing()
        fun showOnHold()
        fun hideOnHold()
        fun navigateToChat()
        fun navigateToCall()
        fun show()
        fun hide()
    }
}
