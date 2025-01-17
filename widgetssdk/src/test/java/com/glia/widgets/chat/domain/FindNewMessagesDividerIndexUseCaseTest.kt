package com.glia.widgets.chat.domain

import com.glia.widgets.chat.model.ChatItem
import com.glia.widgets.chat.model.ServerChatItem
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import kotlin.properties.Delegates

class FindNewMessagesDividerIndexUseCaseTest {
    private val useCase: FindNewMessagesDividerIndexUseCase = FindNewMessagesDividerIndexUseCase()

    private var operatorChatItem: ServerChatItem by Delegates.notNull()
    private var notOperatorChatItem: ChatItem by Delegates.notNull()

    @Before
    fun setUp() {
        operatorChatItem = mock()
        notOperatorChatItem = mock()
    }

    @Test
    fun `FindNewMessagesDividerIndexUseCase return NOT_PROVIDED when empty list passed`() {
        val index = useCase(emptyList(), 2)
        assertEquals(NOT_PROVIDED, index)
    }

    @Test
    fun `FindNewMessagesDividerIndexUseCase return NOT_PROVIDED when no unread messages`() {
        val index = useCase(listOf(mock(), mock()), 0)
        assertEquals(NOT_PROVIDED, index)
    }

    @Test
    fun `FindNewMessagesDividerIndexUseCase return correct index when no visitor message at the end`() {
        val index = useCase((1..10).map { operatorChatItem }, 2)
        assertEquals(8, index)
    }

    @Test
    fun `FindNewMessagesDividerIndexUseCase return correct index when exists visitor message at the end`() {
        val items = (1..10).map {
            if (it > 8) notOperatorChatItem else operatorChatItem
        }
        val index = useCase(items, 2)
        assertEquals(6, index)
    }

    @Test
    fun `FindNewMessagesDividerIndexUseCase return NOT_PROVIDED when unreadMessagesCount bigger than message list size`() {
        val items = (1..10).map {
            if (it > 8) notOperatorChatItem else operatorChatItem
        }
        val index = useCase(items, 20)
        assertEquals(NOT_PROVIDED, index)
    }
}
