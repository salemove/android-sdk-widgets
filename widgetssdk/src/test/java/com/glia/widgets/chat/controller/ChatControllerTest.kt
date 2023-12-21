package com.glia.widgets.chat.controller

import android.net.Uri
import com.glia.androidsdk.chat.SingleChoiceAttachment
import com.glia.widgets.chat.ChatManager
import com.glia.widgets.chat.ChatViewCallback
import com.glia.widgets.chat.domain.GliaOnOperatorTypingUseCase
import com.glia.widgets.chat.domain.GliaSendMessagePreviewUseCase
import com.glia.widgets.chat.domain.GliaSendMessageUseCase
import com.glia.widgets.chat.domain.IsAuthenticatedUseCase
import com.glia.widgets.chat.domain.IsFromCallScreenUseCase
import com.glia.widgets.chat.domain.IsSecureConversationsChatAvailableUseCase
import com.glia.widgets.chat.domain.IsShowSendButtonUseCase
import com.glia.widgets.chat.domain.SiteInfoUseCase
import com.glia.widgets.chat.domain.UpdateFromCallScreenUseCase
import com.glia.widgets.chat.domain.gva.DetermineGvaButtonTypeUseCase
import com.glia.widgets.chat.model.Gva
import com.glia.widgets.chat.model.GvaButton
import com.glia.widgets.core.callvisualizer.domain.IsCallVisualizerUseCase
import com.glia.widgets.core.dialog.DialogController
import com.glia.widgets.core.dialog.domain.ConfirmationDialogLinksUseCase
import com.glia.widgets.core.dialog.domain.IsShowOverlayPermissionRequestDialogUseCase
import com.glia.widgets.core.engagement.domain.ConfirmationDialogUseCase
import com.glia.widgets.core.engagement.domain.GetEngagementStateFlowableUseCase
import com.glia.widgets.core.engagement.domain.GliaEndEngagementUseCase
import com.glia.widgets.core.engagement.domain.GliaOnEngagementEndUseCase
import com.glia.widgets.core.engagement.domain.GliaOnEngagementUseCase
import com.glia.widgets.core.engagement.domain.IsOngoingEngagementUseCase
import com.glia.widgets.core.engagement.domain.IsQueueingEngagementUseCase
import com.glia.widgets.core.engagement.domain.SetEngagementConfigUseCase
import com.glia.widgets.core.engagement.domain.UpdateOperatorDefaultImageUrlUseCase
import com.glia.widgets.core.fileupload.domain.AddFileAttachmentsObserverUseCase
import com.glia.widgets.core.fileupload.domain.AddFileToAttachmentAndUploadUseCase
import com.glia.widgets.core.fileupload.domain.GetFileAttachmentsUseCase
import com.glia.widgets.core.fileupload.domain.RemoveFileAttachmentObserverUseCase
import com.glia.widgets.core.fileupload.domain.RemoveFileAttachmentUseCase
import com.glia.widgets.core.fileupload.domain.SupportedFileCountCheckUseCase
import com.glia.widgets.core.mediaupgradeoffer.MediaUpgradeOfferRepository
import com.glia.widgets.core.mediaupgradeoffer.domain.AcceptMediaUpgradeOfferUseCase
import com.glia.widgets.core.mediaupgradeoffer.domain.AddMediaUpgradeOfferCallbackUseCase
import com.glia.widgets.core.mediaupgradeoffer.domain.RemoveMediaUpgradeOfferCallbackUseCase
import com.glia.widgets.core.notification.domain.CallNotificationUseCase
import com.glia.widgets.core.operator.domain.AddOperatorMediaStateListenerUseCase
import com.glia.widgets.core.queue.domain.GliaCancelQueueTicketUseCase
import com.glia.widgets.core.queue.domain.GliaQueueForChatEngagementUseCase
import com.glia.widgets.core.queue.domain.QueueTicketStateChangeToUnstaffedUseCase
import com.glia.widgets.core.secureconversations.domain.IsSecureEngagementUseCase
import com.glia.widgets.filepreview.domain.usecase.DownloadFileUseCase
import com.glia.widgets.filepreview.domain.usecase.IsFileReadyForPreviewUseCase
import com.glia.widgets.helper.TimeCounter
import com.glia.widgets.view.MessagesNotSeenHandler
import com.glia.widgets.view.MinimizeHandler
import junit.framework.TestCase.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ChatControllerTest {
    private lateinit var chatViewCallback: ChatViewCallback
    private lateinit var mediaUpgradeOfferRepository: MediaUpgradeOfferRepository
    private lateinit var callTimer: TimeCounter
    private lateinit var minimizeHandler: MinimizeHandler
    private lateinit var dialogController: DialogController
    private lateinit var messagesNotSeenHandler: MessagesNotSeenHandler
    private lateinit var callNotificationUseCase: CallNotificationUseCase
    private lateinit var queueForChatEngagementUseCase: GliaQueueForChatEngagementUseCase
    private lateinit var getEngagementUseCase: GliaOnEngagementUseCase
    private lateinit var engagementEndUseCase: GliaOnEngagementEndUseCase
    private lateinit var onOperatorTypingUseCase: GliaOnOperatorTypingUseCase
    private lateinit var sendMessagePreviewUseCase: GliaSendMessagePreviewUseCase
    private lateinit var sendMessageUseCase: GliaSendMessageUseCase
    private lateinit var addOperatorMediaStateListenerUseCase: AddOperatorMediaStateListenerUseCase
    private lateinit var cancelQueueTicketUseCase: GliaCancelQueueTicketUseCase
    private lateinit var endEngagementUseCase: GliaEndEngagementUseCase
    private lateinit var addFileToAttachmentAndUploadUseCase: AddFileToAttachmentAndUploadUseCase
    private lateinit var addFileAttachmentsObserverUseCase: AddFileAttachmentsObserverUseCase
    private lateinit var removeFileAttachmentObserverUseCase: RemoveFileAttachmentObserverUseCase
    private lateinit var getFileAttachmentsUseCase: GetFileAttachmentsUseCase
    private lateinit var removeFileAttachmentUseCase: RemoveFileAttachmentUseCase
    private lateinit var supportedFileCountCheckUseCase: SupportedFileCountCheckUseCase
    private lateinit var isShowSendButtonUseCase: IsShowSendButtonUseCase
    private lateinit var isShowOverlayPermissionRequestDialogUseCase: IsShowOverlayPermissionRequestDialogUseCase
    private lateinit var downloadFileUseCase: DownloadFileUseCase
    private lateinit var siteInfoUseCase: SiteInfoUseCase
    private lateinit var surveyUseCase: GliaSurveyUseCase
    private lateinit var getGliaEngagementStateFlowableUseCase: GetEngagementStateFlowableUseCase
    private lateinit var isFromCallScreenUseCase: IsFromCallScreenUseCase
    private lateinit var updateFromCallScreenUseCase: UpdateFromCallScreenUseCase
    private lateinit var ticketStateChangeToUnstaffedUseCase: QueueTicketStateChangeToUnstaffedUseCase
    private lateinit var addMediaUpgradeOfferCallbackUseCase: AddMediaUpgradeOfferCallbackUseCase
    private lateinit var removeMediaUpgradeOfferCallbackUseCase: RemoveMediaUpgradeOfferCallbackUseCase
    private lateinit var isOngoingEngagementUseCase: IsOngoingEngagementUseCase
    private lateinit var isSecureEngagementUseCase: IsSecureEngagementUseCase
    private lateinit var engagementConfigUseCase: SetEngagementConfigUseCase
    private lateinit var isSecureConversationsChatAvailableUseCase: IsSecureConversationsChatAvailableUseCase
    private lateinit var isQueueingEngagementUseCase: IsQueueingEngagementUseCase
    private lateinit var hasPendingSurveyUseCase: HasPendingSurveyUseCase
    private lateinit var setPendingSurveyUsedUseCase: SetPendingSurveyUsedUseCase
    private lateinit var isCallVisualizerUseCase: IsCallVisualizerUseCase
    private lateinit var isFileReadyForPreviewUseCase: IsFileReadyForPreviewUseCase
    private lateinit var acceptMediaUpgradeOfferUseCase: AcceptMediaUpgradeOfferUseCase
    private lateinit var determineGvaButtonTypeUseCase: DetermineGvaButtonTypeUseCase
    private lateinit var updateOperatorDefaultImageUrlUseCase: UpdateOperatorDefaultImageUrlUseCase
    private lateinit var confirmationDialogUseCase: ConfirmationDialogUseCase
    private lateinit var confirmationDialogLinksUseCase: ConfirmationDialogLinksUseCase

    private lateinit var chatController: ChatController
    private lateinit var isAuthenticatedUseCase: IsAuthenticatedUseCase
    private lateinit var chatManager: ChatManager

    @Before
    fun setUp() {
        chatViewCallback = mock()
        mediaUpgradeOfferRepository = mock()
        callTimer = mock()
        minimizeHandler = mock()
        dialogController = mock()
        messagesNotSeenHandler = mock()
        callNotificationUseCase = mock()
        queueForChatEngagementUseCase = mock()
        getEngagementUseCase = mock()
        engagementEndUseCase = mock()
        onOperatorTypingUseCase = mock()
        sendMessagePreviewUseCase = mock()
        sendMessageUseCase = mock()
        addOperatorMediaStateListenerUseCase = mock()
        cancelQueueTicketUseCase = mock()
        endEngagementUseCase = mock()
        addFileToAttachmentAndUploadUseCase = mock()
        addFileAttachmentsObserverUseCase = mock()
        removeFileAttachmentObserverUseCase = mock()
        getFileAttachmentsUseCase = mock()
        removeFileAttachmentUseCase = mock()
        supportedFileCountCheckUseCase = mock()
        isShowSendButtonUseCase = mock()
        isShowOverlayPermissionRequestDialogUseCase = mock()
        downloadFileUseCase = mock()
        siteInfoUseCase = mock()
        surveyUseCase = mock()
        getGliaEngagementStateFlowableUseCase = mock()
        isFromCallScreenUseCase = mock()
        updateFromCallScreenUseCase = mock()
        ticketStateChangeToUnstaffedUseCase = mock()
        addMediaUpgradeOfferCallbackUseCase = mock()
        removeMediaUpgradeOfferCallbackUseCase = mock()
        isOngoingEngagementUseCase = mock()
        isSecureEngagementUseCase = mock()
        engagementConfigUseCase = mock()
        isSecureConversationsChatAvailableUseCase = mock()
        isQueueingEngagementUseCase = mock()
        hasPendingSurveyUseCase = mock()
        setPendingSurveyUsedUseCase = mock()
        isCallVisualizerUseCase = mock()
        isFileReadyForPreviewUseCase = mock()
        acceptMediaUpgradeOfferUseCase = mock()
        determineGvaButtonTypeUseCase = mock()
        isAuthenticatedUseCase = mock()
        updateOperatorDefaultImageUrlUseCase = mock()
        confirmationDialogUseCase = mock()
        confirmationDialogLinksUseCase = mock()
        chatManager = mock()

        chatController = ChatController(
            chatViewCallback = chatViewCallback,
            mediaUpgradeOfferRepository = mediaUpgradeOfferRepository,
            callTimer = callTimer,
            minimizeHandler = minimizeHandler,
            dialogController = dialogController,
            messagesNotSeenHandler = messagesNotSeenHandler,
            callNotificationUseCase = callNotificationUseCase,
            queueForChatEngagementUseCase = queueForChatEngagementUseCase,
            getEngagementUseCase = getEngagementUseCase,
            engagementEndUseCase = engagementEndUseCase,
            onOperatorTypingUseCase = onOperatorTypingUseCase,
            sendMessagePreviewUseCase = sendMessagePreviewUseCase,
            sendMessageUseCase = sendMessageUseCase,
            addOperatorMediaStateListenerUseCase = addOperatorMediaStateListenerUseCase,
            cancelQueueTicketUseCase = cancelQueueTicketUseCase,
            endEngagementUseCase = endEngagementUseCase,
            addFileToAttachmentAndUploadUseCase = addFileToAttachmentAndUploadUseCase,
            addFileAttachmentsObserverUseCase = addFileAttachmentsObserverUseCase,
            removeFileAttachmentObserverUseCase = removeFileAttachmentObserverUseCase,
            getFileAttachmentsUseCase = getFileAttachmentsUseCase,
            removeFileAttachmentUseCase = removeFileAttachmentUseCase,
            supportedFileCountCheckUseCase = supportedFileCountCheckUseCase,
            isShowSendButtonUseCase = isShowSendButtonUseCase,
            isShowOverlayPermissionRequestDialogUseCase = isShowOverlayPermissionRequestDialogUseCase,
            downloadFileUseCase = downloadFileUseCase,
            siteInfoUseCase = siteInfoUseCase,
            surveyUseCase = surveyUseCase,
            getGliaEngagementStateFlowableUseCase = getGliaEngagementStateFlowableUseCase,
            isFromCallScreenUseCase = isFromCallScreenUseCase,
            updateFromCallScreenUseCase = updateFromCallScreenUseCase,
            ticketStateChangeToUnstaffedUseCase = ticketStateChangeToUnstaffedUseCase,
            hasOngoingEngagementUseCase = isOngoingEngagementUseCase,
            isSecureEngagementUseCase = isSecureEngagementUseCase,
            engagementConfigUseCase = engagementConfigUseCase,
            addMediaUpgradeCallbackUseCase = addMediaUpgradeOfferCallbackUseCase,
            removeMediaUpgradeCallbackUseCase = removeMediaUpgradeOfferCallbackUseCase,
            isSecureEngagementAvailableUseCase = isSecureConversationsChatAvailableUseCase,
            isQueueingEngagementUseCase = isQueueingEngagementUseCase,
            hasPendingSurveyUseCase = hasPendingSurveyUseCase,
            setPendingSurveyUsedUseCase = setPendingSurveyUsedUseCase,
            isCurrentEngagementCallVisualizerUseCase = isCallVisualizerUseCase,
            isFileReadyForPreviewUseCase = isFileReadyForPreviewUseCase,
            acceptMediaUpgradeOfferUseCase = acceptMediaUpgradeOfferUseCase,
            determineGvaButtonTypeUseCase = determineGvaButtonTypeUseCase,
            isAuthenticatedUseCase = isAuthenticatedUseCase,
            updateOperatorDefaultImageUrlUseCase = updateOperatorDefaultImageUrlUseCase,
            confirmationDialogUseCase = confirmationDialogUseCase,
            confirmationDialogLinksUseCase = confirmationDialogLinksUseCase,
            chatManager = chatManager
        )
    }

    @Test
    fun `onGvaButtonClicked triggers viewCallback showBroadcastNotSupportedToast when gva type is BroadcastEvent`() {
        val gvaButton: GvaButton = mock()
        whenever(determineGvaButtonTypeUseCase(any())) doReturn Gva.ButtonType.BroadcastEvent
        chatController.onGvaButtonClicked(gvaButton)
        verify(chatViewCallback).showBroadcastNotSupportedToast()
    }

    @Test
    fun `onGvaButtonClicked triggers viewCallback requestOpenUri when gva type is Url`() {
        val gvaButton: GvaButton = mock()
        val uri: Uri = mock()
        whenever(determineGvaButtonTypeUseCase(any())) doReturn Gva.ButtonType.Url(uri)
        chatController.onGvaButtonClicked(gvaButton)
        verify(chatViewCallback).requestOpenUri(uri)
    }

    @Test
    fun `onGvaButtonClicked triggers viewCallback requestOpenDialer when gva type is Phone`() {
        val gvaButton: GvaButton = mock()
        val uri: Uri = mock()
        whenever(determineGvaButtonTypeUseCase(any())) doReturn Gva.ButtonType.Phone(uri)
        chatController.onGvaButtonClicked(gvaButton)
        verify(chatViewCallback).requestOpenDialer(uri)
    }

    @Test
    fun `onGvaButtonClicked triggers viewCallback requestOpenEmailClient when gva type is Email`() {
        val gvaButton: GvaButton = mock()
        val uri: Uri = mock()
        whenever(determineGvaButtonTypeUseCase(any())) doReturn Gva.ButtonType.Email(uri)
        chatController.onGvaButtonClicked(gvaButton)
        verify(chatViewCallback).requestOpenEmailClient(uri)
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
}
