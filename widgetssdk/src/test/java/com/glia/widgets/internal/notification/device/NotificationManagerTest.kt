package com.glia.widgets.internal.notification.device

import android.app.Application
import android.mock
import android.os.Build
import android.unMock
import com.glia.androidsdk.comms.MediaDirection
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.Logger
import com.glia.widgets.internal.notification.NotificationFactory
import com.glia.widgets.internal.notification.domain.CallNotification
import com.glia.widgets.locale.LocaleProvider
import io.mockk.mockk
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
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

    private val audioCall = CallNotification(CallNotification.Phase.LIVE, MediaDirection.NONE, hasAudio = true)

    @Before
    fun setUp() {
        Logger.mock()
        application = RuntimeEnvironment.getApplication()
        Dependencies.localeProvider = mockk<LocaleProvider>(relaxed = true)
        manager = NotificationManager(application)
    }

    @After
    fun tearDown() {
        Logger.unMock()
    }

    @Test
    fun `startCallService starts CallForegroundService`() {
        assertTrue(manager.startCallService(audioCall, usesMicrophone = true, usesCamera = false))

        val intent = shadowOf(application).nextStartedService
        assertEquals(CallForegroundService::class.java.name, intent?.component?.className)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.S])
    fun `startCallService starts CallForegroundService on API 31`() {
        manager.startCallService(audioCall, usesMicrophone = true, usesCamera = false)

        val intent = shadowOf(application).nextStartedService
        assertEquals(CallForegroundService::class.java.name, intent?.component?.className)
    }

    @Test
    fun `showCallNotification posts the notification without starting a service`() {
        manager.showCallNotification(audioCall)

        assertNull(shadowOf(application).nextStartedService)
        assertNotNull(systemNotificationManager().getNotification(NotificationFactory.CALL_NOTIFICATION_ID))
    }

    @Test
    fun `removeCallNotification stops CallForegroundService and removes the notification`() {
        manager.showCallNotification(audioCall)

        manager.removeCallNotification()

        val stopped = shadowOf(application).nextStoppedService
        assertEquals(CallForegroundService::class.java.name, stopped?.component?.className)
        assertNull(systemNotificationManager().getNotification(NotificationFactory.CALL_NOTIFICATION_ID))
    }

    private fun systemNotificationManager() = shadowOf(application.getSystemService(android.app.NotificationManager::class.java))
}
