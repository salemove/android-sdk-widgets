package com.glia.widgets.chat.domain.gva

import com.glia.widgets.chat.MockChatMessageInternal
import junit.framework.TestCase.assertEquals
import org.junit.After
import org.junit.Before
import org.junit.Test

class MapGvaResponseTextUseCaseTest {
    private val mockChatMessageInternal: MockChatMessageInternal = MockChatMessageInternal()
    private lateinit var useCase: MapGvaResponseTextUseCase

    @Before
    fun setUp() {
        useCase = MapGvaResponseTextUseCase()
    }

    @After
    fun tearDown() {
        mockChatMessageInternal.reset()
    }

    @Test
    fun `invoke parses GvaResponseText when ChatMessage passed with appropriate metadata`() {
        mockChatMessageInternal.apply {
            mockChatMessage(metadataWithContent())
            mockOperatorProperties()

            val galleryCard = useCase(chatMessageInternal, false)

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
}
