package com.glia.widgets.chat.domain

import com.glia.androidsdk.chat.ChatMessage
import com.glia.widgets.chat.domain.gva.MapGvaUseCase
import com.glia.widgets.chat.model.ChatItem
import com.glia.widgets.chat.model.GvaOperatorChatItem
import com.glia.widgets.internal.engagement.domain.model.ChatMessageInternal
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class AppendGvaMessageItemUseCaseTest {
    private val items: MutableList<ChatItem> = mutableListOf()

    private lateinit var mapGvaUseCase: MapGvaUseCase
    private lateinit var useCase: AppendGvaMessageItemUseCase

    @Before
    fun setUp() {
        mapGvaUseCase = mock()
        useCase = AppendGvaMessageItemUseCase(mapGvaUseCase)
    }

    @After
    fun tearDown() {
        items.clear()
    }

    @Test
    fun `invoke adds GvaOperatorChatItem to the list`() {
        whenever(mapGvaUseCase.invoke(any(), any())) doReturn mock<GvaOperatorChatItem>()
        useCase(items, mock<ChatMessageInternal> { on { chatMessage } doReturn mock() })
        assertTrue(items.count() == 1)
        assertTrue(items.first() is GvaOperatorChatItem)
    }
}
