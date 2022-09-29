package com.glia.widgets.view.unifieduiconfig.component.chat;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

import com.glia.widgets.view.unifieduiconfig.component.Layer;
import com.glia.widgets.view.unifieduiconfig.component.Button;
import com.google.gson.annotations.SerializedName;

public class ChatScreenStyle implements Parcelable {

    @SerializedName("background")
    @Nullable
    private final Layer background;

    @SerializedName("header")
    @Nullable
    private final Header header;

    @SerializedName("endButton")
    @Nullable
    private final Button endButton;

    @SerializedName("operatorMessage")
    @Nullable
    private final MessageBalloon operatorMessage;

    @SerializedName("visitorMessage")
    @Nullable
    private final MessageBalloon visitorMessage;

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.background, flags);
        dest.writeParcelable(this.header, flags);
        dest.writeParcelable(this.endButton, flags);
        dest.writeParcelable(this.operatorMessage, flags);
        dest.writeParcelable(this.visitorMessage, flags);
    }

    protected ChatScreenStyle(Parcel in) {
        this.background = in.readParcelable(Layer.class.getClassLoader());
        this.header = in.readParcelable(Header.class.getClassLoader());
        this.endButton = in.readParcelable(Button.class.getClassLoader());
        this.operatorMessage = in.readParcelable(MessageBalloon.class.getClassLoader());
        this.visitorMessage = in.readParcelable(MessageBalloon.class.getClassLoader());
    }

    public static final Parcelable.Creator<ChatScreenStyle> CREATOR = new Parcelable.Creator<ChatScreenStyle>() {
        @Override
        public ChatScreenStyle createFromParcel(Parcel source) {
            return new ChatScreenStyle(source);
        }

        @Override
        public ChatScreenStyle[] newArray(int size) {
            return new ChatScreenStyle[size];
        }
    };
}
