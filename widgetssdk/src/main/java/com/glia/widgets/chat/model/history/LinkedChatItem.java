package com.glia.widgets.chat.model.history;

import com.glia.widgets.chat.adapter.ChatAdapter;

public class LinkedChatItem extends ChatItem {

    private final String messageId;
    private final long timestamp;

    public LinkedChatItem(String id, @ChatAdapter.Type int viewType, String messageId, long timestamp) {
        super(id, viewType);
        this.messageId = messageId;
        this.timestamp = timestamp;
    }

    public String getMessageId() {
        return messageId;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
