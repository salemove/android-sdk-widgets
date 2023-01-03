package com.glia.widgets.chat.model.history;

import androidx.annotation.NonNull;

import com.glia.androidsdk.chat.SingleChoiceOption;
import com.glia.widgets.chat.adapter.ChatAdapter;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class OperatorMessageItem extends OperatorChatItem {
    public final String operatorName;
    public final String content;
    public final List<SingleChoiceOption> singleChoiceOptions;
    public final String choiceCardImageUrl;
    public final boolean isClosed;

    public OperatorMessageItem(
            String id,
            String operatorName,
            String operatorProfileImgUrl,
            boolean showChatHead,
            String content,
            List<SingleChoiceOption> singleChoiceOptions,
            String choiceCardImageUrl,
            String operatorId,
            long timestamp,
            boolean isClosed
    ) {
        super(id, ChatAdapter.OPERATOR_MESSAGE_VIEW_TYPE, showChatHead, operatorProfileImgUrl, operatorId, id, timestamp);
        this.operatorName = operatorName;
        this.content = content;
        this.singleChoiceOptions = singleChoiceOptions != null ? Collections.unmodifiableList(singleChoiceOptions) : null;
        this.choiceCardImageUrl = choiceCardImageUrl;
        this.isClosed = isClosed;
    }

    public boolean isSingleChoiceCard() {
        return singleChoiceOptions != null && !singleChoiceOptions.isEmpty() && !isClosed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OperatorMessageItem)) return false;
        if (!super.equals(o)) return false;
        OperatorMessageItem that = (OperatorMessageItem) o;
        return Objects.equals(operatorName, that.operatorName)
                && Objects.equals(content, that.content)
                && Objects.equals(singleChoiceOptions, that.singleChoiceOptions)
                && Objects.equals(isClosed, that.isClosed)
                && Objects.equals(choiceCardImageUrl, that.choiceCardImageUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), operatorName, content, singleChoiceOptions, choiceCardImageUrl, isClosed);
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
                ", singleChoiceOptions=" + singleChoiceOptions + '\'' +
                ", choiceCardImageUrl='" + choiceCardImageUrl + '\'' +
                ", isClosed='" + isClosed + '\'' +
                "} " + super.toString();
    }
}
