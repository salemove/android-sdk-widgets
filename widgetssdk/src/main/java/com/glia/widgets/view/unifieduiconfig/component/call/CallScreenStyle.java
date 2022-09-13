package com.glia.widgets.view.unifieduiconfig.component.call;

import androidx.annotation.Nullable;

import com.glia.widgets.view.unifieduiconfig.component.Layer;
import com.glia.widgets.view.unifieduiconfig.component.Button;
import com.glia.widgets.view.unifieduiconfig.component.Text;
import com.glia.widgets.view.unifieduiconfig.component.chat.Header;
import com.google.gson.annotations.SerializedName;

public class CallScreenStyle {

    @SerializedName("background")
    @Nullable
    private Layer background;

    @SerializedName("bottomText")
    @Nullable
    private Text bottomText;

    @SerializedName("buttonBar")
    @Nullable
    private ButtonBar buttonBar;

    @SerializedName("duration")
    @Nullable
    private Text duration;

    @SerializedName("endButton")
    @Nullable
    private Button endButton;

    @SerializedName("header")
    @Nullable
    private Header header;

    @SerializedName("operator")
    @Nullable
    private Text operator;

    @SerializedName("topText")
    @Nullable
    private Text topText;

    @Nullable
    public Layer getBackground() {
        return background;
    }

    @Nullable
    public Text getBottomText() {
        return bottomText;
    }

    @Nullable
    public ButtonBar getButtonBar() {
        return buttonBar;
    }

    @Nullable
    public Text getDuration() {
        return duration;
    }

    @Nullable
    public Button getEndButton() {
        return endButton;
    }

    @Nullable
    public Header getHeader() {
        return header;
    }

    @Nullable
    public Text getOperator() {
        return operator;
    }

    @Nullable
    public Text getTopText() {
        return topText;
    }
}
