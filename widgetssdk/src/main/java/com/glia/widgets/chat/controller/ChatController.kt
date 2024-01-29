package com.glia.widgets.chat.controller

import android.net.Uri
import android.view.View
import com.glia.androidsdk.GliaException
import com.glia.androidsdk.Operator
import com.glia.androidsdk.chat.AttachmentFile
import com.glia.androidsdk.chat.SingleChoiceAttachment
import com.glia.androidsdk.chat.SingleChoiceOption
import com.glia.androidsdk.chat.VisitorMessage
import com.glia.androidsdk.comms.MediaDirection
import com.glia.androidsdk.comms.MediaState
import com.glia.androidsdk.comms.MediaUpgradeOffer
import com.glia.androidsdk.engagement.EngagementFile
import com.glia.androidsdk.site.SiteInfo
import com.glia.widgets.Constants
import com.glia.widgets.GliaWidgets
import com.glia.widgets.chat.ChatContract
import com.glia.widgets.chat.ChatManager
import com.glia.widgets.chat.ChatType
import com.glia.widgets.chat.ChatView
import com.glia.widgets.chat.domain.DecideOnQueueingUseCase
import com.glia.widgets.chat.domain.GliaSendMessagePreviewUseCase
import com.glia.widgets.chat.domain.GliaSendMessageUseCase
import com.glia.widgets.chat.domain.IsAuthenticatedUseCase
import com.glia.widgets.chat.domain.IsFromCallScreenUseCase
import com.glia.widgets.chat.domain.IsSecureConversationsChatAvailableUseCase
import com.glia.widgets.chat.domain.IsShowSendButtonUseCase
import com.glia.widgets.chat.domain.SiteInfoUseCase
import com.glia.widgets.chat.domain.UpdateFromCallScreenUseCase
import com.glia.widgets.chat.domain.gva.DetermineGvaButtonTypeUseCase
import com.glia.widgets.chat.model.ChatItem
import com.glia.widgets.chat.model.ChatState
import com.glia.widgets.chat.model.CustomCardChatItem
import com.glia.widgets.chat.model.Gva
import com.glia.widgets.chat.model.GvaButton
import com.glia.widgets.chat.model.OperatorMessageItem
import com.glia.widgets.chat.model.Unsent
import com.glia.widgets.core.dialog.DialogContract
import com.glia.widgets.core.dialog.domain.ConfirmationDialogLinksUseCase
import com.glia.widgets.core.dialog.domain.IsShowOverlayPermissionRequestDialogUseCase
import com.glia.widgets.core.dialog.model.Link
import com.glia.widgets.core.engagement.domain.ConfirmationDialogUseCase
import com.glia.widgets.core.engagement.domain.SetEngagementConfigUseCase
import com.glia.widgets.core.engagement.domain.UpdateOperatorDefaultImageUrlUseCase
import com.glia.widgets.core.fileupload.domain.AddFileAttachmentsObserverUseCase
import com.glia.widgets.core.fileupload.domain.AddFileToAttachmentAndUploadUseCase
import com.glia.widgets.core.fileupload.domain.GetFileAttachmentsUseCase
import com.glia.widgets.core.fileupload.domain.RemoveFileAttachmentObserverUseCase
import com.glia.widgets.core.fileupload.domain.RemoveFileAttachmentUseCase
import com.glia.widgets.core.fileupload.domain.SupportedFileCountCheckUseCase
import com.glia.widgets.core.fileupload.model.FileAttachment
import com.glia.widgets.core.notification.domain.CallNotificationUseCase
import com.glia.widgets.core.secureconversations.domain.IsSecureEngagementUseCase
import com.glia.widgets.di.Dependencies
import com.glia.widgets.engagement.EngagementUpdateState
import com.glia.widgets.engagement.State
import com.glia.widgets.engagement.domain.AcceptMediaUpgradeOfferUseCase
import com.glia.widgets.engagement.domain.DeclineMediaUpgradeOfferUseCase
import com.glia.widgets.engagement.domain.EndEngagementUseCase
import com.glia.widgets.engagement.domain.EngagementStateUseCase
import com.glia.widgets.engagement.domain.EnqueueForEngagementUseCase
import com.glia.widgets.engagement.domain.IsCurrentEngagementCallVisualizerUseCase
import com.glia.widgets.engagement.domain.IsQueueingOrEngagementUseCase
import com.glia.widgets.engagement.domain.MediaUpgradeOfferUseCase
import com.glia.widgets.engagement.domain.OperatorMediaUseCase
import com.glia.widgets.engagement.domain.OperatorTypingUseCase
import com.glia.widgets.filepreview.domain.usecase.DownloadFileUseCase
import com.glia.widgets.filepreview.domain.usecase.IsFileReadyForPreviewUseCase
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.TAG
import com.glia.widgets.helper.TimeCounter
import com.glia.widgets.helper.TimeCounter.FormattedTimerStatusListener
import com.glia.widgets.helper.formattedName
import com.glia.widgets.helper.imageUrl
import com.glia.widgets.helper.isValid
import com.glia.widgets.helper.unSafeSubscribe
import com.glia.widgets.view.MessagesNotSeenHandler
import com.glia.widgets.view.MinimizeHandler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.Observer

internal class ChatController(
    private val callTimer: TimeCounter,
    private val minimizeHandler: MinimizeHandler,
    private val dialogController: DialogContract.Controller,
    private val messagesNotSeenHandler: MessagesNotSeenHandler,
    private val callNotificationUseCase: CallNotificationUseCase,
    private val onOperatorTypingUseCase: OperatorTypingUseCase,
    private val sendMessagePreviewUseCase: GliaSendMessagePreviewUseCase,
    private val sendMessageUseCase: GliaSendMessageUseCase,
    private val endEngagementUseCase: EndEngagementUseCase,
    private val addFileToAttachmentAndUploadUseCase: AddFileToAttachmentAndUploadUseCase,
    private val addFileAttachmentsObserverUseCase: AddFileAttachmentsObserverUseCase,
    private val removeFileAttachmentObserverUseCase: RemoveFileAttachmentObserverUseCase,
    private val getFileAttachmentsUseCase: GetFileAttachmentsUseCase,
    private val removeFileAttachmentUseCase: RemoveFileAttachmentUseCase,
    private val supportedFileCountCheckUseCase: SupportedFileCountCheckUseCase,
    private val isShowSendButtonUseCase: IsShowSendButtonUseCase,
    private val isShowOverlayPermissionRequestDialogUseCase: IsShowOverlayPermissionRequestDialogUseCase,
    private val downloadFileUseCase: DownloadFileUseCase,
    private val siteInfoUseCase: SiteInfoUseCase,
    private val isFromCallScreenUseCase: IsFromCallScreenUseCase,
    private val updateFromCallScreenUseCase: UpdateFromCallScreenUseCase,
    private val isSecureEngagementUseCase: IsSecureEngagementUseCase,
    private val engagementConfigUseCase: SetEngagementConfigUseCase,
    private val isSecureEngagementAvailableUseCase: IsSecureConversationsChatAvailableUseCase,
    private val isCurrentEngagementCallVisualizerUseCase: IsCurrentEngagementCallVisualizerUseCase,
    private val isFileReadyForPreviewUseCase: IsFileReadyForPreviewUseCase,
    private val determineGvaButtonTypeUseCase: DetermineGvaButtonTypeUseCase,
    private val isAuthenticatedUseCase: IsAuthenticatedUseCase,
    private val updateOperatorDefaultImageUrlUseCase: UpdateOperatorDefaultImageUrlUseCase,
    private val confirmationDialogUseCase: ConfirmationDialogUseCase,
    private val confirmationDialogLinksUseCase: ConfirmationDialogLinksUseCase,
    private val chatManager: ChatManager,
    private val engagementStateUseCase: EngagementStateUseCase,
    private val operatorMediaUseCase: OperatorMediaUseCase,
    private val mediaUpgradeOfferUseCase: MediaUpgradeOfferUseCase,
    private val acceptMediaUpgradeOfferUseCase: AcceptMediaUpgradeOfferUseCase,
    private val declineMediaUpgradeOfferUseCase: DeclineMediaUpgradeOfferUseCase,
    private val isQueueingOrEngagementUseCase: IsQueueingOrEngagementUseCase,
    private val enqueueForEngagementUseCase: EnqueueForEngagementUseCase,
    private val decideOnQueueingUseCase: DecideOnQueueingUseCase
) : ChatContract.Controller {
    private var backClickedListener: ChatView.OnBackClickedListener? = null
    private var view: ChatContract.View? = null
    private var timerStatusListener: FormattedTimerStatusListener? = null

    private val disposable = CompositeDisposable()
    private val mediaUpgradeDisposable = CompositeDisposable()

    private val sendMessageCallback: GliaSendMessageUseCase.Listener = object : GliaSendMessageUseCase.Listener {
        override fun messageSent(message: VisitorMessage?) {
            Logger.d(TAG, "messageSent: $message, id: ${message?.id}")
            message?.takeIf { it.isValid() }?.also { chatManager.onChatAction(ChatManager.Action.MessageSent(it)) }
            scrollChatToBottom()
        }

        override fun onMessageValidated() {
            view?.clearMessageInput()
            emitViewState { chatState.setLastTypedText("").setShowSendButton(isShowSendButtonUseCase("")) }
        }

        override fun errorOperatorNotOnline(message: Unsent) {
            onSendMessageOperatorOffline(message)
        }

        override fun error(ex: GliaException, message: Unsent) {
            onMessageSendError(ex, message)
        }
    }


    @Volatile
    private var isChatViewPaused = false

    override var photoCaptureFileUri: Uri? = null

    private val fileAttachmentObserver = Observer { _, _ ->
        view?.apply {
            emitUploadAttachments(getFileAttachmentsUseCase.execute())
            emitViewState {
                chatState.setShowSendButton(isShowSendButtonUseCase(chatState.lastTypedText))
                    .setIsAttachmentButtonEnabled(supportedFileCountCheckUseCase.execute())
            }
        }
    }

    @Volatile
    private var chatState: ChatState

    private val isSecureEngagement get() = isSecureEngagementUseCase()

    private val isQueueingOrOngoingEngagement get() = isQueueingOrEngagementUseCase()

    override val isChatVisible: Boolean
        get() = chatState.isVisible

    init {
        Logger.d(TAG, "constructor")
        chatState = ChatState()
        subscribeToEngagement()
        decideOnQueueingUseCase().unSafeSubscribe(::enqueueForEngagement)
    }

    override fun initChat(companyName: String?, queueId: String?, visitorContextAssetId: String?, chatType: ChatType) {
        val queueIds = if (queueId != null) arrayOf(queueId) else emptyArray()
        engagementConfigUseCase(chatType, queueIds)
        updateOperatorDefaultImageUrlUseCase()

        ensureSecureMessagingAvailable()

        if (chatState.integratorChatStarted || dialogController.isShowingUnexpectedErrorDialog) {
            if (isSecureEngagement) {
                emitViewState { chatState.setSecureMessagingState() }
            }
            chatManager.onChatAction(ChatManager.Action.ChatRestored)
            return
        }

        emitViewState { chatState.initChat(companyName, queueId, visitorContextAssetId) }
        initChatManager()
    }

    private fun subscribeToEngagement() {
        engagementStateUseCase().unSafeSubscribe(::onEngagementStateChanged)
        operatorMediaUseCase().unSafeSubscribe(::onNewOperatorMediaState)
        onOperatorTypingUseCase().unSafeSubscribe(::onOperatorTyping)
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
        minimizeHandler.addListener { minimizeView() }
        timerStatusListener?.also { callTimer.removeFormattedValueListener(it) }
        val newTimerListener = createNewTimerCallback()
        callTimer.addFormattedValueListener(newTimerListener)
        timerStatusListener = newTimerListener
        updateAllowFileSendState()
    }

    override fun onEngagementConfirmationDialogRequested() {
        if (isQueueingOrEngagementUseCase()) return
        view?.showEngagementConfirmationDialog()
    }

    override fun getConfirmationDialogLinks() = confirmationDialogLinksUseCase()

    override fun onLinkClicked(link: Link) {
        Logger.d(TAG, "onLinkClicked")
        view?.navigateToWebBrowserActivity(link.title, link.url)
    }

    override fun onLiveObservationDialogAllowed() {
        Logger.d(TAG, "onLiveObservationDialogAllowed")
        dialogController.dismissCurrentDialog()
        decideOnQueueingUseCase.onQueueingRequested()
    }

    override fun onLiveObservationDialogRejected() {
        Logger.d(TAG, "onLiveObservationDialogRejected")
        stop()
        dialogController.dismissDialogs()
    }

    private fun enqueueForEngagement() {
        enqueueForEngagementUseCase(queueId = chatState.queueId!!, visitorContextAssetId = chatState.visitorContextAssetId)
    }

    @Synchronized
    private fun emitViewState(callback: () -> ChatState?) {
        val state = callback() ?: return

        if (setState(state) && view != null) {
            Logger.d(TAG, "Emit state:\n$state")
            view?.emitState(chatState)
        }
    }

    override fun onDestroy(retain: Boolean) {
        Logger.d(TAG, "onDestroy, retain:$retain")
        dialogController.dismissMessageCenterUnavailableDialog()

        // view is accessed from multiple threads
        // and must be protected from race condition
        synchronized(this) { view = null }
        backClickedListener = null
        if (!retain) {
            disposable.clear()
            timerStatusListener = null
            callTimer.clear()
            minimizeHandler.clear()
            removeFileAttachmentObserverUseCase.execute(fileAttachmentObserver)
            chatManager.reset()
        }
    }

    override fun onDestroy() {
        throw RuntimeException("no op")
    }

    override fun onPause() {
        mediaUpgradeDisposable.clear()
        isChatViewPaused = true
        messagesNotSeenHandler.onChatWentBackground()
    }

    override fun onImageItemClick(item: AttachmentFile, view: View) {
        if (isFileReadyForPreviewUseCase(item)) {
            this.view?.navigateToPreview(item, view)
        } else {
            this.view?.fileIsNotReadyForPreview()
        }
    }

    override fun onMessageTextChanged(message: String) {
        emitViewState { chatState.setLastTypedText(message).setShowSendButton(isShowSendButtonUseCase(message)) }
        sendMessagePreview(message)
    }

    override fun sendMessage(message: String) {
        Logger.d(TAG, "Send MESSAGE: $message")
        clearMessagePreview()
        sendMessageUseCase.execute(message, sendMessageCallback)
        addQuickReplyButtons(emptyList())
    }

    private fun sendMessagePreview(message: String) {
        if (chatState.isOperatorOnline) {
            sendMessagePreviewUseCase.execute(message)
        }
    }

    private fun clearMessagePreview() {
        // An empty string has to be sent to clear the message preview.
        sendMessagePreview("")
    }

    private fun onMessageSendError(ex: GliaException, message: Unsent) {
        Logger.e(TAG, "Message send exception", ex)

        chatManager.onChatAction(ChatManager.Action.MessageSendError(message))
        scrollChatToBottom()
    }

    private fun onSendMessageOperatorOffline(message: Unsent) {
        appendUnsentMessage(message)
        if (!chatState.engagementRequested) {
            viewInitPreQueueing()
        }
    }

    private fun appendUnsentMessage(message: Unsent) {
        Logger.d(TAG, "appendUnsentMessage: $message")
        chatManager.onChatAction(ChatManager.Action.UnsentMessageReceived(message))
        scrollChatToBottom()
    }

    private fun onOperatorTyping(isOperatorTyping: Boolean) {
        emitViewState { chatState.setIsOperatorTyping(isOperatorTyping) }
    }

    override fun show() {
        Logger.d(TAG, "show")
        if (!chatState.isVisible) {
            emitViewState { chatState.changeVisibility(true) }
        }
    }

    override fun onBackArrowClicked() {
        Logger.d(TAG, "onBackArrowClicked")
        if (isQueueingOrOngoingEngagement) {
            emitViewState { chatState.changeVisibility(false) }
            messagesNotSeenHandler.chatOnBackClicked()
        }

        navigateBack()
    }

    private fun navigateBack() {
        if (isFromCallScreenUseCase.isFromCallScreen) {
            view?.backToCall()
        } else {
            backClickedListener?.onBackClicked()
            onDestroy(isQueueingOrOngoingEngagement || isAuthenticatedUseCase())
            Dependencies.getControllerFactory().destroyCallController()
        }
        updateFromCallScreenUseCase.updateFromCallScreen(false)
    }

    override fun noMoreOperatorsAvailableDismissed() {
        Logger.d(TAG, "noMoreOperatorsAvailableDismissed")
        stop()
        dialogController.dismissCurrentDialog()
    }

    override fun unexpectedErrorDialogDismissed() {
        Logger.d(TAG, "unexpectedErrorDialogDismissed")
        stop()
        dialogController.dismissCurrentDialog()
    }

    override fun endEngagementDialogYesClicked() {
        Logger.d(TAG, "endEngagementDialogYesClicked")
        stop()
        dialogController.dismissDialogs()
    }

    override fun endEngagementDialogDismissed() {
        Logger.d(TAG, "endEngagementDialogDismissed")
        dialogController.dismissCurrentDialog()
    }

    override fun leaveChatClicked() {
        Logger.d(TAG, "leaveChatClicked")
        if (chatState.isOperatorOnline) dialogController.showExitChatDialog()
    }

    override fun onXButtonClicked() {
        Logger.d(TAG, "onXButtonClicked")
        if (isQueueingOrOngoingEngagement) {
            dialogController.showExitQueueDialog()
        } else {
            navigateBack()
        }
    }

    // view is accessed from multiple threads
    // and must be protected from race condition
    @Synchronized
    override fun setView(view: ChatContract.View) {
        Logger.d(TAG, "setViewCallback")
        this.view = view
        view.emitState(chatState)
        view.emitUploadAttachments(getFileAttachmentsUseCase.execute())

        // always start in bottom
        emitViewState { chatState.isInBottomChanged(true).changeVisibility(true) }
        view.scrollToBottomImmediate()

        chatState.pendingNavigationType?.also { view.navigateToCall(it) }
    }

    override fun setOnBackClickedListener(finishCallback: ChatView.OnBackClickedListener?) {
        this.backClickedListener = finishCallback
    }

    override fun onResume() {
        Logger.d(TAG, "onResume")
        onResumeSetup()
    }

    private fun onResumeSetup() {
        subscribeToMediaUpgradeEvents()
        isChatViewPaused = false
        messagesNotSeenHandler.callChatButtonClicked()

        if (isShowOverlayPermissionRequestDialogUseCase.execute()) {
            dialogController.showOverlayPermissionsDialog()
        }
    }

    private fun subscribeToMediaUpgradeEvents() {
        mediaUpgradeDisposable.addAll(
            mediaUpgradeOfferUseCase().subscribe(::handleMediaUpgradeRequest),
            acceptMediaUpgradeOfferUseCase.result.subscribe(::handleMediaUpgradeAcceptResult)
        )
    }

    private fun handleMediaUpgradeAcceptResult(it: MediaUpgradeOffer) {
        Logger.d(TAG, "Media upgrade request accepted by visitor")
        val requestedMediaType: String = if (it.video != null && it.video != MediaDirection.NONE) {
            GliaWidgets.MEDIA_TYPE_VIDEO
        } else {
            GliaWidgets.MEDIA_TYPE_AUDIO
        }
        emitViewState { chatState.setPendingNavigationType(requestedMediaType) }
        view?.apply {
            navigateToCall(requestedMediaType)
            Logger.d(TAG, "navigateToCall")
        }
    }

    private fun handleMediaUpgradeRequest(it: MediaUpgradeOffer) {
        when {
            isChatViewPaused -> return
            // audio call
            it.video == MediaDirection.NONE && it.audio == MediaDirection.TWO_WAY -> {
                Logger.d(TAG, "audioUpgradeRequested")
                if (chatState.isOperatorOnline) {
                    dialogController.showUpgradeAudioDialog(it, chatState.formattedOperatorName)
                }
            }
            // video call
            it.video == MediaDirection.TWO_WAY -> {
                Logger.d(TAG, "2 way videoUpgradeRequested")
                if (chatState.isOperatorOnline) {
                    dialogController.showUpgradeVideoDialog2Way(it, chatState.formattedOperatorName)
                }
            }

            it.video == MediaDirection.ONE_WAY -> {
                Logger.d(TAG, "1 way videoUpgradeRequested")
                if (chatState.isOperatorOnline) {
                    dialogController.showUpgradeVideoDialog1Way(it, chatState.formattedOperatorName)
                }
            }
        }
    }

    private fun onEngagementStateChanged(state: State) {
        when (state) {
            State.FinishedCallVisualizer, State.FinishedOmniCore -> {
                if (!isQueueingOrOngoingEngagement) {
                    dialogController.dismissDialogs()
                }
            }

            State.StartedOmniCore -> newEngagementLoaded()
            is State.Update -> handleEngagementStateUpdate(state.updateState)
            is State.PreQueuing, is State.Queuing -> queueForEngagementStarted()
            is State.QueueUnstaffed, is State.UnexpectedErrorHappened, is State.QueueingCanceled -> emitViewState { chatState.stop() }

            else -> {
                // no op
            }
        }
    }

    private fun handleEngagementStateUpdate(state: EngagementUpdateState) {
        when (state) {
            is EngagementUpdateState.Ongoing -> onEngagementOngoing(state.operator)
            is EngagementUpdateState.OperatorChanged -> onOperatorChanged(state.operator)
            is EngagementUpdateState.OperatorConnected -> onOperatorConnected(state.operator)
            EngagementUpdateState.Transferring -> onTransferring()
        }
    }

    private fun onEngagementOngoing(operator: Operator) {
        emitViewState { chatState.operatorConnected(operator.formattedName, operator.imageUrl) }
    }

    private fun onOperatorConnected(operator: Operator) {
        operatorConnected(operator.formattedName, operator.imageUrl)
    }

    private fun onOperatorChanged(operator: Operator) {
        operatorChanged(operator.formattedName, operator.imageUrl)
    }

    private fun onTransferring() {
        emitViewState { chatState.transferring() }
        chatManager.onChatAction(ChatManager.Action.Transferring)
    }

    override fun overlayPermissionsDialogDismissed() {
        Logger.d(TAG, "overlayPermissionsDialogDismissed")
        decideOnQueueingUseCase.onOverlayDialogShown()
        dialogController.dismissCurrentDialog()
        emitViewState { chatState }
    }

    override fun acceptUpgradeOfferClicked(offer: MediaUpgradeOffer) {
        Logger.i(TAG, "Upgrade offer accepted by visitor")
        messagesNotSeenHandler.chatUpgradeOfferAccepted()
        acceptMediaUpgradeOfferUseCase(offer)
        dialogController.dismissCurrentDialog()
    }

    override fun declineUpgradeOfferClicked(offer: MediaUpgradeOffer) {
        Logger.i(TAG, "Upgrade offer declined by visitor")
        declineMediaUpgradeOfferUseCase(offer)
        dialogController.dismissCurrentDialog()
    }

    @Synchronized
    private fun setState(state: ChatState): Boolean {
        if (chatState == state) return false
        chatState = state
        return true
    }

    private fun error(error: Throwable?) {
        error?.also { error(it.toString()) }
    }

    private fun error(error: String) {
        Logger.e(TAG, error)
        dialogController.showUnexpectedErrorDialog()
        emitViewState { chatState.stop() }
    }

    private fun viewInitPreQueueing() {
        if (isQueueingOrOngoingEngagement) return

        Logger.d(TAG, "viewInitPreQueueing")
        chatManager.onChatAction(ChatManager.Action.QueuingStarted(chatState.companyName.orEmpty()))
        confirmationDialogUseCase { shouldShow ->
            if (shouldShow) {
                dialogController.showEngagementConfirmationDialog()
            } else {
                decideOnQueueingUseCase.onQueueingRequested()
            }
        }
    }

    private fun viewInitQueueing() {
        Logger.d(TAG, "viewInitQueueing")
        emitViewState { chatState.queueingStarted() }
    }

    private fun minimizeView() {
        view?.minimizeView()
    }

    private fun operatorConnected(formattedOperatorName: String, profileImgUrl: String?) {
        chatManager.onChatAction(
            ChatManager.Action.OperatorConnected(
                chatState.companyName.orEmpty(), formattedOperatorName, profileImgUrl
            )
        )
        emitViewState { chatState.operatorConnected(formattedOperatorName, profileImgUrl).setLiveChatState() }
    }

    private fun operatorChanged(formattedOperatorName: String, profileImgUrl: String?) {
        chatManager.onChatAction(
            ChatManager.Action.OperatorJoined(
                chatState.companyName.orEmpty(), formattedOperatorName, profileImgUrl
            )
        )
        emitViewState { chatState.operatorConnected(formattedOperatorName, profileImgUrl) }
    }

    private fun stop() {
        Logger.d(TAG, "Stop, engagement ended")
        endEngagementUseCase()
        chatManager.reset()
        mediaUpgradeDisposable.clear()
        emitViewState { chatState.stop() }
    }

    private fun addQuickReplyButtons(options: List<GvaButton>) {
        emitViewState { chatState.copy(gvaQuickReplies = options) }
    }

    private fun startTimer() {
        Logger.d(TAG, "startTimer")
        callTimer.startNew(Constants.CALL_TIMER_DELAY, Constants.CALL_TIMER_INTERVAL_VALUE)
    }

    private fun upgradeMediaItemToVideo() {
        Logger.d(TAG, "upgradeMediaItem")
        emitViewState { chatState.upgradeMedia(true) }
        chatManager.onChatAction(ChatManager.Action.OnMediaUpgradeToVideo)
    }

    private fun createNewTimerCallback(): FormattedTimerStatusListener {
        return object : FormattedTimerStatusListener {
            override fun onNewFormattedTimerValue(formattedValue: String) {
                if (chatState.isMediaUpgradeStarted) {
                    chatManager.onChatAction(
                        ChatManager.Action.OnMediaUpgradeTimerUpdated(
                            formattedValue
                        )
                    )
                }
            }

            override fun onFormattedTimerCancelled() {
                if (chatState.isMediaUpgradeStarted) {
                    emitViewState { chatState.upgradeMedia(null) }
                    chatManager.onChatAction(ChatManager.Action.OnMediaUpgradeCanceled)
                }
            }
        }
    }

    override fun singleChoiceOptionClicked(
        item: OperatorMessageItem.ResponseCard, selectedOption: SingleChoiceOption
    ) {
        Logger.d(TAG, "singleChoiceOptionClicked, id: ${item.id}")
        sendMessageUseCase.execute(selectedOption.asSingleChoiceResponse(), sendMessageCallback)
        chatManager.onChatAction(ChatManager.Action.ResponseCardClicked(item))
    }

    override fun sendCustomCardResponse(customCard: CustomCardChatItem, text: String, value: String) {
        val attachment = SingleChoiceAttachment.from(value, text)
        sendMessageUseCase.execute(attachment, sendMessageCallback)

        chatManager.onChatAction(ChatManager.Action.CustomCardClicked(customCard, attachment))
    }

    private fun sendGvaResponse(singleChoiceAttachment: SingleChoiceAttachment) {
        addQuickReplyButtons(emptyList())
        sendMessageUseCase.execute(singleChoiceAttachment, sendMessageCallback)
    }

    override fun onRecyclerviewPositionChanged(isBottom: Boolean) {
        if (isBottom) {
            Logger.d(TAG, "onRecyclerviewPositionChanged, isBottom = true")
            emitViewState { chatState.isInBottomChanged(true).messagesNotSeenChanged(0) }
        } else {
            Logger.d(TAG, "onRecyclerviewPositionChanged, isBottom = false")
            emitViewState { chatState.isInBottomChanged(false) }
        }
    }

    override fun newMessagesIndicatorClicked() {
        Logger.d(TAG, "newMessagesIndicatorClicked")
        view?.smoothScrollToBottom()
    }

    private fun newEngagementLoaded() {
        emitViewState { chatState.engagementStarted() }
        chatManager.reloadHistoryIfNeeded()
    }

    private fun initChatManager() {
        chatManager.initialize(::onHistoryLoaded, ::addQuickReplyButtons, ::updateUnSeenMessagesCount)
            .subscribe(::emitItems, ::error)
            .also(disposable::add)
    }

    private fun updateUnSeenMessagesCount(count: Int) {
        emitViewState {
            val notSeenCount = chatState.messagesNotSeen
            chatState.messagesNotSeenChanged(if (chatState.isChatInBottom) 0 else notSeenCount + count)
        }
    }

    private fun onHistoryLoaded(hasHistory: Boolean) {
        Logger.d(TAG, "historyLoaded")

        if (!hasHistory) {
            if (!isSecureEngagement && !isQueueingOrOngoingEngagement) {
                viewInitPreQueueing()
            } else {
                Logger.d(TAG, "Opened empty Secure Conversations chat")
            }
        }

        when {
            isSecureEngagement -> emitViewState { chatState.setSecureMessagingState() }
            isQueueingOrEngagementUseCase.hasOngoingEngagement -> emitViewState { chatState.engagementStarted() }
            else -> emitViewState { chatState.historyLoaded() }
        }

        prepareChatComponents()
    }

    private fun emitItems(items: List<ChatItem>) {
        view?.emitItems(items)
    }

    private fun onNewOperatorMediaState(operatorMediaState: MediaState) {
        Logger.d(TAG, "newOperatorMediaState: $operatorMediaState")
        if (chatState.isAudioCallStarted && operatorMediaState.video != null) {
            upgradeMediaItemToVideo()
        } else if (!chatState.isMediaUpgradeStarted) {
            addMediaUpgradeItemToChatItems(operatorMediaState)
            if (chatState.isOperatorOnline && !callTimer.isRunning && timerStatusListener != null) {
                startTimer()
            }
        }

        callNotificationUseCase(operatorMedia = operatorMediaState)
    }

    private fun addMediaUpgradeItemToChatItems(operatorMediaState: MediaState) {
        val isVideo = when {
            operatorMediaState.video == null && operatorMediaState.audio != null -> false
            operatorMediaState.video != null -> true
            else -> null
        } ?: return

        emitViewState { chatState.upgradeMedia(isVideo) }
        chatManager.onChatAction(ChatManager.Action.OnMediaUpgradeStarted(isVideo))
    }

    override fun notificationDialogDismissed() {
        dialogController.dismissCurrentDialog()
    }

    private fun queueForEngagementStarted() {
        if (chatState.isOperatorOnline) {
            return
        }

        viewInitQueueing()
    }

    override fun onRemoveAttachment(attachment: FileAttachment) {
        removeFileAttachmentUseCase.execute(attachment)
    }

    override fun onAttachmentReceived(file: FileAttachment) {
        addFileToAttachmentAndUploadUseCase.execute(file, object : AddFileToAttachmentAndUploadUseCase.Listener {
            override fun onFinished() {
                Logger.d(TAG, "fileUploadFinished")
                view?.clearTempFile()
            }

            override fun onStarted() {
                Logger.d(TAG, "fileUploadStarted")
            }

            override fun onError(ex: Exception) {
                Logger.e(TAG, "Upload file failed: " + ex.message)
                view?.clearTempFile()
            }

            override fun onSecurityCheckStarted() {
                Logger.d(TAG, "fileUploadSecurityCheckStarted")
            }

            override fun onSecurityCheckFinished(scanResult: EngagementFile.ScanResult?) {
                Logger.d(TAG, "fileUploadSecurityCheckFinished result=$scanResult")
            }
        })
    }

    override fun onFileDownloadClicked(attachmentFile: AttachmentFile) {
        disposable.add(downloadFileUseCase(attachmentFile).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({ fileDownloadSuccess(attachmentFile) }) { fileDownloadError(attachmentFile, it) })
    }

    private fun fileDownloadError(attachmentFile: AttachmentFile, error: Throwable) {
        view?.fileDownloadError(attachmentFile, error)
    }

    private fun fileDownloadSuccess(attachmentFile: AttachmentFile) {
        view?.fileDownloadSuccess(attachmentFile)
    }

    private fun updateAllowFileSendState() {
        siteInfoUseCase { siteInfo: SiteInfo?, _ -> onSiteInfoReceived(siteInfo) }
    }

    private fun onSiteInfoReceived(siteInfo: SiteInfo?) {
        emitViewState {
            chatState.allowSendAttachmentStateChanged(siteInfo == null || siteInfo.allowedFileSenders.isVisitorAllowed)
        }
    }

    override fun isCallVisualizerOngoing(): Boolean {
        return isCurrentEngagementCallVisualizerUseCase()
    }

    override fun onGvaButtonClicked(button: GvaButton) {
        when (val buttonType: Gva.ButtonType = determineGvaButtonTypeUseCase(button)) {
            Gva.ButtonType.BroadcastEvent -> view?.showBroadcastNotSupportedToast()
            is Gva.ButtonType.Email -> view?.requestOpenEmailClient(buttonType.uri)
            is Gva.ButtonType.Phone -> view?.requestOpenDialer(buttonType.uri)
            is Gva.ButtonType.PostBack -> sendGvaResponse(buttonType.singleChoiceAttachment)
            is Gva.ButtonType.Url -> view?.requestOpenUri(buttonType.uri)
        }
    }

    override fun onMessageClicked(messageId: String) {
        chatManager.onChatAction(ChatManager.Action.MessageClicked(messageId))
    }

    private fun scrollChatToBottom() {
        emitViewState { chatState.copy(isChatInBottom = true) }
        view?.smoothScrollToBottom()
    }
}
