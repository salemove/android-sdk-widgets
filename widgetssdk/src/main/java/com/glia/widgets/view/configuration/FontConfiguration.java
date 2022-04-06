package com.glia.widgets.view.configuration;

import android.os.Parcel;
import android.os.Parcelable;

public class FontConfiguration implements Parcelable {
    public enum FontSize {
        BODY, HEADER, CAPTION
    }

    public enum FontWeight {
        LIGHT, BOLD, REGULAR, MEDIUM
    }

    public FontSize size;
    public FontWeight fontWeight;

    public FontConfiguration(FontSize size, FontWeight fontWeight) {
        this.size = size;
        this.fontWeight = fontWeight;
    }

    /* BEGIN: Parcelable related */
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.size == null ? -1 : this.size.ordinal());
        dest.writeInt(this.fontWeight == null ? -1 : this.fontWeight.ordinal());
    }

    public void readFromParcel(Parcel source) {
        int tmpSize = source.readInt();
        this.size = tmpSize == -1 ? null : FontSize.values()[tmpSize];
        int tmpFontWeight = source.readInt();
        this.fontWeight = tmpFontWeight == -1 ? null : FontWeight.values()[tmpFontWeight];
    }

    protected FontConfiguration(Parcel in) {
        int tmpSize = in.readInt();
        this.size = tmpSize == -1 ? null : FontSize.values()[tmpSize];
        int tmpFontWeight = in.readInt();
        this.fontWeight = tmpFontWeight == -1 ? null : FontWeight.values()[tmpFontWeight];
    }

    public static final Creator<FontConfiguration> CREATOR = new Creator<FontConfiguration>() {
        @Override
        public FontConfiguration createFromParcel(Parcel source) {
            return new FontConfiguration(source);
        }

        @Override
        public FontConfiguration[] newArray(int size) {
            return new FontConfiguration[size];
        }
    };
    /* END: Parcelable related */
}
