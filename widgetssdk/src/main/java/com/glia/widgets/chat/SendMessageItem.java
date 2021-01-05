package com.glia.widgets.chat;

public class SendMessageItem extends ChatItem {
    private final String message;

    public SendMessageItem(String message) {
        super(ChatAdapter.SEND_MESSAGE_VIEW_TYPE);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
