package com.glia.widgets.chat.controller

import android.net.Uri
import android.view.View
import com.glia.androidsdk.Engagement
import com.glia.androidsdk.GliaException
import com.glia.androidsdk.Operator
import com.glia.androidsdk.chat.AttachmentFile
import com.glia.androidsdk.chat.SendMessagePayload
import com.glia.androidsdk.chat.SingleChoiceAttachment
import com.glia.androidsdk.chat.SingleChoiceOption
import com.glia.androidsdk.chat.VisitorMessage
import com.glia.androidsdk.comms.MediaState
import com.glia.androidsdk.engagement.EngagementFile
import com.glia.androidsdk.site.SiteInfo
import com.glia.widgets.Constants
import com.glia.widgets.chat.ChatContract
import com.glia.widgets.chat.ChatManager
import com.glia.widgets.chat.ChatView
import com.glia.widgets.chat.Intention
import com.glia.widgets.chat.domain.DecideOnQueueingUseCase
import com.glia.widgets.chat.domain.GliaSendMessagePreviewUseCase
import com.glia.widgets.chat.domain.GliaSendMessageUseCase
import com.glia.widgets.chat.domain.IsAuthenticatedUseCase
import com.glia.widgets.chat.domain.IsFromCallScreenUseCase
import com.glia.widgets.chat.domain.IsShowSendButtonUseCase
import com.glia.widgets.chat.domain.SetChatScreenOpenUseCase
import com.glia.widgets.chat.domain.SiteInfoUseCase
import com.glia.widgets.chat.domain.TakePictureUseCase
import com.glia.widgets.chat.domain.UpdateFromCallScreenUseCase
import com.glia.widgets.chat.domain.UriToFileAttachmentUseCase
import com.glia.widgets.chat.domain.gva.DetermineGvaButtonTypeUseCase
import com.glia.widgets.chat.model.ChatItem
import com.glia.widgets.chat.model.ChatState
import com.glia.widgets.chat.model.CustomCardChatItem
import com.glia.widgets.chat.model.Gva
import com.glia.widgets.chat.model.GvaButton
import com.glia.widgets.chat.model.OperatorMessageItem
import com.glia.widgets.chat.model.VisitorAttachmentItem
import com.glia.widgets.chat.model.VisitorChatItem
import com.glia.widgets.di.Dependencies
import com.glia.widgets.engagement.EndAction
import com.glia.widgets.engagement.EngagementUpdateState
import com.glia.widgets.engagement.State
import com.glia.widgets.engagement.domain.AcceptMediaUpgradeOfferUseCase
import com.glia.widgets.engagement.domain.EndEngagementUseCase
import com.glia.widgets.engagement.domain.EngagementStateUseCase
import com.glia.widgets.engagement.domain.EnqueueForEngagementUseCase
import com.glia.widgets.engagement.domain.IsCurrentEngagementCallVisualizerUseCase
import com.glia.widgets.engagement.domain.IsQueueingOrLiveEngagementUseCase
import com.glia.widgets.engagement.domain.OperatorMediaUseCase
import com.glia.widgets.engagement.domain.OperatorTypingUseCase
import com.glia.widgets.engagement.domain.ReleaseResourcesUseCase
import com.glia.widgets.entrywidget.EntryWidgetContract
import com.glia.widgets.filepreview.domain.usecase.DownloadFileUseCase
import com.glia.widgets.filepreview.domain.usecase.IsFileReadyForPreviewUseCase
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.TAG
import com.glia.widgets.helper.TimeCounter
import com.glia.widgets.helper.TimeCounter.FormattedTimerStatusListener
import com.glia.widgets.helper.exists
import com.glia.widgets.helper.formattedName
import com.glia.widgets.helper.imageUrl
import com.glia.widgets.helper.isValid
import com.glia.widgets.helper.unSafeSubscribe
import com.glia.widgets.internal.dialog.DialogContract
import com.glia.widgets.internal.dialog.domain.ConfirmationDialogLinksUseCase
import com.glia.widgets.internal.dialog.domain.IsShowOverlayPermissionRequestDialogUseCase
import com.glia.widgets.internal.dialog.model.LeaveDialogAction
import com.glia.widgets.internal.dialog.model.Link
import com.glia.widgets.internal.engagement.domain.ConfirmationDialogUseCase
import com.glia.widgets.internal.engagement.domain.UpdateOperatorDefaultImageUrlUseCase
import com.glia.widgets.internal.fileupload.domain.AddFileAttachmentsObserverUseCase
import com.glia.widgets.internal.fileupload.domain.AddFileToAttachmentAndUploadUseCase
import com.glia.widgets.internal.fileupload.domain.FileUploadLimitNotExceededObservableUseCase
import com.glia.widgets.internal.fileupload.domain.GetFileAttachmentsUseCase
import com.glia.widgets.internal.fileupload.domain.RemoveFileAttachmentUseCase
import com.glia.widgets.internal.fileupload.model.LocalAttachment
import com.glia.widgets.internal.notification.domain.CallNotificationUseCase
import com.glia.widgets.internal.permissions.domain.RequestNotificationPermissionIfPushNotificationsSetUpUseCase
import com.glia.widgets.internal.permissions.domain.WithCameraPermissionUseCase
import com.glia.widgets.internal.permissions.domain.WithReadWritePermissionsUseCase
import com.glia.widgets.internal.secureconversations.domain.HasOngoingSecureConversationUseCase
import com.glia.widgets.internal.secureconversations.domain.IsMessagingAvailableUseCase
import com.glia.widgets.internal.secureconversations.domain.ManageSecureMessagingStatusUseCase
import com.glia.widgets.internal.secureconversations.domain.SecureConversationTopBannerVisibilityUseCase
import com.glia.widgets.internal.secureconversations.domain.SetLeaveSecureConversationDialogVisibleUseCase
import com.glia.widgets.view.MessagesNotSeenHandler
import com.glia.widgets.view.MinimizeHandler
import com.glia.widgets.webbrowser.domain.GetUrlFromLinkUseCase
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.functions.Consumer
import io.reactivex.rxjava3.schedulers.Schedulers

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
    private val getFileAttachmentsUseCase: GetFileAttachmentsUseCase,
    private val removeFileAttachmentUseCase: RemoveFileAttachmentUseCase,
    private val fileUploadLimitNotExceededObservableUseCase: FileUploadLimitNotExceededObservableUseCase,
    private val isShowSendButtonUseCase: IsShowSendButtonUseCase,
    private val isShowOverlayPermissionRequestDialogUseCase: IsShowOverlayPermissionRequestDialogUseCase,
    private val downloadFileUseCase: DownloadFileUseCase,
    private val siteInfoUseCase: SiteInfoUseCase,
    private val isFromCallScreenUseCase: IsFromCallScreenUseCase,
    private val updateFromCallScreenUseCase: UpdateFromCallScreenUseCase,
    private val manageSecureMessagingStatusUseCase: ManageSecureMessagingStatusUseCase,
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
    private val acceptMediaUpgradeOfferUseCase: AcceptMediaUpgradeOfferUseCase,
    private val isQueueingOrLiveEngagementUseCase: IsQueueingOrLiveEngagementUseCase,
    private val enqueueForEngagementUseCase: EnqueueForEngagementUseCase,
    private val decideOnQueueingUseCase: DecideOnQueueingUseCase,
    private val takePictureUseCase: TakePictureUseCase,
    private val uriToFileAttachmentUseCase: UriToFileAttachmentUseCase,
    private val withCameraPermissionUseCase: WithCameraPermissionUseCase,
    private val withReadWritePermissionsUseCase: WithReadWritePermissionsUseCase,
    private val requestNotificationPermissionIfPushNotificationsSetUpUseCase: RequestNotificationPermissionIfPushNotificationsSetUpUseCase,
    private val releaseResourcesUseCase: ReleaseResourcesUseCase,
    private val getUrlFromLinkUseCase: GetUrlFromLinkUseCase,
    private val isMessagingAvailableUseCase: IsMessagingAvailableUseCase,
    private val shouldShowTopBannerUseCase: SecureConversationTopBannerVisibilityUseCase,
    private val setLeaveSecureConversationDialogVisibleUseCase: SetLeaveSecureConversationDialogVisibleUseCase,
    private val setChatScreenOpenUseCase: SetChatScreenOpenUseCase,
    private val hasOngoingSecureConversationUseCase: HasOngoingSecureConversationUseCase
) : ChatContract.Controller {
    private var backClickedListener: ChatView.OnBackClickedListener? = null
    private var view: ChatContract.View? = null
    private var timerStatusListener: FormattedTimerStatusListener? = null

    private val disposable = CompositeDisposable()
    private val mediaUpgradeDisposable = CompositeDisposable()

    private val sendMessageCallback: GliaSendMessageUseCase.Listener = object : GliaSendMessageUseCase.Listener {
        override fun messageSent(message: VisitorMessage?) {
            Logger.d(TAG, "messageSent: $message, id: ${message?.id}")
            onMessageSent(message)
        }

        override fun onMessageValidated() {
            view?.clearMessageInput()
            emitViewState { chatState.setLastTypedText("").setShowSendButton(isShowSendButtonUseCase("")) }
        }

        override fun errorOperatorOffline(messageId: String) {
            chatManager.onChatAction(ChatManager.Action.OnSendMessageOperatorOffline(messageId))
            onSendMessageOperatorOffline()
        }

        override fun onMessagePrepared(visitorChatItem: VisitorChatItem, payload: SendMessagePayload) {
            addMessagePreview(visitorChatItem, payload)
            scrollChatToBottom()
        }

        override fun onAttachmentsPrepared(items: List<VisitorAttachmentItem>, payload: SendMessagePayload?) {
            addAttachmentPreview(items, payload)
            scrollChatToBottom()
        }

        override fun error(ex: GliaException, messageId: String) {
            Logger.e(TAG, "Message send exception", ex)

            showMessageError(messageId)
        }
    }

    @Volatile
    private var isChatViewPaused = false

    private val fileAttachmentCallback = Consumer<List<LocalAttachment>> { attachments ->
        view?.apply {
            emitViewState {
                emitUploadAttachments(attachments)
                chatState.setShowSendButton(isShowSendButtonUseCase(chatState.lastTypedText))
            }
        }
    }

    private val attachmentButtonState: Observable<Pair<Boolean, Boolean>>
        get() = Observable.combineLatest(fileUploadLimitNotExceededObservableUseCase(), isMessagingAvailableUseCase().toObservable(), ::Pair)
            .observeOn(AndroidSchedulers.mainThread())

    @Volatile
    private var chatState: ChatState

    private val isQueueingOrOngoingEngagement get() = isQueueingOrLiveEngagementUseCase()

    override val isChatVisible: Boolean
        get() = chatState.isVisible

    init {
        Logger.d(TAG, "constructor")
        chatState = ChatState()
        subscribeToEngagement()
        decideOnQueueingUseCase().unSafeSubscribe { enqueueForEngagement() }
    }

    override fun initChat(intention: Intention) {
        updateOperatorDefaultImageUrlUseCase()

        when (intention) {
            Intention.RETURN_TO_CHAT -> returnToChat()
            Intention.SC_DIALOG_START_AUDIO -> initLeaveCurrentConversationDialog(LeaveDialogAction.AUDIO)
            Intention.SC_DIALOG_START_VIDEO -> initLeaveCurrentConversationDialog(LeaveDialogAction.VIDEO)
            Intention.SC_DIALOG_ENQUEUE_FOR_TEXT -> initLeaveCurrentConversationDialog(LeaveDialogAction.LIVE_CHAT)
            Intention.SC_CHAT -> initSecureMessaging()
            Intention.LIVE_CHAT -> initLiveChat()
        }
    }

    private fun returnToChat() {
        if (chatState.isInitialized) {
            restoreChat()
        } else {
            initLiveChat()
        }
    }

    private fun initLeaveCurrentConversationDialog(action: LeaveDialogAction) {
        setLeaveSecureConversationDialogVisibleUseCase(true)
        initSecureMessaging()
        dialogController.showLeaveCurrentConversationDialog(action)
    }

    private fun initSecureMessaging() {
        emitViewState { chatState.initChat().setSecureMessagingState() }
        initChatManager()
        initFileAttachmentState()
        ensureSecureMessagingAvailable()
        observeTopBannerUseCase()
    }

    private fun observeTopBannerUseCase() {
        disposable.add(shouldShowTopBannerUseCase().subscribe({
            emitViewState { chatState.setSecureConversationsTopBannerVisibility(it) }
        }) { error ->
            Logger.w(TAG, "Secure messaging top banner visibility flag observable failed.\n $error")
        })
    }

    private fun initLiveChat() {
        emitViewState { chatState.initChat().setLiveChatState() }
        initChatManager()
        initFileAttachmentState()
    }

    override fun restoreChat() {
        chatManager.onChatAction(ChatManager.Action.ChatRestored)
    }

    private fun subscribeToEngagement() {
        engagementStateUseCase().unSafeSubscribe(::onEngagementStateChanged)
        operatorMediaUseCase().unSafeSubscribe(::onNewOperatorMediaState)
        onOperatorTypingUseCase().unSafeSubscribe(::onOperatorTyping)
    }

    private fun ensureSecureMessagingAvailable() {
        disposable.add(isMessagingAvailableUseCase().observeOn(AndroidSchedulers.mainThread()).subscribe(::handleMessagingAvailableResult))
    }

    private fun handleMessagingAvailableResult(isAvailable: Boolean) {
        if (!isAvailable && manageSecureMessagingStatusUseCase.shouldBehaveAsSecureMessaging && !isQueueingOrOngoingEngagement) {
            Logger.d(TAG, "Messaging is unavailable")
            emitViewState { chatState.setSecureMessagingUnavailable() }
        } else {
            Logger.d(TAG, "Messaging is available")
            emitViewState { chatState.setSecureMessagingAvailable() }
        }
    }

    private fun trackAttachmentButtonState() {
        attachmentButtonState.subscribe { (limitNotExceeded, isMessagingAvailable) ->
            val isEnabled = when {
                manageSecureMessagingStatusUseCase.shouldBehaveAsSecureMessaging -> isMessagingAvailable && limitNotExceeded
                else -> limitNotExceeded
            }
            emitViewState { chatState.setIsAttachmentButtonEnabled(isEnabled) }
        }.also(disposable::add)
    }

    private fun prepareChatComponents() {
        disposable.add(addFileAttachmentsObserverUseCase().subscribe(fileAttachmentCallback))
        minimizeHandler.addListener { minimizeView() }
        timerStatusListener?.also { callTimer.removeFormattedValueListener(it) }
        val newTimerListener = createNewTimerCallback()
        callTimer.addFormattedValueListener(newTimerListener)
        timerStatusListener = newTimerListener
    }

    override fun onEngagementConfirmationDialogRequested() {
        if (isQueueingOrLiveEngagementUseCase()) return
        view?.showEngagementConfirmationDialog()
    }

    override fun getConfirmationDialogLinks() = confirmationDialogLinksUseCase()

    override fun onLinkClicked(link: Link) {
        Logger.d(TAG, "onLinkClicked")
        getUrlFromLinkUseCase(link)?.let {
            view?.navigateToWebBrowserActivity(link.title, it)
        } ?: run {
            Logger.e(TAG, "The URL is missing after the confirmation dialog link is clicked")
        }
    }

    override fun onLiveObservationDialogAllowed() {
        Logger.d(TAG, "onLiveObservationDialogAllowed")
        dialogController.dismissCurrentDialog()
        decideOnQueueingUseCase.onQueueingRequested()
    }

    override fun onLiveObservationDialogRejected() {
        Logger.d(TAG, "onLiveObservationDialogRejected")
        endChat()
        dialogController.dismissDialogs()
    }

    private fun enqueueForEngagement() {
        requestNotificationPermissionIfPushNotificationsSetUpUseCase(enqueueForEngagementUseCase::invoke)
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

        // view is accessed from multiple threads
        // and must be protected from race condition
        synchronized(this) { view = null }
        backClickedListener = null
        if (!retain) {
            disposable.clear()
            timerStatusListener = null
            callTimer.clear()
            minimizeHandler.clear()
            chatManager.reset()
        }
    }

    override fun onDestroy() {
        throw RuntimeException("no op")
    }

    override fun onPause() {
        setChatScreenOpenUseCase(false)
        mediaUpgradeDisposable.clear()
        isChatViewPaused = true
        messagesNotSeenHandler.onChatWentBackground()
    }

    override fun onImageItemClick(item: AttachmentFile, view: View) {
        if (isFileReadyForPreviewUseCase(item)) {
            this.view?.navigateToImagePreview(item, view)
        } else {
            this.view?.fileIsNotReadyForPreview()
        }
    }

    override fun onLocalImageItemClick(attachment: LocalAttachment, view: View) {
        if (attachment.uri.exists(view.context)) {
            this.view?.navigateToImagePreview(attachment, view)
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
            sendMessagePreviewUseCase(message)
        }
    }

    private fun clearMessagePreview() {
        // An empty string has to be sent to clear the message preview.
        sendMessagePreview("")
    }

    private fun showMessageError(messageId: String) {
        chatManager.onChatAction(ChatManager.Action.OnSendMessageError(messageId))
    }

    private fun onSendMessageOperatorOffline() {
        if (!chatState.engagementRequested) {
            viewInitPreQueueing()
        }
    }

    private fun onMessageSent(message: VisitorMessage?) {
        message?.takeIf { it.isValid() }?.also { chatManager.onChatAction(ChatManager.Action.OnMessageSent(it)) }
    }

    private fun addMessagePreview(visitorChatItem: VisitorChatItem, payload: SendMessagePayload) {
        chatManager.onChatAction(ChatManager.Action.MessagePreviewAdded(visitorChatItem, payload))
        scrollChatToBottom()
    }

    private fun addAttachmentPreview(items: List<VisitorAttachmentItem>, payload: SendMessagePayload?) {
        chatManager.onChatAction(ChatManager.Action.AttachmentPreviewAdded(items, payload))
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
        if (isFromCallScreenUseCase()) {
            view?.backToCall()
        } else {
            backClickedListener?.onBackClicked()
            onDestroy(isQueueingOrOngoingEngagement || isAuthenticatedUseCase())
            Dependencies.controllerFactory.destroyCallController()
        }
        updateFromCallScreenUseCase(false)
    }

    override fun noMoreOperatorsAvailableDismissed() {
        Logger.d(TAG, "noMoreOperatorsAvailableDismissed")
        endChat()
        dialogController.dismissCurrentDialog()
    }

    override fun unexpectedErrorDialogDismissed() {
        Logger.d(TAG, "unexpectedErrorDialogDismissed")
        endChat()
        dialogController.dismissCurrentDialog()
    }

    override fun endEngagementDialogYesClicked() {
        Logger.d(TAG, "endEngagementDialogYesClicked")
        endChat()
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
        view.emitUploadAttachments(getFileAttachmentsUseCase())

        // always start in bottom
        emitViewState { chatState.isInBottomChanged(true).changeVisibility(true) }
        view.scrollToBottomImmediate()
    }

    @Synchronized
    override fun getView(): ChatContract.View? {
        return this.view
    }

    override fun setOnBackClickedListener(finishCallback: ChatView.OnBackClickedListener?) {
        this.backClickedListener = finishCallback
    }

    override fun onResume() {
        Logger.d(TAG, "onResume")
        onResumeSetup()
    }

    private fun onResumeSetup() {
        setChatScreenOpenUseCase(true)
        subscribeToMediaUpgradeEvents()
        isChatViewPaused = false
        messagesNotSeenHandler.callChatButtonClicked()

        if (isShowOverlayPermissionRequestDialogUseCase()) {
            dialogController.showOverlayPermissionsDialog()
        } else {
            decideOnQueueingUseCase.markOverlayStepCompleted()
        }
    }

    private fun subscribeToMediaUpgradeEvents() {
        mediaUpgradeDisposable.addAll(acceptMediaUpgradeOfferUseCase.result.subscribe { handleMediaUpgradeAcceptResult() })
    }

    private fun handleMediaUpgradeAcceptResult() {
        messagesNotSeenHandler.chatUpgradeOfferAccepted()
    }

    private fun onEngagementStateChanged(state: State) {
        when (state) {
            is State.EngagementEnded -> {
                if (state.endAction is EndAction.Retain) {
                    onTransferredToSecureConversation()
                }
            }

            is State.EngagementStarted -> if (!state.isCallVisualizer) newEngagementLoaded()
            is State.Update -> handleEngagementStateUpdate(state.updateState)
            is State.PreQueuing, is State.Queuing -> queueForEngagementStarted()
            is State.QueueUnstaffed, is State.UnexpectedErrorHappened, is State.QueueingCanceled -> emitViewState { chatState.chatUnavailableState() }
            State.TransferredToSecureConversation -> onTransferredToSecureConversation()

            else -> {
                // no op
            }
        }
    }

    private fun onTransferredToSecureConversation() {
        emitViewState { chatState.setSecureMessagingState().setSecureMessagingAvailable() }
        ensureSecureMessagingAvailable()
        observeTopBannerUseCase()
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

    @Synchronized
    private fun setState(state: ChatState): Boolean {
        if (chatState == state) return false
        chatState = state
        return true
    }

    private fun error(error: Throwable?) {
        error?.also {
            if ((it as? GliaException)?.cause == GliaException.Cause.AUTHENTICATION_ERROR) {
                // Clear the state because visitor authentication has changed
                releaseResourcesUseCase()
            }
            error(it.toString())
        }
    }

    private fun error(error: String) {
        Logger.e(TAG, error)
        dialogController.showUnexpectedErrorDialog()
        emitViewState { chatState.chatUnavailableState() }
    }

    private fun viewInitPreQueueing() {
        if (isQueueingOrOngoingEngagement) return

        Logger.d(TAG, "viewInitPreQueueing")
        chatManager.onChatAction(ChatManager.Action.QueuingStarted)
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
            ChatManager.Action.OperatorConnected(formattedOperatorName, profileImgUrl)
        )
        emitViewState {
            //Need to call setLiveChatState() first, to keep the input state ENABLED when operator is connected
            //Ticket - https://glia.atlassian.net/browse/MOB-4061
            chatState.setLiveChatState().operatorConnected(formattedOperatorName, profileImgUrl)
        }
    }

    private fun operatorChanged(formattedOperatorName: String, profileImgUrl: String?) {
        chatManager.onChatAction(
            ChatManager.Action.OperatorJoined(formattedOperatorName, profileImgUrl)
        )
        emitViewState {
            //Need to call setLiveChatState() first, to keep the input state ENABLED when operator is connected
            //Ticket - https://glia.atlassian.net/browse/MOB-4061
            chatState.setLiveChatState().operatorConnected(formattedOperatorName, profileImgUrl)
        }
    }

    private fun endChat() {
        Logger.d(TAG, "Stop, engagement ended")
        endEngagementUseCase()
        chatManager.reset()
        mediaUpgradeDisposable.clear()
        emitViewState { chatState.chatUnavailableState() }
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

    override fun singleChoiceOptionClicked(item: OperatorMessageItem.ResponseCard, selectedOption: SingleChoiceOption) {
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

    private fun initFileAttachmentState() {
        updateAllowFileSendState()
        trackAttachmentButtonState()
    }

    private fun updateUnSeenMessagesCount(count: Int) {
        emitViewState {
            val notSeenCount = chatState.messagesNotSeen
            chatState.messagesNotSeenChanged(if (chatState.isChatInBottom) 0 else notSeenCount + count)
        }
    }

    private fun onHistoryLoaded(hasHistory: Boolean) {
        Logger.d(TAG, "historyLoaded")

        val isSecureEngagement = manageSecureMessagingStatusUseCase.shouldBehaveAsSecureMessaging

        if (!hasHistory) {
            if (!isSecureEngagement && !isQueueingOrOngoingEngagement) {
                viewInitPreQueueing()
            } else {
                Logger.d(TAG, "Opened empty Secure Messaging chat")
            }
        }

        when {
            isSecureEngagement -> { /* to prevent calling chatState.liveChatHistoryLoaded() */
            }

            isQueueingOrLiveEngagementUseCase.hasOngoingLiveEngagement -> emitViewState { chatState.engagementStarted() }
            else -> emitViewState { chatState.liveChatHistoryLoaded() }
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

    private fun queueForEngagementStarted() {
        if (chatState.isOperatorOnline) {
            return
        }

        viewInitQueueing()
    }

    override fun onRemoveAttachment(attachment: LocalAttachment) {
        removeFileAttachmentUseCase(attachment)
    }

    private fun onAttachmentReceived(file: LocalAttachment) {
        addFileToAttachmentAndUploadUseCase(file, object : AddFileToAttachmentAndUploadUseCase.Listener {
            override fun onFinished() {
                Logger.d(TAG, "fileUploadFinished")
                //We need this file locally, so clearing only file uri reference
                takePictureUseCase.clearUriReference()
            }

            override fun onStarted() {
                Logger.d(TAG, "fileUploadStarted")
            }

            override fun onError(ex: Exception) {
                Logger.e(TAG, "Upload file failed: " + ex.message)
                takePictureUseCase.deleteCurrent()
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
        withReadWritePermissionsUseCase {
            chatManager.onChatAction(ChatManager.Action.OnFileDownloadStarted(attachmentFile.id))

            val downloadDisposable = downloadFileUseCase(attachmentFile)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    fileDownloadSuccess(attachmentFile)
                }) {
                    fileDownloadError(attachmentFile)
                }
            disposable.add(downloadDisposable)
        }
    }

    private fun fileDownloadError(attachmentFile: AttachmentFile) {
        chatManager.onChatAction(ChatManager.Action.OnFileDownloadFailed(attachmentFile.id))
        view?.fileDownloadError()
    }

    private fun fileDownloadSuccess(attachmentFile: AttachmentFile) {
        chatManager.onChatAction(ChatManager.Action.OnFileDownloadSucceeded(attachmentFile.id))
        view?.fileDownloadSuccess()
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

    override fun onRetryClicked(messageId: String) {
        chatManager.onChatAction(ChatManager.Action.OnRetryClicked(messageId))
    }

    private fun scrollChatToBottom() {
        emitViewState { chatState.copy(isChatInBottom = true) }
        view?.smoothScrollToBottom()
    }

    override fun onTakePhotoClicked() {
        withCameraPermissionUseCase {
            takePictureUseCase.prepare {
                view?.dispatchImageCapture(it)
            }
        }
    }

    override fun onImageCaptured(result: Boolean) {
        takePictureUseCase.onImageCaptured(result, ::onAttachmentReceived)
    }

    override fun onContentChosen(uri: Uri) {
        uriToFileAttachmentUseCase(uri)?.also(::onAttachmentReceived)
    }

    override fun leaveCurrentConversationDialogLeaveClicked(action: LeaveDialogAction) {
        dialogController.dismissCurrentDialog()

        when (action) {
            LeaveDialogAction.LIVE_CHAT -> leaveCurrentConversationAndStartEnqueueing()
            LeaveDialogAction.VIDEO -> view?.launchCall(Engagement.MediaType.VIDEO)
            LeaveDialogAction.AUDIO -> view?.launchCall(Engagement.MediaType.AUDIO)
        }

        setLeaveSecureConversationDialogVisibleUseCase(false)
    }

    override fun onScTopBannerItemClicked(itemType: EntryWidgetContract.ItemType) {
        hasOngoingSecureConversationUseCase(
            onHasOngoingSecureConversation = { onScTopBannerItemClickedHasOngoingSC(itemType) },
            onNoOngoingSecureConversation = { onScTopBannerClickedNoOngoingSC(itemType) }
        )
    }

    private fun onScTopBannerClickedNoOngoingSC(itemType: EntryWidgetContract.ItemType) {
        when (itemType) {
            EntryWidgetContract.ItemType.Chat -> leaveCurrentConversationAndStartEnqueueing()
            EntryWidgetContract.ItemType.VideoCall -> view?.launchCall(Engagement.MediaType.VIDEO)
            EntryWidgetContract.ItemType.AudioCall -> view?.launchCall(Engagement.MediaType.AUDIO)
            else -> {
                /*no op*/
            }
        }
    }

    private fun onScTopBannerItemClickedHasOngoingSC(itemType: EntryWidgetContract.ItemType) {
        when (itemType) {
            EntryWidgetContract.ItemType.AudioCall -> dialogController.showLeaveCurrentConversationDialog(LeaveDialogAction.AUDIO)
            EntryWidgetContract.ItemType.Chat -> dialogController.showLeaveCurrentConversationDialog(LeaveDialogAction.LIVE_CHAT)
            EntryWidgetContract.ItemType.VideoCall -> dialogController.showLeaveCurrentConversationDialog(LeaveDialogAction.VIDEO)
            else -> {
                /*no op*/
            }
        }
    }

    private fun leaveCurrentConversationAndStartEnqueueing() {
        manageSecureMessagingStatusUseCase.updateSecureMessagingStatus(false)
        emitViewState { chatState.setLiveChatState() }
        viewInitPreQueueing()
    }

    override fun leaveCurrentConversationDialogStayClicked() {
        dialogController.dismissCurrentDialog()
        setLeaveSecureConversationDialogVisibleUseCase(false)
    }
}
