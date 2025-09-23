package com.glia.widgets.chat

import android.net.Uri
import android.widget.Toast
import com.glia.androidsdk.Engagement
import com.glia.androidsdk.chat.AttachmentFile
import com.glia.androidsdk.chat.SingleChoiceOption
import com.glia.widgets.base.BaseController
import com.glia.widgets.base.BaseView
import com.glia.widgets.chat.model.ChatItem
import com.glia.widgets.chat.model.ChatState
import com.glia.widgets.chat.model.CustomCardChatItem
import com.glia.widgets.chat.model.GvaButton
import com.glia.widgets.chat.model.OperatorMessageItem
import com.glia.widgets.entrywidget.EntryWidgetContract
import com.glia.widgets.internal.dialog.model.ConfirmationDialogLinks
import com.glia.widgets.internal.dialog.model.LeaveDialogAction
import com.glia.widgets.internal.dialog.model.Link
import com.glia.widgets.internal.fileupload.model.LocalAttachment
import com.glia.widgets.locale.LocaleString

internal interface ChatContract {
    interface Controller : BaseController {
        val isChatVisible: Boolean

        fun setView(view: View)
        fun getView(): View?
        fun onDestroy(retain: Boolean)
        fun onRetryClicked(messageId: String)
        fun onGvaButtonClicked(button: GvaButton)
        fun isCallVisualizerOngoing(): Boolean
        fun onFileDownloadClicked(attachmentFile: AttachmentFile)
        fun onRemoveAttachment(attachment: LocalAttachment)
        fun newMessagesIndicatorClicked()
        fun onRecyclerviewPositionChanged(isBottom: Boolean)
        fun sendCustomCardResponse(customCard: CustomCardChatItem, text: String, value: String)
        fun singleChoiceOptionClicked(item: OperatorMessageItem.ResponseCard, selectedOption: SingleChoiceOption)
        fun overlayPermissionsDialogDismissed()
        fun setOnBackClickedListener(finishCallback: ChatView.OnBackClickedListener?)
        fun onXButtonClicked()
        fun endEngagementClicked()
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
        fun initChat(intention: Intention)
        fun restoreChat()
        fun show()
        fun onPause()
        fun onResume()
        fun onTakePhotoClicked()
        fun onImageCaptured(result: Boolean)
        fun onContentChosen(uri: Uri)
        fun onLocalImageItemClick(attachment: LocalAttachment, view: android.view.View)
        fun leaveCurrentConversationDialogLeaveClicked(action: LeaveDialogAction)
        fun leaveCurrentConversationDialogStayClicked()
        fun onScTopBannerItemClicked(itemType: EntryWidgetContract.ItemType)
    }

    interface View : BaseView<Controller> {
        fun emitUploadAttachments(attachments: List<LocalAttachment>)
        fun emitState(chatState: ChatState)
        fun emitItems(items: List<ChatItem>)
        fun backToCall()
        fun minimizeView()
        fun smoothScrollToBottom()
        fun scrollToBottomImmediate()
        fun fileDownloadError()
        fun fileDownloadSuccess()
        fun clearMessageInput()
        fun fileIsNotReadyForPreview()
        fun showBroadcastNotSupportedToast()
        fun requestOpenUri(uri: Uri)
        fun requestOpenDialer(uri: Uri)
        fun requestOpenEmailClient(uri: Uri)
        fun showEngagementConfirmationDialog()
        fun navigateToWebBrowserActivity(title: LocaleString, url: String)
        fun showToast(message: String, duration: Int = Toast.LENGTH_SHORT)
        fun dispatchImageCapture(uri: Uri)
        fun navigateToImagePreview(attachmentFile: AttachmentFile, view: android.view.View)
        fun navigateToImagePreview(attachmentFile: LocalAttachment, view: android.view.View)
        fun launchCall(mediaType: Engagement.MediaType)
    }
}
