package com.glia.widgets.chat.controller

import android.net.Uri
import com.glia.androidsdk.Engagement
import com.glia.androidsdk.chat.SingleChoiceAttachment
import com.glia.widgets.chat.ChatContract
import com.glia.widgets.chat.ChatManager
import com.glia.widgets.chat.Intention
import com.glia.widgets.chat.domain.DecideOnQueueingUseCase
import com.glia.widgets.chat.domain.GliaSendMessagePreviewUseCase
import com.glia.widgets.chat.domain.GliaSendMessageUseCase
import com.glia.widgets.chat.domain.IsAuthenticatedUseCase
import com.glia.widgets.chat.domain.IsFromCallScreenUseCase
import com.glia.widgets.chat.domain.IsSendButtonEnableUseCase
import com.glia.widgets.chat.domain.SetChatScreenOpenUseCase
import com.glia.widgets.chat.domain.TakePictureUseCase
import com.glia.widgets.chat.domain.UpdateFromCallScreenUseCase
import com.glia.widgets.chat.domain.UriToFileAttachmentUseCase
import com.glia.widgets.chat.domain.gva.DetermineGvaButtonTypeUseCase
import com.glia.widgets.chat.model.ChatInputMode
import com.glia.widgets.chat.model.ChatState
import com.glia.widgets.chat.model.Gva
import com.glia.widgets.chat.model.GvaButton
import com.glia.widgets.engagement.domain.AcceptMediaUpgradeOfferUseCase
import com.glia.widgets.engagement.domain.DeclineMediaUpgradeOfferUseCase
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
import com.glia.widgets.helper.DeviceMonitor
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.NetworkState
import com.glia.widgets.helper.TimeCounter
import com.glia.widgets.internal.dialog.DialogContract
import com.glia.widgets.internal.dialog.domain.ConfirmationDialogLinksUseCase
import com.glia.widgets.internal.dialog.domain.IsShowOverlayPermissionRequestDialogUseCase
import com.glia.widgets.internal.dialog.model.LeaveDialogAction
import com.glia.widgets.internal.engagement.domain.ConfirmationDialogUseCase
import com.glia.widgets.internal.engagement.domain.UpdateOperatorDefaultImageUrlUseCase
import com.glia.widgets.internal.fileupload.domain.AddFileAttachmentsObserverUseCase
import com.glia.widgets.internal.fileupload.domain.AddFileToAttachmentAndUploadUseCase
import com.glia.widgets.internal.fileupload.domain.FileUploadLimitNotExceededObservableUseCase
import com.glia.widgets.internal.fileupload.domain.GetFileAttachmentsUseCase
import com.glia.widgets.internal.fileupload.domain.RemoveFileAttachmentUseCase
import com.glia.widgets.internal.fileupload.domain.SupportedUploadFileTypesUseCase
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
import io.reactivex.rxjava3.android.plugins.RxAndroidPlugins
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.processors.BehaviorProcessor
import io.reactivex.rxjava3.schedulers.Schedulers
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.atLeastOnce
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class ChatControllerTest {
    private lateinit var callTimer: TimeCounter
    private lateinit var minimizeHandler: MinimizeHandler
    private lateinit var dialogController: DialogContract.Controller
    private lateinit var messagesNotSeenHandler: MessagesNotSeenHandler
    private lateinit var callNotificationUseCase: CallNotificationUseCase
    private lateinit var sendMessagePreviewUseCase: GliaSendMessagePreviewUseCase
    private lateinit var sendMessageUseCase: GliaSendMessageUseCase
    private lateinit var addFileToAttachmentAndUploadUseCase: AddFileToAttachmentAndUploadUseCase
    private lateinit var addFileAttachmentsObserverUseCase: AddFileAttachmentsObserverUseCase
    private lateinit var getFileAttachmentsUseCase: GetFileAttachmentsUseCase
    private lateinit var removeFileAttachmentUseCase: RemoveFileAttachmentUseCase
    private lateinit var fileUploadLimitNotExceededObservableUseCase: FileUploadLimitNotExceededObservableUseCase
    private lateinit var isSendButtonEnableUseCase: IsSendButtonEnableUseCase
    private lateinit var isShowOverlayPermissionRequestDialogUseCase: IsShowOverlayPermissionRequestDialogUseCase
    private lateinit var downloadFileUseCase: DownloadFileUseCase
    private lateinit var supportedUploadFileTypesUseCase: SupportedUploadFileTypesUseCase
    private lateinit var isFromCallScreenUseCase: IsFromCallScreenUseCase
    private lateinit var updateFromCallScreenUseCase: UpdateFromCallScreenUseCase
    private lateinit var isSecureEngagementUseCase: ManageSecureMessagingStatusUseCase
    private lateinit var isFileReadyForPreviewUseCase: IsFileReadyForPreviewUseCase
    private lateinit var determineGvaButtonTypeUseCase: DetermineGvaButtonTypeUseCase
    private lateinit var updateOperatorDefaultImageUrlUseCase: UpdateOperatorDefaultImageUrlUseCase
    private lateinit var confirmationDialogUseCase: ConfirmationDialogUseCase
    private lateinit var confirmationDialogLinksUseCase: ConfirmationDialogLinksUseCase
    private lateinit var operatorTypingUseCase: OperatorTypingUseCase
    private lateinit var endEngagementUseCase: EndEngagementUseCase
    private lateinit var isCurrentEngagementCallVisualizerUseCase: IsCurrentEngagementCallVisualizerUseCase
    private lateinit var engagementStateUseCase: EngagementStateUseCase
    private lateinit var operatorMediaUseCase: OperatorMediaUseCase
    private lateinit var acceptMediaUpgradeOfferUseCase: AcceptMediaUpgradeOfferUseCase
    private lateinit var declineMediaUpgradeOfferUseCase: DeclineMediaUpgradeOfferUseCase
    private lateinit var isQueueingOrLiveEngagementUseCase: IsQueueingOrLiveEngagementUseCase
    private lateinit var enqueueForEngagementUseCase: EnqueueForEngagementUseCase
    private lateinit var decideOnQueueingUseCase: DecideOnQueueingUseCase
    private lateinit var takePictureUseCase: TakePictureUseCase
    private lateinit var uriToFileAttachmentUseCase: UriToFileAttachmentUseCase
    private lateinit var withCameraPermissionUseCase: WithCameraPermissionUseCase
    private lateinit var withReadWritePermissionsUseCase: WithReadWritePermissionsUseCase
    private lateinit var requestNotificationPermissionIfPushNotificationsSetUpUseCase: RequestNotificationPermissionIfPushNotificationsSetUpUseCase
    private lateinit var releaseResourcesUseCase: ReleaseResourcesUseCase
    private lateinit var getUrlFromLinkUseCase: GetUrlFromLinkUseCase

    private lateinit var chatController: ChatController
    private lateinit var isAuthenticatedUseCase: IsAuthenticatedUseCase
    private lateinit var chatManager: ChatManager

    private lateinit var chatView: ChatContract.View
    private lateinit var isMessagingAvailableUseCase: IsMessagingAvailableUseCase

    private lateinit var manageSecureMessagingStatusUseCase: ManageSecureMessagingStatusUseCase
    private lateinit var shouldShowTopBannerVisibilityUseCase: SecureConversationTopBannerVisibilityUseCase
    private lateinit var setLeaveSecureConversationDialogVisibleUseCase: SetLeaveSecureConversationDialogVisibleUseCase
    private lateinit var setChatScreenOpenUseCase: SetChatScreenOpenUseCase
    private lateinit var hasOngoingSecureConversationUseCase: HasOngoingSecureConversationUseCase
    private lateinit var deviceMonitor: DeviceMonitor

    private lateinit var networkStateProcessor: BehaviorProcessor<NetworkState>

    @Before
    fun setUp() {
        Logger.setIsDebug(false)
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }
        networkStateProcessor = BehaviorProcessor.createDefault(NetworkState.CONNECTED)
        callTimer = mock()
        minimizeHandler = mock()
        dialogController = mock()
        messagesNotSeenHandler = mock()
        callNotificationUseCase = mock()
        sendMessagePreviewUseCase = mock()
        sendMessageUseCase = mock()
        addFileToAttachmentAndUploadUseCase = mock()
        addFileAttachmentsObserverUseCase = mock()
        getFileAttachmentsUseCase = mock()
        removeFileAttachmentUseCase = mock()
        fileUploadLimitNotExceededObservableUseCase = mock()
        isSendButtonEnableUseCase = mock()
        isShowOverlayPermissionRequestDialogUseCase = mock()
        downloadFileUseCase = mock()
        supportedUploadFileTypesUseCase = mock()
        isFromCallScreenUseCase = mock()
        updateFromCallScreenUseCase = mock()
        isSecureEngagementUseCase = mock()
        isFileReadyForPreviewUseCase = mock()
        determineGvaButtonTypeUseCase = mock()
        isAuthenticatedUseCase = mock()
        updateOperatorDefaultImageUrlUseCase = mock()
        confirmationDialogUseCase = mock()
        confirmationDialogLinksUseCase = mock()
        chatManager = mock()
        operatorTypingUseCase = mock {
            on { invoke() } doReturn Flowable.empty()
        }
        endEngagementUseCase = mock()
        isCurrentEngagementCallVisualizerUseCase = mock()
        engagementStateUseCase = mock {
            on { invoke() } doReturn Flowable.empty()
        }
        operatorMediaUseCase = mock {
            on { invoke() } doReturn Flowable.empty()
        }
        acceptMediaUpgradeOfferUseCase = mock {
            on { result } doReturn Flowable.empty()
        }
        declineMediaUpgradeOfferUseCase = mock()
        isQueueingOrLiveEngagementUseCase = mock()
        enqueueForEngagementUseCase = mock()
        chatView = mock()
        decideOnQueueingUseCase = mock {
            on { invoke() } doReturn Flowable.just(Unit)
        }

        takePictureUseCase = mock()
        uriToFileAttachmentUseCase = mock()
        withCameraPermissionUseCase = mock()
        withReadWritePermissionsUseCase = mock()
        requestNotificationPermissionIfPushNotificationsSetUpUseCase = mock()
        releaseResourcesUseCase = mock()
        getUrlFromLinkUseCase = mock()
        isMessagingAvailableUseCase = mock()
        manageSecureMessagingStatusUseCase = mock()
        shouldShowTopBannerVisibilityUseCase = mock {
            on { invoke() } doReturn Flowable.empty()
        }
        setLeaveSecureConversationDialogVisibleUseCase = mock()
        setChatScreenOpenUseCase = mock()
        hasOngoingSecureConversationUseCase = mock()
        deviceMonitor = mock {
            on { networkState } doReturn networkStateProcessor
        }

        chatController = ChatController(
            callTimer = callTimer,
            minimizeHandler = minimizeHandler,
            dialogController = dialogController,
            messagesNotSeenHandler = messagesNotSeenHandler,
            callNotificationUseCase = callNotificationUseCase,
            sendMessagePreviewUseCase = sendMessagePreviewUseCase,
            sendMessageUseCase = sendMessageUseCase,
            addFileToAttachmentAndUploadUseCase = addFileToAttachmentAndUploadUseCase,
            addFileAttachmentsObserverUseCase = addFileAttachmentsObserverUseCase,
            getFileAttachmentsUseCase = getFileAttachmentsUseCase,
            removeFileAttachmentUseCase = removeFileAttachmentUseCase,
            fileUploadLimitNotExceededObservableUseCase = fileUploadLimitNotExceededObservableUseCase,
            isSendButtonEnableUseCase = isSendButtonEnableUseCase,
            isShowOverlayPermissionRequestDialogUseCase = isShowOverlayPermissionRequestDialogUseCase,
            downloadFileUseCase = downloadFileUseCase,
            supportedUploadFileTypesUseCase = supportedUploadFileTypesUseCase,
            isFromCallScreenUseCase = isFromCallScreenUseCase,
            updateFromCallScreenUseCase = updateFromCallScreenUseCase,
            isFileReadyForPreviewUseCase = isFileReadyForPreviewUseCase,
            determineGvaButtonTypeUseCase = determineGvaButtonTypeUseCase,
            isAuthenticatedUseCase = isAuthenticatedUseCase,
            updateOperatorDefaultImageUrlUseCase = updateOperatorDefaultImageUrlUseCase,
            confirmationDialogUseCase = confirmationDialogUseCase,
            confirmationDialogLinksUseCase = confirmationDialogLinksUseCase,
            chatManager = chatManager,
            onOperatorTypingUseCase = operatorTypingUseCase,
            endEngagementUseCase = endEngagementUseCase,
            isCurrentEngagementCallVisualizerUseCase = isCurrentEngagementCallVisualizerUseCase,
            engagementStateUseCase = engagementStateUseCase,
            operatorMediaUseCase = operatorMediaUseCase,
            acceptMediaUpgradeOfferUseCase = acceptMediaUpgradeOfferUseCase,
            isQueueingOrLiveEngagementUseCase = isQueueingOrLiveEngagementUseCase,
            enqueueForEngagementUseCase = enqueueForEngagementUseCase,
            decideOnQueueingUseCase = decideOnQueueingUseCase,
            takePictureUseCase = takePictureUseCase,
            uriToFileAttachmentUseCase = uriToFileAttachmentUseCase,
            withCameraPermissionUseCase = withCameraPermissionUseCase,
            withReadWritePermissionsUseCase = withReadWritePermissionsUseCase,
            requestNotificationPermissionIfPushNotificationsSetUpUseCase = requestNotificationPermissionIfPushNotificationsSetUpUseCase,
            releaseResourcesUseCase = releaseResourcesUseCase,
            getUrlFromLinkUseCase = getUrlFromLinkUseCase,
            isMessagingAvailableUseCase = isMessagingAvailableUseCase,
            manageSecureMessagingStatusUseCase = manageSecureMessagingStatusUseCase,
            shouldShowTopBannerUseCase = shouldShowTopBannerVisibilityUseCase,
            setLeaveSecureConversationDialogVisibleUseCase = setLeaveSecureConversationDialogVisibleUseCase,
            setChatScreenOpenUseCase = setChatScreenOpenUseCase,
            hasOngoingSecureConversationUseCase = hasOngoingSecureConversationUseCase,
            deviceMonitor = deviceMonitor
        )
        chatController.setView(chatView)
        verify(chatView, never()).showConnectionSnackBar()
        verify(chatView, never()).dismissConnectionSnackBar()
        Mockito.clearInvocations(chatView)
    }

    @After
    fun tearDown() {
        RxAndroidPlugins.reset()
    }

    private fun assertLiveChatState(state: ChatState) {
        state.apply {
            // Init chat
            assertTrue(isVisible)
            assertTrue(isSendButtonVisible)
            assertFalse(isSendButtonEnabled)
            assertTrue(isAttachmentAllowed)
            assertFalse(isAttachmentButtonEnabled)
            // Live chat state
            assertFalse(isSecureMessaging)
            assertFalse(isSecureConversationsUnavailableLabelVisible)
            assertFalse(isSecureConversationsTopBannerVisible)
            assertEquals(ChatInputMode.ENABLED_NO_ENGAGEMENT, chatInputMode)
        }
    }

    private fun assertSecureMessagingState(state: ChatState) {
        state.apply {
            // Init chat
            assertTrue(isVisible)
            assertTrue(isSendButtonVisible)
            assertFalse(isSendButtonEnabled)
            assertTrue(isAttachmentAllowed)
            assertTrue(isAttachmentButtonNeeded)
            assertFalse(isAttachmentButtonEnabled)
            // Secure Messaging state
            assertTrue(isSecureMessaging)
            assertTrue(isAttachmentButtonNeeded)
            assertEquals(ChatInputMode.ENABLED, chatInputMode)
        }
    }

    private fun assertSecureMessagingAvailableState(state: ChatState) {
        state.apply {
            assertTrue(isVisible)
            assertTrue(isSendButtonVisible)
            assertTrue(isSendButtonEnabled)
            assertTrue(isAttachmentAllowed)
            assertTrue(isSecureMessaging)
            assertTrue(isAttachmentButtonNeeded)
            assertFalse(isSecureConversationsUnavailableLabelVisible)
            assertEquals(ChatInputMode.ENABLED, chatInputMode)
        }
    }

    private fun assertSecureMessagingUnAvailableState(state: ChatState) {
        state.apply {
            assertTrue(isVisible)
            assertTrue(isSendButtonVisible)
            assertFalse(isSendButtonEnabled)
            assertTrue(isAttachmentAllowed)
            assertTrue(isSecureMessaging)
            assertTrue(isAttachmentButtonNeeded)
            assertTrue(isSecureConversationsUnavailableLabelVisible)
            assertEquals(ChatInputMode.DISABLED, chatInputMode)
        }
    }

    @Test
    fun `restoreChat calls ChatManager with action ChatManager_Action_ChatRestored`() {
        chatController.restoreChat()
        verify(chatManager).onChatAction(eq(ChatManager.Action.ChatRestored))
    }

    @Test
    fun `initChat calls restoreChat when intention is RETURN_TO_CHAT and chat is initialized`() {
        whenever(chatManager.initialize(any(), any(), any())) doReturn Flowable.empty()
        whenever(isMessagingAvailableUseCase()) doReturn Flowable.empty()
        whenever(fileUploadLimitNotExceededObservableUseCase()) doReturn Observable.empty()

        chatController.initChat(Intention.LIVE_CHAT)
        chatController.initChat(Intention.RETURN_TO_CHAT)
        verify(chatManager, times(1)).initialize(any(), any(), any())
        verify(updateOperatorDefaultImageUrlUseCase, times(2)).invoke()
        verify(chatManager).onChatAction(eq(ChatManager.Action.ChatRestored))
    }

    @Test
    fun `initChat calls initLiveChat when intention is RETURN_TO_CHAT and chat is not initialized`() {
        whenever(chatManager.initialize(any(), any(), any())) doReturn Flowable.empty()
        whenever(isMessagingAvailableUseCase()) doReturn Flowable.empty()
        whenever(fileUploadLimitNotExceededObservableUseCase()) doReturn Observable.empty()

        chatController.initChat(Intention.RETURN_TO_CHAT)
        verify(chatManager, times(1)).initialize(any(), any(), any())
        verify(updateOperatorDefaultImageUrlUseCase, times(1)).invoke()
        verify(chatManager, never()).onChatAction(eq(ChatManager.Action.ChatRestored))
        val stateKArgumentCaptor = argumentCaptor<ChatState>()

        verify(chatView).emitState(stateKArgumentCaptor.capture())

        assertLiveChatState(stateKArgumentCaptor.lastValue)
        assertTrue(stateKArgumentCaptor.lastValue.isAttachmentButtonNeeded)
    }

    @Test
    fun `initChat emits live chat state when intention is LIVE_CHAT`() {
        whenever(chatManager.initialize(any(), any(), any())) doReturn Flowable.empty()
        whenever(isMessagingAvailableUseCase()) doReturn Flowable.empty()
        whenever(fileUploadLimitNotExceededObservableUseCase()) doReturn Observable.empty()

        chatController.initChat(Intention.LIVE_CHAT)
        verify(chatManager).initialize(any(), any(), any())
        val stateKArgumentCaptor = argumentCaptor<ChatState>()

        verify(chatView).emitState(stateKArgumentCaptor.capture())

        assertLiveChatState(stateKArgumentCaptor.lastValue)
        assertTrue(stateKArgumentCaptor.lastValue.isAttachmentButtonNeeded)
    }

    @Test
    fun `initChat emits SC chat state when intention is SC_CHAT`() {
        whenever(chatManager.initialize(any(), any(), any())) doReturn Flowable.empty()
        whenever(isMessagingAvailableUseCase()) doReturn Flowable.just(true)
        whenever(fileUploadLimitNotExceededObservableUseCase()) doReturn Observable.empty()

        chatController.initChat(Intention.SC_CHAT)
        verify(chatManager).initialize(any(), any(), any())
        val stateKArgumentCaptor = argumentCaptor<ChatState>()

        verify(isMessagingAvailableUseCase, times(2)).invoke()
        verify(chatView, times(2)).emitState(stateKArgumentCaptor.capture())

        assertSecureMessagingState(stateKArgumentCaptor.firstValue)
        assertSecureMessagingAvailableState(stateKArgumentCaptor.lastValue)
    }

    @Test
    fun `initChat emits SC chat unavailable state when intention is SC_CHAT and messaging is not available`() {
        whenever(chatManager.initialize(any(), any(), any())) doReturn Flowable.empty()
        whenever(isMessagingAvailableUseCase()) doReturn Flowable.just(false)
        whenever(fileUploadLimitNotExceededObservableUseCase()) doReturn Observable.empty()
        whenever(manageSecureMessagingStatusUseCase.shouldBehaveAsSecureMessaging) doReturn true

        chatController.initChat(Intention.SC_CHAT)
        verify(chatManager).initialize(any(), any(), any())
        val stateKArgumentCaptor = argumentCaptor<ChatState>()

        verify(isMessagingAvailableUseCase, times(2)).invoke()
        verify(chatView, times(2)).emitState(stateKArgumentCaptor.capture())

        assertSecureMessagingState(stateKArgumentCaptor.firstValue)
        assertSecureMessagingUnAvailableState(stateKArgumentCaptor.lastValue)
    }

    @Test
    fun `initChat calls initLeaveCurrentConversationDialog with AUDIO when intention is SC_DIALOG_START_AUDIO`() {
        whenever(chatManager.initialize(any(), any(), any())) doReturn Flowable.empty()
        whenever(isMessagingAvailableUseCase()) doReturn Flowable.just(true)
        whenever(fileUploadLimitNotExceededObservableUseCase()) doReturn Observable.empty()

        chatController.initChat(Intention.SC_DIALOG_START_AUDIO)
        verify(chatManager).initialize(any(), any(), any())
        val stateKArgumentCaptor = argumentCaptor<ChatState>()

        verify(isMessagingAvailableUseCase, times(2)).invoke()
        verify(chatView, times(2)).emitState(stateKArgumentCaptor.capture())

        assertSecureMessagingState(stateKArgumentCaptor.firstValue)
        assertSecureMessagingAvailableState(stateKArgumentCaptor.lastValue)
        verify(dialogController).showLeaveCurrentConversationDialog(LeaveDialogAction.AUDIO)
    }

    @Test
    fun `initChat calls initLeaveCurrentConversationDialog with VIDEO when intention is SC_DIALOG_START_VIDEO`() {
        whenever(chatManager.initialize(any(), any(), any())) doReturn Flowable.empty()
        whenever(isMessagingAvailableUseCase()) doReturn Flowable.just(true)
        whenever(fileUploadLimitNotExceededObservableUseCase()) doReturn Observable.empty()

        chatController.initChat(Intention.SC_DIALOG_START_VIDEO)
        verify(chatManager).initialize(any(), any(), any())
        val stateKArgumentCaptor = argumentCaptor<ChatState>()

        verify(isMessagingAvailableUseCase, times(2)).invoke()
        verify(chatView, times(2)).emitState(stateKArgumentCaptor.capture())

        assertSecureMessagingState(stateKArgumentCaptor.firstValue)
        assertSecureMessagingAvailableState(stateKArgumentCaptor.lastValue)
        verify(dialogController).showLeaveCurrentConversationDialog(LeaveDialogAction.VIDEO)
    }

    @Test
    fun `initChat calls initLeaveCurrentConversationDialog with LIVE_CHAT when intention is SC_DIALOG_ENQUEUE_FOR_TEXT`() {
        whenever(chatManager.initialize(any(), any(), any())) doReturn Flowable.empty()
        whenever(isMessagingAvailableUseCase()) doReturn Flowable.just(true)
        whenever(fileUploadLimitNotExceededObservableUseCase()) doReturn Observable.empty()

        chatController.initChat(Intention.SC_DIALOG_ENQUEUE_FOR_TEXT)
        verify(chatManager).initialize(any(), any(), any())
        val stateKArgumentCaptor = argumentCaptor<ChatState>()

        verify(isMessagingAvailableUseCase, times(2)).invoke()
        verify(chatView, times(2)).emitState(stateKArgumentCaptor.capture())

        assertSecureMessagingState(stateKArgumentCaptor.firstValue)
        assertSecureMessagingAvailableState(stateKArgumentCaptor.lastValue)
        verify(dialogController).showLeaveCurrentConversationDialog(LeaveDialogAction.LIVE_CHAT)
    }

    @Test
    fun `initChat disables the attachment button when sc is unavailable`() {
        whenever(chatManager.initialize(any(), any(), any())) doReturn Flowable.empty()
        whenever(isMessagingAvailableUseCase()) doReturn Flowable.just(false)
        whenever(fileUploadLimitNotExceededObservableUseCase()) doReturn Observable.just(true)
        whenever(manageSecureMessagingStatusUseCase.shouldBehaveAsSecureMessaging) doReturn true

        chatController.initChat(Intention.SC_CHAT)
        verify(chatManager).initialize(any(), any(), any())
        val stateKArgumentCaptor = argumentCaptor<ChatState>()

        verify(isMessagingAvailableUseCase, times(2)).invoke()
        verify(fileUploadLimitNotExceededObservableUseCase).invoke()
        verify(manageSecureMessagingStatusUseCase, times(2)).shouldBehaveAsSecureMessaging
        verify(chatView, times(2)).emitState(stateKArgumentCaptor.capture())

        assertFalse(stateKArgumentCaptor.lastValue.isAttachmentButtonEnabled)
    }

    @Test
    fun `initChat disables the attachment button when sc is available but limit is exceeded`() {
        whenever(chatManager.initialize(any(), any(), any())) doReturn Flowable.empty()
        whenever(isMessagingAvailableUseCase()) doReturn Flowable.just(true)
        whenever(fileUploadLimitNotExceededObservableUseCase()) doReturn Observable.just(false)
        whenever(manageSecureMessagingStatusUseCase.shouldBehaveAsSecureMessaging) doReturn true

        chatController.initChat(Intention.SC_CHAT)
        verify(chatManager).initialize(any(), any(), any())
        val stateKArgumentCaptor = argumentCaptor<ChatState>()

        verify(isMessagingAvailableUseCase, times(2)).invoke()
        verify(fileUploadLimitNotExceededObservableUseCase).invoke()
        verify(manageSecureMessagingStatusUseCase).shouldBehaveAsSecureMessaging
        verify(chatView, times(2)).emitState(stateKArgumentCaptor.capture())

        assertFalse(stateKArgumentCaptor.lastValue.isAttachmentButtonEnabled)
    }

    @Test
    fun `initChat enables the attachment button when history is loaded sc is available and limit is not exceeded`() {
        whenever(chatManager.initialize(any(), any(), any())) doReturn Flowable.empty()
        whenever(isMessagingAvailableUseCase()) doReturn Flowable.just(true)
        whenever(fileUploadLimitNotExceededObservableUseCase()) doReturn Observable.just(true)
        whenever(manageSecureMessagingStatusUseCase.shouldBehaveAsSecureMessaging) doReturn true

        chatController.initChat(Intention.SC_CHAT)
        verify(chatManager).initialize(any(), any(), any())
        val stateKArgumentCaptor = argumentCaptor<ChatState>()

        verify(isMessagingAvailableUseCase, times(2)).invoke()
        verify(fileUploadLimitNotExceededObservableUseCase).invoke()
        verify(manageSecureMessagingStatusUseCase).shouldBehaveAsSecureMessaging
        verify(chatView, times(3)).emitState(stateKArgumentCaptor.capture())

        assertTrue(stateKArgumentCaptor.lastValue.isAttachmentButtonEnabled)
    }

    @Test
    fun `initChat disables the attachment button when history is loaded and upload limit is exceeded`() {
        whenever(chatManager.initialize(any(), any(), any())) doReturn Flowable.empty()
        whenever(isMessagingAvailableUseCase()) doReturn Flowable.just(true)
        whenever(fileUploadLimitNotExceededObservableUseCase()) doReturn Observable.just(false)
        whenever(manageSecureMessagingStatusUseCase.shouldBehaveAsSecureMessaging) doReturn false

        chatController.initChat(Intention.SC_CHAT)
        verify(chatManager).initialize(any(), any(), any())
        val stateKArgumentCaptor = argumentCaptor<ChatState>()

        verify(isMessagingAvailableUseCase, times(2)).invoke()
        verify(fileUploadLimitNotExceededObservableUseCase).invoke()
        verify(manageSecureMessagingStatusUseCase).shouldBehaveAsSecureMessaging
        verify(chatView, times(2)).emitState(stateKArgumentCaptor.capture())

        assertFalse(stateKArgumentCaptor.lastValue.isAttachmentButtonEnabled)
    }

    @Test
    fun `initChat enables the attachment button when history is loaded and upload limit is not exceeded`() {
        whenever(chatManager.initialize(any(), any(), any())) doReturn Flowable.empty()
        whenever(isMessagingAvailableUseCase()) doReturn Flowable.just(true)
        whenever(fileUploadLimitNotExceededObservableUseCase()) doReturn Observable.just(true)
        whenever(manageSecureMessagingStatusUseCase.shouldBehaveAsSecureMessaging) doReturn false

        chatController.initChat(Intention.SC_CHAT)
        verify(chatManager).initialize(any(), any(), any())
        val stateKArgumentCaptor = argumentCaptor<ChatState>()

        verify(isMessagingAvailableUseCase, times(2)).invoke()
        verify(fileUploadLimitNotExceededObservableUseCase).invoke()
        verify(manageSecureMessagingStatusUseCase).shouldBehaveAsSecureMessaging
        verify(chatView, times(2)).emitState(stateKArgumentCaptor.capture())

        assertFalse(stateKArgumentCaptor.lastValue.isAttachmentButtonEnabled)
    }

    @Test
    fun `leaveCurrentConversationDialogLeaveClicked dismisses dialog and starts live chat`() {
        whenever(chatManager.initialize(any(), any(), any())) doReturn Flowable.empty()
        whenever(isMessagingAvailableUseCase()) doReturn Flowable.never()
        whenever(fileUploadLimitNotExceededObservableUseCase()) doReturn Observable.empty()

        doAnswer {
            (it.arguments.first() as ((shouldShow: Boolean) -> Unit)).invoke(true)
        }.whenever(confirmationDialogUseCase)(any())

        val stateKArgumentCaptor = argumentCaptor<ChatState>()

        chatController.initChat(Intention.SC_DIALOG_START_VIDEO)
        chatController.leaveCurrentConversationDialogLeaveClicked(LeaveDialogAction.LIVE_CHAT)
        verify(dialogController).dismissCurrentDialog()
        verify(manageSecureMessagingStatusUseCase).updateSecureMessagingStatus(false)

        verify(chatView, atLeastOnce()).emitState(stateKArgumentCaptor.capture())
        assertLiveChatState(stateKArgumentCaptor.lastValue)

        verify(chatManager).onChatAction(eq(ChatManager.Action.QueuingStarted))
        verify(dialogController).showEngagementConfirmationDialog()
    }

    @Test
    fun `leaveCurrentConversationDialogLeaveClicked dismisses dialog and launches video call`() {
        chatController.leaveCurrentConversationDialogLeaveClicked(LeaveDialogAction.VIDEO)
        verify(dialogController).dismissCurrentDialog()
        verify(chatView).launchCall(Engagement.MediaType.VIDEO)
    }

    @Test
    fun `leaveCurrentConversationDialogLeaveClicked dismisses dialog and launches audio call`() {
        chatController.leaveCurrentConversationDialogLeaveClicked(LeaveDialogAction.AUDIO)
        verify(dialogController).dismissCurrentDialog()
        verify(chatView).launchCall(Engagement.MediaType.AUDIO)
    }

    @Test
    fun `leaveCurrentConversationDialogStayClicked dismisses dialog`() {
        chatController.leaveCurrentConversationDialogStayClicked()
        verify(dialogController).dismissCurrentDialog()
    }

    @Test
    fun `onGvaButtonClicked triggers viewCallback showBroadcastNotSupportedToast when gva type is BroadcastEvent`() {
        val gvaButton: GvaButton = mock()
        whenever(determineGvaButtonTypeUseCase(any())) doReturn Gva.ButtonType.BroadcastEvent
        chatController.onGvaButtonClicked(gvaButton)
        verify(chatView).showBroadcastNotSupportedToast()
    }

    @Test
    fun `onGvaButtonClicked triggers viewCallback requestOpenUri when gva type is Url`() {
        val gvaButton: GvaButton = mock()
        val uri: Uri = mock()
        whenever(determineGvaButtonTypeUseCase(any())) doReturn Gva.ButtonType.Url(uri)
        chatController.onGvaButtonClicked(gvaButton)
        verify(chatView).requestOpenUri(uri)
    }

    @Test
    fun `onGvaButtonClicked triggers viewCallback requestOpenDialer when gva type is Phone`() {
        val gvaButton: GvaButton = mock()
        val uri: Uri = mock()
        whenever(determineGvaButtonTypeUseCase(any())) doReturn Gva.ButtonType.Phone(uri)
        chatController.onGvaButtonClicked(gvaButton)
        verify(chatView).requestOpenDialer(uri)
    }

    @Test
    fun `onGvaButtonClicked triggers viewCallback requestOpenEmailClient when gva type is Email`() {
        val gvaButton: GvaButton = mock()
        val uri: Uri = mock()
        whenever(determineGvaButtonTypeUseCase(any())) doReturn Gva.ButtonType.Email(uri)
        chatController.onGvaButtonClicked(gvaButton)
        verify(chatView).requestOpenEmailClient(uri)
    }

    @Test
    fun `onGvaButtonClicked triggers sendMessageUseCase when gva type is PostBack`() {
        val gvaButton = GvaButton(text = "text", value = "value")
        val singleChoiceAttachment = gvaButton.toResponse()
        whenever(determineGvaButtonTypeUseCase(any())) doReturn Gva.ButtonType.PostBack(singleChoiceAttachment)
        chatController.onGvaButtonClicked(gvaButton)
        assertEquals(gvaButton.text, singleChoiceAttachment.selectedOptionText)
        assertEquals(gvaButton.value, singleChoiceAttachment.selectedOption)
        verify(sendMessageUseCase).execute(any<SingleChoiceAttachment>(), any())
    }

    @Test
    fun `onScTopBannerItemClicked will show leave current conversation dialog when item type is AudioCall and no ongoing SC`() {
        chatController.onScTopBannerItemClicked(itemType = EntryWidgetContract.ItemType.AudioCall)
        val onHasOngoingSecureConversationCaptor = argumentCaptor<() -> Unit>()
        val onNoOngoingSecureConversationCaptor = argumentCaptor<() -> Unit>()
        verify(hasOngoingSecureConversationUseCase).invoke(
            onHasOngoingSecureConversationCaptor.capture(),
            onNoOngoingSecureConversationCaptor.capture()
        )
        onNoOngoingSecureConversationCaptor.lastValue.invoke()
        verify(chatView).launchCall(Engagement.MediaType.AUDIO)
    }

    @Test
    fun `onScTopBannerItemClicked will show leave current conversation dialog when item type is Chat and no ongoing SC`() {
        val stateKArgumentCaptor = argumentCaptor<ChatState>()
        chatController.onScTopBannerItemClicked(itemType = EntryWidgetContract.ItemType.Chat)
        val onHasOngoingSecureConversationCaptor = argumentCaptor<() -> Unit>()
        val onNoOngoingSecureConversationCaptor = argumentCaptor<() -> Unit>()
        verify(hasOngoingSecureConversationUseCase).invoke(
            onHasOngoingSecureConversationCaptor.capture(),
            onNoOngoingSecureConversationCaptor.capture()
        )
        onNoOngoingSecureConversationCaptor.lastValue.invoke()
        verify(chatView, atLeastOnce()).emitState(stateKArgumentCaptor.capture())
        assertLiveChatState(stateKArgumentCaptor.lastValue)

        verify(chatManager).onChatAction(eq(ChatManager.Action.QueuingStarted))
        assertFalse(stateKArgumentCaptor.lastValue.isAttachmentButtonNeeded)
    }

    @Test
    fun `onScTopBannerItemClicked will show leave current conversation dialog when item type is VideoCall and no ongoing SC`() {
        chatController.onScTopBannerItemClicked(itemType = EntryWidgetContract.ItemType.VideoCall)
        val onHasOngoingSecureConversationCaptor = argumentCaptor<() -> Unit>()
        val onNoOngoingSecureConversationCaptor = argumentCaptor<() -> Unit>()
        verify(hasOngoingSecureConversationUseCase).invoke(
            onHasOngoingSecureConversationCaptor.capture(),
            onNoOngoingSecureConversationCaptor.capture()
        )
        onNoOngoingSecureConversationCaptor.lastValue.invoke()
        verify(chatView).launchCall(Engagement.MediaType.VIDEO)
    }

    @Test
    fun `onScTopBannerItemClicked will not show leave current conversation dialog when item type is different and no ongoing SC`() {
        chatController.onScTopBannerItemClicked(itemType = EntryWidgetContract.ItemType.ErrorState)
        val onHasOngoingSecureConversationCaptor = argumentCaptor<() -> Unit>()
        val onNoOngoingSecureConversationCaptor = argumentCaptor<() -> Unit>()
        verify(hasOngoingSecureConversationUseCase).invoke(
            onHasOngoingSecureConversationCaptor.capture(),
            onNoOngoingSecureConversationCaptor.capture()
        )
        onNoOngoingSecureConversationCaptor.lastValue.invoke()
        verify(chatView, never()).launchCall(Engagement.MediaType.VIDEO)
        verify(chatView, never()).launchCall(Engagement.MediaType.AUDIO)
        verify(chatManager, never()).onChatAction(eq(ChatManager.Action.QueuingStarted))
    }

    @Test
    fun `onScTopBannerItemClicked will show leave current conversation dialog when item type is AudioCall`() {
        chatController.onScTopBannerItemClicked(itemType = EntryWidgetContract.ItemType.AudioCall)
        val onHasOngoingSecureConversationCaptor = argumentCaptor<() -> Unit>()
        val onNoOngoingSecureConversationCaptor = argumentCaptor<() -> Unit>()
        verify(hasOngoingSecureConversationUseCase).invoke(
            onHasOngoingSecureConversationCaptor.capture(),
            onNoOngoingSecureConversationCaptor.capture()
        )
        onHasOngoingSecureConversationCaptor.lastValue.invoke()
        verify(dialogController).showLeaveCurrentConversationDialog(eq(LeaveDialogAction.AUDIO))
    }

    @Test
    fun `onScTopBannerItemClicked will show leave current conversation dialog when item type is Chat`() {
        chatController.onScTopBannerItemClicked(itemType = EntryWidgetContract.ItemType.Chat)
        val onHasOngoingSecureConversationCaptor = argumentCaptor<() -> Unit>()
        val onNoOngoingSecureConversationCaptor = argumentCaptor<() -> Unit>()
        verify(hasOngoingSecureConversationUseCase).invoke(
            onHasOngoingSecureConversationCaptor.capture(),
            onNoOngoingSecureConversationCaptor.capture()
        )
        onHasOngoingSecureConversationCaptor.lastValue.invoke()
        verify(dialogController).showLeaveCurrentConversationDialog(eq(LeaveDialogAction.LIVE_CHAT))
    }

    @Test
    fun `onScTopBannerItemClicked will show leave current conversation dialog when item type is VideoCall`() {
        chatController.onScTopBannerItemClicked(itemType = EntryWidgetContract.ItemType.VideoCall)
        val onHasOngoingSecureConversationCaptor = argumentCaptor<() -> Unit>()
        val onNoOngoingSecureConversationCaptor = argumentCaptor<() -> Unit>()
        verify(hasOngoingSecureConversationUseCase).invoke(
            onHasOngoingSecureConversationCaptor.capture(),
            onNoOngoingSecureConversationCaptor.capture()
        )
        onHasOngoingSecureConversationCaptor.lastValue.invoke()
        verify(dialogController).showLeaveCurrentConversationDialog(eq(LeaveDialogAction.VIDEO))
    }

    @Test
    fun `onScTopBannerItemClicked will not show leave current conversation dialog when item type is different`() {
        chatController.onScTopBannerItemClicked(itemType = EntryWidgetContract.ItemType.ErrorState)
        val onHasOngoingSecureConversationCaptor = argumentCaptor<() -> Unit>()
        val onNoOngoingSecureConversationCaptor = argumentCaptor<() -> Unit>()
        verify(hasOngoingSecureConversationUseCase).invoke(
            onHasOngoingSecureConversationCaptor.capture(),
            onNoOngoingSecureConversationCaptor.capture()
        )
        onHasOngoingSecureConversationCaptor.lastValue.invoke()
        verify(dialogController, never()).showLeaveCurrentConversationDialog(eq(LeaveDialogAction.AUDIO))
        verify(dialogController, never()).showLeaveCurrentConversationDialog(eq(LeaveDialogAction.LIVE_CHAT))
        verify(dialogController, never()).showLeaveCurrentConversationDialog(eq(LeaveDialogAction.AUDIO))
    }

    @Test
    fun `onResume subscribes to network state`() {
        chatController.onResume()

        verify(chatView, times(1)).dismissConnectionSnackBar()

        networkStateProcessor.onNext(NetworkState.DISCONNECTED)
        verify(chatView, times(1)).showConnectionSnackBar()
    }

    @Test
    fun `onPause disposes network state subscription`() {
        `onResume subscribes to network state`()

        chatController.onPause()

        networkStateProcessor.onNext(NetworkState.CONNECTED)
        verify(chatView, times(1)).dismissConnectionSnackBar()

        networkStateProcessor.onNext(NetworkState.DISCONNECTED)
        verify(chatView, times(1)).showConnectionSnackBar()
    }
}
