package com.glia.widgets.chat.domain

import com.glia.androidsdk.chat.Chat
import com.glia.androidsdk.chat.OperatorMessage
import com.glia.androidsdk.chat.SingleChoiceAttachment
import com.glia.widgets.chat.adapter.CustomCardMessage
import com.glia.widgets.chat.model.ChatItem
import com.glia.widgets.chat.model.CustomCardChatItem
import com.glia.widgets.chat.model.VisitorMessageItem
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mockStatic
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class AppendHistoryCustomCardItemUseCaseTest {
    private val items: MutableList<ChatItem> = mutableListOf()

    private lateinit var customCardTypeUseCase: CustomCardTypeUseCase
    private lateinit var customCardShouldShowUseCase: CustomCardShouldShowUseCase

    private lateinit var useCase: AppendHistoryCustomCardItemUseCase
    private lateinit var staticMock: AutoCloseable

    @Before
    fun setUp() {
        customCardTypeUseCase = mock()
        customCardShouldShowUseCase = mock()

        whenever(customCardTypeUseCase(any())) doReturn 100

        useCase = AppendHistoryCustomCardItemUseCase(customCardTypeUseCase, customCardShouldShowUseCase)
        staticMock = mockStatic(CustomCardMessage::class.java)
    }

    @After
    fun tearDown() {
        items.clear()
        staticMock.close()
    }

    @Test
    fun `invoke adds CustomCardChatItem when customCardShouldShowUseCase returns true`() {
        whenever(customCardShouldShowUseCase.execute(any(), any(), any())) doReturn true
        val chatMessage: OperatorMessage = mock()
        whenever(chatMessage.id) doReturn "id"
        whenever(chatMessage.content) doReturn ""
        whenever(chatMessage.senderType) doReturn Chat.Participant.OPERATOR
        whenever(chatMessage.timestamp) doReturn -1

        useCase(items, chatMessage, 120)

        assertTrue(items.isNotEmpty())
        assertTrue(items.first() is CustomCardChatItem)
    }

    @Test
    fun `invoke adds VisitorMessage History item  when chatMessage has selectedOptionText`() {
        val singleChoiceAttachment: SingleChoiceAttachment = mock()
        whenever(singleChoiceAttachment.selectedOptionText) doReturn "selected option text"
        val chatMessage: OperatorMessage = mock()

        whenever(chatMessage.attachment) doReturn singleChoiceAttachment
        whenever(chatMessage.id) doReturn "id"
        whenever(chatMessage.content) doReturn ""
        whenever(chatMessage.senderType) doReturn Chat.Participant.OPERATOR
        whenever(chatMessage.timestamp) doReturn -1

        whenever(customCardShouldShowUseCase.execute(any(), any(), any())) doReturn true

        useCase(items, chatMessage, 120)

        assertTrue(items.isNotEmpty())
        assertTrue(items.first() is CustomCardChatItem)
        assertTrue(items[1] is VisitorMessageItem)
    }
}
