package com.glia.widgets.fcm

import org.junit.Test

class GliaPushMessageTest {

    @Test
    fun testWidgetsPushMessageActionsCorrespondToCorePushMessageActions() {
        val allCorePushActions = com.glia.androidsdk.fcm.GliaPushMessage.Action.entries
        val allWidgetsPushActions = GliaPushMessage.Action.entries

        assert(allCorePushActions.size == allWidgetsPushActions.size)
        allCorePushActions.forEachIndexed { index, item ->
            val widgetsAction = item.toWidgetsType()

            assert(widgetsAction.name == allCorePushActions[index].name)
        }
    }

    @Test
    fun testWidgetsPushMessageTypesCorrespondToCorePushMessageTypes() {
        val allCorePushTypes = com.glia.androidsdk.fcm.GliaPushMessage.PushType.entries
        val allWidgetsPushTypes = GliaPushMessage.PushType.entries

        assert(allCorePushTypes.size == allWidgetsPushTypes.size)
        allCorePushTypes.forEachIndexed { index, item ->
            val widgetsType = item.toWidgetsType()

            assert(widgetsType.name == allCorePushTypes[index].name)
        }
    }
}
