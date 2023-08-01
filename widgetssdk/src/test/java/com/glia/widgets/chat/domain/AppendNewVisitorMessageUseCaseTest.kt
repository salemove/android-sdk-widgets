package com.glia.widgets.chat.domain

import com.glia.androidsdk.chat.AttachmentFile
import com.glia.androidsdk.chat.FilesAttachment
import com.glia.androidsdk.chat.VisitorMessage
import com.glia.widgets.chat.ChatManager
import com.glia.widgets.chat.model.VisitorAttachmentItem
import com.glia.widgets.chat.model.VisitorChatItem
import com.glia.widgets.chat.model.VisitorMessageItem
import com.glia.widgets.core.engagement.domain.model.ChatMessageInternal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
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
    fun `addUnsentItem returns false when unsentItems is empty`() {
        assertFalse(useCase.addUnsentItem(state, visitorMessage))
    }

    @Test
    fun `addUnsentItem returns false when unsentItems does not contain received message`() {
        state.unsentItems.add(mock())
        assertFalse(useCase.addUnsentItem(state, visitorMessage))
    }

    @Test
    fun `addUnsentItem returns false when unsentItems contain received message but does not exist in chatItems`() {
        val content = "content"
        whenever(visitorMessage.content) doReturn content

        state.unsentItems.add(VisitorMessageItem.Unsent(message = content))
        assertFalse(useCase.addUnsentItem(state, visitorMessage))
    }

    @Test
    fun `addUnsentItem returns true when unsentItems and chatItems contain received message`() {
        val lastDelivered: VisitorChatItem = VisitorMessageItem.Delivered("id", 1, "message")

        useCase.lastDeliveredItem = lastDelivered
        state.chatItems.add(lastDelivered)

        val content = "content"
        whenever(visitorMessage.content) doReturn content
        whenever(visitorMessage.id) doReturn "id"
        whenever(visitorMessage.timestamp) doReturn 1

        val unsentMessage = VisitorMessageItem.Unsent(message = content)
        state.unsentItems.add(unsentMessage)
        state.chatItems.add(unsentMessage)

        assertTrue(useCase.addUnsentItem(state, visitorMessage))
        assertTrue(state.unsentItems.isEmpty())
        assertTrue(state.chatItems.last() is VisitorMessageItem.Delivered)
        assertTrue(state.chatItems.first() is VisitorMessageItem.New)
    }

    @Test
    fun `invoke does nothing when addUnsentItem returns true`() {
        doReturn(true).whenever(useCase).addUnsentItem(any(), any())

        useCase(state, chatMessageInternal)

        assertTrue(state.chatItems.isEmpty())
        assertNull(useCase.lastDeliveredItem)
    }

    @Test
    fun `invoke appends VisitorMessageItem_Delivered when message has no attachment`() {
        doReturn(false).whenever(useCase).addUnsentItem(any(), any())

        whenever(visitorMessage.content) doReturn "content"
        whenever(visitorMessage.timestamp) doReturn 1
        whenever(visitorMessage.id) doReturn "1"

        useCase(state, chatMessageInternal)

        assertTrue(state.chatItems.count() == 1)
        assertTrue(state.chatItems.first() is VisitorMessageItem.Delivered)
    }

    @Test
    fun `invoke appends VisitorMessageItem_New when message has files`() {
        doReturn(false).whenever(useCase).addUnsentItem(any(), any())

        val filesAttachment: FilesAttachment = mock()
        val file: AttachmentFile = mock()
        whenever(filesAttachment.files) doReturn arrayOf(file)

        whenever(visitorMessage.attachment) doReturn filesAttachment
        whenever(visitorMessage.content) doReturn "content"
        whenever(visitorMessage.timestamp) doReturn 1
        whenever(visitorMessage.id) doReturn "1"

        val attachmentWithShowDeliveredTrue = mock<VisitorAttachmentItem.File>().apply { whenever(this.showDelivered) doReturn true }
        val attachmentWithShowDeliveredFalse = mock<VisitorAttachmentItem.File>().apply { whenever(this.showDelivered) doReturn false }

        whenever(mapVisitorAttachmentUseCase(any(), any(), eq(true))) doReturn attachmentWithShowDeliveredTrue
        whenever(mapVisitorAttachmentUseCase(any(), any(), eq(false))) doReturn attachmentWithShowDeliveredFalse

        useCase(state, chatMessageInternal)

        assertTrue(state.chatItems.count() == 2)
        assertTrue(state.chatItems.first() is VisitorMessageItem.New)
        assertTrue(state.chatItems.last() is VisitorAttachmentItem.File)
        assertTrue((state.chatItems.last() as VisitorChatItem).showDelivered)
        assertTrue(useCase.lastDeliveredItem is VisitorAttachmentItem.File)
        assertEquals(useCase.lastDeliveredItem, state.chatItems.last())
    }

    @Test
    fun `invoke changes lastDeliveredItem when it exists`() {
        doReturn(false).whenever(useCase).addUnsentItem(any(), any())

        whenever(visitorMessage.content) doReturn "content"
        whenever(visitorMessage.timestamp) doReturn 1
        whenever(visitorMessage.id) doReturn "1"

        useCase(state, chatMessageInternal)

        assertTrue(state.chatItems.count() == 1)
        assertTrue((state.chatItems.last() as VisitorMessageItem).showDelivered)
        assertEquals(state.chatItems.last(), useCase.lastDeliveredItem)

        whenever(visitorMessage.id) doReturn "2"

        useCase(state, chatMessageInternal)

        assertTrue(state.chatItems.count() == 2)
        assertFalse((state.chatItems.first() as VisitorMessageItem).showDelivered)
        assertTrue((state.chatItems.last() as VisitorMessageItem).showDelivered)
        assertEquals(state.chatItems.last(), useCase.lastDeliveredItem)
    }
}
