package com.glia.widgets.view.unifieduiconfig.component;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

public class Button {

    @SerializedName("text")
    @Nullable
    private Text text;

    @SerializedName("background")
    @Nullable
    private ColorLayer color;

    @Nullable
    public Text getText() {
        return text;
    }

    @Nullable
    public ColorLayer getColor() {
        return color;
    }
}
