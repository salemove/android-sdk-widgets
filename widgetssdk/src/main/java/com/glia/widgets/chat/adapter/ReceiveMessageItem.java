package com.glia.widgets.chat.adapter;

import java.util.List;
import java.util.Objects;

public class ReceiveMessageItem extends ChatItem {
    private final List<String> messages;

    public ReceiveMessageItem(String id, List<String> messages) {
        super(id, ChatAdapter.RECEIVE_MESSAGE_VIEW_TYPE);
        this.messages = messages;
    }

    public List<String> getMessages() {
        return messages;
    }

    @Override
    public String toString() {
        return "ReceiveMessageItem{" +
                "messages=" + messages +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ReceiveMessageItem that = (ReceiveMessageItem) o;
        return Objects.equals(messages, that.messages);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), messages);
    }
}
