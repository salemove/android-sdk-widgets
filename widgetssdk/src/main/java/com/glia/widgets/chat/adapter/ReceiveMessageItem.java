package com.glia.widgets.chat.adapter;

import java.util.List;
import java.util.Objects;

public class ReceiveMessageItem extends ChatItem {
    private final List<ReceiveMessageItemMessage> messages;
    private final String operatorProfileImgUrl;

    public ReceiveMessageItem(
            String id,
            List<ReceiveMessageItemMessage> messages,
            String operatorProfileImgUrl
    ) {
        super(id, ChatAdapter.RECEIVE_MESSAGE_VIEW_TYPE);
        this.messages = messages;
        this.operatorProfileImgUrl = operatorProfileImgUrl;
    }

    public List<ReceiveMessageItemMessage> getMessages() {
        return messages;
    }

    public String getOperatorProfileImgUrl() {
        return operatorProfileImgUrl;
    }

    @Override
    public String toString() {
        return "ReceiveMessageItem{" +
                "messages=" + messages +
                ", operatorProfileImgUrl='" + operatorProfileImgUrl + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ReceiveMessageItem that = (ReceiveMessageItem) o;
        return Objects.equals(messages, that.messages) &&
                Objects.equals(operatorProfileImgUrl, that.operatorProfileImgUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), messages, operatorProfileImgUrl);
    }
}
