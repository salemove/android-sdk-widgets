package com.glia.widgets.chat.model.history;

public class ParticipantMessageChatItem extends ChatItem {

    private final String messageId;

    public ParticipantMessageChatItem(String id, int viewType, String messageId) {
        super(id, viewType);
        this.messageId = messageId;
    }

    public String getMessageId() {
        return messageId;
    }
}
