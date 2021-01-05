package com.glia.widgets.chat;

import java.util.List;

public class ReceiveMessageItem extends ChatItem {
    private final List<String> messages;

    public ReceiveMessageItem(List<String> messages) {
        super(ChatAdapter.RECEIVE_MESSAGE_VIEW_TYPE);
        this.messages = messages;
    }

    public List<String> getMessages() {
        return messages;
    }
}
