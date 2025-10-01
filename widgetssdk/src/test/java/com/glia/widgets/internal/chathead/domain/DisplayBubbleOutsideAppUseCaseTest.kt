package com.glia.widgets.internal.chathead.domain

import com.glia.widgets.helper.Logger
import com.glia.widgets.internal.chathead.ChatHeadManager
import com.glia.widgets.internal.permissions.PermissionManager
import com.glia.widgets.launcher.ConfigurationManager
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test

class DisplayBubbleOutsideAppUseCaseTest {

    private lateinit var chatHeadManager: ChatHeadManager
    private lateinit var permissionManager: PermissionManager
    private lateinit var configurationManager: ConfigurationManager
    private lateinit var isBubbleNeededUseCase: IsBubbleNeededUseCase
    private lateinit var useCase: DisplayBubbleOutsideAppUseCase

    @Before
    fun setUp() {
        chatHeadManager = mockk(relaxUnitFun = true)
        permissionManager = mockk()
        configurationManager = mockk()
        isBubbleNeededUseCase = mockk()
        useCase = DisplayBubbleOutsideAppUseCase(
            chatHeadManager,
            permissionManager,
            configurationManager,
            isBubbleNeededUseCase
        )

        // Mock Logger static methods
        mockkStatic(Logger::class)
        justRun { Logger.d(any(), any()) }
        justRun { Logger.i(any(), any()) }
    }

    @After
    fun tearDown() {
        unmockkStatic(Logger::class)
    }

    @Test
    fun `invoke does nothing when bubble is not allowed outside app`() {
        // Given
        every { configurationManager.enableBubbleOutsideApp } returns false
        every { permissionManager.hasOverlayPermission() } returns false

        // When
        useCase(viewName = "SomeView")

        // Then
        verify(exactly = 0) { chatHeadManager.startChatHeadService() }
        verify(exactly = 0) { chatHeadManager.stopChatHeadService() }
    }

    @Test
    fun `invoke shows bubble when app is in background`() {
        // Given
        every { configurationManager.enableBubbleOutsideApp } returns true
        every { permissionManager.hasOverlayPermission() } returns true

        // When
        useCase(viewName = null)

        // Then
        verify(exactly = 1) { chatHeadManager.startChatHeadService() }
        verify(exactly = 0) { chatHeadManager.stopChatHeadService() }
    }

    @Test
    fun `invoke shows bubble when bubble is needed by current view`() {
        // Given
        val viewName = "SomeView"
        every { configurationManager.enableBubbleOutsideApp } returns true
        every { permissionManager.hasOverlayPermission() } returns true
        every { isBubbleNeededUseCase(viewName) } returns true

        // When
        useCase(viewName)

        // Then
        verify(exactly = 1) { chatHeadManager.startChatHeadService() }
        verify(exactly = 0) { chatHeadManager.stopChatHeadService() }
    }

    @Test
    fun `invoke hides bubble when bubble is not needed by current view`() {
        // Given
        val viewName = "SomeView"
        every { configurationManager.enableBubbleOutsideApp } returns true
        every { permissionManager.hasOverlayPermission() } returns true
        every { isBubbleNeededUseCase(viewName) } returns false

        // When
        useCase(viewName)

        // Then
        verify(exactly = 0) { chatHeadManager.startChatHeadService() }
        verify(exactly = 1) { chatHeadManager.stopChatHeadService() }
    }

    @Test
    fun `onDestroy stops chat head service`() {
        // When
        useCase.onDestroy()

        // Then
        verify(exactly = 1) { chatHeadManager.stopChatHeadService() }
    }
}
