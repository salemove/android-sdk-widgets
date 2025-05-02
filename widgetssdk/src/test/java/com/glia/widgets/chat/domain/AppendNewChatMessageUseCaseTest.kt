package com.glia.widgets.chat.domain

import com.glia.androidsdk.chat.ChatMessage
import com.glia.androidsdk.chat.OperatorMessage
import com.glia.androidsdk.chat.SystemMessage
import com.glia.androidsdk.chat.VisitorMessage
import com.glia.widgets.chat.ChatManager
import com.glia.widgets.internal.engagement.domain.model.ChatMessageInternal
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AppendNewChatMessageUseCaseTest {
    private lateinit var appendNewOperatorMessageUseCase: AppendNewOperatorMessageUseCase
    private lateinit var appendNewVisitorMessageUseCase: AppendNewVisitorMessageUseCase
    private lateinit var appendSystemMessageItemUseCase: AppendSystemMessageItemUseCase
    private lateinit var state: ChatManager.State
    private lateinit var chatMessageInternal: ChatMessageInternal
    private lateinit var chatMessage: ChatMessage
    private lateinit var useCase: AppendNewChatMessageUseCase

    @Before
    fun setUp() {
        appendNewOperatorMessageUseCase = mock()
        appendNewVisitorMessageUseCase = mock()
        appendSystemMessageItemUseCase = mock()
        state = mock()
        chatMessageInternal = mock()
        useCase = AppendNewChatMessageUseCase(appendNewOperatorMessageUseCase, appendNewVisitorMessageUseCase, appendSystemMessageItemUseCase)
    }

    @Test
    fun `invoke appends visitor message and reset operator when message is Visitor message`() {
        mockCHatMessage<VisitorMessage>()
        useCase(state, chatMessageInternal)

        verify(appendNewVisitorMessageUseCase).invoke(any(), any())
        verify(appendNewOperatorMessageUseCase, never()).invoke(any(), any())
        verify(appendSystemMessageItemUseCase, never()).invoke(any(), any())
        verify(state).resetOperator()
    }

    @Test
    fun `invoke appends system message and reset operator when message is System message`() {
        mockCHatMessage<SystemMessage>()
        useCase(state, chatMessageInternal)

        verify(appendNewVisitorMessageUseCase, never()).invoke(any(), any())
        verify(appendNewOperatorMessageUseCase, never()).invoke(any(), any())
        verify(appendSystemMessageItemUseCase).invoke(any(), any())
        verify(state).resetOperator()
    }

    @Test
    fun `invoke appends operator message and do not reset operator when message is Operator message`() {
        mockCHatMessage<OperatorMessage>()
        useCase(state, chatMessageInternal)

        verify(appendNewVisitorMessageUseCase, never()).invoke(any(), any())
        verify(appendNewOperatorMessageUseCase).invoke(any(), any())
        verify(appendSystemMessageItemUseCase, never()).invoke(any(), any())
        verify(state, never()).resetOperator()
    }

    @Test
    fun `invoke does nothing when message is unknown message`() {
        mockCHatMessage<ChatMessage>()
        useCase(state, chatMessageInternal)

        verify(appendNewVisitorMessageUseCase, never()).invoke(any(), any())
        verify(appendNewOperatorMessageUseCase, never()).invoke(any(), any())
        verify(appendSystemMessageItemUseCase, never()).invoke(any(), any())
        verify(state, never()).resetOperator()
    }

    private inline fun <reified T : ChatMessage> mockCHatMessage() {
        chatMessage = mock<T>()
        whenever(chatMessageInternal.chatMessage) doReturn chatMessage
    }
}
