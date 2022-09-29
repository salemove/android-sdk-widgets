package com.glia.widgets.view.unifieduiconfig.component.call;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

import com.glia.widgets.view.unifieduiconfig.component.Layer;
import com.glia.widgets.view.unifieduiconfig.component.Button;
import com.glia.widgets.view.unifieduiconfig.component.Text;
import com.glia.widgets.view.unifieduiconfig.component.chat.Header;
import com.google.gson.annotations.SerializedName;

public class CallScreenStyle implements Parcelable {

    @SerializedName("background")
    @Nullable
    private final Layer background;

    @SerializedName("bottomText")
    @Nullable
    private final Text bottomText;

    @SerializedName("buttonBar")
    @Nullable
    private final ButtonBar buttonBar;

    @SerializedName("duration")
    @Nullable
    private final Text duration;

    @SerializedName("endButton")
    @Nullable
    private final Button endButton;

    @SerializedName("header")
    @Nullable
    private final Header header;

    @SerializedName("operator")
    @Nullable
    private final Text operator;

    @SerializedName("topText")
    @Nullable
    private final Text topText;

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.background, flags);
        dest.writeParcelable(this.bottomText, flags);
        dest.writeParcelable(this.buttonBar, flags);
        dest.writeParcelable(this.duration, flags);
        dest.writeParcelable(this.endButton, flags);
        dest.writeParcelable(this.header, flags);
        dest.writeParcelable(this.operator, flags);
        dest.writeParcelable(this.topText, flags);
    }

    protected CallScreenStyle(Parcel in) {
        this.background = in.readParcelable(Layer.class.getClassLoader());
        this.bottomText = in.readParcelable(Text.class.getClassLoader());
        this.buttonBar = in.readParcelable(ButtonBar.class.getClassLoader());
        this.duration = in.readParcelable(Text.class.getClassLoader());
        this.endButton = in.readParcelable(Button.class.getClassLoader());
        this.header = in.readParcelable(Header.class.getClassLoader());
        this.operator = in.readParcelable(Text.class.getClassLoader());
        this.topText = in.readParcelable(Text.class.getClassLoader());
    }

    public static final Parcelable.Creator<CallScreenStyle> CREATOR = new Parcelable.Creator<CallScreenStyle>() {
        @Override
        public CallScreenStyle createFromParcel(Parcel source) {
            return new CallScreenStyle(source);
        }

        @Override
        public CallScreenStyle[] newArray(int size) {
            return new CallScreenStyle[size];
        }
    };
}
