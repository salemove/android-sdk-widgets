package com.glia.widgets.view.unifieduiconfig.component.call;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

public class ButtonBar {

    @SerializedName("chatButton")
    @Nullable
    private BarButtonStates chatButton;

    @SerializedName("minimizeButton")
    @Nullable
    private BarButtonStates minimizeButton;

    @SerializedName("muteButton")
    @Nullable
    private BarButtonStates muteButton;

    @SerializedName("speakerButton")
    @Nullable
    private BarButtonStates speakerButton;

    @SerializedName("videoButton")
    @Nullable
    private BarButtonStates videoButton;

    @Nullable
    public BarButtonStates getChatButton() {
        return chatButton;
    }

    @Nullable
    public BarButtonStates getMinimizeButton() {
        return minimizeButton;
    }

    @Nullable
    public BarButtonStates getMuteButton() {
        return muteButton;
    }

    @Nullable
    public BarButtonStates getSpeakerButton() {
        return speakerButton;
    }

    @Nullable
    public BarButtonStates getVideoButton() {
        return videoButton;
    }
}
