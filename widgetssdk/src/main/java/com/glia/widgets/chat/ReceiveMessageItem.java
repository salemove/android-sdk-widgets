package com.glia.widgets.chat;

class ReceiveMessageItem extends ChatItem {
    private final String message;

    public ReceiveMessageItem(String message) {
        super(ChatAdapter.RECEIVE_MESSAGE_VIEW_TYPE);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
