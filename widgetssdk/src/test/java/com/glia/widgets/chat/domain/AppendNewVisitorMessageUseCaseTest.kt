package com.glia.widgets.chat.domain

import com.glia.androidsdk.chat.AttachmentFile
import com.glia.androidsdk.chat.FilesAttachment
import com.glia.androidsdk.chat.VisitorMessage
import com.glia.widgets.chat.ChatManager
import com.glia.widgets.chat.model.DeliveredItem
import com.glia.widgets.chat.model.VisitorAttachmentItem
import com.glia.widgets.chat.model.VisitorMessageItem
import com.glia.widgets.internal.engagement.domain.model.ChatMessageInternal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
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

        assertTrue(state.chatItems.count() == 2)
        val firstItem = state.chatItems.first() as VisitorMessageItem
        assertEquals("content", firstItem.message)
        assertEquals("1", firstItem.id)
        val secondItem = state.chatItems.last() as DeliveredItem
        assertEquals("1", secondItem.messageId)
    }

    @Test
    fun `invoke appends VisitorMessageItem with Delivered status when message has attachments and not in previews`() {
        val filesAttachment: FilesAttachment = mock()
        val file1: AttachmentFile = mock()
        val file2: AttachmentFile = mock()
        whenever(filesAttachment.files) doReturn arrayOf(file1, file2)

        whenever(visitorMessage.attachment) doReturn filesAttachment
        whenever(visitorMessage.content) doReturn "content"
        whenever(visitorMessage.timestamp) doReturn 1
        whenever(visitorMessage.id) doReturn "1"

        val attachment1 = VisitorAttachmentItem.RemoteFile(
            id = "eloquentiam",
            attachment = mock(),
            isFileExists = false,
            isDownloading = false,
            isError = false,
            timestamp = 3047

        )
        val attachment2 = VisitorAttachmentItem.RemoteFile(
            id = "ludus",
            attachment = mock(),
            isFileExists = false,
            isDownloading = false,
            isError = false,
            timestamp = 4522
        )

        whenever(mapVisitorAttachmentUseCase(eq(file1), any())) doReturn attachment1
        whenever(mapVisitorAttachmentUseCase(eq(file2), any())) doReturn attachment2

        useCase(state, chatMessageInternal)

        assertTrue(state.chatItems.count() == 4)
        assertTrue(state.chatItems.first() is VisitorMessageItem)
        assertEquals(attachment1, state.chatItems[1])
        assertEquals(attachment2, state.chatItems[2])
        assertTrue(state.chatItems.last() is DeliveredItem)

        Mockito.reset(visitorMessage)

        whenever(visitorMessage.attachment) doReturn filesAttachment
        whenever(visitorMessage.content) doReturn ""
        whenever(visitorMessage.timestamp) doReturn 1
        whenever(visitorMessage.id) doReturn "2"

        useCase(state, chatMessageInternal)

        assertTrue(state.chatItems.count() == 6)
        assertTrue(state.chatItems.first() is VisitorMessageItem)
        assertEquals(attachment1, state.chatItems[1])
        assertEquals(attachment2, state.chatItems[2])
        assertEquals(attachment1, state.chatItems[3])
        assertEquals(attachment2, state.chatItems[4])
        assertTrue(state.chatItems.last() is DeliveredItem)
    }

    @Test
    fun `invoke marks Visitor message as delivered when message is in previews`() {
        val messageId = "1"
        val messageContent = "content"
        val messageTimeStamp = 1L

        val messageId1 = "1_1"
        val messageContent1 = "content_1"
        val messageTimeStamp1 = 2L

        val attachment = VisitorAttachmentItem.LocalFile(
            id = "facilis", messageId = messageId1, attachment = mock(), isError = false, timestamp = 9113
        )

        val attachment1 = VisitorAttachmentItem.LocalFile(
            id = "duo", messageId = messageId1, attachment = mock(), isError = false, timestamp = 7757
        )

        val filesAttachment: FilesAttachment = mock()
        val file: AttachmentFile = mock()
        whenever(file.id) doReturn attachment.id
        val file1: AttachmentFile = mock()
        whenever(file1.id) doReturn attachment1.id
        whenever(filesAttachment.files) doReturn arrayOf(file, file1)

        whenever(mapVisitorAttachmentUseCase(eq(file), any())) doReturn attachment
        whenever(mapVisitorAttachmentUseCase(eq(file1), any())) doReturn attachment1

        state.messagePreviews[messageId] = mock()
        state.messagePreviews[messageId1] = mock()
        state.chatItems += VisitorMessageItem(messageContent, messageId, isError = false, messageTimeStamp)
        state.chatItems += VisitorMessageItem(messageContent1, messageId1, isError = false, messageTimeStamp1)
        state.chatItems += attachment
        state.chatItems += attachment1

        whenever(visitorMessage.content) doReturn messageContent
        whenever(visitorMessage.timestamp) doReturn messageTimeStamp
        whenever(visitorMessage.id) doReturn messageId

        assertTrue(state.chatItems.count() == 4)
        assertFalse(state.chatItems.any { it is DeliveredItem })

        useCase(state, chatMessageInternal)

        assertTrue(state.chatItems.count() == 5)
        assertTrue(state.chatItems.first() is VisitorMessageItem)
        assertTrue(state.chatItems[1] is DeliveredItem)
        assertEquals(1, state.messagePreviews.count())

        Mockito.reset(visitorMessage)

        whenever(visitorMessage.content) doReturn messageContent1
        whenever(visitorMessage.timestamp) doReturn messageTimeStamp1
        whenever(visitorMessage.id) doReturn messageId1
        whenever(visitorMessage.attachment) doReturn filesAttachment

        useCase(state, chatMessageInternal)

        assertTrue(state.chatItems.count() == 5)
        assertTrue(state.chatItems.first() is VisitorMessageItem)
        assertTrue(state.chatItems[1] is VisitorMessageItem)
        assertTrue(state.chatItems.last() is DeliveredItem)
        assertEquals(0, state.messagePreviews.count())
    }
}
