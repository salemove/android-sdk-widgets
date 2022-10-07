package com.glia.widgets.view.unifieduiconfig.component;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

public class Shadow implements Parcelable {
    @SerializedName("color")
    @Nullable
    private final ColorLayer color;

    @SerializedName("offset")
    @Nullable
    private final Double offset;

    @SerializedName("opacity")
    @Nullable
    private final Double opacity;

    @SerializedName("radius")
    @Nullable
    private final Double radius;

    @Nullable
    public ColorLayer getColor() {
        return color;
    }

    @Nullable
    public Double getOffset() {
        return offset;
    }

    @Nullable
    public Double getOpacity() {
        return opacity;
    }

    @Nullable
    public Double getRadius() {
        return radius;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.color, flags);
        dest.writeValue(this.offset);
        dest.writeValue(this.opacity);
        dest.writeValue(this.radius);
    }

    protected Shadow(Parcel in) {
        this.color = in.readParcelable(ColorLayer.class.getClassLoader());
        this.offset = (Double) in.readValue(Double.class.getClassLoader());
        this.opacity = (Double) in.readValue(Double.class.getClassLoader());
        this.radius = (Double) in.readValue(Double.class.getClassLoader());
    }

    public static final Parcelable.Creator<Shadow> CREATOR = new Parcelable.Creator<Shadow>() {
        @Override
        public Shadow createFromParcel(Parcel source) {
            return new Shadow(source);
        }

        @Override
        public Shadow[] newArray(int size) {
            return new Shadow[size];
        }
    };
}
