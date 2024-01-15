package com.glia.widgets.chat.controller

import android.net.Uri
import com.glia.androidsdk.chat.SingleChoiceAttachment
import com.glia.widgets.chat.ChatContract
import com.glia.widgets.chat.ChatManager
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
import com.glia.widgets.core.dialog.DialogContract
import com.glia.widgets.core.dialog.domain.ConfirmationDialogLinksUseCase
import com.glia.widgets.core.dialog.domain.IsShowOverlayPermissionRequestDialogUseCase
import com.glia.widgets.core.engagement.domain.ConfirmationDialogUseCase
import com.glia.widgets.core.engagement.domain.SetEngagementConfigUseCase
import com.glia.widgets.core.engagement.domain.UpdateOperatorDefaultImageUrlUseCase
import com.glia.widgets.core.fileupload.domain.AddFileAttachmentsObserverUseCase
import com.glia.widgets.core.fileupload.domain.AddFileToAttachmentAndUploadUseCase
import com.glia.widgets.core.fileupload.domain.GetFileAttachmentsUseCase
import com.glia.widgets.core.fileupload.domain.RemoveFileAttachmentObserverUseCase
import com.glia.widgets.core.fileupload.domain.RemoveFileAttachmentUseCase
import com.glia.widgets.core.fileupload.domain.SupportedFileCountCheckUseCase
import com.glia.widgets.core.notification.domain.CallNotificationUseCase
import com.glia.widgets.core.secureconversations.domain.IsSecureEngagementUseCase
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
import com.glia.widgets.helper.TimeCounter
import com.glia.widgets.view.MessagesNotSeenHandler
import com.glia.widgets.view.MinimizeHandler
import io.reactivex.Flowable
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
    private lateinit var callTimer: TimeCounter
    private lateinit var minimizeHandler: MinimizeHandler
    private lateinit var dialogController: DialogContract.Controller
    private lateinit var messagesNotSeenHandler: MessagesNotSeenHandler
    private lateinit var callNotificationUseCase: CallNotificationUseCase
    private lateinit var sendMessagePreviewUseCase: GliaSendMessagePreviewUseCase
    private lateinit var sendMessageUseCase: GliaSendMessageUseCase
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
    private lateinit var isFromCallScreenUseCase: IsFromCallScreenUseCase
    private lateinit var updateFromCallScreenUseCase: UpdateFromCallScreenUseCase
    private lateinit var isSecureEngagementUseCase: IsSecureEngagementUseCase
    private lateinit var engagementConfigUseCase: SetEngagementConfigUseCase
    private lateinit var isSecureConversationsChatAvailableUseCase: IsSecureConversationsChatAvailableUseCase
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
    private lateinit var mediaUpgradeOfferUseCase: MediaUpgradeOfferUseCase
    private lateinit var acceptMediaUpgradeOfferUseCase: AcceptMediaUpgradeOfferUseCase
    private lateinit var declineMediaUpgradeOfferUseCase: DeclineMediaUpgradeOfferUseCase
    private lateinit var isQueueingOrEngagementUseCase: IsQueueingOrEngagementUseCase
    private lateinit var enqueueForEngagementUseCase: EnqueueForEngagementUseCase

    private lateinit var chatController: ChatController
    private lateinit var isAuthenticatedUseCase: IsAuthenticatedUseCase
    private lateinit var chatManager: ChatManager

    private lateinit var chatView: ChatContract.View

    @Before
    fun setUp() {
        callTimer = mock()
        minimizeHandler = mock()
        dialogController = mock()
        messagesNotSeenHandler = mock()
        callNotificationUseCase = mock()
        sendMessagePreviewUseCase = mock()
        sendMessageUseCase = mock()
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
        isFromCallScreenUseCase = mock()
        updateFromCallScreenUseCase = mock()
        isSecureEngagementUseCase = mock()
        engagementConfigUseCase = mock()
        isSecureConversationsChatAvailableUseCase = mock()
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
        mediaUpgradeOfferUseCase = mock()
        acceptMediaUpgradeOfferUseCase = mock()
        declineMediaUpgradeOfferUseCase = mock()
        isQueueingOrEngagementUseCase = mock()
        enqueueForEngagementUseCase = mock()
        chatView = mock()

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
            removeFileAttachmentObserverUseCase = removeFileAttachmentObserverUseCase,
            getFileAttachmentsUseCase = getFileAttachmentsUseCase,
            removeFileAttachmentUseCase = removeFileAttachmentUseCase,
            supportedFileCountCheckUseCase = supportedFileCountCheckUseCase,
            isShowSendButtonUseCase = isShowSendButtonUseCase,
            isShowOverlayPermissionRequestDialogUseCase = isShowOverlayPermissionRequestDialogUseCase,
            downloadFileUseCase = downloadFileUseCase,
            siteInfoUseCase = siteInfoUseCase,
            isFromCallScreenUseCase = isFromCallScreenUseCase,
            updateFromCallScreenUseCase = updateFromCallScreenUseCase,
            isSecureEngagementUseCase = isSecureEngagementUseCase,
            engagementConfigUseCase = engagementConfigUseCase,
            isSecureEngagementAvailableUseCase = isSecureConversationsChatAvailableUseCase,
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
            mediaUpgradeOfferUseCase = mediaUpgradeOfferUseCase,
            acceptMediaUpgradeOfferUseCase = acceptMediaUpgradeOfferUseCase,
            declineMediaUpgradeOfferUseCase = declineMediaUpgradeOfferUseCase,
            isQueueingOrEngagementUseCase = isQueueingOrEngagementUseCase,
            enqueueForEngagementUseCase = enqueueForEngagementUseCase,
        )
        chatController.setView(chatView)
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
}
