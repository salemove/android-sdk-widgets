package com.glia.widgets.chat.domain.gva

import com.glia.widgets.chat.MockChatMessageInternal
import com.glia.widgets.chat.model.ChatState
import com.glia.widgets.chat.model.GvaButton
import junit.framework.TestCase.assertEquals
import org.junit.After
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
        mockChatMessageInternal.apply {
            mockChatMessage(metadataWithContent())
            mockOperatorProperties()

            val galleryCard = useCase(chatMessageInternal, true)

            assertEquals(galleryCard.id, messageId)
            assertEquals(galleryCard.content, content)
            assertEquals(galleryCard.options, emptyList<GvaButton>())
            assertEquals(galleryCard.showChatHead, true)
            assertEquals(galleryCard.operatorId, operatorId)
            assertEquals(galleryCard.id, messageId)
            assertEquals(galleryCard.timestamp, messageTimeStamp)
            assertEquals(galleryCard.operatorProfileImgUrl, operatorImageUrl)
            assertEquals(galleryCard.operatorName, operatorName)
        }
    }
}
