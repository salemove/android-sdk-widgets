package com.glia.widgets.chat.domain.gva

import com.glia.widgets.chat.MockChatMessageInternal
import com.glia.widgets.chat.model.GvaGalleryCard
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class MapGvaGvaGalleryCardsUseCaseTest {
    private lateinit var useCase: MapGvaGvaGalleryCardsUseCase
    private lateinit var parseGvaGalleryCardsUseCase: ParseGvaGalleryCardsUseCase
    private val mockChatMessageInternal: MockChatMessageInternal = MockChatMessageInternal()

    @Before
    fun setUp() {
        parseGvaGalleryCardsUseCase = mock()
        whenever(parseGvaGalleryCardsUseCase(any())) doReturn emptyList()
        useCase = MapGvaGvaGalleryCardsUseCase(parseGvaGalleryCardsUseCase)
    }

    @After
    fun tearDown() {
        mockChatMessageInternal.reset()
    }

    @Test
    fun `invoke parses GvaGalleryCard when ChatMessage passed with appropriate metadata`() {
        mockChatMessageInternal.apply {
            mockChatMessage()
            mockOperatorProperties()

            val galleryCard = useCase(chatMessageInternal, showChatHead = true)

            assertEquals(galleryCard.id, messageId)
            assertEquals(galleryCard.galleryCards, emptyList<GvaGalleryCard>())
            assertEquals(galleryCard.operatorId, operatorId)
            assertEquals(galleryCard.id, messageId)
            assertEquals(galleryCard.timestamp, messageTimeStamp)
            assertEquals(galleryCard.operatorProfileImgUrl, operatorImageUrl)
            assertEquals(galleryCard.operatorName, operatorName)
            assertEquals(galleryCard.showChatHead, true)
        }
    }
}
