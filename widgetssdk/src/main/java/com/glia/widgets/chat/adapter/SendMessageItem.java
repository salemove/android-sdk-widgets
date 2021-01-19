package com.glia.widgets.chat.adapter;

public class SendMessageItem extends ChatItem {
    private final String id;
    private final boolean showDelivered;
    private final String message;

    public SendMessageItem(String id, boolean showDelivered, String message) {
        super(ChatAdapter.SEND_MESSAGE_VIEW_TYPE);
        this.id = id;
        this.showDelivered = showDelivered;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public String getId() {
        return id;
    }

    public boolean isShowDelivered() {
        return showDelivered;
    }

    @Override
    public String toString() {
        return "SendMessageItem{" +
                "id='" + id + '\'' +
                ", showDelivered=" + showDelivered +
                ", message='" + message + '\'' +
                '}';
    }
}
