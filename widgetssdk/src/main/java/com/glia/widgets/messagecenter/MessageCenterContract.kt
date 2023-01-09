package com.glia.widgets.messagecenter

import com.glia.androidsdk.RequestCallback
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
        fun isMessageCenterAvailable(callback: RequestCallback<Boolean>)
    }

    interface View : BaseView<Controller> {
        fun finish()
        fun navigateToMessaging()
        fun showAttachmentPopup()
        fun showUnexpectedErrorDialog()
        fun showMessageCenterUnavailableDialog()
        fun showConfirmationScreen()
        fun hideSoftKeyboard()
    }
}
