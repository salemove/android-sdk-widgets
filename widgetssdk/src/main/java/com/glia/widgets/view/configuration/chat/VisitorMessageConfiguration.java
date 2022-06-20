package com.glia.widgets.view.configuration.chat;

import android.os.Parcel;
import android.os.Parcelable;

import com.glia.widgets.view.configuration.LayerConfiguration;
import com.glia.widgets.view.configuration.TextConfiguration;

public class VisitorMessageConfiguration implements Parcelable {
    public LayerConfiguration layer;
    public TextConfiguration text;

    /* BEGIN: Parcelable related */
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

    public VisitorMessageConfiguration() {
    }

    protected VisitorMessageConfiguration(Parcel in) {
        this.layer = in.readParcelable(LayerConfiguration.class.getClassLoader());
        this.text = in.readParcelable(TextConfiguration.class.getClassLoader());
    }

    public static final Creator<VisitorMessageConfiguration> CREATOR = new Creator<VisitorMessageConfiguration>() {
        @Override
        public VisitorMessageConfiguration createFromParcel(Parcel source) {
            return new VisitorMessageConfiguration(source);
        }

        @Override
        public VisitorMessageConfiguration[] newArray(int size) {
            return new VisitorMessageConfiguration[size];
        }
    };
    /* END: Parcelable related */
}
