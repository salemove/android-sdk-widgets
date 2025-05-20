package com.glia.widgets.push.notifications

import android.app.PendingIntent
import androidx.lifecycle.Lifecycle
import com.glia.widgets.helper.ApplicationLifecycleManager
import com.glia.widgets.helper.IntentHelper
import com.glia.widgets.internal.notification.device.INotificationManager
import com.glia.widgets.internal.permissions.domain.IsNotificationPermissionGrantedUseCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class SecureMessagingPushControllerTest {
    private lateinit var applicationLifecycleManager: ApplicationLifecycleManager
    private lateinit var notificationManager: INotificationManager
    private lateinit var isNotificationPermissionGrantedUseCase: IsNotificationPermissionGrantedUseCase
    private lateinit var intentHelper: IntentHelper

    private lateinit var secureMessagingPushController: SecureMessagingPushController

    @Before
    fun setup() {
        applicationLifecycleManager = mockk(relaxUnitFun = true)
        notificationManager = mockk(relaxUnitFun = true)
        isNotificationPermissionGrantedUseCase = mockk(relaxUnitFun = true)
        intentHelper = mockk(relaxUnitFun = true)

        secureMessagingPushController = SecureMessagingPushControllerImpl(
            applicationLifecycleManager,
            notificationManager,
            isNotificationPermissionGrantedUseCase,
            intentHelper
        )

        verify { notificationManager.createSecureMessagingChannel() }
    }

    @Test
    fun `handleSecureMessage does nothing when the app is on foreground`() {
        every { applicationLifecycleManager.isAtLeast(Lifecycle.State.RESUMED) } returns true

        secureMessagingPushController.handleSecureMessage(mockk(), "queueId", "content")

        verify(exactly = 0) { isNotificationPermissionGrantedUseCase() }
        verify(exactly = 0) { notificationManager.showSecureMessageNotification(any(), any()) }
    }

    @Test
    fun `handleSecureMessage does nothing when notification permission is not granted`() {
        every { applicationLifecycleManager.isAtLeast(Lifecycle.State.RESUMED) } returns false
        every { isNotificationPermissionGrantedUseCase() } returns false

        secureMessagingPushController.handleSecureMessage(mockk(), "queueId", "content")

        verify(exactly = 0) { notificationManager.showSecureMessageNotification(any(), any()) }
    }

    @Test
    fun `handleSecureMessage shows notification when app is not in foreground and notification permission is granted`() {
        val pendingIntent = mockk<PendingIntent>()
        val queueId = "queueId"
        val content = "content"

        every { applicationLifecycleManager.isAtLeast(Lifecycle.State.RESUMED) } returns false
        every { isNotificationPermissionGrantedUseCase() } returns true
        every { intentHelper.pushClickHandlerPendingIntent(any(), queueId) } returns pendingIntent

        secureMessagingPushController.handleSecureMessage(mockk(), queueId, content)

        verify { intentHelper.pushClickHandlerPendingIntent(any(), queueId) }
        verify { notificationManager.showSecureMessageNotification(content, pendingIntent) }
    }

}
