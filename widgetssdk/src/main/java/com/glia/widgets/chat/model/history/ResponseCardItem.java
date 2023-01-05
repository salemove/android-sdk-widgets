package com.glia.widgets.chat.model.history;

import androidx.annotation.NonNull;

import com.glia.androidsdk.chat.SingleChoiceOption;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ResponseCardItem extends OperatorMessageItem {
    @NonNull
    public final List<SingleChoiceOption> singleChoiceOptions;
    public final String choiceCardImageUrl;

    public ResponseCardItem(
            @NonNull String id,
            String operatorName,
            String operatorProfileImgUrl,
            boolean showChatHead,
            String content,
            String operatorId,
            long timestamp,
            //Must not be empty, otherwise it is OperatorMessageItem
            @NonNull List<SingleChoiceOption> singleChoiceOptions,
            String choiceCardImageUrl
    ) {
        super(id, operatorName, operatorProfileImgUrl, showChatHead, content, operatorId, timestamp);

        assert !singleChoiceOptions.isEmpty();
        this.singleChoiceOptions = Collections.unmodifiableList(singleChoiceOptions);
        this.choiceCardImageUrl = choiceCardImageUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ResponseCardItem)) return false;
        if (!super.equals(o)) return false;
        ResponseCardItem that = (ResponseCardItem) o;
        return singleChoiceOptions.equals(that.singleChoiceOptions) && Objects.equals(choiceCardImageUrl, that.choiceCardImageUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), singleChoiceOptions, choiceCardImageUrl);
    }

    @NonNull
    @Override
    public String toString() {
        return "ResponseCardItem{" +
                "singleChoiceOptions=" + singleChoiceOptions +
                ", choiceCardImageUrl='" + choiceCardImageUrl + '\'' +
                "} " + super.toString();
    }
}
