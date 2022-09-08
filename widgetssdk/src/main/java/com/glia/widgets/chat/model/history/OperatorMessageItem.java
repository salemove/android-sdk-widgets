package com.glia.widgets.chat.model.history;

import com.glia.androidsdk.chat.SingleChoiceOption;
import com.glia.widgets.chat.adapter.ChatAdapter;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class OperatorMessageItem extends OperatorChatItem {
    public final String operatorName;
    public final String content;
    public final List<SingleChoiceOption> singleChoiceOptions;
    public final Integer selectedChoiceIndex;
    public final String choiceCardImageUrl;

    public OperatorMessageItem(
            String id,
            String operatorName,
            String operatorProfileImgUrl,
            boolean showChatHead,
            String content,
            List<SingleChoiceOption> singleChoiceOptions,
            Integer selectedChoiceIndex,
            String choiceCardImageUrl,
            String operatorId
    ) {
        super(id, ChatAdapter.OPERATOR_MESSAGE_VIEW_TYPE, showChatHead, operatorProfileImgUrl, operatorId);
        this.operatorName = operatorName;
        this.content = content;
        this.singleChoiceOptions = singleChoiceOptions != null ? Collections.unmodifiableList(singleChoiceOptions) : null;
        this.selectedChoiceIndex = selectedChoiceIndex;
        this.choiceCardImageUrl = choiceCardImageUrl;
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
                && Objects.equals(selectedChoiceIndex, that.selectedChoiceIndex)
                && Objects.equals(choiceCardImageUrl, that.choiceCardImageUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), operatorName, content, singleChoiceOptions, selectedChoiceIndex, choiceCardImageUrl);
    }

    @Override
    public String toString() {
        return "OperatorMessageItem{" +
                "showChatHead=" + showChatHead +
                ", operatorProfileImgUrl='" + operatorProfileImgUrl + '\'' +
                ", operatorId='" + operatorId + '\'' +
                ", operatorName='" + operatorName + '\'' +
                ", content='" + content + '\'' +
                ", singleChoiceOptions=" + singleChoiceOptions +
                ", selectedChoiceIndex=" + selectedChoiceIndex +
                ", choiceCardImageUrl='" + choiceCardImageUrl + '\'' +
                "} " + super.toString();
    }
}
