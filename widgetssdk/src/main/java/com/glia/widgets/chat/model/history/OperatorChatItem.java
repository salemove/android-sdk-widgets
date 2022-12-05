package com.glia.widgets.chat.model.history;

import java.util.Objects;

public abstract class OperatorChatItem extends ParticipantMessageChatItem {

    public final boolean showChatHead;
    public final String operatorProfileImgUrl;
    public final String operatorId;

    protected OperatorChatItem(String id, int viewType, boolean showChatHead, String operatorProfileImgUrl, String operatorId, String messageId) {
        super(id, viewType, messageId);
        this.showChatHead = showChatHead;
        this.operatorProfileImgUrl = operatorProfileImgUrl;
        this.operatorId = operatorId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OperatorChatItem)) return false;
        if (!super.equals(o)) return false;
        OperatorChatItem that = (OperatorChatItem) o;
        return showChatHead == that.showChatHead
                && Objects.equals(operatorProfileImgUrl, that.operatorProfileImgUrl)
                && Objects.equals(operatorId, that.operatorId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), showChatHead, operatorProfileImgUrl, operatorId);
    }
}
