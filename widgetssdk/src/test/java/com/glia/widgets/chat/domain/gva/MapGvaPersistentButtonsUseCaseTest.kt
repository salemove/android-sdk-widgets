package com.glia.widgets.chat.domain.gva

import com.glia.widgets.chat.MockChatMessageInternal
import com.glia.widgets.chat.model.ChatState
import com.glia.widgets.chat.model.Gva
import com.glia.widgets.chat.model.GvaButton
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class MapGvaPersistentButtonsUseCaseTest {
    private val mockChatMessageInternal: MockChatMessageInternal = MockChatMessageInternal()
    private lateinit var chatState: ChatState
    private lateinit var useCase: MapGvaPersistentButtonsUseCase
    private lateinit var parseGvaButtonsUseCase: ParseGvaButtonsUseCase

    @Before
    fun setUp() {
        chatState = mock()

        parseGvaButtonsUseCase = mock()
        whenever(parseGvaButtonsUseCase(anyOrNull())) doReturn emptyList()

        useCase = MapGvaPersistentButtonsUseCase(parseGvaButtonsUseCase)
    }

    @After
    fun tearDown() {
        mockChatMessageInternal.reset()
    }

    @Test
    fun `invoke parses GvaPersistentButtons when ChatMessage passed with appropriate metadata`() {
        val content = "content"
        mockChatMessageInternal.apply {
            mockChatMessage(JSONObject().put(Gva.Keys.CONTENT, content))
            mockOperatorProperties()

            val galleryCard = useCase(chatMessageInternal, chatState)

            assertEquals(galleryCard.messageId, messageId)
            assertEquals(galleryCard.content, content)
            assertEquals(galleryCard.options, emptyList<GvaButton>())
            assertEquals(galleryCard.showChatHead, false)
            assertEquals(galleryCard.operatorId, operatorId)
            assertEquals(galleryCard.messageId, messageId)
            assertEquals(galleryCard.timestamp, messageTimeStamp)
            assertEquals(galleryCard.operatorProfileImageUrl, operatorImageUrl)
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

            assertEquals(galleryCard.operatorProfileImageUrl, operatorImageUrl)
            assertEquals(galleryCard.operatorName, operatorName)
        }
    }
}
