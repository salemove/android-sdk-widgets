package com.glia.widgets.fcm

import android.GLIA_LOGGER_PATH
import android.content.Intent
import android.os.Bundle
import com.glia.widgets.push.notifications.PushClickHandlerController
import com.glia.widgets.push.notifications.SecureMessagingPushController
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import com.glia.androidsdk.fcm.GliaPushMessage.PushType as CorePushType
import com.glia.androidsdk.fcm.PushNotifications as CorePushNotifications

class PushNotificationsImplTest {
    private lateinit var corePushNotifications: CorePushNotifications
    private lateinit var pushClickHandlerController: PushClickHandlerController
    private lateinit var pushNotifications: PushNotificationsImpl

    @Before
    fun setUp() {
        mockkStatic(GLIA_LOGGER_PATH)
        corePushNotifications = mockk(relaxed = true)
        pushClickHandlerController = mockk(relaxUnitFun = true)
        pushNotifications = PushNotificationsImpl(
            corePushNotifications,
            mockk<SecureMessagingPushController>(relaxed = true),
            pushClickHandlerController
        )
    }

    @After
    fun tearDown() {
        unmockkStatic(GLIA_LOGGER_PATH)
    }

    @Test
    fun `pushMessageTypeOf maps every core push type to its widgets counterpart`() {
        CorePushType.entries.forEach { coreType ->
            val bundle = mockk<Bundle>()
            every { corePushNotifications.parsePushMessageType(bundle) } returns coreType

            assertEquals(coreType.toWidgetsType(), pushNotifications.pushMessageTypeOf(bundle))
        }
    }

    @Test
    fun `pushMessageTypeOf returns null when core does not recognise the bundle`() {
        val bundle = mockk<Bundle>()
        every { corePushNotifications.parsePushMessageType(bundle) } returns null

        assertNull(pushNotifications.pushMessageTypeOf(bundle))
    }

    @Test
    fun `pushMessageTypeOf passes the intent extras to core`() {
        val bundle = mockk<Bundle>()
        val intent = mockk<Intent> { every { extras } returns bundle }
        every { corePushNotifications.parsePushMessageType(bundle) } returns CorePushType.CHAT_MESSAGE

        assertEquals(PushMessageType.CHAT_MESSAGE, pushNotifications.pushMessageTypeOf(intent))
        verify { corePushNotifications.parsePushMessageType(bundle) }
    }

    @Test
    fun `pushMessageTypeOf returns null for a null intent`() {
        every { corePushNotifications.parsePushMessageType(null) } returns null

        assertNull(pushNotifications.pushMessageTypeOf(null as Intent?))
    }

    @Test
    fun `pushMessageTypeOf returns null for a null bundle`() {
        every { corePushNotifications.parsePushMessageType(null) } returns null

        assertNull(pushNotifications.pushMessageTypeOf(null as Bundle?))
    }

    @Test
    fun `handlePushNotificationClick passes the intent extras to the controller`() {
        val bundle = mockk<Bundle>()
        val intent = mockk<Intent> { every { extras } returns bundle }

        pushNotifications.handlePushNotificationClick(intent)

        verify { pushClickHandlerController.handlePushNotificationClick(bundle) }
    }

    @Test
    fun `handlePushNotificationClick passes the bundle to the controller`() {
        val bundle = mockk<Bundle>()

        pushNotifications.handlePushNotificationClick(bundle)

        verify { pushClickHandlerController.handlePushNotificationClick(bundle) }
    }

    @Test
    fun `handlePushNotificationClick passes null to the controller for a null intent`() {
        pushNotifications.handlePushNotificationClick(null as Intent?)

        verify { pushClickHandlerController.handlePushNotificationClick(null) }
    }
}
