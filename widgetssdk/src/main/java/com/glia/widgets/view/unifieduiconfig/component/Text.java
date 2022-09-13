package com.glia.widgets.view.unifieduiconfig.component;

import androidx.annotation.Nullable;

import com.glia.widgets.view.unifieduiconfig.component.base.Alignment;
import com.google.gson.annotations.SerializedName;

public class Text {

    @SerializedName("foreground")
    @Nullable
    private ColorLayer textColor;

    @SerializedName("background")
    @Nullable
    private ColorLayer backgroundColor;

    @SerializedName("font")
    @Nullable
    private Font font;

    @SerializedName("alignment")
    @Nullable
    private Alignment alignment;

    @Nullable
    public ColorLayer getTextColor() {
        return textColor;
    }

    @Nullable
    public ColorLayer getBackgroundColor() {
        return backgroundColor;
    }

    @Nullable
    public Font getFont() {
        return font;
    }

    @Nullable
    public Alignment getAlignment() {
        return alignment;
    }
}
