package com.glia.widgets.chat.controller

import com.glia.androidsdk.chat.ChatMessage
import com.glia.widgets.chat.ChatViewCallback
import com.glia.widgets.chat.domain.*
import com.glia.widgets.chat.model.history.ChatItem
import com.glia.widgets.chat.model.history.LinkedChatItem
import com.glia.widgets.core.dialog.DialogController
import com.glia.widgets.core.dialog.domain.IsShowOverlayPermissionRequestDialogUseCase
import com.glia.widgets.core.engagement.domain.*
import com.glia.widgets.core.engagement.domain.model.ChatMessageInternal
import com.glia.widgets.core.fileupload.domain.*
import com.glia.widgets.core.mediaupgradeoffer.MediaUpgradeOfferRepository
import com.glia.widgets.core.mediaupgradeoffer.domain.AddMediaUpgradeOfferCallbackUseCase
import com.glia.widgets.core.mediaupgradeoffer.domain.RemoveMediaUpgradeOfferCallbackUseCase
import com.glia.widgets.core.notification.domain.RemoveCallNotificationUseCase
import com.glia.widgets.core.notification.domain.ShowAudioCallNotificationUseCase
import com.glia.widgets.core.notification.domain.ShowVideoCallNotificationUseCase
import com.glia.widgets.core.operator.domain.AddOperatorMediaStateListenerUseCase
import com.glia.widgets.core.queue.domain.GliaCancelQueueTicketUseCase
import com.glia.widgets.core.queue.domain.GliaQueueForChatEngagementUseCase
import com.glia.widgets.core.queue.domain.QueueTicketStateChangeToUnstaffedUseCase
import com.glia.widgets.core.secureconversations.domain.IsSecureEngagementUseCase
import com.glia.widgets.core.survey.domain.GliaSurveyUseCase
import com.glia.widgets.filepreview.domain.usecase.DownloadFileUseCase
import com.glia.widgets.helper.TimeCounter
import com.glia.widgets.view.MessagesNotSeenHandler
import com.glia.widgets.view.MinimizeHandler
import junit.framework.TestCase.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class ChatControllerTest {
    private lateinit var chatViewCallback: ChatViewCallback
    private lateinit var mediaUpgradeOfferRepository: MediaUpgradeOfferRepository
    private lateinit var callTimer: TimeCounter
    private lateinit var minimizeHandler: MinimizeHandler
    private lateinit var dialogController: DialogController
    private lateinit var messagesNotSeenHandler: MessagesNotSeenHandler
    private lateinit var showAudioCallNotificationUseCase: ShowAudioCallNotificationUseCase
    private lateinit var showVideoCallNotificationUseCase: ShowVideoCallNotificationUseCase
    private lateinit var removeCallNotificationUseCase: RemoveCallNotificationUseCase
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
    private lateinit var customCardInteractableUseCase: CustomCardInteractableUseCase
    private lateinit var customCardShouldShowUseCase: CustomCardShouldShowUseCase
    private lateinit var ticketStateChangeToUnstaffedUseCase: QueueTicketStateChangeToUnstaffedUseCase
    private lateinit var addMediaUpgradeOfferCallbackUseCase: AddMediaUpgradeOfferCallbackUseCase
    private lateinit var removeMediaUpgradeOfferCallbackUseCase: RemoveMediaUpgradeOfferCallbackUseCase
    private lateinit var isOngoingEngagementUseCase: IsOngoingEngagementUseCase
    private lateinit var isSecureEngagementUseCase: IsSecureEngagementUseCase
    private lateinit var engagementConfigUseCase: SetEngagementConfigUseCase
    private lateinit var isSecureConversationsChatAvailableUseCase: IsSecureConversationsChatAvailableUseCase

    private lateinit var chatController: ChatController

    @Before
    fun setUp() {
        chatViewCallback = mock()
        mediaUpgradeOfferRepository = mock()
        callTimer = mock()
        minimizeHandler = mock()
        dialogController = mock()
        messagesNotSeenHandler = mock()
        showAudioCallNotificationUseCase = mock()
        showVideoCallNotificationUseCase = mock()
        removeCallNotificationUseCase = mock()
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
        customCardInteractableUseCase = mock()
        customCardShouldShowUseCase = mock()
        ticketStateChangeToUnstaffedUseCase = mock()
        addMediaUpgradeOfferCallbackUseCase = mock()
        removeMediaUpgradeOfferCallbackUseCase = mock()
        isOngoingEngagementUseCase = mock()
        isSecureEngagementUseCase = mock()
        engagementConfigUseCase = mock()
        isSecureConversationsChatAvailableUseCase = mock()

        chatController = ChatController(
            chatViewCallback = chatViewCallback,
            mediaUpgradeOfferRepository = mediaUpgradeOfferRepository,
            callTimer = callTimer,
            minimizeHandler = minimizeHandler,
            dialogController = dialogController,
            messagesNotSeenHandler = messagesNotSeenHandler,
            showAudioCallNotificationUseCase = showAudioCallNotificationUseCase,
            showVideoCallNotificationUseCase = showVideoCallNotificationUseCase,
            removeCallNotificationUseCase = removeCallNotificationUseCase,
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
            customCardInteractableUseCase = customCardInteractableUseCase,
            customCardShouldShowUseCase = customCardShouldShowUseCase,
            ticketStateChangeToUnstaffedUseCase = ticketStateChangeToUnstaffedUseCase,
            isOngoingEngagementUseCase = isOngoingEngagementUseCase,
            isSecureEngagementUseCase = isSecureEngagementUseCase,
            engagementConfigUseCase = engagementConfigUseCase,
            isSecureEngagementAvailableUseCase = isSecureConversationsChatAvailableUseCase,
            addMediaUpgradeCallbackUseCase = addMediaUpgradeOfferCallbackUseCase,
            removeMediaUpgradeCallbackUseCase = removeMediaUpgradeOfferCallbackUseCase
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
}
