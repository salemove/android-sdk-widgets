package com.glia.widgets.core.engagement

import com.glia.widgets.chat.ChatType

internal class GliaEngagementConfigRepository {
    var chatType = ChatType.LIVE_CHAT

    fun reset() {
        chatType = ChatType.LIVE_CHAT
    }
}
