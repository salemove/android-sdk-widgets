package com.glia.widgets.chat.domain

import com.glia.androidsdk.chat.Chat
import com.glia.androidsdk.chat.ChatMessage
import com.glia.widgets.core.engagement.domain.model.ChatMessageInternal
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import kotlin.properties.Delegates

class FindNewMessagesDividerIndexUseCaseTest {
    private val useCase: FindNewMessagesDividerIndexUseCase =
        FindNewMessagesDividerIndexUseCase()

    private var operatorChatMessage: ChatMessage by Delegates.notNull()
    private var visitorChatMessage: ChatMessage by Delegates.notNull()

    @Before
    fun setUp() {
        operatorChatMessage = mock()
        whenever(operatorChatMessage.senderType) doReturn Chat.Participant.OPERATOR

        visitorChatMessage = mock()
        whenever(visitorChatMessage.senderType) doReturn Chat.Participant.VISITOR
    }

    @Test
    fun `ComputeNewMessagesDividerIndexUseCase return -1 when empty list passed`() {
        val index = useCase.invoke(emptyList(), 2)
        assertEquals(-1, index)
    }

    @Test
    fun `ComputeNewMessagesDividerIndexUseCase return -1 when unreadMessagesCount is 0`() {
        val index = useCase.invoke(listOf(mock(), mock()), 0)
        assertEquals(-1, index)
    }

    @Test
    fun `ComputeNewMessagesDividerIndexUseCase return correct index when no visitor message at the end`() {
        val index =
            useCase.invoke((1..10).map { ChatMessageInternal(operatorChatMessage, null) }, 2)
        assertEquals(8, index)
    }

    @Test
    fun `ComputeNewMessagesDividerIndexUseCase return correct index when exists visitor message at the end`() {
        val items = (1..10).map {
            ChatMessageInternal(if (it > 8) visitorChatMessage else operatorChatMessage, null)
        }
        val index = useCase.invoke(items, 2)
        assertEquals(6, index)
    }

    @Test
    fun `ComputeNewMessagesDividerIndexUseCase return 0 when unreadMessagesCount bigger than message list size`() {
        val items = (1..10).map {
            ChatMessageInternal(if (it > 8) visitorChatMessage else operatorChatMessage, null)
        }
        val index = useCase.invoke(items, 20)
        assertEquals(0, index)
    }
}