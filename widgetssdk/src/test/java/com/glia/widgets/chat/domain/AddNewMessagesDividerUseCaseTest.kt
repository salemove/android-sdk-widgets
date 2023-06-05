package com.glia.widgets.chat.domain

import com.glia.widgets.chat.model.history.ChatItem
import com.glia.widgets.chat.model.history.NewMessagesItem
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class AddNewMessagesDividerUseCaseTest {
    private lateinit var findNewMessagesDividerIndexUseCase: FindNewMessagesDividerIndexUseCase
    private lateinit var useCase: AddNewMessagesDividerUseCase

    @Before
    fun setUp() {
        findNewMessagesDividerIndexUseCase = mock()
        useCase = AddNewMessagesDividerUseCase(findNewMessagesDividerIndexUseCase)
    }

    @Test
    fun `AddNewMessagesDividerUseCase returns false if index is NOT_PROVIDED`() {
        whenever(findNewMessagesDividerIndexUseCase(any(), any())) doReturn NOT_PROVIDED
        val items: MutableList<ChatItem> = mutableListOf()
        val added = useCase(items, 10)
        assertFalse(added)
        assertFalse(items.contains(NewMessagesItem))
    }

    @Test
    fun `AddNewMessagesDividerUseCase returns true if index is 0`() {
        whenever(findNewMessagesDividerIndexUseCase(any(), any())) doReturn 0
        val items: MutableList<ChatItem> = mutableListOf()
        val added = useCase(items, 10)
        assertTrue(added)
        assertTrue(items.contains(NewMessagesItem))
    }

    @Test
    fun `AddNewMessagesDividerUseCase returns true if index is positive`() {
        whenever(findNewMessagesDividerIndexUseCase(any(), any())) doReturn 1
        val items: MutableList<ChatItem> = mutableListOf(mock(), mock())
        val added = useCase(items, 10)
        assertTrue(added)
        assertTrue(items.contains(NewMessagesItem))
    }
}
