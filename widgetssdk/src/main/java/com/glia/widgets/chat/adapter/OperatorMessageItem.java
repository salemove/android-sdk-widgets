package com.glia.widgets.chat.adapter;

import com.glia.androidsdk.chat.SingleChoiceOption;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class OperatorMessageItem extends ChatItem {
    public final String operatorProfileImgUrl;
    public final boolean showChatHead;
    public final String content;
    public final List<SingleChoiceOption> singleChoiceOptions;
    public final Integer selectedIndex;
    public final String imageUrl;

    public OperatorMessageItem(
            String id,
            String operatorProfileImgUrl,
            boolean showChatHead,
            String content,
            List<SingleChoiceOption> singleChoiceOptions,
            Integer selectedIndex,
            String imageUrl
    ) {
        super(id, ChatAdapter.OPERATOR_MESSAGE_VIEW_TYPE);
        this.operatorProfileImgUrl = operatorProfileImgUrl;
        this.showChatHead = showChatHead;
        this.content = content;
        this.singleChoiceOptions = singleChoiceOptions != null ? Collections.unmodifiableList(singleChoiceOptions) : null;
        this.selectedIndex = selectedIndex;
        this.imageUrl = imageUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        OperatorMessageItem that = (OperatorMessageItem) o;
        return showChatHead == that.showChatHead &&
                Objects.equals(operatorProfileImgUrl, that.operatorProfileImgUrl) &&
                Objects.equals(content, that.content) &&
                Objects.equals(singleChoiceOptions, that.singleChoiceOptions) &&
                Objects.equals(selectedIndex, that.selectedIndex) &&
                Objects.equals(imageUrl, that.imageUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), operatorProfileImgUrl, showChatHead, content, singleChoiceOptions, selectedIndex, imageUrl);
    }

    @Override
    public String toString() {
        return "OperatorMessageItem{" +
                "operatorProfileImgUrl='" + operatorProfileImgUrl + '\'' +
                ", showChatHead=" + showChatHead +
                ", content='" + content + '\'' +
                ", singleChoiceOptions=" + singleChoiceOptions +
                ", selectedIndex=" + selectedIndex +
                ", imageUrl='" + imageUrl + '\'' +
                '}';
    }
}
