package com.glia.widgets.chat.domain

import com.glia.androidsdk.chat.OperatorMessage
import com.glia.widgets.chat.ChatManager
import com.glia.widgets.chat.domain.gva.IsGvaUseCase
import com.glia.widgets.chat.model.OperatorChatItem
import com.glia.widgets.chat.model.OperatorMessageItem
import com.glia.widgets.chat.model.VisitorMessageItem
import com.glia.widgets.internal.engagement.domain.model.ChatMessageInternal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.spy
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class AppendNewOperatorMessageUseCaseTest {
    private lateinit var isGvaUseCase: IsGvaUseCase
    private lateinit var customCardAdapterTypeUseCase: CustomCardAdapterTypeUseCase
    private lateinit var appendGvaMessageItemUseCase: AppendGvaMessageItemUseCase
    private lateinit var appendHistoryCustomCardItemUseCase: AppendHistoryCustomCardItemUseCase
    private lateinit var appendNewResponseCardOrTextItemUseCase: AppendNewResponseCardOrTextItemUseCase
    private lateinit var operatorMessage: OperatorMessage
    private lateinit var chatMessageInternal: ChatMessageInternal
    private lateinit var state: ChatManager.State

    private lateinit var useCase: AppendNewOperatorMessageUseCase

    @Before
    fun setUp() {
        isGvaUseCase = mock()
        customCardAdapterTypeUseCase = mock()
        appendGvaMessageItemUseCase = mock()
        appendHistoryCustomCardItemUseCase = mock()
        appendNewResponseCardOrTextItemUseCase = mock()

        operatorMessage = mock()
        chatMessageInternal = mock()
        whenever(chatMessageInternal.chatMessage) doReturn operatorMessage

        state = spy(ChatManager.State())

        useCase = AppendNewOperatorMessageUseCase(
            isGvaUseCase,
            customCardAdapterTypeUseCase,
            appendGvaMessageItemUseCase,
            appendHistoryCustomCardItemUseCase,
            appendNewResponseCardOrTextItemUseCase
        )
    }

    @Test
    fun `invoke adds GVA message when chatMessage type is GVA`() {
        whenever(isGvaUseCase(any())) doReturn true
        useCase(state, chatMessageInternal)
        verify(appendGvaMessageItemUseCase).invoke(any(), any(), any())
        verify(appendHistoryCustomCardItemUseCase, never()).invoke(any(), any(), any())
        verify(appendNewResponseCardOrTextItemUseCase, never()).invoke(any(), any())
    }

    @Test
    fun `invoke adds Custom Card message when chatMessage type is Custom Card`() {
        whenever(customCardAdapterTypeUseCase(any())) doReturn 100
        useCase(state, chatMessageInternal)
        verify(appendGvaMessageItemUseCase, never()).invoke(any(), any(), any())
        verify(appendHistoryCustomCardItemUseCase).invoke(any(), any(), any())
        verify(appendNewResponseCardOrTextItemUseCase, never()).invoke(any(), any())
    }

    @Test
    fun `invoke adds ResponseCard or Text message when chatMessage type is not Custom Card or GVA`() {
        whenever(customCardAdapterTypeUseCase(any())) doReturn null
        whenever(isGvaUseCase(any())) doReturn false
        useCase(state, chatMessageInternal)
        verify(appendGvaMessageItemUseCase, never()).invoke(any(), any(), any())
        verify(appendHistoryCustomCardItemUseCase, never()).invoke(any(), any(), any())
        verify(appendNewResponseCardOrTextItemUseCase).invoke(any(), any())
    }

    @Test
    fun `invoke updates addedMessagesCount when new message added`() {
        whenever(customCardAdapterTypeUseCase(any())) doReturn null
        whenever(isGvaUseCase(any())) doReturn false
        doAnswer {
            state.chatItems.add(mock<OperatorMessageItem.PlainText>())
        }.whenever(appendNewResponseCardOrTextItemUseCase).invoke(any(), any())

        useCase(state, chatMessageInternal)
        assertEquals(1, state.addedMessagesCount)
    }

    @Test
    fun `invoke resets operator when the last item is not OperatorChatItem`() {
        whenever(customCardAdapterTypeUseCase(any())) doReturn null
        whenever(isGvaUseCase(any())) doReturn false
        doAnswer {
            state.chatItems.add(mock())
            state.chatItems.add(mock<VisitorMessageItem>())
        }.whenever(appendNewResponseCardOrTextItemUseCase).invoke(any(), any())

        useCase(state, chatMessageInternal)
        verify(state).resetOperator()
        verify(state, never()).isOperatorChanged(any())
    }

    @Test
    fun `invoke changes hides operator image for previous item when operator not changed`() {
        whenever(customCardAdapterTypeUseCase(any())) doReturn null
        whenever(isGvaUseCase(any())) doReturn false

        val operatorChatItem: OperatorChatItem = OperatorMessageItem.PlainText("id", 1, true, "img", "operator_id", "name", "content")
        state.lastMessageWithVisibleOperatorImage = operatorChatItem

        doAnswer {
            state.chatItems.add(operatorChatItem)
        }.whenever(appendNewResponseCardOrTextItemUseCase).invoke(any(), any())

        whenever(state.isOperatorChanged(operatorChatItem)) doReturn false

        useCase(state, chatMessageInternal)
        assertFalse((state.chatItems.last() as OperatorChatItem).showChatHead)
    }
}
