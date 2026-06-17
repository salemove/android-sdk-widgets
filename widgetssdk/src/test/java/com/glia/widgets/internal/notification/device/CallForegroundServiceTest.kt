package com.glia.widgets.internal.notification.device

import android.app.Application
import android.mock
import android.os.Build
import android.unMock
import com.glia.widgets.helper.Logger
import com.glia.widgets.internal.notification.NotificationFactory
import io.mockk.mockk
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.UPSIDE_DOWN_CAKE])
class CallForegroundServiceTest {
    private lateinit var application: Application

    @Before
    fun setUp() {
        Logger.mock()
        application = RuntimeEnvironment.getApplication()
        setupLocaleProvider()
    }

    @After
    fun tearDown() {
        Logger.unMock()
    }

    @Test
    fun `startAudio enqueues a start intent for CallForegroundService`() {
        CallForegroundService.startAudio(application)

        val started = shadowOf(application).nextStartedService
        assertNotNull(started)
        assertEquals(CallForegroundService::class.java.name, started?.component?.className)
    }

    @Test
    fun `startVideo enqueues a start intent for CallForegroundService`() {
        CallForegroundService.startVideo(application, isTwoWayVideo = true, hasAudio = true)

        val started = shadowOf(application).nextStartedService
        assertNotNull(started)
        assertEquals(CallForegroundService::class.java.name, started?.component?.className)
    }

    @Test
    fun `stop enqueues a stop intent for CallForegroundService`() {
        CallForegroundService.stop(application)

        val stopped = shadowOf(application).nextStoppedService
        assertNotNull(stopped)
        assertEquals(CallForegroundService::class.java.name, stopped?.component?.className)
    }

    @Test
    fun `audio call promotes service to foreground`() {
        CallForegroundService.startAudio(application)
        val intent = shadowOf(application).nextStartedService

        val controller = Robolectric.buildService(CallForegroundService::class.java, intent)
            .create()
            .startCommand(0, 0)
        val shadow = shadowOf(controller.get())

        assertNotNull("Foreground notification should be set for audio call", shadow.lastForegroundNotification)
        assertEquals(NotificationFactory.CALL_NOTIFICATION_ID, shadow.lastForegroundNotificationId)
    }

    @Test
    fun `two-way video call promotes service to foreground`() {
        CallForegroundService.startVideo(application, isTwoWayVideo = true, hasAudio = true)
        val intent = shadowOf(application).nextStartedService

        val controller = Robolectric.buildService(CallForegroundService::class.java, intent)
            .create()
            .startCommand(0, 0)
        val shadow = shadowOf(controller.get())

        assertNotNull("Foreground notification should be set for two-way video", shadow.lastForegroundNotification)
        assertEquals(NotificationFactory.CALL_NOTIFICATION_ID, shadow.lastForegroundNotificationId)
    }

    @Test
    fun `one-way video call promotes service to foreground`() {
        CallForegroundService.startVideo(application, isTwoWayVideo = false, hasAudio = true)
        val intent = shadowOf(application).nextStartedService

        val controller = Robolectric.buildService(CallForegroundService::class.java, intent)
            .create()
            .startCommand(0, 0)
        val shadow = shadowOf(controller.get())

        assertNotNull("Foreground notification should be set for one-way video", shadow.lastForegroundNotification)
        assertEquals(NotificationFactory.CALL_NOTIFICATION_ID, shadow.lastForegroundNotificationId)
    }

    @Test
    fun `onTaskRemoved removes the foreground notification`() {
        CallForegroundService.startAudio(application)
        val intent = shadowOf(application).nextStartedService

        val controller = Robolectric.buildService(CallForegroundService::class.java, intent)
            .create()
            .startCommand(0, 0)
        val service = controller.get()

        service.onTaskRemoved(null)
        controller.destroy()

        assertTrue(
            "Foreground should be stopped after onTaskRemoved",
            shadowOf(service).isForegroundStopped
        )
    }

    @Test
    fun `startForeground failure is swallowed without crashing`() {
        CallForegroundService.startAudio(application)
        val intent = shadowOf(application).nextStartedService

        val controller = Robolectric.buildService(CallForegroundService::class.java, intent).create()
        shadowOf(controller.get()).setThrowInStartForeground(RuntimeException("simulated failure"))

        // Should not throw
        controller.startCommand(0, 0)

        assertNull("Foreground notification should be null after failure", shadowOf(controller.get()).lastForegroundNotification)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.S])
    fun `audio call promotes service to foreground on API 31`() {
        CallForegroundService.startAudio(application)
        val intent = shadowOf(application).nextStartedService

        val controller = Robolectric.buildService(CallForegroundService::class.java, intent)
            .create()
            .startCommand(0, 0)
        val shadow = shadowOf(controller.get())

        assertNotNull("Foreground notification should be set on API 31", shadow.lastForegroundNotification)
        assertEquals(NotificationFactory.CALL_NOTIFICATION_ID, shadow.lastForegroundNotificationId)
    }

    private fun setupLocaleProvider() {
        com.glia.widgets.di.Dependencies.localeProvider =
            mockk<com.glia.widgets.locale.LocaleProvider>(relaxed = true)
    }
}
