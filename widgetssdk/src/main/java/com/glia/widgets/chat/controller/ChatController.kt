package com.glia.widgets.chat.controller

import android.net.Uri
import androidx.annotation.VisibleForTesting
import com.glia.androidsdk.GliaException
import com.glia.androidsdk.Operator
import com.glia.androidsdk.chat.*
import com.glia.androidsdk.comms.MediaDirection
import com.glia.androidsdk.comms.MediaUpgradeOffer
import com.glia.androidsdk.comms.OperatorMediaState
import com.glia.androidsdk.engagement.EngagementFile
import com.glia.androidsdk.engagement.Survey
import com.glia.androidsdk.omnicore.OmnicoreEngagement
import com.glia.androidsdk.site.SiteInfo
import com.glia.widgets.Constants
import com.glia.widgets.GliaWidgets
import com.glia.widgets.chat.ChatType
import com.glia.widgets.chat.ChatView
import com.glia.widgets.chat.ChatViewCallback
import com.glia.widgets.chat.adapter.ChatAdapter
import com.glia.widgets.chat.domain.*
import com.glia.widgets.chat.model.ChatInputMode
import com.glia.widgets.chat.model.ChatState
import com.glia.widgets.chat.model.history.*
import com.glia.widgets.core.dialog.DialogController
import com.glia.widgets.core.dialog.domain.IsShowOverlayPermissionRequestDialogUseCase
import com.glia.widgets.core.engagement.domain.*
import com.glia.widgets.core.engagement.domain.model.ChatMessageInternal
import com.glia.widgets.core.engagement.domain.model.EngagementStateEvent
import com.glia.widgets.core.engagement.domain.model.EngagementStateEventVisitor
import com.glia.widgets.core.engagement.domain.model.EngagementStateEventVisitor.OperatorVisitor
import com.glia.widgets.core.fileupload.domain.*
import com.glia.widgets.core.fileupload.model.FileAttachment
import com.glia.widgets.core.mediaupgradeoffer.MediaUpgradeOfferRepository
import com.glia.widgets.core.mediaupgradeoffer.MediaUpgradeOfferRepository.Submitter
import com.glia.widgets.core.mediaupgradeoffer.MediaUpgradeOfferRepositoryCallback
import com.glia.widgets.core.mediaupgradeoffer.domain.AddMediaUpgradeOfferCallbackUseCase
import com.glia.widgets.core.mediaupgradeoffer.domain.RemoveMediaUpgradeOfferCallbackUseCase
import com.glia.widgets.core.notification.domain.RemoveCallNotificationUseCase
import com.glia.widgets.core.notification.domain.ShowAudioCallNotificationUseCase
import com.glia.widgets.core.notification.domain.ShowVideoCallNotificationUseCase
import com.glia.widgets.core.operator.GliaOperatorMediaRepository.OperatorMediaStateListener
import com.glia.widgets.core.operator.domain.AddOperatorMediaStateListenerUseCase
import com.glia.widgets.core.queue.domain.GliaCancelQueueTicketUseCase
import com.glia.widgets.core.queue.domain.GliaQueueForChatEngagementUseCase
import com.glia.widgets.core.queue.domain.QueueTicketStateChangeToUnstaffedUseCase
import com.glia.widgets.core.queue.domain.exception.QueueingOngoingException
import com.glia.widgets.core.secureconversations.domain.GetUnreadMessagesCountWithTimeoutUseCase
import com.glia.widgets.core.secureconversations.domain.IsSecureEngagementUseCase
import com.glia.widgets.core.secureconversations.domain.MarkMessagesReadWithDelayUseCase
import com.glia.widgets.core.survey.OnSurveyListener
import com.glia.widgets.core.survey.domain.GliaSurveyUseCase
import com.glia.widgets.di.Dependencies
import com.glia.widgets.filepreview.domain.usecase.DownloadFileUseCase
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.TimeCounter
import com.glia.widgets.helper.TimeCounter.FormattedTimerStatusListener
import com.glia.widgets.helper.Utils
import com.glia.widgets.view.MessagesNotSeenHandler
import com.glia.widgets.view.MinimizeHandler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.*

internal class ChatController(
    chatViewCallback: ChatViewCallback,
    private val mediaUpgradeOfferRepository: MediaUpgradeOfferRepository,
    private val callTimer: TimeCounter,
    private val minimizeHandler: MinimizeHandler,
    private val dialogController: DialogController,
    private val messagesNotSeenHandler: MessagesNotSeenHandler,
    private val showAudioCallNotificationUseCase: ShowAudioCallNotificationUseCase,
    private val showVideoCallNotificationUseCase: ShowVideoCallNotificationUseCase,
    private val removeCallNotificationUseCase: RemoveCallNotificationUseCase,
    private val loadHistoryUseCase: GliaLoadHistoryUseCase,
    private val queueForChatEngagementUseCase: GliaQueueForChatEngagementUseCase,
    private val getEngagementUseCase: GliaOnEngagementUseCase,
    private val engagementEndUseCase: GliaOnEngagementEndUseCase,
    private val onMessageUseCase: GliaOnMessageUseCase,
    private val onOperatorTypingUseCase: GliaOnOperatorTypingUseCase,
    private val sendMessagePreviewUseCase: GliaSendMessagePreviewUseCase,
    private val sendMessageUseCase: GliaSendMessageUseCase,
    private val addOperatorMediaStateListenerUseCase: AddOperatorMediaStateListenerUseCase,
    private val cancelQueueTicketUseCase: GliaCancelQueueTicketUseCase,
    private val endEngagementUseCase: GliaEndEngagementUseCase,
    private val addFileToAttachmentAndUploadUseCase: AddFileToAttachmentAndUploadUseCase,
    private val addFileAttachmentsObserverUseCase: AddFileAttachmentsObserverUseCase,
    private val removeFileAttachmentObserverUseCase: RemoveFileAttachmentObserverUseCase,
    private val getFileAttachmentsUseCase: GetFileAttachmentsUseCase,
    private val removeFileAttachmentUseCase: RemoveFileAttachmentUseCase,
    private val supportedFileCountCheckUseCase: SupportedFileCountCheckUseCase,
    private val isShowSendButtonUseCase: IsShowSendButtonUseCase,
    private val isShowOverlayPermissionRequestDialogUseCase: IsShowOverlayPermissionRequestDialogUseCase,
    private val downloadFileUseCase: DownloadFileUseCase,
    private val isEnableChatEditTextUseCase: IsEnableChatEditTextUseCase,
    private val siteInfoUseCase: SiteInfoUseCase,
    private val surveyUseCase: GliaSurveyUseCase,
    private val getGliaEngagementStateFlowableUseCase: GetEngagementStateFlowableUseCase,
    private val isFromCallScreenUseCase: IsFromCallScreenUseCase,
    private val updateFromCallScreenUseCase: UpdateFromCallScreenUseCase,
    private val customCardAdapterTypeUseCase: CustomCardAdapterTypeUseCase,
    private val customCardTypeUseCase: CustomCardTypeUseCase,
    private val customCardInteractableUseCase: CustomCardInteractableUseCase,
    private val customCardShouldShowUseCase: CustomCardShouldShowUseCase,
    private val ticketStateChangeToUnstaffedUseCase: QueueTicketStateChangeToUnstaffedUseCase,
    private val isQueueingEngagementUseCase: IsQueueingEngagementUseCase,
    private val addMediaUpgradeCallbackUseCase: AddMediaUpgradeOfferCallbackUseCase,
    private val removeMediaUpgradeCallbackUseCase: RemoveMediaUpgradeOfferCallbackUseCase,
    private val isSecureEngagementUseCase: IsSecureEngagementUseCase,
    private val isOngoingEngagementUseCase: IsOngoingEngagementUseCase,
    private val engagementConfigUseCase: SetEngagementConfigUseCase,
    private val isSecureEngagementAvailableUseCase: IsSecureConversationsChatAvailableUseCase,
    private val markMessagesReadWithDelayUseCase: MarkMessagesReadWithDelayUseCase,
    private val getUnreadMessagesCountWithTimeoutUseCase: GetUnreadMessagesCountWithTimeoutUseCase
) : GliaOnEngagementUseCase.Listener, GliaOnEngagementEndUseCase.Listener, OnSurveyListener {
    private var viewCallback: ChatViewCallback? = null
    private var mediaUpgradeOfferRepositoryCallback: MediaUpgradeOfferRepositoryCallback? = null
    private var timerStatusListener: FormattedTimerStatusListener? = null
    private var engagementStateEventDisposable: Disposable? = null

    private val disposable = CompositeDisposable()
    private val operatorMediaStateListener =
        OperatorMediaStateListener { onNewOperatorMediaState(it) }

    private val sendMessageCallback: GliaSendMessageUseCase.Listener =
        object : GliaSendMessageUseCase.Listener {
            override fun messageSent(message: VisitorMessage?) {
                onMessageSent(message)
            }

            override fun onCardMessageUpdated(message: OperatorMessage) {
                updateCustomCard(message)
            }

            override fun onMessageValidated() {
                viewCallback?.clearMessageInput()

                emitViewState {
                    chatState
                        .setLastTypedText(EMPTY_MESSAGE)
                        .setShowSendButton(isShowSendButtonUseCase.execute(EMPTY_MESSAGE))
                }
            }

            override fun errorOperatorNotOnline(message: String) {
                onSendMessageOperatorOffline(message)
            }

            override fun errorMessageInvalid() {}
            override fun error(ex: GliaException) {
                onMessageSendError(ex)
            }
        }
    private var isVisitorEndEngagement = false

    @Volatile
    private var isChatViewPaused = false

    // TODO pending photoCaptureFileUri - need to move some place better
    var photoCaptureFileUri: Uri? = null

    private val fileAttachmentObserver = Observer { _, _ ->
        viewCallback?.apply {
            emitUploadAttachments(getFileAttachmentsUseCase.execute())
            emitViewState{
                chatState
                    .setShowSendButton(isShowSendButtonUseCase.execute(chatState.lastTypedText))
                    .setIsAttachmentButtonEnabled(supportedFileCountCheckUseCase.execute())
            }
        }
    }

    @Volatile
    private var chatState: ChatState

    private val isSecureEngagement get() = isSecureEngagementUseCase()

    private val isQueueingOrOngoingEngagement get() = isQueueingEngagementUseCase() || isOngoingEngagementUseCase()

    fun initChat(
        companyName: String?, queueId: String?, visitorContextAssetId: String?, chatType: ChatType
    ) {
        val queueIds = if (queueId != null) arrayOf(queueId) else emptyArray()
        engagementConfigUseCase(chatType, queueIds)

        if (isShowOverlayPermissionRequestDialogUseCase.execute()) dialogController.showOverlayPermissionsDialog()

        ensureSecureMessagingAvailable()

        if (chatState.integratorChatStarted || dialogController.isShowingChatEnderDialog) {
            if (isSecureEngagement) {
                emitViewState { chatState.setSecureMessagingState() }
            }
            return
        }

        var initChatState = chatState.initChat(companyName, queueId, visitorContextAssetId)
        if (isSecureEngagement) {
            initChatState = initChatState.setSecureMessagingState()
        }
        prepareChatComponents()
        emitViewState { initChatState }
        loadChatHistory()
    }

    private fun ensureSecureMessagingAvailable() {
        if (!isSecureEngagement) return

        disposable.add(
            isSecureEngagementAvailableUseCase().subscribe({
                if (it) {
                    Logger.d(TAG, "Messaging is available")
                } else {
                    Logger.d(TAG, "Messaging is unavailable")
                    dialogController.showMessageCenterUnavailableDialog()
                }
            }, {
                Logger.e(TAG, "Checking for Messaging availability failed", it)
                dialogController.showUnexpectedErrorDialog()
            })
        )
    }

    private fun prepareChatComponents() {
        addFileAttachmentsObserverUseCase.execute(fileAttachmentObserver)
        initMediaUpgradeCallback()
        minimizeHandler.addListener { minimizeView() }
        createNewTimerCallback()
        callTimer.addFormattedValueListener(timerStatusListener)
        updateAllowFileSendState()
    }

    private fun queueForEngagement() {
        Logger.d(TAG, "queueForEngagement")
        disposable.add(
            queueForChatEngagementUseCase
                .execute(chatState.queueId, chatState.visitorContextAssetId)
                .subscribe({ queueForEngagementStarted() }) { queueForEngagementError(it) }
        )
    }

    @Synchronized
    private fun emitViewState(callback: () -> ChatState?) {
        val state = callback() ?: return

        if (setState(state) && viewCallback != null) {
            Logger.d(TAG, "Emit state:\n$state")
            viewCallback?.emitState(chatState)
        }
    }

    @Synchronized
    private fun emitChatItems(callback: () -> ChatState?) {
        val state = callback() ?: return

        if (setState(state) && viewCallback != null) {
            Logger.d(
                TAG, """
     Emit chat items:
     ${state.chatItems}
     (State): $state
     """.trimIndent()
            )

            viewCallback?.emitItems(state.chatItems)
            viewCallback?.emitUploadAttachments(getFileAttachmentsUseCase.execute())
        }
    }

    fun onDestroy(retain: Boolean) {
        Logger.d(TAG, "onDestroy, retain:$retain")
        destroyView()

        // viewCallback is accessed from multiple threads
        // and must be protected from race condition
        synchronized(this) { viewCallback = null }
        if (!retain) {
            disposable.dispose()
            mediaUpgradeOfferRepository.stopAll()
            mediaUpgradeOfferRepositoryCallback = null
            timerStatusListener = null
            callTimer.clear()
            minimizeHandler.clear()
            getEngagementUseCase.unregisterListener(this)
            engagementEndUseCase.unregisterListener(this)
            onMessageUseCase.unregisterListener()
            onOperatorTypingUseCase.unregisterListener()
            removeFileAttachmentObserverUseCase.execute(fileAttachmentObserver)
        }
    }

    fun onPause() {
        isChatViewPaused = true
        messagesNotSeenHandler.onChatWentBackground()
        surveyUseCase.unregisterListener(this)
        mediaUpgradeOfferRepositoryCallback?.let { removeMediaUpgradeCallbackUseCase(it) }
    }

    fun onMessageTextChanged(message: String) {
        emitViewState {
            chatState
                .setLastTypedText(message)
                .setShowSendButton(isShowSendButtonUseCase.execute(message))
        }
        sendMessagePreview(message)
    }

    fun sendMessage(message: String) {
        Logger.d(TAG, "Send MESSAGE: $message")
        clearMessagePreview()
        sendMessageUseCase.execute(message, sendMessageCallback)
    }

    private fun sendMessagePreview(message: String) {
        if (chatState.isOperatorOnline) {
            sendMessagePreviewUseCase.execute(message)
        }
    }

    private fun clearMessagePreview() {
        // An empty string has to be sent to clear the message preview.
        sendMessagePreview(EMPTY_MESSAGE)
    }

    private fun subscribeToMessages() {
        disposable.add(
            onMessageUseCase.execute()
                .doOnNext { onMessage(it) }
                .subscribe())
    }

    private fun onMessage(messageInternal: ChatMessageInternal) {
        val message = messageInternal.chatMessage
        if (!isNewMessage(chatState.chatItems, message)) {
            return
        }
        val isUnsentMessage =
            chatState.unsentMessages.isNotEmpty() && chatState.unsentMessages[0].message == message.content
        Logger.d(
            TAG,
            "onMessage: ${message.content}, id: ${message.id}, isUnsentMessage: $isUnsentMessage"
        )
        if (isUnsentMessage) {
            // emitting state because there is no need to change recyclerview items here
            emitViewState {
                val unsentMessages: MutableList<VisitorMessageItem> =
                    chatState.unsentMessages.toMutableList()
                val currentMessage = unsentMessages[0]
                unsentMessages.remove(currentMessage)
                val currentChatItems: MutableList<ChatItem> = chatState.chatItems.toMutableList()
                val currentMessageIndex = currentChatItems.indexOf(currentMessage)
                currentChatItems.remove(currentMessage)
                currentChatItems.add(currentMessageIndex, VisitorMessageItem.asNewMessage(message))

                return@emitViewState chatState.changeItems(currentChatItems).changeUnsentMessages(unsentMessages)
            }
            if (chatState.unsentMessages.isNotEmpty()) {
                sendMessageUseCase.execute(chatState.unsentMessages[0].message, sendMessageCallback)
            }
            return
        }
        emitChatItems {
            val items: MutableList<ChatItem> = chatState.chatItems.toMutableList()
            appendMessageItem(items, messageInternal)

            return@emitChatItems chatState.changeItems(items)
        }
    }

    private fun onMessageSent(message: VisitorMessage?) {
        if (message != null) {
            Logger.d(TAG, "messageSent: $message, id: ${message.id}")
            emitChatItems {
                val currentChatItems: MutableList<ChatItem> = chatState.chatItems.toMutableList()
                if (isQueueingOrOngoingEngagement) {
                    changeDeliveredIndex(currentChatItems, message)
                } else if (isSecureEngagementUseCase()) {
                    appendSentMessage(currentChatItems, message)
                    addVisitorAttachmentItemsToChatItems(currentChatItems, message)
                }

                // chat input mode has to be set to enable after a message is sent
                if (isEnableChatEditTextUseCase(currentChatItems)) {
                    emitViewState { chatState.chatInputModeChanged(ChatInputMode.ENABLED) }
                }

                return@emitChatItems chatState.changeItems(currentChatItems)
            }
        }
    }

    private fun onMessageSendError(exception: GliaException) {
        Logger.d(TAG, "messageSent exception")
        error(exception)
    }

    private fun onSendMessageOperatorOffline(message: String) {
        appendUnsentMessage(message)
        if (!chatState.engagementRequested) {
            queueForEngagement()
        }
    }

    private fun appendUnsentMessage(message: String) {
        Logger.d(TAG, "appendUnsentMessage: $message")
        emitChatItems {
            val unsentMessages: MutableList<VisitorMessageItem> =
                chatState.unsentMessages.toMutableList()
            val unsentItem = VisitorMessageItem.asUnsentItem(message)
            unsentMessages.add(unsentItem)
            val currentChatItems: MutableList<ChatItem> = chatState.chatItems.toMutableList()
            currentChatItems.add(unsentItem)
            emitViewState { chatState.changeUnsentMessages(unsentMessages) }

            return@emitChatItems chatState.changeItems(currentChatItems)
        }
    }

    private fun onOperatorTyping(isOperatorTyping: Boolean) {
        emitViewState { chatState.setIsOperatorTyping(isOperatorTyping) }
    }

    fun show() {
        Logger.d(TAG, "show")
        if (!chatState.isVisible) {
            emitViewState { chatState.changeVisibility(true) }
        }
    }

    fun onBackArrowClicked() {
        Logger.d(TAG, "onBackArrowClicked")
        if (isQueueingOrOngoingEngagement) {
            emitViewState { chatState.changeVisibility(false) }
            messagesNotSeenHandler.chatOnBackClicked()
        } else {
            Dependencies.getControllerFactory().destroyControllers()
        }
    }

    fun onBackArrowClicked(onBackClickedListener: ChatView.OnBackClickedListener?) {
        if (isFromCallScreenUseCase.isFromCallScreen) {
            viewCallback?.backToCall()
        } else {
            onBackClickedListener?.onBackClicked()
        }
        updateFromCallScreenUseCase.updateFromCallScreen(false)
    }

    fun noMoreOperatorsAvailableDismissed() {
        Logger.d(TAG, "noMoreOperatorsAvailableDismissed")
        stop()
        dialogController.dismissCurrentDialog()
    }

    fun unexpectedErrorDialogDismissed() {
        Logger.d(TAG, "unexpectedErrorDialogDismissed")
        stop()
        dialogController.dismissCurrentDialog()
    }

    fun endEngagementDialogYesClicked() {
        Logger.d(TAG, "endEngagementDialogYesClicked")
        isVisitorEndEngagement = true
        stop()
        dialogController.dismissDialogs()
    }

    fun endEngagementDialogDismissed() {
        Logger.d(TAG, "endEngagementDialogDismissed")
        dialogController.dismissCurrentDialog()
    }

    fun leaveChatClicked() {
        Logger.d(TAG, "leaveChatClicked")
        if (chatState.isOperatorOnline) dialogController.showExitChatDialog(chatState.formattedOperatorName)
    }

    fun onXButtonClicked() {
        Logger.d(TAG, "onXButtonClicked")
        if (isQueueingEngagementUseCase()) {
            dialogController.showExitQueueDialog()
        } else {
            Dependencies.getControllerFactory().destroyControllers()
        }
    }

    val isChatVisible: Boolean
        get() = chatState.isVisible

    // viewCallback is accessed from multiple threads
    // and must be protected from race condition
    @Synchronized
    fun setViewCallback(chatViewCallback: ChatViewCallback) {
        Logger.d(TAG, "setViewCallback")
        viewCallback = chatViewCallback
        viewCallback?.emitState(chatState)
        viewCallback?.emitItems(chatState.chatItems)
        viewCallback?.emitUploadAttachments(getFileAttachmentsUseCase.execute())

        // always start in bottom
        emitViewState {
            chatState.isInBottomChanged(true).changeVisibility(true)
        }
        viewCallback?.scrollToBottomImmediate()

        chatState.pendingNavigationType?.also { viewCallback?.navigateToCall(it) }
    }

    fun onResume() {
        Logger.d(TAG, "onResume")
        isChatViewPaused = false
        messagesNotSeenHandler.callChatButtonClicked()
        surveyUseCase.registerListener(this)
        subscribeToEngagementStateChange()
        mediaUpgradeOfferRepositoryCallback?.let { addMediaUpgradeCallbackUseCase(it) }
    }

    private fun subscribeToEngagementStateChange() {
        engagementStateEventDisposable?.dispose()

        engagementStateEventDisposable = getGliaEngagementStateFlowableUseCase
            .execute()
            .subscribe({ onEngagementStateChanged(it) }) { Logger.e(TAG, it.message) }
        disposable.add(engagementStateEventDisposable!!)
    }

    private fun onEngagementStateChanged(engagementState: EngagementStateEvent) {
        val visitor: EngagementStateEventVisitor<Operator> = OperatorVisitor()
        when (engagementState.type!!) {
            EngagementStateEvent.Type.ENGAGEMENT_OPERATOR_CHANGED -> onOperatorChanged(
                visitor.visit(engagementState)
            )
            EngagementStateEvent.Type.ENGAGEMENT_OPERATOR_CONNECTED -> onOperatorConnected(
                visitor.visit(engagementState)
            )
            EngagementStateEvent.Type.ENGAGEMENT_TRANSFERRING -> onTransferring()
            EngagementStateEvent.Type.ENGAGEMENT_ONGOING -> onEngagementOngoing(
                visitor.visit(engagementState)
            )
            EngagementStateEvent.Type.ENGAGEMENT_ENDED -> {}
        }
    }

    private fun onEngagementOngoing(operator: Operator) {
        val name = operator.name
        val imageUrl = Utils.getOperatorImageUrl(operator)
        emitViewState { chatState.operatorConnected(name, imageUrl) }
    }

    private fun onOperatorConnected(operator: Operator) {
        val name = operator.name
        val imageUrl = Utils.getOperatorImageUrl(operator)
        operatorConnected(name, imageUrl)
    }

    private fun onOperatorChanged(operator: Operator) {
        val name = operator.name
        val imageUrl = Utils.getOperatorImageUrl(operator)
        operatorChanged(name, imageUrl)
    }

    private fun onTransferring() {
        emitChatItems {
            val items: MutableList<ChatItem> = chatState.chatItems.toMutableList()
            if (chatState.operatorStatusItem != null) {
                items.remove(chatState.operatorStatusItem)
            }
            items.add(OperatorStatusItem.TransferringStatusItem())
            emitViewState { chatState.transferring() }

            return@emitChatItems chatState.changeItems(items)
        }
    }

    fun overlayPermissionsDialogDismissed() {
        Logger.d(TAG, "overlayPermissionsDialogDismissed")
        dialogController.dismissCurrentDialog()
        emitViewState { chatState }
    }

    fun acceptUpgradeOfferClicked(offer: MediaUpgradeOffer?) {
        Logger.d(TAG, "upgradeToAudioClicked")
        messagesNotSeenHandler.chatUpgradeOfferAccepted()
        mediaUpgradeOfferRepository.acceptOffer(offer, Submitter.CHAT)
        dialogController.dismissCurrentDialog()
    }

    fun declineUpgradeOfferClicked(offer: MediaUpgradeOffer?) {
        Logger.d(TAG, "closeUpgradeDialogClicked")
        mediaUpgradeOfferRepository.declineOffer(offer, Submitter.CHAT)
        dialogController.dismissCurrentDialog()
    }

    fun navigateToCallSuccess() {
        Logger.d(TAG, "navigateToCallSuccess")
        emitViewState { chatState.setPendingNavigationType(null) }
    }

    @Synchronized
    private fun setState(state: ChatState): Boolean {
        if (chatState == state) return false
        chatState = state
        return true
    }

    private fun error(error: String) {
        Logger.e(TAG, error)
        dialogController.showUnexpectedErrorDialog()
        emitViewState { chatState.stop() }
    }

    private fun onOperatorMediaStateVideo() {
        Logger.d(TAG, "newOperatorMediaState: video")
        showVideoCallNotificationUseCase.execute()
    }

    private fun onOperatorMediaStateAudio() {
        Logger.d(TAG, "newOperatorMediaState: audio")
        showAudioCallNotificationUseCase.execute()
    }

    private fun onOperatorMediaStateUnknown() {
        Logger.d(TAG, "newOperatorMediaState: null")
        removeCallNotificationUseCase.execute()
    }

    private fun initMediaUpgradeCallback() {
        mediaUpgradeOfferRepositoryCallback = object : MediaUpgradeOfferRepositoryCallback {
            override fun newOffer(offer: MediaUpgradeOffer) {
                if (isChatViewPaused) return
                if (offer.video == MediaDirection.NONE && offer.audio == MediaDirection.TWO_WAY) {
                    // audio call
                    Logger.d(TAG, "audioUpgradeRequested")
                    if (chatState.isOperatorOnline) dialogController.showUpgradeAudioDialog(
                        offer,
                        chatState.formattedOperatorName
                    )
                } else if (offer.video == MediaDirection.TWO_WAY) {
                    // video call
                    Logger.d(TAG, "2 way videoUpgradeRequested")
                    if (chatState.isOperatorOnline) dialogController.showUpgradeVideoDialog2Way(
                        offer,
                        chatState.formattedOperatorName
                    )
                } else if (offer.video == MediaDirection.ONE_WAY) {
                    Logger.d(TAG, "1 way videoUpgradeRequested")
                    if (chatState.isOperatorOnline) dialogController.showUpgradeVideoDialog1Way(
                        offer,
                        chatState.formattedOperatorName
                    )
                }
            }

            override fun upgradeOfferChoiceSubmitSuccess(
                offer: MediaUpgradeOffer,
                submitter: Submitter
            ) {
                Logger.d(TAG, "upgradeOfferChoiceSubmitSuccess")
                if (submitter == Submitter.CHAT) {
                    val requestedMediaType: String =
                        if (offer.video != null && offer.video != MediaDirection.NONE) {
                            GliaWidgets.MEDIA_TYPE_VIDEO
                        } else {
                            GliaWidgets.MEDIA_TYPE_AUDIO
                        }
                    emitViewState { chatState.setPendingNavigationType(requestedMediaType) }
                    viewCallback?.apply {
                        navigateToCall(requestedMediaType)
                        Logger.d(TAG, "navigateToCall")
                    }
                }
            }

            override fun upgradeOfferChoiceDeclinedSuccess(
                submitter: Submitter
            ) {
                Logger.d(TAG, "upgradeOfferChoiceDeclinedSuccess")
            }
        }
    }

    private fun viewInitQueueing() {
        Logger.d(TAG, "viewInitQueueing")
        emitChatItems {
            val items: MutableList<ChatItem> = chatState.chatItems.toMutableList()
            if (chatState.operatorStatusItem != null) {
                items.remove(chatState.operatorStatusItem)
            }
            val operatorStatusItem = OperatorStatusItem.QueueingStatusItem(chatState.companyName)
            items.add(operatorStatusItem)
            emitViewState { chatState.queueingStarted(operatorStatusItem) }

            return@emitChatItems chatState.changeItems(items)
        }
    }

    private fun destroyView() {
        viewCallback?.apply {
            Logger.d(TAG, "destroyingView")
            destroyView()
        }
    }

    private fun minimizeView() {
        viewCallback?.minimizeView()
    }

    private fun operatorConnected(operatorName: String, profileImgUrl: String?) {
        emitChatItems {
            val items: MutableList<ChatItem> = chatState.chatItems.toMutableList()
            if (chatState.operatorStatusItem != null) {
                // remove previous operator status item
                val operatorStatusItemIndex = items.indexOf(chatState.operatorStatusItem)
                Logger.d(
                    TAG,
                    "operatorStatusItemIndex: " + operatorStatusItemIndex + ", size: " + items.size
                )
                items.remove(chatState.operatorStatusItem)
                items.add(
                    operatorStatusItemIndex,
                    OperatorStatusItem.OperatorFoundStatusItem(
                        chatState.companyName,
                        Utils.formatOperatorName(operatorName),
                        profileImgUrl
                    )
                )
            } else {
                items.add(
                    OperatorStatusItem.OperatorFoundStatusItem(
                        chatState.companyName,
                        Utils.formatOperatorName(operatorName),
                        profileImgUrl
                    )
                )
            }
            emitViewState {
                chatState
                    .operatorConnected(operatorName, profileImgUrl)
                    .setLiveChatState()
            }

            return@emitChatItems chatState.changeItems(items)
        }
    }

    private fun operatorChanged(operatorName: String, profileImgUrl: String?) {
        emitChatItems {
            val items: MutableList<ChatItem> = chatState.chatItems.toMutableList()
            val operatorStatusItem = OperatorStatusItem.OperatorJoinedStatusItem(
                chatState.companyName,
                Utils.formatOperatorName(operatorName),
                profileImgUrl
            )
            items.add(operatorStatusItem)

            return@emitChatItems chatState.changeItems(items)
        }
        emitViewState { chatState.operatorConnected(operatorName, profileImgUrl) }
    }

    private fun stop() {
        Logger.d(TAG, "Stop, engagement ended")
        disposable.add(
            cancelQueueTicketUseCase.execute()
                .subscribe({ queueForEngagementStopped() }) {
                    Logger.e(TAG, "cancelQueueTicketUseCase error: ${it.message}")
                }
        )
        endEngagementUseCase.execute()
        mediaUpgradeOfferRepository.stopAll()
        emitViewState { chatState.stop() }
    }

    private fun appendHistoryChatItem(
        currentChatItems: MutableList<ChatItem>,
        chatMessageInternal: ChatMessageInternal,
        isLastItem: Boolean
    ) {
        val message = chatMessageInternal.chatMessage
        if (message.senderType == Chat.Participant.VISITOR) {
            appendHistoryMessage(currentChatItems, message)
            addVisitorAttachmentItemsToChatItems(currentChatItems, message)
        } else if (message.senderType == Chat.Participant.OPERATOR) {
            appendOperatorMessage(currentChatItems, chatMessageInternal, isLastItem)
        }
    }

    private fun appendHistoryMessage(
        currentChatItems: MutableList<ChatItem>,
        message: ChatMessage
    ) {
        if (!message.content.isNullOrEmpty()) {
            currentChatItems.add(VisitorMessageItem.asHistoryItem(message))
        }
    }

    private fun appendMessageItem(
        currentChatItems: MutableList<ChatItem>,
        messageInternal: ChatMessageInternal
    ) {
        val message = messageInternal.chatMessage
        if (message.senderType == Chat.Participant.VISITOR) {
            appendSentMessage(currentChatItems, message)
            addVisitorAttachmentItemsToChatItems(currentChatItems, message)
        } else if (message.senderType == Chat.Participant.OPERATOR) {
            onOperatorMessageReceived(currentChatItems, messageInternal)
        }
    }

    private fun onOperatorMessageReceived(
        currentChatItems: MutableList<ChatItem>,
        messageInternal: ChatMessageInternal
    ) {
        appendOperatorMessage(currentChatItems, messageInternal, true)
        appendMessagesNotSeen()
    }

    private fun changeChatInputMode(currentChatItems: List<ChatItem>, message: ChatMessage) {
        val newInputMode = getChatInputMode(currentChatItems, message)
        if (chatState.chatInputMode != newInputMode) {
            emitViewState { chatState.chatInputModeChanged(newInputMode) }
        }
    }

    private fun getChatInputMode(
        currentChatItems: List<ChatItem>,
        message: ChatMessage
    ): ChatInputMode {
        val customCardChatInputMode =
            customCardInteractableUseCase.execute(currentChatItems, message)
        if (customCardChatInputMode != null) {
            return customCardChatInputMode
        }
        if (message.attachment is SingleChoiceAttachment) {
            val attachment = message.attachment as SingleChoiceAttachment
            if (attachment.options != null) {
                return ChatInputMode.SINGLE_CHOICE_CARD
            }
        }
        return ChatInputMode.ENABLED
    }

    private fun addVisitorAttachmentItemsToChatItems(
        currentChatItems: MutableList<ChatItem>,
        chatMessage: ChatMessage
    ) {
        val attachment = chatMessage.attachment
        if (attachment is FilesAttachment) {
            val files = attachment.files
            for (file in files) {
                currentChatItems.add(
                    VisitorAttachmentItem.fromAttachmentFile(
                        chatMessage.id,
                        chatMessage.timestamp,
                        file
                    )
                )
            }
        }
    }

    private fun appendSentMessage(items: MutableList<ChatItem>, message: ChatMessage) {
        if (!message.content.isNullOrEmpty()) {
            items.add(VisitorMessageItem.asNewMessage(message))
        }
    }

    private fun appendMessagesNotSeen() {
        emitViewState {
            chatState.messagesNotSeenChanged(
                if (chatState.isChatInBottom) 0 else chatState.messagesNotSeen + 1
            )
        }
    }

    private fun initGliaEngagementObserving() {
        getEngagementUseCase.execute(this)
        engagementEndUseCase.execute(this)
    }

    private fun changeDeliveredIndex(
        currentChatItems: MutableList<ChatItem>,
        message: VisitorMessage
    ) {
        // "Delivered" status only applies to visitor messages
        if (message.senderType != Chat.Participant.VISITOR) return
        val messageId = message.id
        var foundDelivered = false
        for (i in currentChatItems.indices.reversed()) {
            val currentChatItem = currentChatItems[i]
            if (currentChatItem is VisitorMessageItem) {
                val itemId = currentChatItem.id
                if (itemId == VisitorMessageItem.HISTORY_ID) {
                    // we reached the history items no point in going searching further
                    break
                } else if (!foundDelivered && itemId == messageId) {
                    foundDelivered = true
                    currentChatItems[i] = VisitorMessageItem.editDeliveredStatus(
                        currentChatItem,
                        true
                    )
                } else if (currentChatItem.isShowDelivered) {
                    currentChatItems[i] = VisitorMessageItem.editDeliveredStatus(
                        currentChatItem,
                        false
                    )
                }
            } else if (currentChatItem is VisitorAttachmentItem) {
                if (!foundDelivered && currentChatItem.id == messageId) {
                    foundDelivered = true
                    setDelivered(currentChatItems, i, currentChatItem, true)
                } else if (currentChatItem.showDelivered) {
                    setDelivered(currentChatItems, i, currentChatItem, false)
                }
            }
        }
    }

    private fun setDelivered(
        currentChatItems: MutableList<ChatItem>,
        i: Int,
        item: VisitorAttachmentItem,
        delivered: Boolean
    ) {
        currentChatItems[i] = VisitorAttachmentItem.editDeliveredStatus(item, delivered)
    }

    private fun appendOperatorMessage(
        currentChatItems: MutableList<ChatItem>,
        chatMessageInternal: ChatMessageInternal,
        isLastItem: Boolean
    ) {
        setLastOperatorItemChatHeadVisibility(
            currentChatItems, isOperatorChanged(currentChatItems, chatMessageInternal)
        )
        appendOperatorOrCustomCardItem(currentChatItems, chatMessageInternal, isLastItem)
        appendOperatorAttachmentItems(currentChatItems, chatMessageInternal)
        setLastOperatorItemChatHeadVisibility(currentChatItems, true)

        if (isLastItem) {
            changeChatInputMode(currentChatItems, chatMessageInternal.chatMessage)
        }
    }

    private fun isOperatorChanged(
        currentChatItems: List<ChatItem>,
        chatMessageInternal: ChatMessageInternal
    ): Boolean {
        if (currentChatItems.isEmpty()) return false
        val lastItem = currentChatItems.last()
        if (lastItem is OperatorChatItem) {
            return !chatMessageInternal
                .operatorId
                .filter { it == lastItem.operatorId }
                .isPresent
        }
        return false
    }

    private fun setLastOperatorItemChatHeadVisibility(
        currentChatItems: MutableList<ChatItem>,
        showChatHead: Boolean
    ) {
        if (currentChatItems.isNotEmpty()) {
            when (val lastItem = currentChatItems.last()) {
                is ResponseCardItem -> {
                    currentChatItems.remove(lastItem)
                    currentChatItems.add(
                        ResponseCardItem(
                            lastItem.id,
                            lastItem.operatorName,
                            lastItem.operatorProfileImgUrl,
                            showChatHead,
                            lastItem.content,
                            lastItem.operatorId,
                            lastItem.timestamp,
                            lastItem.singleChoiceOptions,
                            lastItem.choiceCardImageUrl
                        )
                    )
                }
                is OperatorMessageItem -> {
                    currentChatItems.remove(lastItem)
                    currentChatItems.add(
                        OperatorMessageItem(
                            lastItem.id,
                            lastItem.operatorName,
                            lastItem.operatorProfileImgUrl,
                            showChatHead,
                            lastItem.content,
                            lastItem.operatorId,
                            lastItem.timestamp
                        )
                    )
                }
                is OperatorAttachmentItem -> {
                    currentChatItems.remove(lastItem)
                    currentChatItems.add(
                        OperatorAttachmentItem(
                            lastItem.id,
                            lastItem.viewType,
                            showChatHead,
                            lastItem.attachmentFile,
                            lastItem.operatorProfileImgUrl,
                            false,
                            false,
                            lastItem.operatorId,
                            lastItem.messageId,
                            lastItem.timestamp
                        )
                    )
                }
                is CustomCardItem -> {
                    currentChatItems.remove(lastItem)
                    currentChatItems.add(
                        CustomCardItem(
                            lastItem.message,
                            lastItem.viewType
                        )
                    )
                }
            }
        }
    }

    private fun appendOperatorAttachmentItems(
        currentChatItems: MutableList<ChatItem>,
        messageInternal: ChatMessageInternal
    ) {
        val message = messageInternal.chatMessage
        val attachment = message.attachment
        if (attachment is FilesAttachment) {
            val files = attachment.files
            for (file in files) {
                val viewType: Int = if (file.contentType.startsWith("image")) {
                    ChatAdapter.OPERATOR_IMAGE_VIEW_TYPE
                } else {
                    ChatAdapter.OPERATOR_FILE_VIEW_TYPE
                }
                currentChatItems.add(
                    OperatorAttachmentItem(
                        message.id,
                        viewType,
                        false,
                        file,
                        messageInternal.operatorImageUrl.orElse(chatState.operatorProfileImgUrl),
                        false,
                        false,
                        messageInternal.operatorId.orElse(UUID.randomUUID().toString()),
                        message.id,
                        message.timestamp
                    )
                )
            }
        }
    }

    private fun appendOperatorOrCustomCardItem(
        currentChatItems: MutableList<ChatItem>,
        messageInternal: ChatMessageInternal,
        isLastItem: Boolean
    ) {
        val message = messageInternal.chatMessage
        if (message.content != EMPTY_MESSAGE) {
            val viewType = customCardAdapterTypeUseCase.execute(message)
            if (viewType != null) {
                appendCustomCardItem(currentChatItems, message, viewType)
            } else {
                appendOperatorMessageItem(currentChatItems, messageInternal, isLastItem)
            }
        }
    }

    private fun appendCustomCardItem(
        currentChatItems: MutableList<ChatItem>,
        message: ChatMessage,
        viewType: Int
    ) {
        val customCardType = customCardTypeUseCase.execute(viewType) ?: return
        if (customCardShouldShowUseCase.execute(message, customCardType, true)) {
            currentChatItems.add(CustomCardItem(message, viewType))
        }
        val visitorCardResponseItem = VisitorMessageItem.asCardResponseItem(message)
        if (!visitorCardResponseItem.message.isNullOrEmpty()) {
            currentChatItems.add(visitorCardResponseItem)
        }
    }

    private fun appendOperatorMessageItem(
        currentChatItems: MutableList<ChatItem>,
        messageInternal: ChatMessageInternal,
        isLastItem: Boolean
    ) {
        val message = messageInternal.chatMessage
        val messageAttachment = message.attachment
        val singleChoiceAttachmentOptions = getSingleChoiceAttachmentOptions(messageAttachment)
        val operatorName = messageInternal.operatorName.orElse(chatState.operatorName)
        val operatorImage = messageInternal.operatorImageUrl.orElse(chatState.operatorProfileImgUrl)
        val operatorId = messageInternal.operatorId.orElse(UUID.randomUUID().toString())

        val item = if (singleChoiceAttachmentOptions.isNullOrEmpty() || !isLastItem) {
            OperatorMessageItem(
                message.id,
                operatorName,
                operatorImage,
                false,
                message.content,
                operatorId,
                message.timestamp
            )
        } else {
            ResponseCardItem(
                message.id,
                operatorName,
                operatorImage,
                false,
                message.content,
                operatorId,
                message.timestamp,
                singleChoiceAttachmentOptions,
                getSingleChoiceAttachmentImgUrl(messageAttachment)
            )
        }

        currentChatItems.add(item)
    }

    private fun getSingleChoiceAttachmentImgUrl(attachment: MessageAttachment): String? {
        var imageUrl: String? = null
        if (attachment is SingleChoiceAttachment) {
            val optionalImageUrl = attachment.imageUrl
            if (optionalImageUrl.isPresent) {
                imageUrl = optionalImageUrl.get()
            }
        }
        return imageUrl
    }

    private fun getSingleChoiceAttachmentOptions(attachment: MessageAttachment?): List<SingleChoiceOption>? {
        return (attachment as? SingleChoiceAttachment)?.options?.toList()
    }

    private fun startTimer() {
        Logger.d(TAG, "startTimer")
        callTimer.startNew(Constants.CALL_TIMER_DELAY, Constants.CALL_TIMER_INTERVAL_VALUE)
    }

    private fun upgradeMediaItem() {
        Logger.d(TAG, "upgradeMediaItem")
        emitChatItems {
            val newItems: MutableList<ChatItem> = chatState.chatItems.toMutableList()
            val mediaUpgradeStartedTimerItem = MediaUpgradeStartedTimerItem(
                MediaUpgradeStartedTimerItem.Type.VIDEO,
                chatState.mediaUpgradeStartedTimerItem.time
            )
            newItems.remove(chatState.mediaUpgradeStartedTimerItem)
            newItems.add(mediaUpgradeStartedTimerItem)

            return@emitChatItems chatState.changeTimerItem(newItems, mediaUpgradeStartedTimerItem)
        }
    }

    private fun createNewTimerCallback() {
        timerStatusListener?.also { callTimer.removeFormattedValueListener(it) }
        timerStatusListener = object : FormattedTimerStatusListener {
            override fun onNewFormattedTimerValue(formatedValue: String) {
                emitChatItems {
                    if (chatState.isMediaUpgradeStarted) {
                        val index = chatState.chatItems.indexOf(chatState.mediaUpgradeStartedTimerItem)
                        if (index != -1) {
                            val newItems: MutableList<ChatItem> = chatState.chatItems.toMutableList()
                            val type = chatState.mediaUpgradeStartedTimerItem.type
                            newItems.removeAt(index)
                            val mediaUpgradeStartedTimerItem =
                                MediaUpgradeStartedTimerItem(type, formatedValue)
                            newItems.add(index, mediaUpgradeStartedTimerItem)

                            return@emitChatItems chatState.changeTimerItem(
                                newItems,
                                mediaUpgradeStartedTimerItem
                            )
                        }
                    }
                    return@emitChatItems null
                }
            }

            override fun onFormattedTimerCancelled() {
                if (chatState.isMediaUpgradeStarted &&
                    chatState.chatItems.contains(chatState.mediaUpgradeStartedTimerItem)
                ) {
                    emitChatItems {
                        val newItems: MutableList<ChatItem> = chatState.chatItems.toMutableList()
                        newItems.remove(chatState.mediaUpgradeStartedTimerItem)

                        return@emitChatItems chatState.changeTimerItem(newItems, null)
                    }
                }
            }
        }
    }

    fun singleChoiceOptionClicked(
        item: ResponseCardItem,
        selectedOption: SingleChoiceOption
    ) {
        Logger.d(TAG, "singleChoiceOptionClicked, id: ${item.id}")
        sendMessageUseCase.execute(selectedOption.asSingleChoiceResponse(), sendMessageCallback)
        val choiceCardItemWithSelected = OperatorMessageItem(
            item.id,
            item.operatorName,
            item.operatorProfileImgUrl,
            item.showChatHead,
            item.content,
            item.operatorId,
            item.timestamp
        )
        emitChatItems {
            val modifiedItems: MutableList<ChatItem> = chatState.chatItems.toMutableList()
            val indexInList = modifiedItems.indexOf(item)
            modifiedItems.remove(item)
            if (indexInList >= 0) {
                modifiedItems.add(indexInList, choiceCardItemWithSelected)
            } else {
                Logger.e(TAG, "singleChoiceOptionClicked, ResponseCardItem is not in the list!")
            }

            return@emitChatItems chatState.changeItems(modifiedItems)
        }
    }

    fun sendCustomCardResponse(messageId: String, text: String?, value: String?) {
        sendMessageUseCase.execute(messageId, text, value, sendMessageCallback)
        emitChatItems {
            chatState.chatItems
                .firstOrNull { messageId == it.id }
                ?.let { it as CustomCardItem }
                ?.also {
                    val customCardType = customCardTypeUseCase.execute(it.viewType) ?: return@also
                    val currentMessage = it.message
                    val showCustomCard = customCardShouldShowUseCase.execute(
                        currentMessage,
                        customCardType,
                        false
                    )
                    val chatItems: MutableList<ChatItem> = chatState.chatItems.toMutableList()
                    var indexForResponseMessage = chatItems.indexOf(it)
                    if (showCustomCard) {
                        // The index for the response message should be next to the card
                        // when the card is shown after the response.
                        indexForResponseMessage += 1
                    } else {
                        // If the card should be hidden after the response, we remove it from the
                        // item list. The response message will be shown instead of the card.
                        chatItems.remove(it)
                    }
                    chatItems.add(
                        indexForResponseMessage,
                        VisitorMessageItem.asUnsentCardResponse(text)
                    )
                    return@emitChatItems chatState.changeItems(chatItems)
                }
            return@emitChatItems null
        }
    }

    private fun updateCustomCard(message: OperatorMessage) {
        chatState.chatItems
            .firstOrNull { message.id == it.id }
            ?.let { it as CustomCardItem }
            ?.also {
                emitChatItems {
                    val chatItems: MutableList<ChatItem> = chatState.chatItems.toMutableList()
                    updateCustomCardSelectedOption(it, message, chatItems)

                    return@emitChatItems chatState.changeItems(chatItems)
                }
            }
    }

    private fun updateCustomCardSelectedOption(
        currentCustomCardItem: CustomCardItem,
        updatedMessage: ChatMessage,
        chatItems: MutableList<ChatItem>
    ) {
        val updatedCustomCardItem = CustomCardItem(
            updatedMessage,
            currentCustomCardItem.viewType
        )
        val indexInList = chatItems.indexOf(currentCustomCardItem)
        chatItems.removeAt(indexInList)
        chatItems.add(indexInList, updatedCustomCardItem)
    }

    fun onRecyclerviewPositionChanged(isBottom: Boolean) {
        if (isBottom) {
            Logger.d(TAG, "onRecyclerviewPositionChanged, isBottom = true")
            emitViewState { chatState.isInBottomChanged(true).messagesNotSeenChanged(0) }
        } else {
            Logger.d(TAG, "onRecyclerviewPositionChanged, isBottom = false")
            emitViewState { chatState.isInBottomChanged(false) }
        }
    }

    fun newMessagesIndicatorClicked() {
        Logger.d(TAG, "newMessagesIndicatorClicked")
        viewCallback?.smoothScrollToBottom()
    }

    private fun loadChatHistory() {
        val historyDisposable = loadHistoryUseCase.execute()
            .doOnError { error(it) }
            .doOnSuccess { historyLoaded(it) }
            .subscribe()
        disposable.add(historyDisposable)
    }

    @Synchronized
    private fun historyLoaded(messages: List<ChatMessageInternal>) {
        Logger.d(TAG, "historyLoaded")
        val currentItems: MutableList<ChatItem> = chatState.chatItems.toMutableList()
        val newItems = removeDuplicates(currentItems, messages)

        when {
            !newItems.isNullOrEmpty() -> submitHistoryItems(newItems, currentItems)
            !chatState.engagementRequested && !isSecureEngagement -> queueForEngagement()
            else -> Logger.d(TAG, "Opened empty Secure Conversations chat")
        }

        initGliaEngagementObserving()
    }

    private fun submitHistoryItems(
        newItems: List<ChatMessageInternal>, currentItems: MutableList<ChatItem>
    ) {
        newItems.forEachIndexed { index, message ->
            appendHistoryChatItem(currentItems, message, index == newItems.lastIndex)
        }

        val sortedItems = currentItems.sortedBy { (it as? LinkedChatItem)?.timestamp }

        val newState = if (isSecureEngagementUseCase() && !isQueueingOrOngoingEngagement) {
            chatState.changeItems(sortedItems)
        } else {
            chatState.historyLoaded(sortedItems)
        }

        if (isSecureEngagement) {
            disposable.add(
                getUnreadMessagesCountWithTimeoutUseCase().subscribe { count -> tryToAddNewMessagesDivider(count, newState) }
            )
        } else {
            emitChatItems { newState }
        }

    }

    @Synchronized
    private fun tryToAddNewMessagesDivider(count: Int, newState: ChatState) {
        if (count < 1) {
            emitChatItems { newState }
            return
        }

        val index = newState.chatItems.count().minus(count).coerceAtLeast(0)

        val newItems = newState.chatItems.toMutableList().apply {
            add(index, NewMessagesItem)
        }

        emitChatItems { newState.changeItems(newItems) }

        markMessagesReadWithDelay()
    }

    private fun markMessagesReadWithDelay() {
        disposable.add(
            markMessagesReadWithDelayUseCase().subscribe({
                removeNewMessagesDivider()
            }, {
                Logger.e(TAG, "Marking messages read failed", it)
            })
        )
    }

    private fun removeNewMessagesDivider() {
        emitChatItems { chatState.run { changeItems(chatItems - NewMessagesItem) } }
    }

    init {
        Logger.d(TAG, "constructor")

        // viewCallback is accessed from multiple threads
        // and must be protected from race condition
        synchronized(this) { viewCallback = chatViewCallback }

        chatState = ChatState.Builder()
            .setOperatorName(null)
            .setCompanyName(null)
            .setQueueId(null)
            .setVisitorContextAssetId(null)
            .setIsVisible(false)
            .setIntegratorChatStarted(false)
            .setChatItems(ArrayList())
            .setLastTypedText(EMPTY_MESSAGE)
            .setChatInputMode(ChatInputMode.ENABLED_NO_ENGAGEMENT)
            .setIsAttachmentButtonNeeded(false)
            .setIsAttachmentAllowed(true)
            .setIsChatInBottom(true)
            .setMessagesNotSeen(0)
            .setPendingNavigationType(null)
            .setUnsentMessages(ArrayList())
            .setIsOperatorTyping(false)
            .createChatState()
    }

    @VisibleForTesting
    fun removeDuplicates(
        oldHistory: List<ChatItem>?, newHistory: List<ChatMessageInternal>?
    ): List<ChatMessageInternal>? {
        return if (newHistory.isNullOrEmpty() || oldHistory.isNullOrEmpty()) {
            newHistory
        } else newHistory.filter { isNewMessage(oldHistory, it.chatMessage) }
    }

    @VisibleForTesting
    fun isNewMessage(oldHistory: List<ChatItem>?, newMessage: ChatMessage): Boolean =
        oldHistory
            ?.filterIsInstance<LinkedChatItem>()
            ?.any { oldMessage -> oldMessage.messageId != null && oldMessage.messageId == newMessage.id }
            ?.not()
            ?: true

    private fun error(error: Throwable?) {
        error?.also { error(it.toString()) }
    }

    override fun newEngagementLoaded(engagement: OmnicoreEngagement) {
        Logger.d(TAG, "newEngagementLoaded")
        subscribeToMessages()
        onOperatorTypingUseCase.execute { onOperatorTyping(it) }
        addOperatorMediaStateListenerUseCase.execute(operatorMediaStateListener)
        mediaUpgradeOfferRepository.startListening()
        if (chatState.unsentMessages.isNotEmpty()) {
            sendMessageUseCase.execute(chatState.unsentMessages[0].message, sendMessageCallback)
            Logger.d(TAG, "unsentMessage sent!")
        }
        emitViewState { chatState.engagementStarted() }
        // Loading chat history again on engagement start in case it was an-authenticated visitor that restored ongoing engagement
        // Currently there is no direct way to know if Visitor is authenticated.
        loadChatHistory()
    }

    override fun engagementEnded() {
        Logger.d(TAG, "engagementEnded")
        stop()
    }

    override fun onSurveyLoaded(survey: Survey?) {
        Logger.d(TAG, "newSurveyLoaded")
        if (viewCallback != null && survey != null) {
            viewCallback!!.navigateToSurvey(survey)
            Dependencies.getControllerFactory().destroyControllers()
        } else if (!isVisitorEndEngagement) {
            dialogController.showEngagementEndedDialog()
        } else {
            Dependencies.getControllerFactory().destroyControllers()
        }
    }

    private fun onNewOperatorMediaState(operatorMediaState: OperatorMediaState) {
        Logger.d(TAG, "newOperatorMediaState: $operatorMediaState")
        if (chatState.isAudioCallStarted && operatorMediaState.video != null) {
            upgradeMediaItem()
        } else if (!chatState.isMediaUpgradeStarted) {
            addMediaUpgradeItemToChatItems(operatorMediaState)
            if (!callTimer.isRunning) {
                startTimer()
            }
        }
        if (operatorMediaState.video != null) {
            onOperatorMediaStateVideo()
        } else if (operatorMediaState.audio != null) {
            onOperatorMediaStateAudio()
        } else {
            onOperatorMediaStateUnknown()
        }
    }

    private fun addMediaUpgradeItemToChatItems(operatorMediaState: OperatorMediaState) {
        var type: MediaUpgradeStartedTimerItem.Type? = null
        if (operatorMediaState.video == null && operatorMediaState.audio != null) {
            Logger.d(TAG, "starting audio timer")
            type = MediaUpgradeStartedTimerItem.Type.AUDIO
        } else if (operatorMediaState.video != null) {
            Logger.d(TAG, "starting video timer")
            type = MediaUpgradeStartedTimerItem.Type.VIDEO
        }
        emitChatItems {
            val newItems: MutableList<ChatItem> = chatState.chatItems.toMutableList()
            val mediaUpgradeStartedTimerItem = MediaUpgradeStartedTimerItem(type, Utils.toMmSs(0))
            newItems.add(mediaUpgradeStartedTimerItem)

            return@emitChatItems chatState.changeTimerItem(newItems, mediaUpgradeStartedTimerItem)
        }
    }

    fun notificationsDialogDismissed() {
        dialogController.dismissCurrentDialog()
    }

    private fun queueForEngagementStarted() {
        Logger.d(TAG, "queueForEngagementStarted")
        if (chatState.isOperatorOnline) {
            return
        }
        observeQueueTicketState()
        viewInitQueueing()
    }

    private fun queueForEngagementStopped() {
        Logger.d(TAG, "queueForEngagementStopped")
    }

    private fun queueForEngagementError(exception: Throwable?) {

        (exception as? GliaException)?.also {
            Logger.e(TAG, it.toString())
            when (it.cause) {
                GliaException.Cause.QUEUE_CLOSED, GliaException.Cause.QUEUE_FULL -> dialogController.showNoMoreOperatorsAvailableDialog()
                else -> dialogController.showUnexpectedErrorDialog()
            }
            emitViewState { chatState.stop() }
        } ?: (exception as? QueueingOngoingException)?.also {
            queueForEngagementStarted()
        }

    }

    fun onRemoveAttachment(attachment: FileAttachment) {
        removeFileAttachmentUseCase.execute(attachment)
    }

    fun onAttachmentReceived(file: FileAttachment) {
        addFileToAttachmentAndUploadUseCase
            .execute(file, object : AddFileToAttachmentAndUploadUseCase.Listener {
                override fun onFinished() {
                    Logger.d(TAG, "fileUploadFinished")
                }

                override fun onStarted() {
                    Logger.d(TAG, "fileUploadStarted")
                }

                override fun onError(ex: Exception) {
                    Logger.e(TAG, "Upload file failed: " + ex.message)
                }

                override fun onSecurityCheckStarted() {
                    Logger.d(TAG, "fileUploadSecurityCheckStarted")
                }

                override fun onSecurityCheckFinished(scanResult: EngagementFile.ScanResult?) {
                    Logger.d(TAG, "fileUploadSecurityCheckFinished result=$scanResult")
                }
            })
    }

    fun onFileDownloadClicked(attachmentFile: AttachmentFile) {
        disposable.add(
            downloadFileUseCase
                .execute(attachmentFile)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ fileDownloadSuccess(attachmentFile) }) {
                    fileDownloadError(attachmentFile, it)
                }
        )
    }

    private fun fileDownloadError(attachmentFile: AttachmentFile, error: Throwable) {
        viewCallback?.fileDownloadError(attachmentFile, error)
    }

    private fun fileDownloadSuccess(attachmentFile: AttachmentFile) {
        viewCallback?.fileDownloadSuccess(attachmentFile)
    }

    private fun updateAllowFileSendState() {
        siteInfoUseCase.execute { siteInfo: SiteInfo?, _ ->
            onSiteInfoReceived(siteInfo)
        }
    }

    private fun onSiteInfoReceived(siteInfo: SiteInfo?) {
        emitViewState {
            chatState.allowSendAttachmentStateChanged(
                siteInfo == null || siteInfo.allowedFileSenders.isVisitorAllowed
            )
        }
    }

    private fun observeQueueTicketState() {
        Logger.d(TAG, "observeQueueTicketState")
        disposable.add(
            ticketStateChangeToUnstaffedUseCase
                .execute()
                .subscribe({ dialogController.showNoMoreOperatorsAvailableDialog() }) {
                    Logger.e(TAG, "Error happened while observing queue state : $it")
                }
        )
    }

    companion object {
        private val TAG = ChatController::class.java.simpleName
        private const val EMPTY_MESSAGE = ""
    }
}
