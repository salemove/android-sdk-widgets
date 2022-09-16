package com.glia.widgets.view.unifieduiconfig.component.chat;

import androidx.annotation.Nullable;

import com.glia.widgets.view.unifieduiconfig.component.Layer;
import com.glia.widgets.view.unifieduiconfig.component.Button;
import com.google.gson.annotations.SerializedName;

public class ChatScreenStyle {

    @SerializedName("background")
    @Nullable
    private Layer background;

    @SerializedName("header")
    @Nullable
    private Header header;

    @SerializedName("endButton")
    @Nullable
    private Button endButton;

    @SerializedName("operatorMessage")
    @Nullable
    private MessageBalloon operatorMessage;

    @SerializedName("visitorMessage")
    @Nullable
    private MessageBalloon visitorMessage;

    @Nullable
    public Layer getBackground() {
        return background;
    }

    @Nullable
    public Header getHeader() {
        return header;
    }

    @Nullable
    public Button getEndButton() {
        return endButton;
    }

    @Nullable
    public MessageBalloon getOperatorMessage() {
        return operatorMessage;
    }

    @Nullable
    public MessageBalloon getVisitorMessage() {
        return visitorMessage;
    }
}
