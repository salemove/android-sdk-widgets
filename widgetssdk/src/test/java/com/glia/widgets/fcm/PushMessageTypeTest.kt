package com.glia.widgets.fcm

import org.junit.Test
import com.glia.androidsdk.fcm.GliaPushMessage.PushType as CorePushType

class PushMessageTypeTest {

    @Test
    fun testWidgetsPushMessageTypesCorrespondToCorePushTypes() {
        val allCorePushTypes = CorePushType.entries
        val allWidgetsPushTypes = PushMessageType.entries

        assert(allCorePushTypes.size == allWidgetsPushTypes.size)
        allCorePushTypes.forEachIndexed { index, item ->
            val widgetsType = item.toWidgetsType()

            assert(widgetsType.name == allCorePushTypes[index].name)
        }
    }
}
