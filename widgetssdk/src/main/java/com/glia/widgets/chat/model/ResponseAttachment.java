package com.glia.widgets.chat.model;

import com.glia.androidsdk.chat.SingleChoiceAttachment;
import com.glia.androidsdk.chat.SingleChoiceOption;
import com.google.gson.annotations.SerializedName;

import java.util.Optional;

public class ResponseAttachment implements SingleChoiceAttachment {
    @SerializedName("selected_option")
    private final String selectedOption;

    @SerializedName("type")
    private final Type type;

    public ResponseAttachment(String selectedOption) {
        this.selectedOption = selectedOption;
        this.type = Type.SINGLE_CHOICE_RESPONSE;
    }

    @Override
    public SingleChoiceOption[] getOptions() {
        return new SingleChoiceOption[0];
    }

    @Override
    public String getSelectedOption() {
        return selectedOption;
    }

    @Override
    public Optional<String> getImageUrl() {
        return Optional.empty();
    }

    @Override
    public Type getType() {
        return type;
    }
}
