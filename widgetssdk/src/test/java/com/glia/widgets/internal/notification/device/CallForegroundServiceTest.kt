package com.glia.widgets.internal.notification.device

import android.app.Application
import android.content.Context
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
import android.mock
import android.os.Build
import android.unMock
import com.glia.androidsdk.comms.MediaDirection
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.Logger
import com.glia.widgets.internal.notification.NotificationFactory
import com.glia.widgets.internal.notification.OngoingCallNotificationManager
import com.glia.widgets.internal.notification.domain.CallNotification
import com.glia.widgets.locale.LocaleProvider
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
import org.robolectric.android.controller.ServiceController
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.UPSIDE_DOWN_CAKE])
class CallForegroundServiceTest {
    private lateinit var application: Application
    private val ongoingCallNotificationManager: OngoingCallNotificationManager = mockk(relaxUnitFun = true)

    private val audioCall = CallNotification(CallNotification.Phase.LIVE, MediaDirection.NONE, hasAudio = true)

    @Before
    fun setUp() {
        Logger.mock()
        application = RuntimeEnvironment.getApplication()
        Dependencies.localeProvider = mockk<LocaleProvider>(relaxed = true)
        Dependencies.ongoingCallNotificationManager = ongoingCallNotificationManager
    }

    @After
    fun tearDown() {
        Logger.unMock()
    }

    @Test
    fun `start enqueues a start intent for CallForegroundService`() {
        CallForegroundService.start(application, audioCall, usesMicrophone = true, usesCamera = false)

        val started = shadowOf(application).nextStartedService
        assertEquals(CallForegroundService::class.java.name, started?.component?.className)
    }

    @Test
    fun `stop enqueues a stop intent for CallForegroundService`() {
        CallForegroundService.stop(application)

        val stopped = shadowOf(application).nextStoppedService
        assertEquals(CallForegroundService::class.java.name, stopped?.component?.className)
    }

    @Test
    fun `microphone call promotes the service to foreground with the microphone type`() {
        val controller = startService(audioCall, usesMicrophone = true, usesCamera = false)

        assertNotNull(shadowOf(controller.get()).lastForegroundNotification)
        assertEquals(NotificationFactory.CALL_NOTIFICATION_ID, shadowOf(controller.get()).lastForegroundNotificationId)
        assertEquals(FOREGROUND_SERVICE_TYPE_MICROPHONE, controller.get().foregroundServiceType)
    }

    @Test
    fun `microphone and camera call uses both types`() {
        val videoCall = CallNotification(CallNotification.Phase.WAITING, MediaDirection.TWO_WAY, hasAudio = true)

        val controller = startService(videoCall, usesMicrophone = true, usesCamera = true)

        assertEquals(FOREGROUND_SERVICE_TYPE_MICROPHONE or FOREGROUND_SERVICE_TYPE_CAMERA, controller.get().foregroundServiceType)
    }

    @Test
    fun `camera-only call does not ask for the microphone type`() {
        val videoCall = CallNotification(CallNotification.Phase.LIVE, MediaDirection.TWO_WAY, hasAudio = false)

        val controller = startService(videoCall, usesMicrophone = false, usesCamera = true)

        assertEquals(FOREGROUND_SERVICE_TYPE_CAMERA, controller.get().foregroundServiceType)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.S])
    fun `microphone call promotes the service to foreground on API 31`() {
        val controller = startService(audioCall, usesMicrophone = true, usesCamera = false)

        assertEquals(NotificationFactory.CALL_NOTIFICATION_ID, shadowOf(controller.get()).lastForegroundNotificationId)
    }

    @Test
    fun `onTaskRemoved removes the foreground notification`() {
        val controller = startService(audioCall, usesMicrophone = true, usesCamera = false)
        val service = controller.get()

        service.onTaskRemoved(null)
        controller.destroy()

        assertTrue(shadowOf(service).isForegroundStopped)
        verify(exactly = 1) { ongoingCallNotificationManager.onCallServiceStopped() }
    }

    @Test
    fun `refused promotion stops the service and reports it`() {
        CallForegroundService.start(application, audioCall, usesMicrophone = true, usesCamera = false)
        val controller = Robolectric.buildService(CallForegroundService::class.java, shadowOf(application).nextStartedService).create()
        shadowOf(controller.get()).setThrowInStartForeground(SecurityException("app is not in the foreground"))

        controller.startCommand(0, 0)

        assertNull(shadowOf(controller.get()).lastForegroundNotification)
        assertTrue(shadowOf(controller.get()).isStoppedBySelf)
        verify(exactly = 1) { ongoingCallNotificationManager.onCallServiceStopped() }
    }

    @Test
    fun `successful promotion keeps the service running`() {
        val controller = startService(audioCall, usesMicrophone = true, usesCamera = false)

        assertFalse(shadowOf(controller.get()).isStoppedBySelf)
        verify(exactly = 0) { ongoingCallNotificationManager.onCallServiceStopped() }
    }

    @Test
    fun `start reports a start the system refused`() {
        val context: Context = mockk(relaxed = true) {
            every { startForegroundService(any()) } throws IllegalStateException("not allowed to start service")
        }

        assertFalse(CallForegroundService.start(context, audioCall, usesMicrophone = true, usesCamera = false))
    }

    @Test
    fun `start reports an accepted start`() {
        assertTrue(CallForegroundService.start(application, audioCall, usesMicrophone = true, usesCamera = false))
    }

    private fun startService(call: CallNotification, usesMicrophone: Boolean, usesCamera: Boolean): ServiceController<CallForegroundService> {
        CallForegroundService.start(application, call, usesMicrophone, usesCamera)
        return Robolectric.buildService(CallForegroundService::class.java, shadowOf(application).nextStartedService)
            .create()
            .startCommand(0, 0)
    }
}
