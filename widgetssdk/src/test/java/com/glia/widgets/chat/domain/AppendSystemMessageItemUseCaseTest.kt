package com.glia.widgets.chat.domain

import com.glia.androidsdk.chat.SystemMessage
import com.glia.widgets.chat.model.ChatItem
import com.glia.widgets.chat.model.OperatorStatusItem
import com.glia.widgets.chat.model.SystemChatItem
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class AppendSystemMessageItemUseCaseTest {
    private val items: MutableList<ChatItem> = mutableListOf()

    private lateinit var useCase: AppendSystemMessageItemUseCase
    private lateinit var systemMessage: SystemMessage

    @Before
    fun setUp() {
        useCase = AppendSystemMessageItemUseCase()
        systemMessage = mock<SystemMessage>().apply {
            whenever(id) doReturn "id"
            whenever(timestamp) doReturn -1
            whenever(content) doReturn "content"
        }
    }

    @After
    fun tearDown() {
        items.clear()
    }

    @Test
    fun `invoke adds system message at the end of list when OperatorStatusItem_InQueue is not present in list`() {
        useCase(items, systemMessage)
        assertTrue(items.count() == 1)
        assertTrue(items.last() is SystemChatItem)
    }

    @Test
    fun `invoke adds system message before OperatorStatusItem_InQueue when it is the latest item in list`() {
        items += mock<OperatorStatusItem.InQueue>()
        useCase(items, systemMessage)
        assertTrue(items.count() == 2)
        assertTrue(items.last() is OperatorStatusItem.InQueue)
        assertTrue(items.first() is SystemChatItem)
    }
}
