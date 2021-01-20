package com.glia.widgets.chat.adapter;

import java.util.Objects;

public class SendMessageItem extends ChatItem {
    public final static String HISTORY_ID = "history_id";
    private final String id;
    private final boolean showDelivered;
    private final String message;

    public SendMessageItem(String id, boolean showDelivered, String message) {
        super(id, ChatAdapter.SEND_MESSAGE_VIEW_TYPE);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        SendMessageItem that = (SendMessageItem) o;
        return showDelivered == that.showDelivered &&
                id.equals(that.id) &&
                Objects.equals(message, that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), id, showDelivered, message);
    }
}
