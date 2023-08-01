package com.glia.widgets.chat.domain.gva

import com.glia.widgets.chat.MockChatMessageInternal
import com.glia.widgets.chat.model.GvaButton
import com.glia.widgets.chat.model.GvaResponseText
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class MapGvaQuickRepliesUseCaseTest {
    private val mockChatMessageInternal: MockChatMessageInternal = MockChatMessageInternal()
    private lateinit var useCase: MapGvaQuickRepliesUseCase
    private lateinit var parseGvaButtonsUseCase: ParseGvaButtonsUseCase
    private lateinit var mapGvaResponseTextUseCase: MapGvaResponseTextUseCase
    private lateinit var gvaResponseText: GvaResponseText

    @Before
    fun setUp() {

        parseGvaButtonsUseCase = mock()
        whenever(parseGvaButtonsUseCase(anyOrNull())) doReturn emptyList()

        gvaResponseText = mock()

        mapGvaResponseTextUseCase = mock()
        whenever(mapGvaResponseTextUseCase(any(), any())) doReturn gvaResponseText

        useCase = MapGvaQuickRepliesUseCase(parseGvaButtonsUseCase)
    }

    @After
    fun tearDown() {
        mockChatMessageInternal.reset()
    }

    @Test
    fun `invoke parses GvaQuickReplies when ChatMessage passed with appropriate metadata`() {
        mockChatMessageInternal.apply {
            mockChatMessage(metadataWithContent())
            mockOperatorProperties()

            val quickReplies = useCase(chatMessageInternal, showChatHead = true)

            Assert.assertEquals(quickReplies.id, messageId)
            Assert.assertEquals(quickReplies.content, content)
            Assert.assertEquals(quickReplies.options, emptyList<GvaButton>())
            Assert.assertEquals(quickReplies.operatorId, operatorId)
            Assert.assertEquals(quickReplies.id, messageId)
            Assert.assertEquals(quickReplies.timestamp, messageTimeStamp)
            Assert.assertEquals(quickReplies.operatorProfileImgUrl, operatorImageUrl)
            Assert.assertEquals(quickReplies.operatorName, operatorName)
            Assert.assertEquals(quickReplies.showChatHead, true)
        }
    }
}
