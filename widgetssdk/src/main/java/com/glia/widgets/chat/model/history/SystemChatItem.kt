package com.glia.widgets.chat.model.history

import com.glia.widgets.chat.adapter.ChatAdapter

class SystemChatItem(id: String, timestamp: Long, val message: String) :
    LinkedChatItem(id, ChatAdapter.SYSTEM_MESSAGE_TYPE, id, timestamp), ServerChatItem
