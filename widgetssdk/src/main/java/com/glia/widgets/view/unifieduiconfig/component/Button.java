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
    private final Layer background;

    @SerializedName("tintColor")
    @Nullable
    private final ColorLayer tintColor;

    @SerializedName("shadow")
    @Nullable
    private final Shadow shadow;

    @Nullable
    public Text getText() {
        return text;
    }

    @Nullable
    public Layer getBackground() {
        return background;
    }

    @Nullable
    public ColorLayer getTintColor() {
        return tintColor;
    }

    @Nullable
    public Shadow getShadow() {
        return shadow;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.text, flags);
        dest.writeParcelable(this.background, flags);
        dest.writeParcelable(this.tintColor, flags);
        dest.writeParcelable(this.shadow, flags);
    }

    protected Button(Parcel in) {
        this.text = in.readParcelable(Text.class.getClassLoader());
        this.background = in.readParcelable(Layer.class.getClassLoader());
        this.tintColor = in.readParcelable(ColorLayer.class.getClassLoader());
        this.shadow = in.readParcelable(Shadow.class.getClassLoader());
    }

    public static final Creator<Button> CREATOR = new Creator<Button>() {
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
