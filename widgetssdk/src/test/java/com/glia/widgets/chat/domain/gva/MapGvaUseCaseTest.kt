package com.glia.widgets.chat.domain.gva

import com.glia.widgets.chat.MockChatMessageInternal
import com.glia.widgets.chat.model.Gva
import com.glia.widgets.chat.model.GvaGalleryCards
import com.glia.widgets.chat.model.GvaPersistentButtons
import com.glia.widgets.chat.model.GvaQuickReplies
import com.glia.widgets.chat.model.GvaResponseText
import junit.framework.TestCase.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class MapGvaUseCaseTest {
    private var mockChatMessageInternal = MockChatMessageInternal()
    private lateinit var getGvaTypeUseCase: GetGvaTypeUseCase
    private lateinit var mapGvaResponseTextUseCase: MapGvaResponseTextUseCase
    private lateinit var mapGvaPersistentButtonsUseCase: MapGvaPersistentButtonsUseCase
    private lateinit var mapGvaQuickRepliesUseCase: MapGvaQuickRepliesUseCase
    private lateinit var mapGvaGvaGalleryCardsUseCase: MapGvaGvaGalleryCardsUseCase

    private lateinit var useCase: MapGvaUseCase

    @Before
    fun setUp() {
        getGvaTypeUseCase = mock()
        mapGvaResponseTextUseCase = mock()
        mapGvaPersistentButtonsUseCase = mock()
        mapGvaQuickRepliesUseCase = mock()
        mapGvaGvaGalleryCardsUseCase = mock()

        useCase = MapGvaUseCase(
            getGvaTypeUseCase,
            mapGvaResponseTextUseCase,
            mapGvaPersistentButtonsUseCase,
            mapGvaQuickRepliesUseCase,
            mapGvaGvaGalleryCardsUseCase
        )

        mockChatMessageInternal.mockChatMessage()
        mockChatMessageInternal.mockOperatorProperties()
    }

    @After
    fun tearDown() {
        mockChatMessageInternal.reset()
    }

    @Test
    fun `invoke returns GvaResponseText when GVA type is PLAIN_TEXT`() {
        whenever(getGvaTypeUseCase(any())) doReturn Gva.Type.PLAIN_TEXT
        whenever(mapGvaResponseTextUseCase(any(), any())) doReturn mock()

        mockChatMessageInternal.apply {
            val gva = useCase(chatMessageInternal)
            assertTrue(gva is GvaResponseText)
        }
    }

    @Test
    fun `invoke returns GvaPersistentButtons when GVA type is PERSISTENT_BUTTONS`() {
        whenever(getGvaTypeUseCase(any())) doReturn Gva.Type.PERSISTENT_BUTTONS
        whenever(mapGvaPersistentButtonsUseCase(any(), any())) doReturn mock()

        mockChatMessageInternal.apply {
            val gva = useCase(chatMessageInternal)
            assertTrue(gva is GvaPersistentButtons)
        }
    }

    @Test
    fun `invoke returns GvaQuickReplies when GVA type is QUICK_REPLIES`() {
        whenever(getGvaTypeUseCase(any())) doReturn Gva.Type.QUICK_REPLIES
        whenever(mapGvaQuickRepliesUseCase(any(), any())) doReturn mock()

        mockChatMessageInternal.apply {
            val gva = useCase(chatMessageInternal)
            assertTrue(gva is GvaQuickReplies)
        }
    }

    @Test
    fun `invoke returns GvaGalleryCards when GVA type is GALLERY_CARDS`() {
        whenever(getGvaTypeUseCase(any())) doReturn Gva.Type.GALLERY_CARDS
        whenever(mapGvaGvaGalleryCardsUseCase(any(), any())) doReturn mock()

        mockChatMessageInternal.apply {
            val gva = useCase(chatMessageInternal)
            assertTrue(gva is GvaGalleryCards)
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `invoke throws exception when ChatMessage is not GVA message`() {
        whenever(getGvaTypeUseCase(any())) doReturn null

        mockChatMessageInternal.apply { useCase(chatMessageInternal) }
    }
}
