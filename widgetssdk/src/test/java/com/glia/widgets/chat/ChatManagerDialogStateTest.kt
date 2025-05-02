package com.glia.widgets.chat

import com.glia.androidsdk.chat.ChatMessage
import com.glia.widgets.chat.model.ChatItem
import com.glia.widgets.chat.model.OperatorChatItem
import com.glia.widgets.internal.engagement.domain.model.ChatMessageInternal
import junit.framework.TestCase.assertTrue
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class ChatManagerDialogStateTest {
    private lateinit var state: ChatManager.State

    @Before
    fun setUp() {
        state = ChatManager.State()
    }

    @After
    fun tearDown() {
    }

    @Test(expected = UnsupportedOperationException::class)
    fun `immutableChatItems returns immutableList`() {
        state.chatItems.add(mock())
        val immutableList = state.immutableChatItems as MutableList<ChatItem>
        immutableList.add(mock())
    }

    @Test
    fun `isNew adds chatItemId to the ids when it is new`() {
        state.chatItemIds.add("1")
        state.chatItemIds.add("2")
        state.chatItemIds.add("3")

        val chatMessage: ChatMessage = mock()
        whenever(chatMessage.id) doReturn "4"

        val chatMessageInternal: ChatMessageInternal = mock()
        whenever(chatMessageInternal.chatMessage) doReturn chatMessage

        assertTrue(state.isNew(chatMessageInternal))
        assertTrue(state.chatItemIds.count() == 4)
        assertTrue(state.chatItemIds.contains("4"))
    }

    @Test
    fun `isNew does not add chatItemId to the ids when it exists`() {
        state.chatItemIds.add("1")
        state.chatItemIds.add("2")
        state.chatItemIds.add("3")

        val chatMessage: ChatMessage = mock()
        whenever(chatMessage.id) doReturn "3"

        val chatMessageInternal: ChatMessageInternal = mock()
        whenever(chatMessageInternal.chatMessage) doReturn chatMessage

        assertFalse(state.isNew(chatMessageInternal))
        assertTrue(state.chatItemIds.count() == 3)
    }

    @Test
    fun `resetOperator resets lastMessageWithVisibleOperatorImage`() {
        state.lastMessageWithVisibleOperatorImage = mock()
        state.resetOperator()
        assertNull(state.lastMessageWithVisibleOperatorImage)
    }

    @Test
    fun `isOperatorChanged returns true when operator id differs from the existing one`() {
        val operatorChatItem: OperatorChatItem = mock()
        whenever(operatorChatItem.operatorId) doReturn "operator_id"
        assertTrue(state.isOperatorChanged(operatorChatItem))
        assertEquals(state.lastMessageWithVisibleOperatorImage, operatorChatItem)
    }

    @Test
    fun `isOperatorChanged returns false when operator id is the same as existing one`() {
        val operatorChatItem: OperatorChatItem = mock()
        whenever(operatorChatItem.operatorId) doReturn "operator_id"

        state.lastMessageWithVisibleOperatorImage = operatorChatItem

        assertFalse(state.isOperatorChanged(operatorChatItem))
        assertEquals(state.lastMessageWithVisibleOperatorImage, operatorChatItem)
    }
}
