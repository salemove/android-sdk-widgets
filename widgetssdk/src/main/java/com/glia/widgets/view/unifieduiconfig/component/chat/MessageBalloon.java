package com.glia.widgets.view.unifieduiconfig.component.chat;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

import com.glia.widgets.view.unifieduiconfig.component.Layer;
import com.glia.widgets.view.unifieduiconfig.component.Text;
import com.glia.widgets.view.unifieduiconfig.component.base.Alignment;
import com.google.gson.annotations.SerializedName;

public class MessageBalloon implements Parcelable {

    @SerializedName("background")
    @Nullable
    private final Layer background;

    @SerializedName("text")
    @Nullable
    private final Text text;

    @SerializedName("alignment")
    @Nullable
    private final Alignment alignment;

    @Nullable
    public Layer getBackground() {
        return background;
    }

    @Nullable
    public Text getText() {
        return text;
    }

    @Nullable
    public Alignment getAlignment() {
        return alignment;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.background, flags);
        dest.writeParcelable(this.text, flags);
        dest.writeParcelable(this.alignment, flags);
    }

    protected MessageBalloon(Parcel in) {
        this.background = in.readParcelable(Layer.class.getClassLoader());
        this.text = in.readParcelable(Text.class.getClassLoader());
        this.alignment = in.readParcelable(Alignment.class.getClassLoader());
    }

    public static final Parcelable.Creator<MessageBalloon> CREATOR = new Parcelable.Creator<MessageBalloon>() {
        @Override
        public MessageBalloon createFromParcel(Parcel source) {
            return new MessageBalloon(source);
        }

        @Override
        public MessageBalloon[] newArray(int size) {
            return new MessageBalloon[size];
        }
    };
}
