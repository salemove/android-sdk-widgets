package com.glia.widgets.view.unifieduiconfig.component.call;

import androidx.annotation.Nullable;

import com.glia.widgets.view.unifieduiconfig.component.ColorLayer;
import com.glia.widgets.view.unifieduiconfig.component.Text;
import com.google.gson.annotations.SerializedName;

public class BarButtonStyle {

    @SerializedName("background")
    @Nullable
    private ColorLayer background;

    @SerializedName("imageColor")
    @Nullable
    private ColorLayer imageColor;

    @SerializedName("title")
    @Nullable
    private Text title;

    @Nullable
    public ColorLayer getBackground() {
        return background;
    }

    @Nullable
    public ColorLayer getImageColor() {
        return imageColor;
    }

    @Nullable
    public Text getTitle() {
        return title;
    }
}
