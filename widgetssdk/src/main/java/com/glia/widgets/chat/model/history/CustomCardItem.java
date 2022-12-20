package com.glia.widgets.chat.model.history;

import androidx.annotation.NonNull;

import com.glia.androidsdk.chat.ChatMessage;

import java.util.Objects;

public class CustomCardItem extends ParticipantMessageChatItem {
    private final ChatMessage message;

    public CustomCardItem(ChatMessage message, int viewType) {
        super(message.getId(), viewType, message.getId(), message.getTimestamp());

        this.message = message;
    }

    public ChatMessage getMessage() {
        return message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        CustomCardItem that = (CustomCardItem) o;

        if (message.getTimestamp() != that.message.getTimestamp()) return false;
        if (!message.getId().equals(that.message.getId())) return false;
        if (!message.getContent().equals(that.message.getContent())) return false;
        if (message.getSender() != that.message.getSender()) return false;
        if (!Objects.equals(message.getAttachment(), that.message.getAttachment())) return false;
        return Objects.equals(message.getMetadata(), that.message.getMetadata());
    }

    @Override
    public int hashCode() {
        int result = (int) (message.getTimestamp() ^ (message.getTimestamp() >>> 32));
        result = 31 * result + message.getId().hashCode();
        result = 31 * result + message.getContent().hashCode();
        result = 31 * result + (message.getSender() != null ? message.getSender().hashCode() : 0);
        result = 31 * result + (message.getAttachment() != null ? message.getAttachment().hashCode() : 0);
        result = 31 * result + (message.getMetadata() != null ? message.getMetadata().hashCode() : 0);
        return result;
    }

    @NonNull
    @Override
    public String toString() {
        return "CustomCardItem{" +
                "message={" +
                "timestamp=" + message.getTimestamp() +
                ", id='" + message.getId() + '\'' +
                ", content='" + message.getContent() + '\'' +
                ", sender=" + message.getSender() +
                ", attachment=" + message.getAttachment() +
                ", metadata=" + message.getMetadata() +
                "}" +
                "}";
    }
}
