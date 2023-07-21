package com.glia.widgets.chat.domain.gva

import com.glia.widgets.chat.MockChatMessageInternal
import com.glia.widgets.chat.model.ChatState
import com.glia.widgets.chat.model.GvaChatItem
import com.glia.widgets.chat.model.GvaQuickReplies
import com.glia.widgets.chat.model.GvaResponseText
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class MapGvaGvaQuickRepliesUseCaseTest {
    private val mockChatMessageInternal: MockChatMessageInternal = MockChatMessageInternal()
    private lateinit var chatState: ChatState
    private lateinit var useCase: MapGvaGvaQuickRepliesUseCase
    private lateinit var parseGvaButtonsUseCase: ParseGvaButtonsUseCase
    private lateinit var mapGvaResponseTextUseCase: MapGvaResponseTextUseCase
    private lateinit var gvaResponseText: GvaResponseText

    @Before
    fun setUp() {
        chatState = mock()

        parseGvaButtonsUseCase = mock()
        whenever(parseGvaButtonsUseCase(anyOrNull())) doReturn emptyList()

        gvaResponseText = mock()

        mapGvaResponseTextUseCase = mock()
        whenever(mapGvaResponseTextUseCase(any(), any())) doReturn gvaResponseText

        useCase = MapGvaGvaQuickRepliesUseCase(parseGvaButtonsUseCase, mapGvaResponseTextUseCase)
    }

    @After
    fun tearDown() {
        mockChatMessageInternal.reset()
    }

    @Test
    fun `invoke returns GvaResponseText when chat message is not last item in chat transcript`() {
        mockChatMessageInternal.apply {
            mockChatMessage()
            mockOperatorProperties()

            whenever(chatMessageInternal.isHistory) doReturn true
            whenever(chatMessageInternal.isLatest) doReturn false

            val gvaChatItem: GvaChatItem = useCase(chatMessageInternal, chatState)
            assertTrue(gvaChatItem is GvaResponseText)
        }
    }

    @Test
    fun `invoke returns GvaQuickReplies when chat message is the last item in chat transcript`() {
        mockChatMessageInternal.apply {
            mockChatMessage()
            mockOperatorProperties()

            whenever(chatMessageInternal.isHistory) doReturn true
            whenever(chatMessageInternal.isLatest) doReturn true

            val gvaChatItem: GvaChatItem = useCase(chatMessageInternal, chatState)
            assertTrue(gvaChatItem is GvaQuickReplies)
        }
    }

    @Test
    fun `invoke returns GvaQuickReplies when chat message is not from chat transcript`() {
        mockChatMessageInternal.apply {
            mockChatMessage()
            mockOperatorProperties()

            whenever(chatMessageInternal.isHistory) doReturn false
            whenever(chatMessageInternal.isLatest) doReturn false

            val gvaChatItem: GvaChatItem = useCase(chatMessageInternal, chatState)
            assertTrue(gvaChatItem is GvaQuickReplies)
        }
    }
}
