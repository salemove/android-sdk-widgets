package com.glia.widgets.chat.domain.gva

import com.glia.widgets.chat.MockChatMessageInternal
import com.glia.widgets.chat.model.ChatState
import com.glia.widgets.chat.model.Gva
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class MapGvaResponseTextUseCaseTest {
    private val mockChatMessageInternal: MockChatMessageInternal = MockChatMessageInternal()
    private lateinit var chatState: ChatState
    private lateinit var useCase: MapGvaResponseTextUseCase

    @Before
    fun setUp() {
        chatState = mock()
        useCase = MapGvaResponseTextUseCase()
    }

    @After
    fun tearDown() {
        mockChatMessageInternal.reset()
    }

    @Test
    fun `invoke parses GvaResponseText when ChatMessage passed with appropriate metadata`() {
        val content = "content"
        mockChatMessageInternal.apply {
            mockChatMessage(JSONObject().put(Gva.Keys.CONTENT, content))
            mockOperatorProperties()

            val galleryCard = useCase(chatMessageInternal, chatState)

            assertEquals(galleryCard.id, messageId)
            assertEquals(galleryCard.content, content)
            assertEquals(galleryCard.showChatHead, false)
            assertEquals(galleryCard.operatorId, operatorId)
            assertEquals(galleryCard.id, messageId)
            assertEquals(galleryCard.timestamp, messageTimeStamp)
            assertEquals(galleryCard.operatorProfileImgUrl, operatorImageUrl)
            assertEquals(galleryCard.operatorName, operatorName)
        }
    }

    @Test
    fun `invoke takes operator data from chatState when it is null in ChatMessage`() {
        mockChatMessageInternal.apply {
            mockChatMessage()
            mockOperatorPropertiesWithNull()

            whenever(chatState.operatorProfileImgUrl) doReturn operatorImageUrl
            whenever(chatState.formattedOperatorName) doReturn operatorName

            val galleryCard = useCase(chatMessageInternal, chatState)

            assertEquals(galleryCard.operatorProfileImgUrl, operatorImageUrl)
            assertEquals(galleryCard.operatorName, operatorName)
        }
    }
}
