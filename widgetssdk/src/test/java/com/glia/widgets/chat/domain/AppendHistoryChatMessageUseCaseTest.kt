package com.glia.widgets.chat.domain

import com.glia.androidsdk.chat.OperatorMessage
import com.glia.androidsdk.chat.SystemMessage
import com.glia.androidsdk.chat.VisitorMessage
import com.glia.widgets.internal.engagement.domain.model.ChatMessageInternal
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.spy
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AppendHistoryChatMessageUseCaseTest {
    private lateinit var appendHistoryVisitorChatItemUseCase: AppendHistoryVisitorChatItemUseCase
    private lateinit var appendHistoryOperatorChatItemUseCase: AppendHistoryOperatorChatItemUseCase
    private lateinit var appendSystemMessageItemUseCase: AppendSystemMessageItemUseCase

    private lateinit var useCase: AppendHistoryChatMessageUseCase
    private lateinit var chatMessageInternal: ChatMessageInternal

    @Before
    fun setUp() {
        appendHistoryOperatorChatItemUseCase = mock()
        appendHistoryVisitorChatItemUseCase = mock()
        appendSystemMessageItemUseCase = mock()

        chatMessageInternal = mock()

        useCase = spy(
            AppendHistoryChatMessageUseCase(
                appendHistoryVisitorChatItemUseCase,
                appendHistoryOperatorChatItemUseCase,
                appendSystemMessageItemUseCase
            )
        )
    }

    @Test
    fun `resetOperatorId resets operator Id`() {
        useCase.apply {
            operatorId = ""
            resetOperatorId()
            assertNull(operatorId)
        }
    }

    @Test
    fun `shouldShowChatHead returns true when new operator is different`() {
        val operatorId = "operator_id"
        whenever(chatMessageInternal.operatorId) doReturn operatorId

        useCase.apply {
            assertTrue(shouldShowChatHead(chatMessageInternal))
            assertEquals(this.operatorId, operatorId)
        }
    }

    @Test
    fun `shouldShowChatHead returns false when new operator is the same`() {
        val operatorId = "operator_id"
        whenever(chatMessageInternal.operatorId) doReturn operatorId

        useCase.apply {
            this.operatorId = operatorId
            assertFalse(shouldShowChatHead(chatMessageInternal))
            assertEquals(this.operatorId, operatorId)
        }
    }

    /*    operator fun invoke(chatItems: MutableList<ChatItem>, chatMessageInternal: ChatMessageInternal, isLatest: Boolean) {
            when (val message = chatMessageInternal.chatMessage) {
                is VisitorMessage -> {
                    resetOperatorId()
                    appendHistoryVisitorChatItemUseCase(chatItems, message)
                }

                is OperatorMessage -> appendHistoryOperatorChatItemUseCase(
                    chatItems,
                    chatMessageInternal,
                    isLatest,
                    shouldShowChatHead(chatMessageInternal)
                )

                is SystemMessage -> {
                    resetOperatorId()
                    appendSystemMessageItemUseCase(chatItems, message)
                }

                else -> Logger.d(TAG, "Unexpected type of message received -> $message")
            }
        }*/

    @Test
    fun `invoke resets operatorId and appends visitor item when chatMessage is VisitorMessage`() {
        whenever(chatMessageInternal.chatMessage) doReturn mock<VisitorMessage>()

        useCase(mock(), chatMessageInternal, true)

        verify(useCase).resetOperatorId()
        verify(appendHistoryVisitorChatItemUseCase).invoke(any(), any())
    }

    @Test
    fun `invoke resets operatorId and appends system item when chatMessage is SystemMessage`() {
        whenever(chatMessageInternal.chatMessage) doReturn mock<SystemMessage>()

        useCase(mock(), chatMessageInternal, true)

        verify(useCase).resetOperatorId()
        verify(appendSystemMessageItemUseCase).invoke(any(), any())
    }

    @Test
    fun `invoke appends OperatorItem when chatMessage is OperatorMessage`() {
        whenever(chatMessageInternal.chatMessage) doReturn mock<OperatorMessage>()

        useCase(mock(), chatMessageInternal, true)

        verify(useCase).shouldShowChatHead(chatMessageInternal)
        verify(appendHistoryOperatorChatItemUseCase).invoke(any(), any(), any(), any())
    }

    @Test
    fun `invoke does nothing when chatMessage is not one of known types`() {
        whenever(chatMessageInternal.chatMessage) doReturn mock()

        useCase(mock(), chatMessageInternal, true)

        verify(useCase, never()).shouldShowChatHead(any())
        verify(useCase, never()).resetOperatorId()

        verify(appendHistoryVisitorChatItemUseCase, never()).invoke(any(), any())
        verify(appendSystemMessageItemUseCase, never()).invoke(any(), any())
        verify(appendHistoryOperatorChatItemUseCase, never()).invoke(any(), any(), any(), any())
    }
}
