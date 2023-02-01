package com.glia.widgets.messagecenter

import android.net.Uri
import com.glia.androidsdk.RequestCallback
import com.glia.widgets.base.BaseController
import com.glia.widgets.base.BaseView
import com.glia.widgets.core.fileupload.model.FileAttachment

interface MessageCenterContract {
    interface Controller : BaseController {
        var photoCaptureFileUri: Uri?
        fun setView(view: View)
        fun onCheckMessagesClicked()
        fun onMessageChanged(message: String)
        fun onSendMessageClicked(message: String)
        fun onBackArrowClicked()
        fun onCloseButtonClicked()
        fun onAddAttachmentButtonClicked()
        fun onGalleryClicked()
        fun onBrowseClicked()
        fun onTakePhotoClicked()
        fun isMessageCenterAvailable(callback: RequestCallback<Boolean>)
        fun onAttachmentReceived(file: FileAttachment)
        fun onRemoveAttachment(file: FileAttachment)
    }

    interface View : BaseView<Controller> {
        fun setupViewAppearance()
        fun onStateUpdated(state: State)
        fun emitUploadAttachments(attachments: List<FileAttachment>)
        fun selectAttachmentFile(type: String)
        fun takePhoto()
        fun finish()
        fun navigateToMessaging()
        fun showAttachmentPopup()
        fun showUnexpectedErrorDialog()
        fun showMessageCenterUnavailableDialog()
        fun showConfirmationScreen()
        fun hideSoftKeyboard()
        fun showUnAuthenticatedDialog()
    }
}
