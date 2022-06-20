package com.glia.widgets.view.configuration.chat;

import android.os.Parcel;
import android.os.Parcelable;

import com.glia.widgets.view.configuration.TextConfiguration;

public class WelcomeViewConfiguration implements Parcelable {
    public String tintColor;
    public TextConfiguration title;
    public TextConfiguration description;
    public String descriptionValue;
    public String titleValue;

    /* BEGIN: Parcelable related */
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.tintColor);
        dest.writeParcelable(this.title, flags);
        dest.writeParcelable(this.description, flags);
        dest.writeString(this.descriptionValue);
        dest.writeString(this.titleValue);
    }

    public void readFromParcel(Parcel source) {
        this.tintColor = source.readString();
        this.title = source.readParcelable(TextConfiguration.class.getClassLoader());
        this.description = source.readParcelable(TextConfiguration.class.getClassLoader());
        this.descriptionValue = source.readString();
        this.titleValue = source.readString();
    }

    public WelcomeViewConfiguration() {
    }

    protected WelcomeViewConfiguration(Parcel in) {
        this.tintColor = in.readString();
        this.title = in.readParcelable(TextConfiguration.class.getClassLoader());
        this.description = in.readParcelable(TextConfiguration.class.getClassLoader());
        this.descriptionValue = in.readString();
        this.titleValue = in.readString();
    }

    public static final Creator<WelcomeViewConfiguration> CREATOR = new Creator<WelcomeViewConfiguration>() {
        @Override
        public WelcomeViewConfiguration createFromParcel(Parcel source) {
            return new WelcomeViewConfiguration(source);
        }

        @Override
        public WelcomeViewConfiguration[] newArray(int size) {
            return new WelcomeViewConfiguration[size];
        }
    };
    /* END: Parcelable related */
}
