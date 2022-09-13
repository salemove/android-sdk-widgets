package com.glia.widgets.view.unifieduiconfig.component.call;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

public class BarButtonStates {

    @SerializedName("inactive")
    @Nullable
    private BarButtonStyle inactive;

    @SerializedName("active")
    @Nullable
    private BarButtonStyle active;

    @Nullable
    public BarButtonStyle getInactive() {
        return inactive;
    }

    @Nullable
    public BarButtonStyle getActive() {
        return active;
    }
}
