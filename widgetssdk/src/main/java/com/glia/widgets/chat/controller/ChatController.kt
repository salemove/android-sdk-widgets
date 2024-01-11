package com.glia.widgets.chat.controller

import android.net.Uri
import android.view.View
import com.glia.androidsdk.chat.AttachmentFile
import com.glia.androidsdk.chat.SingleChoiceOption
import com.glia.androidsdk.comms.MediaUpgradeOffer
import com.glia.widgets.chat.ChatType
import com.glia.widgets.chat.ChatView
import com.glia.widgets.chat.ChatViewCallback
import com.glia.widgets.chat.model.CustomCardChatItem
import com.glia.widgets.chat.model.GvaButton
import com.glia.widgets.chat.model.OperatorMessageItem
import com.glia.widgets.core.dialog.model.ConfirmationDialogLinks
import com.glia.widgets.core.dialog.model.Link
import com.glia.widgets.core.fileupload.model.FileAttachment

internal interface ChatController {
    val isChatVisible: Boolean
    var photoCaptureFileUri: Uri?

    fun setViewCallback(chatViewCallback: ChatViewCallback)
    fun onDestroy(retain: Boolean)
    fun onMessageClicked(messageId: String)
    fun onGvaButtonClicked(button: GvaButton)
    fun isCallVisualizerOngoing(): Boolean
    fun onFileDownloadClicked(attachmentFile: AttachmentFile)
    fun onAttachmentReceived(file: FileAttachment)
    fun onRemoveAttachment(attachment: FileAttachment)
    fun notificationDialogDismissed()
    fun newMessagesIndicatorClicked()
    fun onRecyclerviewPositionChanged(isBottom: Boolean)
    fun sendCustomCardResponse(customCard: CustomCardChatItem, text: String, value: String)
    fun singleChoiceOptionClicked(item: OperatorMessageItem.ResponseCard, selectedOption: SingleChoiceOption)
    fun declineUpgradeOfferClicked(offer: MediaUpgradeOffer)
    fun acceptUpgradeOfferClicked(offer: MediaUpgradeOffer)
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
    fun onImageItemClick(item: AttachmentFile, view: View)
    fun onLiveObservationDialogRejected()
    fun onLiveObservationDialogAllowed()
    fun onLinkClicked(link: Link)
    fun getConfirmationDialogLinks(): ConfirmationDialogLinks
    fun onEngagementConfirmationDialogRequested()
    fun initChat(companyName: String?, queueId: String?, visitorContextAssetId: String?, chatType: ChatType)
    fun show()
    fun onPause()
    fun onResume()
}
