package com.glia.widgets.chat.domain

import com.glia.androidsdk.chat.ChatMessage
import com.glia.androidsdk.chat.FilesAttachment
import com.glia.androidsdk.chat.SingleChoiceAttachment
import com.glia.widgets.chat.model.ChatItem
import com.glia.widgets.chat.model.OperatorAttachmentItem
import com.glia.widgets.chat.model.OperatorMessageItem
import com.glia.widgets.internal.engagement.domain.model.ChatMessageInternal
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

class AppendNewResponseCardOrTextItemUseCaseTest {
    private val chatItems: MutableList<ChatItem> = mutableListOf()
    private lateinit var mapOperatorAttachmentUseCase: MapOperatorAttachmentUseCase
    private lateinit var mapOperatorPlainTextUseCase: MapOperatorPlainTextUseCase
    private lateinit var mapResponseCardUseCase: MapResponseCardUseCase
    private lateinit var useCase: AppendNewResponseCardOrTextItemUseCase
    private lateinit var chatMessageInternal: ChatMessageInternal
    private lateinit var chatMessage: ChatMessage

    @Before
    fun setUp() {
        chatMessageInternal = mock()
        chatMessage = mock()
        whenever(chatMessageInternal.chatMessage) doReturn chatMessage

        mapOperatorAttachmentUseCase = mock()
        mapOperatorPlainTextUseCase = mock()
        mapResponseCardUseCase = mock()
        useCase = spy(AppendNewResponseCardOrTextItemUseCase(mapOperatorAttachmentUseCase, mapOperatorPlainTextUseCase, mapResponseCardUseCase))
    }

    @After
    fun tearDown() {
        chatItems.clear()
    }

    @Test
    fun `addResponseCard add ResponseCard to the chat items list`() {
        whenever(mapResponseCardUseCase(any(), any(), any())) doReturn mock()
        useCase.addResponseCard(chatItems, mock(), mock{ on { chatMessage } doReturn mock() })
        assertTrue(chatItems.isNotEmpty())
        assertTrue(chatItems.last() is OperatorMessageItem.ResponseCard)
    }

    @Test
    fun `addPlainTextAndAttachments does not add anything to chat when message content and attachment are empty`() {
        whenever(chatMessage.content) doReturn ""
        useCase.addPlainTextAndAttachments(chatItems, chatMessageInternal)
        assertTrue(chatItems.isEmpty())
    }

    @Test
    fun `addPlainTextAndAttachments adds attachments after text when both are available`() {
        val attachment: FilesAttachment = mock()
        whenever(attachment.files) doReturn arrayOf(mock())

        whenever(chatMessage.content) doReturn "content"
        whenever(chatMessage.attachment) doReturn attachment
        whenever(mapOperatorPlainTextUseCase(any(), any())) doReturn mock<OperatorMessageItem.PlainText>()

        val operatorAttachmentItemTrue = mock<OperatorAttachmentItem.Image>().apply { whenever(showChatHead) doReturn true }
        val operatorAttachmentItemFalse = mock<OperatorAttachmentItem.File>().apply { whenever(showChatHead) doReturn false }
        whenever(mapOperatorAttachmentUseCase(any(), any(), eq(true))) doReturn operatorAttachmentItemTrue
        whenever(mapOperatorAttachmentUseCase(any(), any(), eq(false))) doReturn operatorAttachmentItemFalse

        useCase.addPlainTextAndAttachments(chatItems, chatMessageInternal)

        assertTrue(chatItems.count() == 2)
        assertTrue(chatItems.first() is OperatorMessageItem.PlainText)
        assertTrue((chatItems.last() as OperatorAttachmentItem).showChatHead)
    }

    @Test
    fun `invoke adds ResponseCard when message has SingleChoiceAttachment with at least one option`() {
        val attachment: SingleChoiceAttachment = mock()
        whenever(attachment.options) doReturn arrayOf(mock())

        whenever(chatMessage.attachment) doReturn attachment

        useCase(chatItems, chatMessageInternal)

        verify(useCase).addResponseCard(any(), any(), any())
        verify(useCase, never()).addPlainTextAndAttachments(any(), any())
    }

    @Test
    fun `invoke adds PlainText when message does not have SingleChoiceAttachment`() {
        val attachment: FilesAttachment = mock()
        whenever(attachment.files) doReturn arrayOf(mock())

        whenever(chatMessage.content) doReturn "content"
        whenever(chatMessage.attachment) doReturn attachment

        useCase(chatItems, chatMessageInternal)

        verify(useCase, never()).addResponseCard(any(), any(), any())
        verify(useCase).addPlainTextAndAttachments(any(), any())
    }
}
