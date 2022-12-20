package com.glia.widgets.chat.model.history;

public class LinkedChatItem extends ChatItem {

    private final String messageId;
    private final long timestamp;

    public LinkedChatItem(String id, int viewType, String messageId, long timestamp) {
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
