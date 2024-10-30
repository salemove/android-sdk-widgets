package com.glia.widgets.chat.domain

import com.glia.androidsdk.chat.AttachmentFile
import com.glia.androidsdk.chat.FilesAttachment
import com.glia.androidsdk.chat.VisitorMessage
import com.glia.widgets.chat.model.ChatItem
import com.glia.widgets.chat.model.VisitorAttachmentItem
import com.glia.widgets.chat.model.VisitorItemStatus
import com.glia.widgets.chat.model.VisitorMessageItem
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class AppendHistoryVisitorChatItemUseCaseTest {
    private val items: MutableList<ChatItem> = mutableListOf()
    private lateinit var mapVisitorAttachmentUseCase: MapVisitorAttachmentUseCase
    private lateinit var visitorMessage: VisitorMessage
    private lateinit var useCase: AppendHistoryVisitorChatItemUseCase

    @Before
    fun setUp() {
        mapVisitorAttachmentUseCase = mock()
        visitorMessage = mock()
        useCase = AppendHistoryVisitorChatItemUseCase(mapVisitorAttachmentUseCase)
    }

    @After
    fun tearDown() {
        items.clear()
    }

    @Test
    fun `invoke adds VisitorMessageItem_History item to the list when visitor message content is not empty`() {
        whenever(mapVisitorAttachmentUseCase.invoke(any(), any(), any())) doReturn mock<VisitorAttachmentItem.RemoteImage>()

        whenever(visitorMessage.id) doReturn "id"
        whenever(visitorMessage.timestamp) doReturn -1
        whenever(visitorMessage.content) doReturn "content"

        useCase(items, visitorMessage)

        assertTrue(items.count() == 1)
        assertTrue(items.last() is VisitorMessageItem)
        assertEquals(VisitorItemStatus.HISTORY, (items.last() as VisitorMessageItem).status)
    }

    @Test
    fun `invoke does not add VisitorMessageItem_History item to the list when visitor message content is null or empty`() {
        whenever(mapVisitorAttachmentUseCase.invoke(any(), any(), any())) doReturn mock<VisitorAttachmentItem.RemoteFile>()
        whenever(visitorMessage.content) doReturn ""
        useCase(items, visitorMessage)

        assertTrue(items.isEmpty())
    }

    @Test
    fun `invoke adds VisitorAttachmentItem before VisitorMessageItem_History when both present`() {
        whenever(mapVisitorAttachmentUseCase.invoke(any(), any(), any())) doReturn mock<VisitorAttachmentItem.RemoteFile>()

        val filesAttachment: FilesAttachment = mock()
        val file: AttachmentFile = mock()
        whenever(filesAttachment.files) doReturn arrayOf(file)

        whenever(visitorMessage.id) doReturn "id"
        whenever(visitorMessage.timestamp) doReturn -1
        whenever(visitorMessage.content) doReturn "content"
        whenever(visitorMessage.attachment) doReturn filesAttachment

        useCase(items, visitorMessage)

        assertTrue(items.count() == 2)
        assertTrue(items.first() is VisitorAttachmentItem.RemoteFile)
        assertTrue(items[1] is VisitorMessageItem)
    }

    @Test
    fun `invoke adds VisitorAttachmentItems in reversed order if there are more than one`() {
        val filesAttachment: FilesAttachment = mock()
        val file1: AttachmentFile = mock()
        val file2: AttachmentFile = mock()
        whenever(filesAttachment.files) doReturn arrayOf(file1, file2)

        val visitorAttachment1 = mock<VisitorAttachmentItem.RemoteImage>()
        val visitorAttachment2 = mock<VisitorAttachmentItem.RemoteFile>()

        whenever(mapVisitorAttachmentUseCase.invoke(eq(file1), any(), any())) doReturn visitorAttachment1
        whenever(mapVisitorAttachmentUseCase.invoke(eq(file2), any(), any())) doReturn visitorAttachment2

        whenever(visitorMessage.id) doReturn "id"
        whenever(visitorMessage.timestamp) doReturn -1
        whenever(visitorMessage.content) doReturn "content"
        whenever(visitorMessage.attachment) doReturn filesAttachment

        useCase(items, visitorMessage)

        assertTrue(items.count() == 3)
        assertTrue(items.first() is VisitorAttachmentItem.RemoteFile)
        assertTrue(items[1] is VisitorAttachmentItem.RemoteImage)
        assertTrue(items[2] is VisitorMessageItem)
    }
}
