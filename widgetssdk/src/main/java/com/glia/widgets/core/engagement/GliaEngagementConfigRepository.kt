package com.glia.widgets.core.engagement

import com.glia.widgets.chat.ChatType

internal class GliaEngagementConfigRepository {
    @Volatile
    var queueIds = emptyList<String>() //TODO this line must be removed after we have layer for up to date queue list monitoring
    var chatType = ChatType.LIVE_CHAT

    fun reset() {
        chatType = ChatType.LIVE_CHAT
    }
}
