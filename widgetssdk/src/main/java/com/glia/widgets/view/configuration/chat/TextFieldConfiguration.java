package com.glia.widgets.view.configuration.chat;

import android.os.Parcel;
import android.os.Parcelable;

import com.glia.widgets.view.configuration.TextConfiguration;

public class TextFieldConfiguration implements Parcelable {
    public TextConfiguration text;
    public String tintColor;

    /* BEGIN: Parcelable related */
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.text, flags);
        dest.writeString(this.tintColor);
    }

    public void readFromParcel(Parcel source) {
        this.text = source.readParcelable(TextConfiguration.class.getClassLoader());
        this.tintColor = source.readString();
    }

    public TextFieldConfiguration() {
    }

    protected TextFieldConfiguration(Parcel in) {
        this.text = in.readParcelable(TextConfiguration.class.getClassLoader());
        this.tintColor = in.readString();
    }

    public static final Creator<TextFieldConfiguration> CREATOR = new Creator<TextFieldConfiguration>() {
        @Override
        public TextFieldConfiguration createFromParcel(Parcel source) {
            return new TextFieldConfiguration(source);
        }

        @Override
        public TextFieldConfiguration[] newArray(int size) {
            return new TextFieldConfiguration[size];
        }
    };
    /* END: Parcelable related */
}
