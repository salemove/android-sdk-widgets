package com.glia.widgets.messagecenter

import android.net.Uri
import com.glia.widgets.UiTheme
import com.glia.widgets.base.BaseController
import com.glia.widgets.base.BaseView
import com.glia.widgets.core.configuration.GliaSdkConfiguration
import com.glia.widgets.core.dialog.DialogContract
import com.glia.widgets.core.fileupload.model.FileAttachment

internal interface MessageCenterContract {
    interface Controller : BaseController {
        fun setConfiguration(uiTheme: UiTheme?, configuration: GliaSdkConfiguration?)
        fun setView(view: View)
        fun initialize()
        fun onCheckMessagesClicked()
        fun onMessageChanged(message: String)
        fun onSendMessageClicked()
        fun onCloseButtonClicked()
        fun onSystemBack()
        fun onAddAttachmentButtonClicked()
        fun onGalleryClicked()
        fun onBrowseClicked()
        fun onTakePhotoClicked()
        fun ensureMessageCenterAvailability()
        fun onRemoveAttachment(file: FileAttachment)
        fun addCallback(dialogCallback: DialogContract.Controller.Callback)
        fun removeCallback(dialogCallback: DialogContract.Controller.Callback)
        fun dismissDialogs()
        fun dismissCurrentDialog()
        fun onImageCaptured(result: Boolean)
        fun onContentChosen(uri: Uri)
    }

    interface View : BaseView<Controller> {
        fun setupViewAppearance()
        fun onStateUpdated(state: MessageCenterState)
        fun emitUploadAttachments(attachments: List<FileAttachment>)
        fun selectAttachmentFile(type: String)
        fun takePhoto(uri: Uri)
        fun finish()
        fun navigateToMessaging()
        fun showAttachmentPopup()
        fun showConfirmationScreen()
        fun hideSoftKeyboard()
    }
}
