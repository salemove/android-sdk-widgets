package com.glia.widgets.chat.controller

import com.glia.androidsdk.chat.ChatMessage
import com.glia.widgets.chat.ChatViewCallback
import com.glia.widgets.chat.domain.AddNewMessagesDividerUseCase
import com.glia.widgets.chat.domain.CustomCardAdapterTypeUseCase
import com.glia.widgets.chat.domain.CustomCardShouldShowUseCase
import com.glia.widgets.chat.domain.CustomCardTypeUseCase
import com.glia.widgets.chat.domain.GliaLoadHistoryUseCase
import com.glia.widgets.chat.domain.GliaOnMessageUseCase
import com.glia.widgets.chat.domain.GliaOnOperatorTypingUseCase
import com.glia.widgets.chat.domain.GliaSendMessagePreviewUseCase
import com.glia.widgets.chat.domain.GliaSendMessageUseCase
import com.glia.widgets.chat.domain.IsEnableChatEditTextUseCase
import com.glia.widgets.chat.domain.IsFromCallScreenUseCase
import com.glia.widgets.chat.domain.IsSecureConversationsChatAvailableUseCase
import com.glia.widgets.chat.domain.IsShowSendButtonUseCase
import com.glia.widgets.chat.domain.SiteInfoUseCase
import com.glia.widgets.chat.domain.UpdateFromCallScreenUseCase
import com.glia.widgets.chat.domain.gva.DetermineGvaButtonTypeUseCase
import com.glia.widgets.chat.domain.gva.IsGvaUseCase
import com.glia.widgets.chat.domain.gva.MapGvaUseCase
import com.glia.widgets.chat.model.Gva
import com.glia.widgets.chat.model.GvaButton
import com.glia.widgets.chat.model.history.ChatItem
import com.glia.widgets.chat.model.history.LinkedChatItem
import com.glia.widgets.core.callvisualizer.domain.IsCallVisualizerUseCase
import com.glia.widgets.core.chathead.domain.HasPendingSurveyUseCase
import com.glia.widgets.core.chathead.domain.SetPendingSurveyUsedUseCase
import com.glia.widgets.core.dialog.DialogController
import com.glia.widgets.core.dialog.domain.IsShowOverlayPermissionRequestDialogUseCase
import com.glia.widgets.core.engagement.domain.GetEngagementStateFlowableUseCase
import com.glia.widgets.core.engagement.domain.GliaEndEngagementUseCase
import com.glia.widgets.core.engagement.domain.GliaOnEngagementEndUseCase
import com.glia.widgets.core.engagement.domain.GliaOnEngagementUseCase
import com.glia.widgets.core.engagement.domain.IsOngoingEngagementUseCase
import com.glia.widgets.core.engagement.domain.IsQueueingEngagementUseCase
import com.glia.widgets.core.engagement.domain.SetEngagementConfigUseCase
import com.glia.widgets.core.engagement.domain.model.ChatMessageInternal
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
import com.glia.widgets.core.secureconversations.domain.MarkMessagesReadWithDelayUseCase
import com.glia.widgets.core.survey.domain.GliaSurveyUseCase
import com.glia.widgets.filepreview.domain.usecase.DownloadFileUseCase
import com.glia.widgets.filepreview.domain.usecase.IsFileReadyForPreviewUseCase
import com.glia.widgets.helper.TimeCounter
import com.glia.widgets.view.MessagesNotSeenHandler
import com.glia.widgets.view.MinimizeHandler
import io.reactivex.Completable
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class ChatControllerTest {
    private lateinit var chatViewCallback: ChatViewCallback
    private lateinit var mediaUpgradeOfferRepository: MediaUpgradeOfferRepository
    private lateinit var callTimer: TimeCounter
    private lateinit var minimizeHandler: MinimizeHandler
    private lateinit var dialogController: DialogController
    private lateinit var messagesNotSeenHandler: MessagesNotSeenHandler
    private lateinit var callNotificationUseCase: CallNotificationUseCase
    private lateinit var loadHistoryUseCase: GliaLoadHistoryUseCase
    private lateinit var queueForChatEngagementUseCase: GliaQueueForChatEngagementUseCase
    private lateinit var getEngagementUseCase: GliaOnEngagementUseCase
    private lateinit var engagementEndUseCase: GliaOnEngagementEndUseCase
    private lateinit var onMessageUseCase: GliaOnMessageUseCase
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
    private lateinit var isEnableChatEditTextUseCase: IsEnableChatEditTextUseCase
    private lateinit var siteInfoUseCase: SiteInfoUseCase
    private lateinit var surveyUseCase: GliaSurveyUseCase
    private lateinit var getGliaEngagementStateFlowableUseCase: GetEngagementStateFlowableUseCase
    private lateinit var isFromCallScreenUseCase: IsFromCallScreenUseCase
    private lateinit var updateFromCallScreenUseCase: UpdateFromCallScreenUseCase
    private lateinit var customCardAdapterTypeUseCase: CustomCardAdapterTypeUseCase
    private lateinit var customCardTypeUseCase: CustomCardTypeUseCase
    private lateinit var customCardShouldShowUseCase: CustomCardShouldShowUseCase
    private lateinit var ticketStateChangeToUnstaffedUseCase: QueueTicketStateChangeToUnstaffedUseCase
    private lateinit var addMediaUpgradeOfferCallbackUseCase: AddMediaUpgradeOfferCallbackUseCase
    private lateinit var removeMediaUpgradeOfferCallbackUseCase: RemoveMediaUpgradeOfferCallbackUseCase
    private lateinit var isOngoingEngagementUseCase: IsOngoingEngagementUseCase
    private lateinit var isSecureEngagementUseCase: IsSecureEngagementUseCase
    private lateinit var engagementConfigUseCase: SetEngagementConfigUseCase
    private lateinit var isSecureConversationsChatAvailableUseCase: IsSecureConversationsChatAvailableUseCase
    private lateinit var markMessagesReadWithDelayUseCase: MarkMessagesReadWithDelayUseCase
    private lateinit var isQueueingEngagementUseCase: IsQueueingEngagementUseCase
    private lateinit var hasPendingSurveyUseCase: HasPendingSurveyUseCase
    private lateinit var setPendingSurveyUsedUseCase: SetPendingSurveyUsedUseCase
    private lateinit var isCallVisualizerUseCase: IsCallVisualizerUseCase
    private lateinit var addNewMessagesDividerUseCase: AddNewMessagesDividerUseCase
    private lateinit var isFileReadyForPreviewUseCase: IsFileReadyForPreviewUseCase
    private lateinit var acceptMediaUpgradeOfferUseCase: AcceptMediaUpgradeOfferUseCase
    private lateinit var isGvaUseCase: IsGvaUseCase
    private lateinit var mapGvaUseCase: MapGvaUseCase
    private lateinit var determineGvaButtonTypeUseCase: DetermineGvaButtonTypeUseCase

    private lateinit var chatController: ChatController

    @Before
    fun setUp() {
        chatViewCallback = mock()
        mediaUpgradeOfferRepository = mock()
        callTimer = mock()
        minimizeHandler = mock()
        dialogController = mock()
        messagesNotSeenHandler = mock()
        callNotificationUseCase = mock()
        loadHistoryUseCase = mock()
        queueForChatEngagementUseCase = mock()
        getEngagementUseCase = mock()
        engagementEndUseCase = mock()
        onMessageUseCase = mock()
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
        isEnableChatEditTextUseCase = mock()
        siteInfoUseCase = mock()
        surveyUseCase = mock()
        getGliaEngagementStateFlowableUseCase = mock()
        isFromCallScreenUseCase = mock()
        updateFromCallScreenUseCase = mock()
        customCardAdapterTypeUseCase = mock()
        customCardTypeUseCase = mock()
        customCardShouldShowUseCase = mock()
        ticketStateChangeToUnstaffedUseCase = mock()
        addMediaUpgradeOfferCallbackUseCase = mock()
        removeMediaUpgradeOfferCallbackUseCase = mock()
        isOngoingEngagementUseCase = mock()
        isSecureEngagementUseCase = mock()
        engagementConfigUseCase = mock()
        isSecureConversationsChatAvailableUseCase = mock()
        markMessagesReadWithDelayUseCase = mock()
        isQueueingEngagementUseCase = mock()
        hasPendingSurveyUseCase = mock()
        setPendingSurveyUsedUseCase = mock()
        isCallVisualizerUseCase = mock()
        addNewMessagesDividerUseCase = mock()
        isFileReadyForPreviewUseCase = mock()
        acceptMediaUpgradeOfferUseCase = mock()
        isGvaUseCase = mock()
        mapGvaUseCase = mock()
        determineGvaButtonTypeUseCase = mock()

        chatController = ChatController(
            chatViewCallback = chatViewCallback,
            mediaUpgradeOfferRepository = mediaUpgradeOfferRepository,
            callTimer = callTimer,
            minimizeHandler = minimizeHandler,
            dialogController = dialogController,
            messagesNotSeenHandler = messagesNotSeenHandler,
            callNotificationUseCase = callNotificationUseCase,
            loadHistoryUseCase = loadHistoryUseCase,
            queueForChatEngagementUseCase = queueForChatEngagementUseCase,
            getEngagementUseCase = getEngagementUseCase,
            engagementEndUseCase = engagementEndUseCase,
            onMessageUseCase = onMessageUseCase,
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
            isEnableChatEditTextUseCase = isEnableChatEditTextUseCase,
            siteInfoUseCase = siteInfoUseCase,
            surveyUseCase = surveyUseCase,
            getGliaEngagementStateFlowableUseCase = getGliaEngagementStateFlowableUseCase,
            isFromCallScreenUseCase = isFromCallScreenUseCase,
            updateFromCallScreenUseCase = updateFromCallScreenUseCase,
            customCardAdapterTypeUseCase = customCardAdapterTypeUseCase,
            customCardTypeUseCase = customCardTypeUseCase,
            customCardShouldShowUseCase = customCardShouldShowUseCase,
            ticketStateChangeToUnstaffedUseCase = ticketStateChangeToUnstaffedUseCase,
            isOngoingEngagementUseCase = isOngoingEngagementUseCase,
            isSecureEngagementUseCase = isSecureEngagementUseCase,
            engagementConfigUseCase = engagementConfigUseCase,
            addMediaUpgradeCallbackUseCase = addMediaUpgradeOfferCallbackUseCase,
            removeMediaUpgradeCallbackUseCase = removeMediaUpgradeOfferCallbackUseCase,
            isSecureEngagementAvailableUseCase = isSecureConversationsChatAvailableUseCase,
            markMessagesReadWithDelayUseCase = markMessagesReadWithDelayUseCase,
            isQueueingEngagementUseCase = isQueueingEngagementUseCase,
            hasPendingSurveyUseCase = hasPendingSurveyUseCase,
            setPendingSurveyUsedUseCase = setPendingSurveyUsedUseCase,
            isCallVisualizerUseCase = isCallVisualizerUseCase,
            addNewMessagesDividerUseCase = addNewMessagesDividerUseCase,
            isFileReadyForPreviewUseCase = isFileReadyForPreviewUseCase,
            acceptMediaUpgradeOfferUseCase = acceptMediaUpgradeOfferUseCase,
            isGvaUseCase = isGvaUseCase,
            mapGvaUseCase = mapGvaUseCase,
            determineGvaButtonTypeUseCase = determineGvaButtonTypeUseCase
        )
    }

    @Test
    fun removeDuplicates_returnsNull_whenNewMessagesIsNull() {
        val oldHistory = listOf<ChatItem>(
            mock<LinkedChatItem>().also { whenever(it.messageId).thenReturn("Id1") },
            mock<LinkedChatItem>().also { whenever(it.messageId).thenReturn("Id2") },
            mock<LinkedChatItem>().also { whenever(it.messageId).thenReturn("Id3") }
        )

        assertNull(chatController.removeDuplicates(oldHistory, null))
    }

    @Test
    fun removeDuplicates_returnsNewMessages_whenOldMessagesIsNull() {
        val newMessages = listOf<ChatMessage>(
            mock<ChatMessage>().also { whenever(it.id).thenReturn("Id1") },
            mock<ChatMessage>().also { whenever(it.id).thenReturn("Id2") },
            mock<ChatMessage>().also { whenever(it.id).thenReturn("Id3") }
        ).map { chatItem ->
            mock<ChatMessageInternal>().also { whenever(it.chatMessage).thenReturn(chatItem) }
        }

        val result = chatController.removeDuplicates(null, newMessages)
        assertEquals(newMessages, result)
    }

    @Test
    fun removeDuplicates_returnsAllMessages_whenAllNewMessagesAreNotContainInHistory() {
        val oldHistory = listOf<ChatItem>(
            mock<LinkedChatItem>().also { whenever(it.messageId).thenReturn("Id1") },
            mock<LinkedChatItem>().also { whenever(it.messageId).thenReturn("Id2") },
            mock<LinkedChatItem>().also { whenever(it.messageId).thenReturn("Id3") }
        )
        val newMessages = listOf<ChatMessage>(
            mock<ChatMessage>().also { whenever(it.id).thenReturn("Id4") },
            mock<ChatMessage>().also { whenever(it.id).thenReturn("Id5") },
            mock<ChatMessage>().also { whenever(it.id).thenReturn("Id6") }
        ).map { chatItem ->
            mock<ChatMessageInternal>().also { whenever(it.chatMessage).thenReturn(chatItem) }
        }

        val result =
            chatController.removeDuplicates(oldHistory, newMessages)!!.map { it.chatMessage.id }
        assertEquals(listOf("Id4", "Id5", "Id6"), result)
    }

    @Test
    fun removeDuplicates_returnsOnlyNewMessages_whenSomeNewMessagesAreContainInHistory() {
        val oldHistory = listOf<ChatItem>(
            mock<LinkedChatItem>().also { whenever(it.messageId).thenReturn("Id1") },
            mock<LinkedChatItem>().also { whenever(it.messageId).thenReturn("Id2") },
            mock<LinkedChatItem>().also { whenever(it.messageId).thenReturn("Id3") }
        )
        val newMessages = listOf<ChatMessage>(
            mock<ChatMessage>().also { whenever(it.id).thenReturn("Id3") },
            mock<ChatMessage>().also { whenever(it.id).thenReturn("Id4") },
            mock<ChatMessage>().also { whenever(it.id).thenReturn("Id5") }
        ).map { chatItem ->
            mock<ChatMessageInternal>().also { whenever(it.chatMessage).thenReturn(chatItem) }
        }

        val result =
            chatController.removeDuplicates(oldHistory, newMessages)!!.map { it.chatMessage.id }
        assertEquals(listOf("Id4", "Id5"), result)
    }

    @Test
    fun removeDuplicates_removesAllMessages_whenAllNewMessagesAreContainInHistory() {
        val oldHistory = listOf<ChatItem>(
            mock<LinkedChatItem>().also { whenever(it.messageId).thenReturn("Id1") },
            mock<LinkedChatItem>().also { whenever(it.messageId).thenReturn("Id2") },
            mock<LinkedChatItem>().also { whenever(it.messageId).thenReturn("Id3") }
        )
        val newMessages = listOf<ChatMessage>(
            mock<ChatMessage>().also { whenever(it.id).thenReturn("Id1") },
            mock<ChatMessage>().also { whenever(it.id).thenReturn("Id2") },
            mock<ChatMessage>().also { whenever(it.id).thenReturn("Id3") }
        ).map { chatItem ->
            mock<ChatMessageInternal>().also { whenever(it.chatMessage).thenReturn(chatItem) }
        }

        assertTrue(chatController.removeDuplicates(oldHistory, newMessages)!!.isEmpty())
    }

    @Test
    fun isNewMessage_returnsTrue_whenOldMessagesEmpty() {
        val oldHistory = listOf<ChatItem>()
        val newMessage = mock<ChatMessage>()

        assertTrue(chatController.isNewMessage(oldHistory, newMessage))
    }

    @Test
    fun isNewMessage_returnsTrue_whenOldMessagesIsNull() {
        val newMessage = mock<ChatMessage>()

        assertTrue(chatController.isNewMessage(null, newMessage))
    }

    @Test
    fun isNewMessage_returnsTrue_whenOldMessagesDoesNotContainNewOne() {
        val oldHistory = listOf<ChatItem>(
            mock<LinkedChatItem>().also { whenever(it.messageId).thenReturn("oldId1") },
            mock<LinkedChatItem>().also { whenever(it.messageId).thenReturn("oldId2") },
            mock<LinkedChatItem>().also { whenever(it.messageId).thenReturn("oldId3") }
        )
        val newMessage = mock<ChatMessage>()
        whenever(newMessage.id).thenReturn("newId")

        assertTrue(chatController.isNewMessage(oldHistory, newMessage))
    }

    @Test
    fun isNewMessage_returnsFalse_whenOldMessagesContainsNewOne() {
        val oldHistory = listOf<ChatItem>(
            mock<LinkedChatItem>().also { whenever(it.messageId).thenReturn("oldId1") },
            mock<LinkedChatItem>().also { whenever(it.messageId).thenReturn("oldId2") },
            mock<LinkedChatItem>().also { whenever(it.messageId).thenReturn("oldId3") }
        )
        val newMessage = mock<ChatMessage>()
        whenever(newMessage.id).thenReturn("oldId2")

        assertFalse(chatController.isNewMessage(oldHistory, newMessage))
    }

    @Test
    fun isNewMessage_filterNullId_whenOldMessageDoesNotHaveId() {
        val oldHistory = listOf<ChatItem>(
            mock<LinkedChatItem>().also { whenever(it.messageId).thenReturn("oldId1") },
            mock<LinkedChatItem>().also { whenever(it.messageId).thenReturn(null) },
            mock<LinkedChatItem>().also { whenever(it.messageId).thenReturn(null) }
        )
        val newMessage = mock<ChatMessage>()
        whenever(newMessage.id).thenReturn("oldId1")

        assertFalse(chatController.isNewMessage(oldHistory, newMessage))
    }

    @Test
    fun isNewMessage_filterLinkedChatItem_whenOldMessageHasIncorrectType() {
        val oldHistory = listOf<ChatItem>(
            mock<LinkedChatItem>().also { whenever(it.messageId).thenReturn("oldId1") },
            mock<ChatItem>().also { whenever(it.id).thenReturn("oldId2") },
            mock<ChatItem>().also { whenever(it.id).thenReturn("oldId3") }
        )
        val newMessage = mock<ChatMessage>()
        whenever(newMessage.id).thenReturn("oldId2")

        assertTrue(chatController.isNewMessage(oldHistory, newMessage))
    }

    @Test
    fun `emitChatTranscriptItems triggers remove new messages divider when divider is added`() {
        whenever(addNewMessagesDividerUseCase(any(), any())) doReturn true
        whenever(markMessagesReadWithDelayUseCase()) doReturn Completable.complete()

        chatController.emitChatTranscriptItems(mutableListOf(), 10)
        verify(markMessagesReadWithDelayUseCase).invoke()
    }

    @Test
    fun `emitChatTranscriptItems not triggers remove new messages divider when divider is added`() {
        whenever(addNewMessagesDividerUseCase(any(), any())) doReturn false
        whenever(markMessagesReadWithDelayUseCase()) doReturn Completable.complete()

        chatController.emitChatTranscriptItems(mutableListOf(), 10)
        verify(markMessagesReadWithDelayUseCase, never()).invoke()
    }

    @Test
    fun `onGvaButtonClicked triggers viewCallback showBroadcastNotSupportedToast when gva type is BroadcastEvent`() {
        val gvaButton: GvaButton = mock()
        whenever(determineGvaButtonTypeUseCase(any())) doReturn Gva.ButtonType.BroadcastEvent

        chatController.onGvaButtonClicked(gvaButton)

        verify(chatViewCallback).showBroadcastNotSupportedToast()
    }

}
