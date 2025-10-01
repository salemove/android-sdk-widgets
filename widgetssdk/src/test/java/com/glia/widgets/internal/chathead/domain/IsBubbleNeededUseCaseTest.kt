package com.glia.widgets.internal.chathead.domain

import com.glia.widgets.call.CallView
import com.glia.widgets.chat.ChatView
import com.glia.widgets.engagement.domain.EngagementTypeUseCase
import com.glia.widgets.engagement.domain.IsQueueingOrLiveEngagementUseCase
import com.glia.widgets.filepreview.ui.ImagePreviewView
import com.glia.widgets.helper.DialogHolderView
import com.glia.widgets.messagecenter.MessageCenterView
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Before
import org.junit.Test

class IsBubbleNeededUseCaseTest {

    private lateinit var isQueueingOrLiveEngagementUseCase: IsQueueingOrLiveEngagementUseCase
    private lateinit var engagementTypeUseCase: EngagementTypeUseCase
    private lateinit var useCase: IsBubbleNeededUseCase

    @Before
    fun setUp() {
        isQueueingOrLiveEngagementUseCase = mockk()
        engagementTypeUseCase = mockk()
        useCase = IsBubbleNeededUseCase(isQueueingOrLiveEngagementUseCase, engagementTypeUseCase)
    }

    @Test
    fun `bubble is needed when queueing for media and not in excluded screen`() {
        // Given
        val viewName = "SomeRandomView"
        every { isQueueingOrLiveEngagementUseCase.isQueueingForMedia } returns true
        every { isQueueingOrLiveEngagementUseCase.isQueueingForLiveChat } returns false
        every { engagementTypeUseCase.isMediaEngagement } returns false
        every { engagementTypeUseCase.isChatEngagement } returns false

        // When
        val result = useCase(viewName)

        // Then
        assertTrue(result)
    }

    @Test
    fun `bubble is needed when queueing for live chat and not in excluded screen`() {
        // Given
        val viewName = "SomeRandomView"
        every { isQueueingOrLiveEngagementUseCase.isQueueingForMedia } returns false
        every { isQueueingOrLiveEngagementUseCase.isQueueingForLiveChat } returns true
        every { engagementTypeUseCase.isMediaEngagement } returns false
        every { engagementTypeUseCase.isChatEngagement } returns false

        // When
        val result = useCase(viewName)

        // Then
        assertTrue(result)
    }

    @Test
    fun `bubble is needed when media engagement and not in excluded screen`() {
        // Given
        val viewName = "SomeRandomView"
        every { isQueueingOrLiveEngagementUseCase.isQueueingForMedia } returns false
        every { isQueueingOrLiveEngagementUseCase.isQueueingForLiveChat } returns false
        every { engagementTypeUseCase.isMediaEngagement } returns true
        every { engagementTypeUseCase.isChatEngagement } returns false

        // When
        val result = useCase(viewName)

        // Then
        assertTrue(result)
    }

    @Test
    fun `bubble is needed when chat engagement and not in excluded screen`() {
        // Given
        val viewName = "SomeRandomView"
        every { isQueueingOrLiveEngagementUseCase.isQueueingForMedia } returns false
        every { isQueueingOrLiveEngagementUseCase.isQueueingForLiveChat } returns false
        every { engagementTypeUseCase.isMediaEngagement } returns false
        every { engagementTypeUseCase.isChatEngagement } returns true

        // When
        val result = useCase(viewName)

        // Then
        assertTrue(result)
    }

    @Test
    fun `bubble is not needed when no engagement activity`() {
        // Given
        val viewName = "SomeRandomView"
        every { isQueueingOrLiveEngagementUseCase.isQueueingForMedia } returns false
        every { isQueueingOrLiveEngagementUseCase.isQueueingForLiveChat } returns false
        every { engagementTypeUseCase.isMediaEngagement } returns false
        every { engagementTypeUseCase.isChatEngagement } returns false

        // When
        val result = useCase(viewName)

        // Then
        assertFalse(result)
    }

    @Test
    fun `bubble is not needed when in excluded screen except special case`() {
        // Given
        val excludedScreens = listOf(
            ChatView::class.java.simpleName,
            CallView::class.java.simpleName,
            ImagePreviewView::class.java.simpleName,
            MessageCenterView::class.java.simpleName,
            DialogHolderView::class.java.simpleName
        )

        every { isQueueingOrLiveEngagementUseCase.isQueueingForMedia } returns false
        every { isQueueingOrLiveEngagementUseCase.isQueueingForLiveChat } returns false
        every { engagementTypeUseCase.isMediaEngagement } returns false
        every { engagementTypeUseCase.isChatEngagement } returns true

        // Test each excluded screen
        excludedScreens.forEach { viewName ->
            // When
            val result = useCase(viewName)

            // Then
            assertFalse("Bubble should not be needed for $viewName", result)
        }
    }

    @Test
    fun `bubble is needed for chat screen during media engagement`() {
        // Given
        val viewName = ChatView::class.java.simpleName
        every { isQueueingOrLiveEngagementUseCase.isQueueingForMedia } returns false
        every { isQueueingOrLiveEngagementUseCase.isQueueingForLiveChat } returns false
        every { engagementTypeUseCase.isMediaEngagement } returns true
        every { engagementTypeUseCase.isChatEngagement } returns false

        // When
        val result = useCase(viewName)

        // Then
        assertTrue(result)
    }

    @Test
    fun `isBubbleNeededByChatScreenDuringMediaEngagement returns true for chat view during media engagement`() {
        // Given
        val viewName = ChatView::class.java.simpleName
        every { engagementTypeUseCase.isMediaEngagement } returns true

        // When
        val result = useCase.isBubbleNeededByChatScreenDuringMediaEngagement(viewName)

        // Then
        assertTrue(result)
    }

    @Test
    fun `isBubbleNeededByChatScreenDuringMediaEngagement returns false when not media engagement`() {
        // Given
        val viewName = ChatView::class.java.simpleName
        every { engagementTypeUseCase.isMediaEngagement } returns false

        // When
        val result = useCase.isBubbleNeededByChatScreenDuringMediaEngagement(viewName)

        // Then
        assertFalse(result)
    }

    @Test
    fun `isBubbleNeededByChatScreenDuringMediaEngagement returns false when not chat view`() {
        // Given
        val viewName = "SomeOtherView"
        every { engagementTypeUseCase.isMediaEngagement } returns true

        // When
        val result = useCase.isBubbleNeededByChatScreenDuringMediaEngagement(viewName)

        // Then
        assertFalse(result)
    }
}
