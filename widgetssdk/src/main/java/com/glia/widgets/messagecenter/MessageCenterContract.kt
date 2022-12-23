package com.glia.widgets.messagecenter

import com.glia.widgets.base.BaseController
import com.glia.widgets.base.BaseView

interface MessageCenterContract {
    interface Controller : BaseController {
        fun setView(view: View)
        fun onCheckMessagesClicked()
        fun onSendMessageClicked(message: String)
        fun onBackArrowClicked()
        fun onCloseButtonClicked()
        fun onAddAttachmentButtonClicked()
        fun onGalleryClicked()
        fun onBrowseClicked()
        fun onTakePhotoClicked()
    }

    interface View : BaseView<Controller> {
        fun finish()
        fun navigateToMessaging()
        fun showAttachmentPopup()
        fun showUnexpectedErrorDialog()
        fun showMessageCenterUnavailableDialog()
    }
}
