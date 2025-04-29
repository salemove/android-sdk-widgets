package com.glia.widgets.fcm

import org.junit.Test

class PushNotificationTypeTest {

    @Test
    fun testWidgetsPushNotificationTypesCorrespondToCorePushNotificationTypes() {
        val allCorePushTypes = com.glia.androidsdk.fcm.PushNotificationType.entries
        val allWidgetsPushTypes = PushNotificationType.entries

        assert(allCorePushTypes.size == allWidgetsPushTypes.size)
        allCorePushTypes.forEachIndexed { index, item ->
            val widgetsType = item.toWidgetsType()

            assert(widgetsType.name == allCorePushTypes[index].name)

            val coreType = widgetsType.toCoreType()
            assert(coreType == item)
        }
    }
}
