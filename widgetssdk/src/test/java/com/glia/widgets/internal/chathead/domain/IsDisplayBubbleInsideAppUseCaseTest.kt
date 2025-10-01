package com.glia.widgets.internal.chathead.domain

import com.glia.widgets.internal.permissions.PermissionManager
import com.glia.widgets.launcher.ConfigurationManager
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Before
import org.junit.Test

class IsDisplayBubbleInsideAppUseCaseTest {

    private lateinit var permissionManager: PermissionManager
    private lateinit var configurationManager: ConfigurationManager
    private lateinit var isBubbleNeededUseCase: IsBubbleNeededUseCase
    private lateinit var useCase: IsDisplayBubbleInsideAppUseCase

    @Before
    fun setUp() {
        permissionManager = mockk()
        configurationManager = mockk()
        isBubbleNeededUseCase = mockk()
        useCase = IsDisplayBubbleInsideAppUseCase(
            permissionManager,
            configurationManager,
            isBubbleNeededUseCase
        )
    }

    @Test
    fun `invoke returns false when bubbles are allowed outside app`() {
        // Given
        val viewName = "SomeView"
        every { configurationManager.enableBubbleInsideApp } returns true
        every { permissionManager.hasOverlayPermission() } returns true

        // When
        val result = useCase(viewName)

        // Then
        assertFalse(result)
    }

    @Test
    fun `invoke returns true for chat screen during media engagement regardless of other settings`() {
        // Given
        val viewName = "ChatView"
        every { isBubbleNeededUseCase.isBubbleNeededByChatScreenDuringMediaEngagement(viewName) } returns true
        // Even if these are false, we should still show bubble
        every { configurationManager.enableBubbleInsideApp } returns false
        every { permissionManager.hasOverlayPermission() } returns false

        // When
        val result = useCase(viewName)

        // Then
        assertTrue(result)
    }

    @Test
    fun `invoke returns false when bubble inside app is disabled`() {
        // Given
        val viewName = "SomeView"
        every { isBubbleNeededUseCase.isBubbleNeededByChatScreenDuringMediaEngagement(viewName) } returns false
        every { configurationManager.enableBubbleInsideApp } returns false

        // When
        val result = useCase(viewName)

        // Then
        assertFalse(result)
    }

    @Test
    fun `invoke returns true when bubble is needed and enabled inside app`() {
        // Given
        val viewName = "SomeView"
        every { isBubbleNeededUseCase.isBubbleNeededByChatScreenDuringMediaEngagement(viewName) } returns false
        every { configurationManager.enableBubbleInsideApp } returns true
        every { permissionManager.hasOverlayPermission() } returns false
        every { isBubbleNeededUseCase(viewName) } returns true

        // When
        val result = useCase(viewName)

        // Then
        assertTrue(result)
    }

    @Test
    fun `invoke returns false when bubble is not needed even if enabled inside app`() {
        // Given
        val viewName = "SomeView"
        every { isBubbleNeededUseCase.isBubbleNeededByChatScreenDuringMediaEngagement(viewName) } returns false
        every { configurationManager.enableBubbleInsideApp } returns true
        every { permissionManager.hasOverlayPermission() } returns false
        every { isBubbleNeededUseCase(viewName) } returns false

        // When
        val result = useCase(viewName)

        // Then
        assertFalse(result)
    }
}
