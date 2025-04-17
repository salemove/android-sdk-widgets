package com.glia.widgets.chat.adapter

import junit.framework.TestCase.assertEquals
import org.junit.Assert
import org.junit.Test

class CustomCardMessageTest {
    @Test
    fun `widgets chat Participant types correspond to core types`() {
        val allCoreChatParticipants = com.glia.androidsdk.chat.Chat.Participant.entries
        val allWidgetsChatParticipants = CustomCardMessage.Participant.entries

        assertEquals(allCoreChatParticipants.size, allWidgetsChatParticipants.size)
        allCoreChatParticipants.forEachIndexed { index, item ->
            val widgetsType = CustomCardMessage.Participant.toWidgetsType(item.name)

            Assert.assertNotNull(widgetsType)
            Assert.assertEquals(widgetsType.name, allCoreChatParticipants[index].name)
        }
    }
}
