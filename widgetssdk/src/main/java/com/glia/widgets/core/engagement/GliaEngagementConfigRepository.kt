package com.glia.widgets.core.engagement

import com.glia.widgets.chat.ChatType

class GliaEngagementConfigRepository {
    var queueIds = emptyArray<String>()
    var chatType = ChatType.LIVE_CHAT

    fun reset() {
        queueIds = emptyArray()
        chatType = ChatType.LIVE_CHAT
    }
}
