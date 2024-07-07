package com.glia.widgets.chat

import android.net.Uri
import android.widget.Toast
import com.glia.androidsdk.chat.AttachmentFile
import com.glia.androidsdk.chat.SingleChoiceOption
import com.glia.widgets.base.BaseController
import com.glia.widgets.base.BaseView
import com.glia.widgets.chat.model.ChatItem
import com.glia.widgets.chat.model.ChatState
import com.glia.widgets.chat.model.CustomCardChatItem
import com.glia.widgets.chat.model.GvaButton
import com.glia.widgets.chat.model.OperatorMessageItem
import com.glia.widgets.core.dialog.model.ConfirmationDialogLinks
import com.glia.widgets.core.dialog.model.Link
import com.glia.widgets.core.fileupload.model.FileAttachment

internal interface ChatContract {
    interface Controller : BaseController {
        val isChatVisible: Boolean

        fun setView(view: View)
        fun onDestroy(retain: Boolean)
        fun onMessageClicked(messageId: String)
        fun onGvaButtonClicked(button: GvaButton)
        fun isCallVisualizerOngoing(): Boolean
        fun onFileDownloadClicked(attachmentFile: AttachmentFile)
        fun onRemoveAttachment(attachment: FileAttachment)
        fun newMessagesIndicatorClicked()
        fun onRecyclerviewPositionChanged(isBottom: Boolean)
        fun sendCustomCardResponse(customCard: CustomCardChatItem, text: String, value: String)
        fun singleChoiceOptionClicked(item: OperatorMessageItem.ResponseCard, selectedOption: SingleChoiceOption)
        fun overlayPermissionsDialogDismissed()
        fun setOnBackClickedListener(finishCallback: ChatView.OnBackClickedListener?)
        fun onXButtonClicked()
        fun leaveChatClicked()
        fun endEngagementDialogDismissed()
        fun endEngagementDialogYesClicked()
        fun unexpectedErrorDialogDismissed()
        fun noMoreOperatorsAvailableDismissed()
        fun onBackArrowClicked()
        fun sendMessage(message: String)
        fun onMessageTextChanged(message: String)
        fun onImageItemClick(item: AttachmentFile, view: android.view.View)
        fun onLiveObservationDialogRejected()
        fun onLiveObservationDialogAllowed()
        fun onLinkClicked(link: Link)
        fun getConfirmationDialogLinks(): ConfirmationDialogLinks
        fun onEngagementConfirmationDialogRequested()
        fun initChat(companyName: String?, queueIds: List<String>?, visitorContextAssetId: String?, chatType: ChatType)
        fun show()
        fun onPause()
        fun onResume()
        fun onForceStopScreenSharing()
        fun onTakePhotoClicked()
        fun onImageCaptured(result: Boolean)
        fun onContentChosen(uri: Uri)
    }

    interface View : BaseView<Controller> {
        fun emitUploadAttachments(attachments: List<FileAttachment>)
        fun emitState(chatState: ChatState)
        fun emitItems(items: List<ChatItem>)
        fun navigateToCall(mediaType: String)
        fun backToCall()
        fun minimizeView()
        fun smoothScrollToBottom()
        fun scrollToBottomImmediate()
        fun fileDownloadError(attachmentFile: AttachmentFile, error: Throwable)
        fun fileDownloadSuccess(attachmentFile: AttachmentFile)
        fun clearMessageInput()
        fun navigateToPreview(attachmentFile: AttachmentFile, view: android.view.View)
        fun fileIsNotReadyForPreview()
        fun showBroadcastNotSupportedToast()
        fun requestOpenUri(uri: Uri)
        fun requestOpenDialer(uri: Uri)
        fun requestOpenEmailClient(uri: Uri)
        fun showEngagementConfirmationDialog()
        fun navigateToWebBrowserActivity(title: String, url: String)
        fun showToast(message: String, duration: Int = Toast.LENGTH_SHORT)
        fun dispatchImageCapture(uri: Uri)
        fun onFileDownload(attachmentFile: AttachmentFile)
    }
}
