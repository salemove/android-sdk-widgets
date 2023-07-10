package com.glia.widgets.chat.domain.gva

import com.glia.androidsdk.Operator
import com.glia.widgets.chat.MockChatMessageInternal
import com.glia.widgets.chat.model.ChatState
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
    private lateinit var chatState: ChatState
    private lateinit var operator: Operator
    private val mockChatMessageInternal: MockChatMessageInternal = MockChatMessageInternal()

    @Before
    fun setUp() {
        parseGvaGalleryCardsUseCase = mock()
        whenever(parseGvaGalleryCardsUseCase(any())) doReturn emptyList()

        chatState = mock()
        operator = mock()

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

            val galleryCard = useCase(chatMessageInternal, chatState)

            assertEquals(galleryCard.messageId, messageId)
            assertEquals(galleryCard.galleryCards, emptyList<GvaGalleryCard>())
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
