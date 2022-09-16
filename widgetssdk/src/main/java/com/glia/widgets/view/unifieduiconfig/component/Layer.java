package com.glia.widgets.view.unifieduiconfig.component;

import androidx.annotation.Nullable;

import com.glia.widgets.view.unifieduiconfig.component.base.Size;
import com.google.gson.annotations.SerializedName;

public class Layer {

    @SerializedName("color")
    @Nullable
    private ColorLayer color;

    @SerializedName("border")
    @Nullable
    private ColorLayer borderColor;

    @SerializedName("borderWidth")
    @Nullable
    private Size.Dp borderWidth;

    @SerializedName("cornerRadius")
    @Nullable
    private Size.Dp cornerRadius;

    @Nullable
    public ColorLayer getColor() {
        return color;
    }

    @Nullable
    public ColorLayer getBorderColor() {
        return borderColor;
    }

    @Nullable
    public Size.Dp getBorderWidth() {
        return borderWidth;
    }

    @Nullable
    public Size.Dp getCornerRadius() {
        return cornerRadius;
    }
}
