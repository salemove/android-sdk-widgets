package com.glia.widgets.chat.domain

import com.glia.androidsdk.chat.AttachmentFile
import com.glia.androidsdk.chat.FilesAttachment
import com.glia.androidsdk.chat.VisitorMessage
import com.glia.widgets.chat.ChatManager
import com.glia.widgets.chat.model.VisitorAttachmentItem
import com.glia.widgets.chat.model.VisitorChatItem
import com.glia.widgets.chat.model.VisitorItemStatus
import com.glia.widgets.chat.model.VisitorMessageItem
import com.glia.widgets.core.engagement.domain.model.ChatMessageInternal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.spy
import org.mockito.kotlin.whenever

class AppendNewVisitorMessageUseCaseTest {
    private lateinit var mapVisitorAttachmentUseCase: MapVisitorAttachmentUseCase
    private lateinit var useCase: AppendNewVisitorMessageUseCase
    private lateinit var state: ChatManager.State

    private lateinit var chatMessageInternal: ChatMessageInternal
    private lateinit var visitorMessage: VisitorMessage

    @Before
    fun setUp() {
        mapVisitorAttachmentUseCase = mock()
        useCase = spy(AppendNewVisitorMessageUseCase(mapVisitorAttachmentUseCase))
        state = ChatManager.State()
        chatMessageInternal = mock()
        visitorMessage = mock()
        whenever(chatMessageInternal.chatMessage) doReturn visitorMessage
    }


    @Test
    fun `invoke appends VisitorMessageItem with Delivered status when message has no attachment and not in previews`() {
        whenever(visitorMessage.content) doReturn "content"
        whenever(visitorMessage.timestamp) doReturn 1
        whenever(visitorMessage.id) doReturn "1"

        useCase(state, chatMessageInternal)

        assertTrue(state.chatItems.count() == 1)
        val firstItem = state.chatItems.first() as VisitorMessageItem
        assertEquals("content", firstItem.message)
        assertEquals("1", firstItem.id)
    }

    @Test
    fun `invoke appends VisitorMessageItem with History status when message has attachments and not in previews`() {
        val filesAttachment: FilesAttachment = mock()
        val file: AttachmentFile = mock()
        whenever(filesAttachment.files) doReturn arrayOf(file)

        whenever(visitorMessage.attachment) doReturn filesAttachment
        whenever(visitorMessage.content) doReturn "content"
        whenever(visitorMessage.timestamp) doReturn 1
        whenever(visitorMessage.id) doReturn "1"

        val attachmentWithShowDeliveredTrue = VisitorAttachmentItem.RemoteFile(
            id = "eloquentiam",
            attachment = mock(),
            isFileExists = false,
            isDownloading = false,
            status = VisitorItemStatus.DELIVERED,
            timestamp = 3047

        )
        val attachmentWithShowDeliveredFalse = VisitorAttachmentItem.RemoteFile(
            id = "ludus",
            attachment = mock(),
            isFileExists = false,
            isDownloading = false,
            status = VisitorItemStatus.HISTORY,
            timestamp = 4522
        )

        whenever(mapVisitorAttachmentUseCase(any(), any(), eq(true))) doReturn attachmentWithShowDeliveredTrue
        whenever(mapVisitorAttachmentUseCase(any(), any(), eq(false))) doReturn attachmentWithShowDeliveredFalse

        useCase(state, chatMessageInternal)

        assertTrue(state.chatItems.count() == 2)
        assertTrue(state.chatItems.first() is VisitorMessageItem)
        assertTrue(state.chatItems.last() is VisitorAttachmentItem.RemoteFile)
        assertEquals(VisitorItemStatus.DELIVERED, (state.chatItems.last() as VisitorChatItem).status)
        assertEquals(VisitorItemStatus.HISTORY, (state.chatItems.first() as VisitorMessageItem).status)

        Mockito.reset(visitorMessage)

        whenever(visitorMessage.attachment) doReturn filesAttachment
        whenever(visitorMessage.content) doReturn ""
        whenever(visitorMessage.timestamp) doReturn 1
        whenever(visitorMessage.id) doReturn "2"

        useCase(state, chatMessageInternal)

        assertTrue(state.chatItems.count() == 3)
        assertEquals(VisitorItemStatus.DELIVERED, (state.chatItems.last() as VisitorChatItem).status)
        assertEquals(VisitorItemStatus.HISTORY, (state.chatItems[1] as VisitorChatItem).status)
    }

    @Test
    fun `invoke marks Visitor message as delivered when message is in previews`() {
        val messageId = "1"
        val messageContent = "content"
        val messageTimeStamp = 1L

        val messageId1 = "1_1"
        val messageContent1 = "content_1"
        val messageTimeStamp1 = 2L

        val attachmentWithShowDeliveredTrue = VisitorAttachmentItem.LocalFile(
            id = "facilis", messageId = messageId1, attachment = mock(), status = VisitorItemStatus.DELIVERED, timestamp = 9113
        )

        val attachmentWithShowDeliveredFalse = VisitorAttachmentItem.LocalFile(
            id = "duo", messageId = messageId1, attachment = mock(), status = VisitorItemStatus.HISTORY, timestamp = 7757
        )

        val filesAttachment: FilesAttachment = mock()
        val file: AttachmentFile = mock()
        whenever(file.id) doReturn attachmentWithShowDeliveredFalse.id
        val file1: AttachmentFile = mock()
        whenever(file1.id) doReturn attachmentWithShowDeliveredTrue.id
        whenever(filesAttachment.files) doReturn arrayOf(file, file1)

        whenever(mapVisitorAttachmentUseCase(any(), any(), eq(true))) doReturn attachmentWithShowDeliveredTrue
        whenever(mapVisitorAttachmentUseCase(any(), any(), eq(false))) doReturn attachmentWithShowDeliveredFalse

        state.messagePreviews[messageId] = mock()
        state.messagePreviews[messageId1] = mock()
        state.chatItems += VisitorMessageItem(messageContent, messageId, VisitorItemStatus.PREVIEW, messageTimeStamp)
        state.chatItems += VisitorMessageItem(messageContent1, messageId1, VisitorItemStatus.PREVIEW, messageTimeStamp1)
        state.chatItems += attachmentWithShowDeliveredFalse.copyWithError(VisitorItemStatus.PREVIEW)
        state.chatItems += attachmentWithShowDeliveredTrue.copyWithError(VisitorItemStatus.PREVIEW)

        whenever(visitorMessage.content) doReturn messageContent
        whenever(visitorMessage.timestamp) doReturn messageTimeStamp
        whenever(visitorMessage.id) doReturn messageId

        assertTrue(state.chatItems.count() == 4)
        assertFalse(state.chatItems.any { it is VisitorChatItem && it.status == VisitorItemStatus.DELIVERED })

        useCase(state, chatMessageInternal)

        val visitorChatItem = state.chatItems.first() as VisitorChatItem

        assertTrue(state.chatItems.count() == 4)
        assertFalse(state.messagePreviews.containsKey(messageId))
        assertTrue(state.messagePreviews.containsKey(messageId1))
        assertEquals(VisitorItemStatus.DELIVERED, visitorChatItem.status)
        assertEquals(1, state.chatItems.count { it is VisitorChatItem && it.status == VisitorItemStatus.DELIVERED })

        Mockito.reset(visitorMessage)

        whenever(visitorMessage.attachment) doReturn filesAttachment
        whenever(visitorMessage.content) doReturn messageContent1
        whenever(visitorMessage.timestamp) doReturn messageTimeStamp1
        whenever(visitorMessage.id) doReturn messageId1

        useCase(state, chatMessageInternal)

        val visitorChatItem1 = state.chatItems.last() as VisitorChatItem

        assertTrue(state.chatItems.count() == 4)
        assertTrue(state.messagePreviews.isEmpty())
        assertEquals(VisitorItemStatus.DELIVERED, visitorChatItem1.status)
        assertEquals(1, state.chatItems.count { it is VisitorChatItem && it.status == VisitorItemStatus.DELIVERED })
    }
}
