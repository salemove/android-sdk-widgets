package com.glia.widgets.view.unifieduiconfig.component;

import androidx.annotation.Nullable;

import com.glia.widgets.view.unifieduiconfig.component.base.Size;
import com.glia.widgets.view.unifieduiconfig.component.base.TextStyle;
import com.google.gson.annotations.SerializedName;

public class Font {

    @SerializedName("size")
    @Nullable
    Size.Sp size;

    @SerializedName("style")
    @Nullable
    TextStyle style;
}
