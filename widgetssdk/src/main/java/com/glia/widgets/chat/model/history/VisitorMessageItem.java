package com.glia.widgets.chat.model.history;

import androidx.annotation.NonNull;

import com.glia.androidsdk.chat.ChatMessage;
import com.glia.androidsdk.chat.SingleChoiceAttachment;
import com.glia.widgets.chat.adapter.ChatAdapter;

import java.util.Objects;

public class VisitorMessageItem extends LinkedChatItem {
    public final static String HISTORY_ID = "history_id";
    public final static String CARD_RESPONSE_ID = "card_response_id";
    public final static String UNSENT_MESSAGE_ID = "unsent_message_id";
    private final boolean showDelivered;
    private final String message;

    private VisitorMessageItem(String id, String messageId, String message, long timestamp, boolean showDelivered) {
        super(id, ChatAdapter.VISITOR_MESSAGE_TYPE, messageId, timestamp);
        this.showDelivered = showDelivered;
        this.message = message;
    }

    public static VisitorMessageItem asNewMessage(ChatMessage message) {
        return new VisitorMessageItem(message.getId(), message.getId(), message.getContent(), message.getTimestamp(), false);
    }

    public static VisitorMessageItem asUnsentItem(String unsentMessageText) {
        return new VisitorMessageItem(UNSENT_MESSAGE_ID, null, unsentMessageText, System.currentTimeMillis(), false);
    }

    public static VisitorMessageItem asHistoryItem(ChatMessage message) {
        return new VisitorMessageItem(HISTORY_ID, message.getId(), message.getContent(), message.getTimestamp(), false);
    }

    public static VisitorMessageItem asCardResponseItem(ChatMessage message) {
        String selectedOptionText = null;
        if (message.getAttachment() != null && message.getAttachment() instanceof SingleChoiceAttachment) {
            SingleChoiceAttachment singleChoiceAttachment = (SingleChoiceAttachment) message.getAttachment();
            selectedOptionText = singleChoiceAttachment.getSelectedOptionText();
        }
        return new VisitorMessageItem(CARD_RESPONSE_ID, message.getId(), selectedOptionText, message.getTimestamp(), false);
    }

    public static VisitorMessageItem asUnsentCardResponse(String unsentResponse) {
        return new VisitorMessageItem(CARD_RESPONSE_ID, null, unsentResponse, System.currentTimeMillis(), false);
    }

    public static VisitorMessageItem editDeliveredStatus(VisitorMessageItem source, boolean showDelivered) {
        return new VisitorMessageItem(source.getId(), source.getMessageId(), source.getMessage(), source.getTimestamp(), showDelivered);
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
