package com.glia.widgets.view.unifieduiconfig.component.chat;

import androidx.annotation.Nullable;

import com.glia.widgets.view.unifieduiconfig.component.Layer;
import com.glia.widgets.view.unifieduiconfig.component.Text;
import com.google.gson.annotations.SerializedName;

public class Header {

    @SerializedName("text")
    @Nullable
    private Text text;

    @SerializedName("background")
    @Nullable
    private Layer color;

    @Nullable
    public Text getText() {
        return text;
    }

    @Nullable
    public Layer getColor() {
        return color;
    }
}
