package com.glia.widgets.view.unifieduiconfig.component.chat;

import androidx.annotation.Nullable;

import com.glia.widgets.view.unifieduiconfig.component.Layer;
import com.glia.widgets.view.unifieduiconfig.component.Text;
import com.glia.widgets.view.unifieduiconfig.component.base.Alignment;
import com.google.gson.annotations.SerializedName;

public class MessageBalloon {

    @SerializedName("background")
    @Nullable
    private Layer background;

    @SerializedName("text")
    @Nullable
    private Text text;

    @SerializedName("alignment")
    @Nullable
    private Alignment alignment;

    @Nullable
    public Layer getBackground() {
        return background;
    }

    @Nullable
    public Text getText() {
        return text;
    }

    @Nullable
    public Alignment getAlignment() {
        return alignment;
    }
}
