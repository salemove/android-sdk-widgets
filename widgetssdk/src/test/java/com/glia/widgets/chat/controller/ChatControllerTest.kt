package com.glia.widgets.chat.controller

import android.net.Uri
import com.glia.androidsdk.chat.SingleChoiceAttachment
import com.glia.widgets.chat.ChatContract
import com.glia.widgets.chat.ChatManager
import com.glia.widgets.chat.ChatType
import com.glia.widgets.chat.domain.DecideOnQueueingUseCase
import com.glia.widgets.chat.domain.GliaSendMessagePreviewUseCase
import com.glia.widgets.chat.domain.GliaSendMessageUseCase
import com.glia.widgets.chat.domain.IsAuthenticatedUseCase
import com.glia.widgets.chat.domain.IsFromCallScreenUseCase
import com.glia.widgets.chat.domain.IsShowSendButtonUseCase
import com.glia.widgets.chat.domain.SiteInfoUseCase
import com.glia.widgets.chat.domain.TakePictureUseCase
import com.glia.widgets.chat.domain.UpdateFromCallScreenUseCase
import com.glia.widgets.chat.domain.UriToFileAttachmentUseCase
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
import com.glia.widgets.core.permissions.domain.RequestNotificationPermissionIfPushNotificationsSetUpUseCase
import com.glia.widgets.core.permissions.domain.WithCameraPermissionUseCase
import com.glia.widgets.core.permissions.domain.WithReadWritePermissionsUseCase
import com.glia.widgets.core.secureconversations.domain.IsSecureEngagementUseCase
import com.glia.widgets.core.secureconversations.domain.GetAvailableQueueIdsForSecureMessagingUseCase
import com.glia.widgets.engagement.domain.AcceptMediaUpgradeOfferUseCase
import com.glia.widgets.engagement.domain.DeclineMediaUpgradeOfferUseCase
import com.glia.widgets.engagement.domain.EndEngagementUseCase
import com.glia.widgets.engagement.domain.EngagementStateUseCase
import com.glia.widgets.engagement.domain.EnqueueForEngagementUseCase
import com.glia.widgets.engagement.domain.IsCurrentEngagementCallVisualizerUseCase
import com.glia.widgets.engagement.domain.IsQueueingOrEngagementUseCase
import com.glia.widgets.engagement.domain.OperatorMediaUseCase
import com.glia.widgets.engagement.domain.OperatorTypingUseCase
import com.glia.widgets.engagement.domain.ReleaseResourcesUseCase
import com.glia.widgets.engagement.domain.ScreenSharingUseCase
import com.glia.widgets.filepreview.domain.usecase.DownloadFileUseCase
import com.glia.widgets.filepreview.domain.usecase.IsFileReadyForPreviewUseCase
import com.glia.widgets.helper.Data
import com.glia.widgets.helper.TimeCounter
import com.glia.widgets.view.MessagesNotSeenHandler
import com.glia.widgets.view.MinimizeHandler
import com.glia.widgets.webbrowser.domain.GetUrlFromLinkUseCase
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
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
    private lateinit var getAvailableQueueIdsForSecureMessagingUseCase: GetAvailableQueueIdsForSecureMessagingUseCase
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
    private lateinit var isQueueingOrEngagementUseCase: IsQueueingOrEngagementUseCase
    private lateinit var enqueueForEngagementUseCase: EnqueueForEngagementUseCase
    private lateinit var decideOnQueueingUseCase: DecideOnQueueingUseCase
    private lateinit var screenSharingUseCase: ScreenSharingUseCase
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
        getAvailableQueueIdsForSecureMessagingUseCase = mock()
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
        acceptMediaUpgradeOfferUseCase = mock()
        declineMediaUpgradeOfferUseCase = mock()
        isQueueingOrEngagementUseCase = mock()
        enqueueForEngagementUseCase = mock()
        chatView = mock()
        decideOnQueueingUseCase = mock {
            on { invoke() } doReturn Completable.complete()
        }

        screenSharingUseCase = mock {
            on { invoke() } doReturn Flowable.empty()
        }

        takePictureUseCase = mock()
        uriToFileAttachmentUseCase = mock()
        withCameraPermissionUseCase = mock()
        withReadWritePermissionsUseCase = mock()
        requestNotificationPermissionIfPushNotificationsSetUpUseCase = mock()
        releaseResourcesUseCase = mock()
        getUrlFromLinkUseCase = mock()

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
            getAvailableQueueIdsForSecureMessagingUseCase = getAvailableQueueIdsForSecureMessagingUseCase,
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
            isQueueingOrEngagementUseCase = isQueueingOrEngagementUseCase,
            enqueueForEngagementUseCase = enqueueForEngagementUseCase,
            decideOnQueueingUseCase = decideOnQueueingUseCase,
            screenSharingUseCase = screenSharingUseCase,
            takePictureUseCase = takePictureUseCase,
            uriToFileAttachmentUseCase = uriToFileAttachmentUseCase,
            withCameraPermissionUseCase = withCameraPermissionUseCase,
            withReadWritePermissionsUseCase = withReadWritePermissionsUseCase,
            requestNotificationPermissionIfPushNotificationsSetUpUseCase = requestNotificationPermissionIfPushNotificationsSetUpUseCase,
            releaseResourcesUseCase = releaseResourcesUseCase,
            getUrlFromLinkUseCase = getUrlFromLinkUseCase
        )
        chatController.setView(chatView)
    }

    @Test
    fun initChat_setsConfiguration_withInitialParams() {
        val queueIds = listOf("QueueId1", "QueueId2")
        whenever(chatManager.initialize(any(), any(), any())) doReturn Flowable.never()

        chatController.initChat(
            "CompanyName",
            queueIds,
            "VisitorId",
            ChatType.SECURE_MESSAGING
        )

        verify(engagementConfigUseCase).invoke(ChatType.SECURE_MESSAGING, queueIds)
    }

    @Test
    fun initChat_setsConfiguration_withAvailableQueues() {
        val queueIds = listOf("QueueId1", "QueueId2")
        whenever(isSecureEngagementUseCase.invoke()) doReturn true
        whenever(getAvailableQueueIdsForSecureMessagingUseCase.invoke()) doReturn Single.just(Data.Value(queueIds))
        whenever(chatManager.initialize(any(), any(), any())) doReturn Flowable.never()

        chatController.initChat(
            "CompanyName",
            emptyList(),
            "VisitorId",
            ChatType.SECURE_MESSAGING
        )

        verify(engagementConfigUseCase).invoke(ChatType.SECURE_MESSAGING, emptyList())
        verify(engagementConfigUseCase).invoke(ChatType.SECURE_MESSAGING, queueIds)
    }

    @Test
    fun initChat_showsMessageCenterUnavailableDialog_whenNoAvailableQueues() {
        whenever(isSecureEngagementUseCase.invoke()) doReturn true
        whenever(getAvailableQueueIdsForSecureMessagingUseCase.invoke()) doReturn Single.just(Data.Value(null))
        whenever(chatManager.initialize(any(), any(), any())) doReturn Flowable.never()

        chatController.initChat(
            "CompanyName",
            emptyList(),
            "VisitorId",
            ChatType.SECURE_MESSAGING
        )

        verify(dialogController).showMessageCenterUnavailableDialog()
    }

    @Test
    fun initChat_showsUnexpectedErrorDialog_onException() {
        whenever(isSecureEngagementUseCase.invoke()) doReturn true
        whenever(getAvailableQueueIdsForSecureMessagingUseCase.invoke()) doReturn Single.error(Exception())
        whenever(chatManager.initialize(any(), any(), any())) doReturn Flowable.never()

        chatController.initChat(
            "CompanyName",
            emptyList(),
            "VisitorId",
            ChatType.SECURE_MESSAGING
        )

        verify(dialogController).showUnexpectedErrorDialog()
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
