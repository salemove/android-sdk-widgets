package com.glia.widgets.internal.notification.device

import android.app.Application
import android.mock
import android.os.Build
import android.unMock
import com.glia.widgets.helper.Logger
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.UPSIDE_DOWN_CAKE])
class NotificationManagerTest {
    private lateinit var application: Application
    private lateinit var manager: NotificationManager

    @Before
    fun setUp() {
        Logger.mock()
        application = RuntimeEnvironment.getApplication()
        manager = NotificationManager(application)
    }

    @After
    fun tearDown() {
        Logger.unMock()
    }

    @Test
    fun `showAudioCallNotification starts CallForegroundService`() {
        manager.showAudioCallNotification()

        val intent = shadowOf(application).nextStartedService
        assertNotNull("Expected CallForegroundService to be started", intent)
        assertEquals(
            CallForegroundService::class.java.name,
            intent?.component?.className
        )
    }

    @Test
    fun `showVideoCallNotification starts CallForegroundService`() {
        manager.showVideoCallNotification(isTwoWayVideo = true, hasAudio = true)

        val intent = shadowOf(application).nextStartedService
        assertNotNull("Expected CallForegroundService to be started", intent)
        assertEquals(
            CallForegroundService::class.java.name,
            intent?.component?.className
        )
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.S])
    fun `showAudioCallNotification starts CallForegroundService on API 31`() {
        manager.showAudioCallNotification()

        val intent = shadowOf(application).nextStartedService
        assertNotNull("Expected CallForegroundService to be started on API 31+", intent)
        assertEquals(
            CallForegroundService::class.java.name,
            intent?.component?.className
        )
    }

    @Test
    fun `removeCallNotification stops CallForegroundService`() {
        manager.removeCallNotification()

        val stopped = shadowOf(application).nextStoppedService
        assertNotNull("Expected CallForegroundService to be stopped", stopped)
        assertEquals(
            CallForegroundService::class.java.name,
            stopped?.component?.className
        )
    }

    @Test
    fun `showVideoCallNotification one-way starts CallForegroundService`() {
        manager.showVideoCallNotification(isTwoWayVideo = false, hasAudio = true)

        val intent = shadowOf(application).nextStartedService
        assertNotNull("Expected CallForegroundService to be started for one-way video", intent)
        assertEquals(
            CallForegroundService::class.java.name,
            intent?.component?.className
        )
    }
}
