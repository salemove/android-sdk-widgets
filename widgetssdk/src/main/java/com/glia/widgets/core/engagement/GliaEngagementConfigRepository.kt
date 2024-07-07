package com.glia.widgets.core.engagement

import com.glia.widgets.chat.ChatType

internal class GliaEngagementConfigRepository {
    var queueIds = emptyList<String>()
    var chatType = ChatType.LIVE_CHAT

    fun reset() {
        queueIds = emptyList()
        chatType = ChatType.LIVE_CHAT
    }
}
