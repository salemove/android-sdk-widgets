package com.glia.widgets.view.unifieduiconfig.component.call;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

public class ButtonBar implements Parcelable {

    @SerializedName("chatButton")
    @Nullable
    private final BarButtonStates chatButton;

    @SerializedName("minimizeButton")
    @Nullable
    private final BarButtonStates minimizeButton;

    @SerializedName("muteButton")
    @Nullable
    private final BarButtonStates muteButton;

    @SerializedName("speakerButton")
    @Nullable
    private final BarButtonStates speakerButton;

    @SerializedName("videoButton")
    @Nullable
    private final BarButtonStates videoButton;

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.chatButton, flags);
        dest.writeParcelable(this.minimizeButton, flags);
        dest.writeParcelable(this.muteButton, flags);
        dest.writeParcelable(this.speakerButton, flags);
        dest.writeParcelable(this.videoButton, flags);
    }

    protected ButtonBar(Parcel in) {
        this.chatButton = in.readParcelable(BarButtonStates.class.getClassLoader());
        this.minimizeButton = in.readParcelable(BarButtonStates.class.getClassLoader());
        this.muteButton = in.readParcelable(BarButtonStates.class.getClassLoader());
        this.speakerButton = in.readParcelable(BarButtonStates.class.getClassLoader());
        this.videoButton = in.readParcelable(BarButtonStates.class.getClassLoader());
    }

    public static final Parcelable.Creator<ButtonBar> CREATOR = new Parcelable.Creator<ButtonBar>() {
        @Override
        public ButtonBar createFromParcel(Parcel source) {
            return new ButtonBar(source);
        }

        @Override
        public ButtonBar[] newArray(int size) {
            return new ButtonBar[size];
        }
    };
}
