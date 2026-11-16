package com.glia.widgets.push.notifications

import android.app.PendingIntent
import android.mock
import android.unMock
import androidx.lifecycle.Lifecycle
import com.glia.widgets.helper.ApplicationLifecycleManager
import com.glia.widgets.helper.IntentHelper
import com.glia.widgets.helper.Logger
import com.glia.widgets.internal.notification.device.INotificationManager
import com.glia.widgets.internal.permissions.domain.IsNotificationPermissionGrantedUseCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test

private const val MESSAGE_ID: String = "0:1763278884318%a1b2c3d4"

class InteractionPushControllerTest {
    private lateinit var applicationLifecycleManager: ApplicationLifecycleManager
    private lateinit var notificationManager: INotificationManager
    private lateinit var isNotificationPermissionGrantedUseCase: IsNotificationPermissionGrantedUseCase
    private lateinit var intentHelper: IntentHelper

    private lateinit var controller: InteractionPushController

    private val data: Map<String, String> = mapOf(
        "event_type" to "engagement.chat.message",
        "interaction_id" to "24dad414-071a-42f4-98d9-9c0d3bc31d11",
        "push_subscription" to "true",
        "body" to "You have received a new message"
    )

    @Before
    fun setup() {
        Logger.mock()
        applicationLifecycleManager = mockk()
        notificationManager = mockk(relaxUnitFun = true)
        isNotificationPermissionGrantedUseCase = mockk()
        intentHelper = mockk()

        controller = InteractionPushControllerImpl(
            applicationLifecycleManager,
            notificationManager,
            isNotificationPermissionGrantedUseCase,
            intentHelper
        )

        every { applicationLifecycleManager.isAtLeast(Lifecycle.State.RESUMED) } returns false
        every { isNotificationPermissionGrantedUseCase() } returns true
    }

    @After
    fun tearDown() {
        Logger.unMock()
    }

    @Test
    fun `creates the interaction channel`() {
        verify { notificationManager.createInteractionChannel() }
    }

    @Test
    fun `shows the alert text with an intent that opens the app with the push`() {
        val pendingIntent = mockk<PendingIntent>()
        every { intentHelper.pushNotificationLaunchPendingIntent(any(), data, MESSAGE_ID) } returns pendingIntent

        controller.handleInteractionPush(mockk(), data, MESSAGE_ID)

        verify { notificationManager.showInteractionNotification("You have received a new message", pendingIntent) }
    }

    @Test
    fun `shows nothing while the app is on foreground`() {
        every { applicationLifecycleManager.isAtLeast(Lifecycle.State.RESUMED) } returns true

        controller.handleInteractionPush(mockk(), data, MESSAGE_ID)

        verify(exactly = 0) { notificationManager.showInteractionNotification(any(), any()) }
    }

    @Test
    fun `shows nothing without the notification permission`() {
        every { isNotificationPermissionGrantedUseCase() } returns false

        controller.handleInteractionPush(mockk(), data, MESSAGE_ID)

        verify(exactly = 0) { notificationManager.showInteractionNotification(any(), any()) }
    }

    @Test
    fun `shows nothing without an alert text`() {
        controller.handleInteractionPush(mockk(), data - "body", MESSAGE_ID)

        verify(exactly = 0) { notificationManager.showInteractionNotification(any(), any()) }
    }

    @Test
    fun `shows nothing when the app has no launch activity`() {
        every { intentHelper.pushNotificationLaunchPendingIntent(any(), any(), any()) } returns null

        controller.handleInteractionPush(mockk(), data, MESSAGE_ID)

        verify(exactly = 0) { notificationManager.showInteractionNotification(any(), any()) }
    }
}
