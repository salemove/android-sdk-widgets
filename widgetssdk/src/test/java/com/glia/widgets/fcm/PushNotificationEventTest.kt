package com.glia.widgets.fcm

import com.glia.androidsdk.fcm.PushNotificationEvent as CorePushNotificationEvent
import com.glia.androidsdk.fcm.PushNotificationType as CorePushNotificationType
import org.junit.Assert.assertEquals
import org.junit.Test

class PushNotificationEventTest {

    @Test
    fun toCoreType_convertsToCorePushNotificationEvent() {
        val widgetsEvent = PushNotificationEvent(
            type = PushNotificationType.MESSAGE,
            message = "Test message"
        )

        val coreEvent = widgetsEvent.toCoreType()

        assertEquals(CorePushNotificationEvent::class.java, coreEvent::class.java)
        assertEquals(CorePushNotificationType.MESSAGE, coreEvent.type)
        assertEquals(widgetsEvent.message, coreEvent.message)
    }

    @Test
    fun toCoreType_convertsCollectionToCorePushNotificationEventList() {
        val widgetsEvents = listOf(
            PushNotificationEvent(PushNotificationType.MESSAGE, "Message"),
            PushNotificationEvent(PushNotificationType.END, "Ended")
        )

        val coreEvents = widgetsEvents.toCoreType()

        assertEquals(2, coreEvents.size)
        assertEquals("Message", coreEvents.first().message)
        assertEquals(CorePushNotificationType.MESSAGE, coreEvents.first().type)
        assertEquals("Ended", coreEvents.last().message)
        assertEquals(CorePushNotificationType.END, coreEvents.last().type)
    }

    @Test
    fun toWidgetsType_convertsToWidgetsPushNotificationEvent() {
        val coreEvent = CorePushNotificationEvent(
            CorePushNotificationType.MESSAGE,
            "Test message"
        )

        val widgetsEvent = coreEvent.toWidgetsType()

        assertEquals(PushNotificationEvent::class.java, widgetsEvent::class.java)
        assertEquals(PushNotificationType.MESSAGE, widgetsEvent.type)
        assertEquals(coreEvent.message, widgetsEvent.message)
    }

    @Test
    fun toWidgetsType_convertsSetToWidgetsPushNotificationEventCollection() {
        val coreEvents = listOf(
            CorePushNotificationEvent(
                CorePushNotificationType.MESSAGE,
                "Message"
            ),
            CorePushNotificationEvent(
                CorePushNotificationType.END,
                "Ended"
            )
        )

        val widgetsEvents = coreEvents.toWidgetsType()

        assertEquals(2, widgetsEvents.size)
        assertEquals("Message", widgetsEvents.first().message)
        assertEquals(PushNotificationType.MESSAGE, widgetsEvents.first().type)
        assertEquals("Ended", widgetsEvents.last().message)
        assertEquals(PushNotificationType.END, widgetsEvents.last().type)
    }
}
