package com.glia.widgets.chat.model.history;

import androidx.annotation.NonNull;

import com.glia.widgets.chat.adapter.ChatAdapter;

import java.util.Objects;

public class OperatorMessageItem extends OperatorChatItem {
    public final String operatorName;
    public final String content;

    public OperatorMessageItem(
            String id,
            String operatorName,
            String operatorProfileImgUrl,
            boolean showChatHead,
            String content,
            String operatorId,
            long timestamp
    ) {
        super(id, ChatAdapter.OPERATOR_MESSAGE_VIEW_TYPE, showChatHead, operatorProfileImgUrl, operatorId, id, timestamp);
        this.operatorName = operatorName;
        this.content = content;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OperatorMessageItem)) return false;
        if (!super.equals(o)) return false;
        OperatorMessageItem that = (OperatorMessageItem) o;
        return Objects.equals(operatorName, that.operatorName)
                && Objects.equals(content, that.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), operatorName, content);
    }

    @NonNull
    @Override
    public String toString() {
        return "OperatorMessageItem{" +
                "showChatHead=" + showChatHead +
                ", operatorProfileImgUrl='" + operatorProfileImgUrl + '\'' +
                ", operatorId='" + operatorId + '\'' +
                ", operatorName='" + operatorName + '\'' +
                ", content='" + content + '\'' +
                "} " + super.toString();
    }
}
