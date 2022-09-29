package com.glia.widgets.view.unifieduiconfig.component.base;

import android.graphics.Typeface;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

/**
 * Represents text style from remote config
 */
public class TextStyle implements Parcelable {

    private final int style;

    public TextStyle(int style) {
        this.style = style;
    }

    @NonNull
    public Typeface applyTo(Typeface typeface) {
        return Typeface.create(typeface, style);
    }

    public int getStyle() {
        return style;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.style);
    }

    protected TextStyle(Parcel in) {
        this.style = in.readInt();
    }

    public static final Parcelable.Creator<TextStyle> CREATOR = new Parcelable.Creator<TextStyle>() {
        @Override
        public TextStyle createFromParcel(Parcel source) {
            return new TextStyle(source);
        }

        @Override
        public TextStyle[] newArray(int size) {
            return new TextStyle[size];
        }
    };
}
