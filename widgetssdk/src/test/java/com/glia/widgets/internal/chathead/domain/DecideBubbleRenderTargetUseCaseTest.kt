package com.glia.widgets.internal.chathead.domain

import com.glia.widgets.engagement.domain.EngagementTypeUseCase
import com.glia.widgets.engagement.domain.IsQueueingOrLiveEngagementUseCase
import com.glia.widgets.internal.chathead.BubbleContext
import com.glia.widgets.internal.chathead.BubbleRenderTarget
import com.glia.widgets.internal.chathead.VisibleScreen
import com.glia.widgets.internal.permissions.PermissionManager
import com.glia.widgets.launcher.ConfigurationManager
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DecideBubbleRenderTargetUseCaseTest {

    private lateinit var permissionManager: PermissionManager
    private lateinit var configurationManager: ConfigurationManager
    private lateinit var isQueueingOrLiveEngagementUseCase: IsQueueingOrLiveEngagementUseCase
    private lateinit var engagementTypeUseCase: EngagementTypeUseCase
    private lateinit var useCase: DecideBubbleRenderTargetUseCase

    @Before
    fun setUp() {
        permissionManager = mockk()
        configurationManager = mockk()
        isQueueingOrLiveEngagementUseCase = mockk()
        engagementTypeUseCase = mockk()
        useCase = DecideBubbleRenderTargetUseCase(
            permissionManager,
            configurationManager,
            isQueueingOrLiveEngagementUseCase,
            engagementTypeUseCase
        )
    }

    private fun givenConfig(outsideApp: Boolean, overlayPermission: Boolean, insideApp: Boolean) {
        every { configurationManager.enableBubbleOutsideApp } returns outsideApp
        every { permissionManager.hasOverlayPermission() } returns overlayPermission
        every { configurationManager.enableBubbleInsideApp } returns insideApp
    }

    private fun givenEngagement(
        queueing: Boolean = false,
        liveEngagement: Boolean = false,
        media: Boolean = false,
        callVisualizer: Boolean = false
    ) {
        every { isQueueingOrLiveEngagementUseCase.isQueueing } returns queueing
        every { isQueueingOrLiveEngagementUseCase.hasOngoingLiveEngagement } returns liveEngagement
        every { engagementTypeUseCase.hasMedia } returns media
        every { engagementTypeUseCase.isMediaEngagement } returns media
        every { engagementTypeUseCase.isCallVisualizer } returns callVisualizer
    }

    /** Nothing to return to: no queueing, no engagement. */
    private fun givenNoEngagement() = givenEngagement()

    // region What counts as needing a bubble

    @Test
    fun `queueing needs a bubble`() {
        // Given
        givenConfig(outsideApp = false, overlayPermission = false, insideApp = true)
        givenEngagement(queueing = true)

        // Then
        assertEquals(BubbleRenderTarget.APPLICATION, useCase(BubbleContext.InApp(VisibleScreen.OTHER)))
    }

    @Test
    fun `a live engagement needs a bubble`() {
        // Given
        givenConfig(outsideApp = false, overlayPermission = false, insideApp = true)
        givenEngagement(liveEngagement = true)

        // Then
        assertEquals(BubbleRenderTarget.APPLICATION, useCase(BubbleContext.InApp(VisibleScreen.OTHER)))
    }

    @Test
    fun `a media engagement needs a bubble`() {
        // Given
        givenConfig(outsideApp = false, overlayPermission = false, insideApp = true)
        givenEngagement(liveEngagement = true, media = true)

        // Then
        assertEquals(BubbleRenderTarget.APPLICATION, useCase(BubbleContext.InApp(VisibleScreen.OTHER)))
    }

    @Test
    fun `a live engagement counts before the operator connects`() {
        // Given the engagement started but no operator has arrived yet
        givenConfig(outsideApp = false, overlayPermission = false, insideApp = true)
        givenEngagement(liveEngagement = true, media = false)

        // Then the bubble shows a placeholder rather than disappearing
        assertEquals(BubbleRenderTarget.APPLICATION, useCase(BubbleContext.InApp(VisibleScreen.OTHER)))
    }

    @Test
    fun `a Call Visualizer engagement without media needs no bubble`() {
        // Given there is no chat or call screen behind a media-less Call Visualizer engagement
        givenConfig(outsideApp = false, overlayPermission = false, insideApp = true)
        givenEngagement(liveEngagement = true, callVisualizer = true, media = false)

        // Then
        assertEquals(BubbleRenderTarget.NONE, useCase(BubbleContext.InApp(VisibleScreen.OTHER)))
    }

    @Test
    fun `a Call Visualizer engagement with media needs a bubble`() {
        // Given
        givenConfig(outsideApp = false, overlayPermission = false, insideApp = true)
        givenEngagement(liveEngagement = true, callVisualizer = true, media = true)

        // Then
        assertEquals(BubbleRenderTarget.APPLICATION, useCase(BubbleContext.InApp(VisibleScreen.OTHER)))
    }

    @Test
    fun `a Call Visualizer engagement with media counts before the operator connects`() {
        // Given media is flowing but no operator is registered yet
        givenConfig(outsideApp = false, overlayPermission = false, insideApp = true)
        every { isQueueingOrLiveEngagementUseCase.isQueueing } returns false
        every { isQueueingOrLiveEngagementUseCase.hasOngoingLiveEngagement } returns true
        every { engagementTypeUseCase.isCallVisualizer } returns true
        every { engagementTypeUseCase.hasMedia } returns true
        // isMediaEngagement stays false because it additionally requires an operator to be present
        every { engagementTypeUseCase.isMediaEngagement } returns false

        // Then
        assertEquals(BubbleRenderTarget.APPLICATION, useCase(BubbleContext.InApp(VisibleScreen.OTHER)))
    }

    @Test
    fun `nothing to return to needs no bubble`() {
        // Given
        givenConfig(outsideApp = false, overlayPermission = false, insideApp = true)
        givenNoEngagement()

        // Then
        assertEquals(BubbleRenderTarget.NONE, useCase(BubbleContext.InApp(VisibleScreen.OTHER)))
    }

    @Test
    fun `NONE on every Glia screen that hides the bubble`() {
        // Given
        givenConfig(outsideApp = false, overlayPermission = false, insideApp = true)
        givenEngagement(liveEngagement = true)

        listOf(
            VisibleScreen.CHAT,
            VisibleScreen.CALL,
            VisibleScreen.IMAGE_PREVIEW,
            VisibleScreen.MESSAGE_CENTER,
            VisibleScreen.DIALOG_HOLDER
        ).forEach { screen ->
            // Then
            assertEquals("Unexpected target for $screen", BubbleRenderTarget.NONE, useCase(BubbleContext.InApp(screen)))
        }
    }

    // endregion

    // region Overlay allowed: outside app enabled AND overlay permission granted

    @Test
    fun `APPLICATION while the app is foregrounded even when the overlay is allowed`() {
        // Given the overlay serves the background only - the in-app bubble is always the visible layer
        givenConfig(outsideApp = true, overlayPermission = true, insideApp = true)
        givenEngagement(liveEngagement = true)

        // Then
        assertEquals(BubbleRenderTarget.APPLICATION, useCase(BubbleContext.InApp(VisibleScreen.OTHER)))
    }

    @Test
    fun `NONE when overlay is allowed but bubble is not needed on the foreground screen`() {
        // Given
        givenConfig(outsideApp = true, overlayPermission = true, insideApp = true)
        givenNoEngagement()

        // Then
        assertEquals(BubbleRenderTarget.NONE, useCase(BubbleContext.InApp(VisibleScreen.OTHER)))
    }

    @Test
    fun `NONE when overlay is allowed but bubbles inside the app are disabled and the app is foregrounded`() {
        // Given
        givenConfig(outsideApp = true, overlayPermission = true, insideApp = false)
        givenEngagement(liveEngagement = true)

        // Then
        assertEquals(BubbleRenderTarget.NONE, useCase(BubbleContext.InApp(VisibleScreen.OTHER)))
    }

    @Test
    fun `SERVICE when the app is backgrounded even though bubbles inside the app are disabled`() {
        // Given
        givenConfig(outsideApp = true, overlayPermission = true, insideApp = false)
        givenEngagement(liveEngagement = true)

        // Then
        assertEquals(BubbleRenderTarget.SERVICE, useCase(BubbleContext.AppInBackground))
    }

    @Test
    fun `SERVICE when the app is backgrounded and bubbles inside the app are enabled`() {
        // Given
        givenConfig(outsideApp = true, overlayPermission = true, insideApp = true)
        givenEngagement(queueing = true)

        // Then
        assertEquals(BubbleRenderTarget.SERVICE, useCase(BubbleContext.AppInBackground))
    }

    @Test
    fun `NONE when the app is backgrounded and no engagement needs a bubble`() {
        // Given
        givenConfig(outsideApp = true, overlayPermission = true, insideApp = true)
        givenNoEngagement()

        // Then
        assertEquals(BubbleRenderTarget.NONE, useCase(BubbleContext.AppInBackground))
    }

    @Test
    fun `APPLICATION on the chat screen during a media engagement when the overlay is allowed`() {
        // Given
        givenConfig(outsideApp = true, overlayPermission = true, insideApp = true)
        givenEngagement(liveEngagement = true, media = true)

        // Then
        assertEquals(BubbleRenderTarget.APPLICATION, useCase(BubbleContext.InApp(VisibleScreen.CHAT)))
    }

    @Test
    fun `APPLICATION on the chat screen during a media engagement even when both settings would say otherwise`() {
        // Given the chat-during-media rule overrides enableBubbleInsideApp regardless of the overlay
        givenConfig(outsideApp = true, overlayPermission = true, insideApp = false)
        givenEngagement(liveEngagement = true, media = true)

        // Then
        assertEquals(BubbleRenderTarget.APPLICATION, useCase(BubbleContext.InApp(VisibleScreen.CHAT)))
    }

    @Test
    fun `the two settings are independent - outside app off does not affect the foreground bubble`() {
        // Given the same foreground context, differing only in enableBubbleOutsideApp
        givenEngagement(liveEngagement = true)
        givenConfig(outsideApp = true, overlayPermission = true, insideApp = true)
        val withOverlay = useCase(BubbleContext.InApp(VisibleScreen.OTHER))
        givenConfig(outsideApp = false, overlayPermission = false, insideApp = true)
        val withoutOverlay = useCase(BubbleContext.InApp(VisibleScreen.OTHER))

        // Then
        assertEquals(BubbleRenderTarget.APPLICATION, withOverlay)
        assertEquals(withOverlay, withoutOverlay)
    }

    @Test
    fun `the two settings are independent - inside app off does not affect the background bubble`() {
        // Given the same background context, differing only in enableBubbleInsideApp
        givenEngagement(liveEngagement = true)
        givenConfig(outsideApp = true, overlayPermission = true, insideApp = true)
        val withInApp = useCase(BubbleContext.AppInBackground)
        givenConfig(outsideApp = true, overlayPermission = true, insideApp = false)
        val withoutInApp = useCase(BubbleContext.AppInBackground)

        // Then
        assertEquals(BubbleRenderTarget.SERVICE, withInApp)
        assertEquals(withInApp, withoutInApp)
    }

    // endregion

    // region Overlay service lifetime

    @Test
    fun `the service is needed while the app is still foregrounded`() {
        // Given API 26+ refuses a background service start, so it has to be up before backgrounding
        givenConfig(outsideApp = true, overlayPermission = true, insideApp = true)
        givenEngagement(liveEngagement = true)

        // Then it is needed even though the visible target is the in-app bubble
        assertEquals(BubbleRenderTarget.APPLICATION, useCase(BubbleContext.InApp(VisibleScreen.OTHER)))
        assertTrue(useCase.isOverlayServiceNeeded)
    }

    @Test
    fun `the service is needed on a Glia screen that hides the bubble`() {
        // Given the bubble is not drawn on the chat screen, but backgrounding from it still needs one
        givenConfig(outsideApp = true, overlayPermission = true, insideApp = true)
        givenEngagement(liveEngagement = true)

        // Then
        assertEquals(BubbleRenderTarget.NONE, useCase(BubbleContext.InApp(VisibleScreen.MESSAGE_CENTER)))
        assertTrue(useCase.isOverlayServiceNeeded)
    }

    @Test
    fun `the service is not needed without an engagement`() {
        // Given
        givenConfig(outsideApp = true, overlayPermission = true, insideApp = true)
        givenNoEngagement()

        // Then
        assertFalse(useCase.isOverlayServiceNeeded)
    }

    @Test
    fun `the service is not needed when the integrator disabled bubbles outside the app`() {
        // Given
        givenConfig(outsideApp = false, overlayPermission = true, insideApp = true)
        givenEngagement(liveEngagement = true)

        // Then
        assertFalse(useCase.isOverlayServiceNeeded)
    }

    @Test
    fun `the service is not needed without the overlay permission`() {
        // Given
        givenConfig(outsideApp = true, overlayPermission = false, insideApp = true)
        givenEngagement(liveEngagement = true)

        // Then
        assertFalse(useCase.isOverlayServiceNeeded)
    }

    @Test
    fun `the service is needed regardless of bubbles inside the app being disabled`() {
        // Given
        givenConfig(outsideApp = true, overlayPermission = true, insideApp = false)
        givenEngagement(liveEngagement = true)

        // Then
        assertTrue(useCase.isOverlayServiceNeeded)
    }

    // endregion

    // region Outside app enabled but overlay permission missing

    @Test
    fun `APPLICATION when the overlay permission is missing and the bubble is needed`() {
        // Given
        givenConfig(outsideApp = true, overlayPermission = false, insideApp = true)
        givenEngagement(liveEngagement = true)

        // Then
        assertEquals(BubbleRenderTarget.APPLICATION, useCase(BubbleContext.InApp(VisibleScreen.OTHER)))
    }

    @Test
    fun `NONE when the overlay permission is missing and the bubble is not needed`() {
        // Given
        givenConfig(outsideApp = true, overlayPermission = false, insideApp = true)
        givenNoEngagement()

        // Then
        assertEquals(BubbleRenderTarget.NONE, useCase(BubbleContext.InApp(VisibleScreen.OTHER)))
    }

    @Test
    fun `APPLICATION on the chat screen during a media engagement even when bubbles inside the app are disabled`() {
        // Given
        givenConfig(outsideApp = true, overlayPermission = false, insideApp = false)
        givenEngagement(liveEngagement = true, media = true)

        // Then
        assertEquals(BubbleRenderTarget.APPLICATION, useCase(BubbleContext.InApp(VisibleScreen.CHAT)))
    }

    @Test
    fun `NONE on the chat screen without a media engagement when bubbles inside the app are disabled`() {
        // Given a chat engagement, so the media override does not apply
        givenConfig(outsideApp = true, overlayPermission = false, insideApp = false)
        givenEngagement(liveEngagement = true)

        // Then
        assertEquals(BubbleRenderTarget.NONE, useCase(BubbleContext.InApp(VisibleScreen.CHAT)))
    }

    @Test
    fun `NONE when bubbles inside the app are disabled and it is not the chat during media case`() {
        // Given
        givenConfig(outsideApp = true, overlayPermission = false, insideApp = false)
        givenEngagement(liveEngagement = true)

        // Then
        assertEquals(BubbleRenderTarget.NONE, useCase(BubbleContext.InApp(VisibleScreen.OTHER)))
    }

    @Test
    fun `NONE when the app is backgrounded without the overlay permission`() {
        // Given nothing can host an in-app bubble while the app is in the background
        givenConfig(outsideApp = true, overlayPermission = false, insideApp = true)
        givenEngagement(liveEngagement = true)

        // Then
        assertEquals(BubbleRenderTarget.NONE, useCase(BubbleContext.AppInBackground))
    }

    @Test
    fun `NONE when the app is backgrounded without the overlay permission and bubbles inside are disabled`() {
        // Given
        givenConfig(outsideApp = true, overlayPermission = false, insideApp = false)
        givenEngagement(liveEngagement = true)

        // Then
        assertEquals(BubbleRenderTarget.NONE, useCase(BubbleContext.AppInBackground))
    }

    // endregion

    // region Outside app disabled by the integrator

    @Test
    fun `APPLICATION when bubbles outside the app are disabled and the bubble is needed`() {
        // Given
        givenConfig(outsideApp = false, overlayPermission = true, insideApp = true)
        givenEngagement(liveEngagement = true)

        // Then
        assertEquals(BubbleRenderTarget.APPLICATION, useCase(BubbleContext.InApp(VisibleScreen.OTHER)))
    }

    @Test
    fun `APPLICATION on the chat screen during a media engagement when bubbles outside the app are disabled`() {
        // Given
        givenConfig(outsideApp = false, overlayPermission = true, insideApp = false)
        givenEngagement(liveEngagement = true, media = true)

        // Then
        assertEquals(BubbleRenderTarget.APPLICATION, useCase(BubbleContext.InApp(VisibleScreen.CHAT)))
    }

    @Test
    fun `NONE when bubbles outside the app are disabled and the app is backgrounded`() {
        // Given
        givenConfig(outsideApp = false, overlayPermission = true, insideApp = true)
        givenEngagement(liveEngagement = true)

        // Then
        assertEquals(BubbleRenderTarget.NONE, useCase(BubbleContext.AppInBackground))
    }

    @Test
    fun `NONE when both bubble kinds are disabled by the integrator`() {
        // Given
        givenConfig(outsideApp = false, overlayPermission = true, insideApp = false)
        givenEngagement(liveEngagement = true)

        // Then
        assertEquals(BubbleRenderTarget.NONE, useCase(BubbleContext.InApp(VisibleScreen.OTHER)))
    }

    // endregion
}
