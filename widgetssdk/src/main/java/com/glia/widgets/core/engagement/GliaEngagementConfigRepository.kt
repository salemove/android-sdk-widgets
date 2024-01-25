package com.glia.widgets.core.engagement

import com.glia.widgets.chat.ChatType

internal class GliaEngagementConfigRepository {
    var queueIds = emptyArray<String>()
    var chatType = ChatType.LIVE_CHAT

    fun reset() {
        queueIds = emptyArray()
        chatType = ChatType.LIVE_CHAT
    }
}
