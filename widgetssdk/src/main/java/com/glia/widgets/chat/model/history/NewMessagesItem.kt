package com.glia.widgets.chat.model.history

import com.glia.widgets.chat.adapter.ChatAdapter
import java.util.*

object NewMessagesItem : ChatItem(UUID.randomUUID().toString(), ChatAdapter.NEW_MESSAGES_DIVIDER_TYPE)
