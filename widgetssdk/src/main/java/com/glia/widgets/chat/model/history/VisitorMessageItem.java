package com.glia.widgets.chat.model.history;

import androidx.annotation.NonNull;

import com.glia.widgets.chat.adapter.ChatAdapter;

import java.util.Objects;

public class VisitorMessageItem extends ChatItem {
    public final static String HISTORY_ID = "history_id";
    public final static String CARD_RESPONSE_ID = "card_response_id";
    public final static String UNSENT_MESSAGE_ID = "unsent_message_id";
    private final boolean showDelivered;
    private final String message;

    public VisitorMessageItem(String id, boolean showDelivered, String message) {
        super(id, ChatAdapter.VISITOR_MESSAGE_TYPE);
        this.showDelivered = showDelivered;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public boolean isShowDelivered() {
        return showDelivered;
    }

    @NonNull
    @Override
    public String toString() {
        return "VisitorMessageItem{" +
                "chatItemId='" + getId() + '\'' +
                ", showDelivered=" + showDelivered +
                ", message='" + message + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        VisitorMessageItem that = (VisitorMessageItem) o;
        return showDelivered == that.showDelivered &&
                getId().equals(that.getId()) &&
                Objects.equals(message, that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getId(), showDelivered, message);
    }
}
