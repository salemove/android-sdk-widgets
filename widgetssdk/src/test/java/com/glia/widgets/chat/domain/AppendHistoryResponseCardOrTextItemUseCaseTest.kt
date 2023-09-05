package com.glia.widgets.chat.domain

import com.glia.androidsdk.chat.AttachmentFile
import com.glia.androidsdk.chat.FilesAttachment
import com.glia.androidsdk.chat.SingleChoiceAttachment
import com.glia.widgets.chat.MockChatMessageInternal
import com.glia.widgets.chat.model.ChatItem
import com.glia.widgets.chat.model.OperatorAttachmentItem
import com.glia.widgets.chat.model.OperatorMessageItem
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.spy
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class AppendHistoryResponseCardOrTextItemUseCaseTest {
    private var mockChatMessageInternal: MockChatMessageInternal = MockChatMessageInternal()
    private lateinit var useCase: AppendHistoryResponseCardOrTextItemUseCase

    private lateinit var mapOperatorAttachmentUseCase: MapOperatorAttachmentUseCase
    private lateinit var mapOperatorPlainTextUseCase: MapOperatorPlainTextUseCase
    private lateinit var mapResponseCardUseCase: MapResponseCardUseCase
    private val items: MutableList<ChatItem> = mutableListOf()

    @Before
    fun setUp() {
        mapOperatorAttachmentUseCase = mock()
        mapOperatorPlainTextUseCase = mock()
        mapResponseCardUseCase = mock()

        useCase = spy(AppendHistoryResponseCardOrTextItemUseCase(mapOperatorAttachmentUseCase, mapOperatorPlainTextUseCase, mapResponseCardUseCase))

        mockChatMessageInternal.mockChatMessage()
        mockChatMessageInternal.mockOperatorProperties()
    }

    @After
    fun tearDown() {
        mockChatMessageInternal.reset()
        items.clear()
    }

    @Test
    fun `addResponseCard adds OperatorMessageItem_ResponseCard to provided list`() {
        whenever(mapResponseCardUseCase.invoke(any(), any(), any())) doReturn mock()
        useCase.addResponseCard(items, mock(), mockChatMessageInternal.chatMessageInternal, true)

        assertTrue(items.count() == 1)
        assertTrue(items.first() is OperatorMessageItem.ResponseCard)
    }

    @Test
    fun `addPlainTextAndAttachments adds OperatorMessageItem_PlainText when chatMessage content is not empty`() {
        whenever(mockChatMessageInternal.chatMessageInternal.chatMessage.attachment) doReturn mock()
        whenever(mapOperatorPlainTextUseCase.invoke(any(), any())) doReturn mock<OperatorMessageItem.PlainText>()

        useCase.addPlainTextAndAttachments(items, mockChatMessageInternal.chatMessageInternal, true)

        assertTrue(items.count() == 1)
        assertTrue(items.first() is OperatorMessageItem.PlainText)
    }

    @Test
    fun `addPlainTextAndAttachments does not add OperatorMessageItem_PlainText when chatMessage content is null or empty`() {
        whenever(mockChatMessageInternal.chatMessageInternal.chatMessage.attachment) doReturn mock()
        whenever(mockChatMessageInternal.chatMessageInternal.chatMessage.content) doReturn ""
        whenever(mapOperatorPlainTextUseCase.invoke(any(), any())) doReturn mock<OperatorMessageItem.PlainText>()

        useCase.addPlainTextAndAttachments(items, mockChatMessageInternal.chatMessageInternal, true)

        assertTrue(items.isEmpty())
    }

    @Test
    fun `addPlainTextAndAttachments adds Operator Attachment before plain text when both present`() {
        val filesAttachment: FilesAttachment = mock()
        val file: AttachmentFile = mock()
        whenever(filesAttachment.files) doReturn arrayOf(file)

        whenever(mapOperatorAttachmentUseCase.invoke(any(), any(), any())) doReturn mock<OperatorAttachmentItem.File>()

        whenever(mockChatMessageInternal.chatMessageInternal.chatMessage.attachment) doReturn filesAttachment

        whenever(mapOperatorPlainTextUseCase.invoke(any(), any())) doReturn mock<OperatorMessageItem.PlainText>()

        useCase.addPlainTextAndAttachments(items, mockChatMessageInternal.chatMessageInternal, true)

        assertTrue(items.count() == 2)
        assertTrue(items.first() is OperatorAttachmentItem.File)
        assertTrue(items[1] is OperatorMessageItem.PlainText)
    }

    @Test
    fun `addPlainTextAndAttachments adds Operator Attachment in reversed order if there are more than one`() {
        val filesAttachment: FilesAttachment = mock()
        val file1: AttachmentFile = mock()
        val file2: AttachmentFile = mock()
        whenever(filesAttachment.files) doReturn arrayOf(file1, file2)

        val operatorAttachment1 = mock<OperatorAttachmentItem.Image>()
        val operatorAttachment2 = mock<OperatorAttachmentItem.File>()

        whenever(mapOperatorAttachmentUseCase.invoke(eq(file1), any(), any())) doReturn operatorAttachment1
        whenever(mapOperatorAttachmentUseCase.invoke(eq(file2), any(), any())) doReturn operatorAttachment2

        whenever(mockChatMessageInternal.chatMessageInternal.chatMessage.attachment) doReturn filesAttachment

        whenever(mapOperatorPlainTextUseCase.invoke(any(), any())) doReturn mock<OperatorMessageItem.PlainText>()

        useCase.addPlainTextAndAttachments(items, mockChatMessageInternal.chatMessageInternal, true)

        assertTrue(items.count() == 3)
        assertTrue(items.first() is OperatorAttachmentItem.File)
        assertTrue(items[1] is OperatorAttachmentItem.Image)
        assertTrue(items[2] is OperatorMessageItem.PlainText)
    }

    @Test
    fun `addPlainTextAndAttachments adds Response Card when chatMessage is the latest in history and has SingleChoiceAttachment`() {
        val singleChoiceAttachment: SingleChoiceAttachment = mock()
        whenever(singleChoiceAttachment.options) doReturn arrayOf(mock())

        whenever(mockChatMessageInternal.chatMessageInternal.chatMessage.attachment) doReturn singleChoiceAttachment

        useCase.invoke(items, mockChatMessageInternal.chatMessageInternal, isLatest = true, showChatHead = true)

        verify(useCase).addResponseCard(any(), any(), any(), any())
        verify(useCase, never()).addPlainTextAndAttachments(any(), any(), any())
    }

    @Test
    fun `addPlainTextAndAttachments adds Plain Text when chatMessage is not the latest in history and has SingleChoiceAttachment`() {
        val singleChoiceAttachment: SingleChoiceAttachment = mock()
        whenever(singleChoiceAttachment.options) doReturn arrayOf(mock())

        whenever(mockChatMessageInternal.chatMessageInternal.chatMessage.attachment) doReturn singleChoiceAttachment

        useCase.invoke(items, mockChatMessageInternal.chatMessageInternal, isLatest = false, showChatHead = true)

        verify(useCase, never()).addResponseCard(any(), any(), any(), any())
        verify(useCase).addPlainTextAndAttachments(any(), any(), any())
    }

    @Test
    fun `addPlainTextAndAttachments adds Plain Text when chatMessage is the latest in history and has empty SingleChoiceAttachment`() {
        val singleChoiceAttachment: SingleChoiceAttachment = mock()
        whenever(singleChoiceAttachment.options) doReturn arrayOf()

        whenever(mockChatMessageInternal.chatMessageInternal.chatMessage.attachment) doReturn singleChoiceAttachment

        useCase.invoke(items, mockChatMessageInternal.chatMessageInternal, isLatest = false, showChatHead = true)

        verify(useCase, never()).addResponseCard(any(), any(), any(), any())
        verify(useCase).addPlainTextAndAttachments(any(), any(), any())
    }

    @Test
    fun `addPlainTextAndAttachments adds Plain Text when chatMessage does not has SingleChoiceAttachment`() {
        whenever(mockChatMessageInternal.chatMessageInternal.chatMessage.attachment) doReturn mock<FilesAttachment>()

        useCase.invoke(items, mockChatMessageInternal.chatMessageInternal, isLatest = false, showChatHead = true)

        verify(useCase, never()).addResponseCard(any(), any(), any(), any())
        verify(useCase).addPlainTextAndAttachments(any(), any(), any())
    }

}
