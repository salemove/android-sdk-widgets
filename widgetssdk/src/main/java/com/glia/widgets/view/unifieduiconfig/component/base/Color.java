package com.glia.widgets.view.unifieduiconfig.component.base;

import android.os.Parcel;
import android.os.Parcelable;

import com.glia.widgets.view.unifieduiconfig.deserializer.ColorDeserializer;

/**
 * Represents ARGB color fields from remote config e.g. #FF44DD55
 * with the help of {@link ColorDeserializer} it guarantees
 * that {@link #getColor()} will return parsed valid color value
 */
public class Color implements Parcelable {
    private final int color;

    public Color(int color) {
        this.color = color;
    }

    public int getColor() {
        return color;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.color);
    }

    protected Color(Parcel in) {
        this.color = in.readInt();
    }

    public static final Parcelable.Creator<Color> CREATOR = new Parcelable.Creator<Color>() {
        @Override
        public Color createFromParcel(Parcel source) {
            return new Color(source);
        }

        @Override
        public Color[] newArray(int size) {
            return new Color[size];
        }
    };
}
