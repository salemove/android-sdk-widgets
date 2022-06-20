package com.glia.widgets.view.configuration.chat;

import android.os.Parcel;
import android.os.Parcelable;

import com.glia.widgets.view.configuration.LayerConfiguration;
import com.glia.widgets.view.configuration.TextConfiguration;
import com.google.gson.annotations.SerializedName;

public class NavigationBarConfiguration implements Parcelable {
    public LayerConfiguration layer;

    @SerializedName("title")
    public TextConfiguration text;


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.layer, flags);
        dest.writeParcelable(this.text, flags);
    }

    public void readFromParcel(Parcel source) {
        this.layer = source.readParcelable(LayerConfiguration.class.getClassLoader());
        this.text = source.readParcelable(TextConfiguration.class.getClassLoader());
    }

    public NavigationBarConfiguration() {
    }

    protected NavigationBarConfiguration(Parcel in) {
        this.layer = in.readParcelable(LayerConfiguration.class.getClassLoader());
        this.text = in.readParcelable(TextConfiguration.class.getClassLoader());
    }

    public static final Creator<NavigationBarConfiguration> CREATOR = new Creator<NavigationBarConfiguration>() {
        @Override
        public NavigationBarConfiguration createFromParcel(Parcel source) {
            return new NavigationBarConfiguration(source);
        }

        @Override
        public NavigationBarConfiguration[] newArray(int size) {
            return new NavigationBarConfiguration[size];
        }
    };
}
