package com.glia.widgets.view.head

import com.glia.widgets.base.BaseController
import com.glia.widgets.base.BaseView

internal interface ChatHeadContract {
    interface Controller : BaseController {
        val chatHeadPosition: ChatHeadPosition
        fun onChatHeadClicked()
        fun onResume(view: android.view.View?)
        fun onApplicationStop()
        fun onChatHeadPositionChanged(x: Int, y: Int)
        fun onPause(gliaOrRootView: android.view.View?)
        fun updateChatHeadView()
        fun onSetChatHeadView(view: View)
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
    }
}
