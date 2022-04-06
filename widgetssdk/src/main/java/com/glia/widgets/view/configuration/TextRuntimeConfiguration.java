package com.glia.widgets.view.configuration;

import android.os.Parcel;
import android.os.Parcelable;

public class TextRuntimeConfiguration implements Parcelable {
    public FontConfiguration font;
    public String normalColor;
    public String highlightedColor;

    public TextRuntimeConfiguration(FontConfiguration font, String normalColor, String highlightedColor) {
        this.font = font;
        this.normalColor = normalColor;
        this.highlightedColor = highlightedColor;
    }

    /* BEGIN: Parcelable related */
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.font, flags);
        dest.writeString(this.normalColor);
        dest.writeString(this.highlightedColor);
    }

    public void readFromParcel(Parcel source) {
        this.font = source.readParcelable(FontConfiguration.class.getClassLoader());
        this.normalColor = source.readString();
        this.highlightedColor = source.readString();
    }

    protected TextRuntimeConfiguration(Parcel in) {
        this.font = in.readParcelable(FontConfiguration.class.getClassLoader());
        this.normalColor = in.readString();
        this.highlightedColor = in.readString();
    }

    public static final Creator<TextRuntimeConfiguration> CREATOR = new Creator<TextRuntimeConfiguration>() {
        @Override
        public TextRuntimeConfiguration createFromParcel(Parcel source) {
            return new TextRuntimeConfiguration(source);
        }

        @Override
        public TextRuntimeConfiguration[] newArray(int size) {
            return new TextRuntimeConfiguration[size];
        }
    };
    /* END: Parcelable related */
}
