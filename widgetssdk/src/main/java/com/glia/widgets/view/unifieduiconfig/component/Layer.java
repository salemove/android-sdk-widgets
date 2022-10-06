package com.glia.widgets.view.unifieduiconfig.component;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

import com.glia.widgets.view.unifieduiconfig.component.base.Size;
import com.google.gson.annotations.SerializedName;

public class Layer implements Parcelable {

    @SerializedName("color")
    @Nullable
    private final ColorLayer color;

    @SerializedName("border")
    @Nullable
    private final ColorLayer borderColor;

    @SerializedName("borderWidth")
    @Nullable
    private final Size.Dp borderWidth;

    @SerializedName("cornerRadius")
    @Nullable
    private final Size.Dp cornerRadius;

    @Nullable
    public ColorLayer getColor() {
        return color;
    }

    @Nullable
    public ColorLayer getBorderColor() {
        return borderColor;
    }

    @Nullable
    public Size.Dp getBorderWidth() {
        return borderWidth;
    }

    @Nullable
    public Size.Dp getCornerRadius() {
        return cornerRadius;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.color, flags);
        dest.writeParcelable(this.borderColor, flags);
        dest.writeParcelable(this.borderWidth, flags);
        dest.writeParcelable(this.cornerRadius, flags);
    }

    protected Layer(Parcel in) {
        this.color = in.readParcelable(ColorLayer.class.getClassLoader());
        this.borderColor = in.readParcelable(ColorLayer.class.getClassLoader());
        this.borderWidth = in.readParcelable(Size.Dp.class.getClassLoader());
        this.cornerRadius = in.readParcelable(Size.Dp.class.getClassLoader());
    }

    public static final Parcelable.Creator<Layer> CREATOR = new Parcelable.Creator<Layer>() {
        @Override
        public Layer createFromParcel(Parcel source) {
            return new Layer(source);
        }

        @Override
        public Layer[] newArray(int size) {
            return new Layer[size];
        }
    };
}
