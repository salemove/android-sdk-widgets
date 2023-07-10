package com.glia.widgets.chat.model.history;

import com.glia.widgets.chat.adapter.ChatAdapter;

import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public abstract class OperatorChatItem extends LinkedChatItem implements ServerChatItem {

    private final boolean showChatHead;
    private final String operatorProfileImgUrl;
    private final String operatorId;

    protected OperatorChatItem(String id, @ChatAdapter.Type int viewType, boolean showChatHead, String operatorProfileImgUrl, String operatorId, String messageId, long timestamp) {
        super(id, viewType, messageId, timestamp);
        this.showChatHead = showChatHead;
        this.operatorProfileImgUrl = operatorProfileImgUrl;
        this.operatorId = operatorId;
    }

    public boolean getShowChatHead() {
        return showChatHead;
    }

    @Nullable
    public String getOperatorProfileImgUrl() {
        return operatorProfileImgUrl;
    }

    @Nullable
    public String getOperatorId() {
        return operatorId;
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
