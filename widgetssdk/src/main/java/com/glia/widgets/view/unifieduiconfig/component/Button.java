package com.glia.widgets.view.unifieduiconfig.component;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

public class Button implements Parcelable {

    @SerializedName("text")
    @Nullable
    private final Text text;

    @SerializedName("background")
    @Nullable
    private final ColorLayer color;

    @Nullable
    public Text getText() {
        return text;
    }

    @Nullable
    public ColorLayer getColor() {
        return color;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.text, flags);
        dest.writeParcelable(this.color, flags);
    }

    protected Button(Parcel in) {
        this.text = in.readParcelable(Text.class.getClassLoader());
        this.color = in.readParcelable(ColorLayer.class.getClassLoader());
    }

    public static final Parcelable.Creator<Button> CREATOR = new Parcelable.Creator<Button>() {
        @Override
        public Button createFromParcel(Parcel source) {
            return new Button(source);
        }

        @Override
        public Button[] newArray(int size) {
            return new Button[size];
        }
    };
}
