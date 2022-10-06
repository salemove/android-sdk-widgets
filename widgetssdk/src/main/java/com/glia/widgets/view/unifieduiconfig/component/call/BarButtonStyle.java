package com.glia.widgets.view.unifieduiconfig.component.call;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

import com.glia.widgets.view.unifieduiconfig.component.ColorLayer;
import com.glia.widgets.view.unifieduiconfig.component.Text;
import com.google.gson.annotations.SerializedName;

public class BarButtonStyle implements Parcelable {

    @SerializedName("background")
    @Nullable
    private final ColorLayer background;

    @SerializedName("imageColor")
    @Nullable
    private final ColorLayer imageColor;

    @SerializedName("title")
    @Nullable
    private final Text title;

    @Nullable
    public ColorLayer getBackground() {
        return background;
    }

    @Nullable
    public ColorLayer getImageColor() {
        return imageColor;
    }

    @Nullable
    public Text getTitle() {
        return title;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.background, flags);
        dest.writeParcelable(this.imageColor, flags);
        dest.writeParcelable(this.title, flags);
    }

    protected BarButtonStyle(Parcel in) {
        this.background = in.readParcelable(ColorLayer.class.getClassLoader());
        this.imageColor = in.readParcelable(ColorLayer.class.getClassLoader());
        this.title = in.readParcelable(Text.class.getClassLoader());
    }

    public static final Parcelable.Creator<BarButtonStyle> CREATOR = new Parcelable.Creator<BarButtonStyle>() {
        @Override
        public BarButtonStyle createFromParcel(Parcel source) {
            return new BarButtonStyle(source);
        }

        @Override
        public BarButtonStyle[] newArray(int size) {
            return new BarButtonStyle[size];
        }
    };
}
