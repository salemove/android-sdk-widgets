package com.glia.widgets.view.configuration.chat;

import android.os.Parcel;
import android.os.Parcelable;

import com.glia.widgets.view.configuration.LayerConfiguration;
import com.glia.widgets.view.configuration.TextConfiguration;

public class OperatorMessageConfiguration implements Parcelable {
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

    public OperatorMessageConfiguration() {
    }

    protected OperatorMessageConfiguration(Parcel in) {
        this.layer = in.readParcelable(LayerConfiguration.class.getClassLoader());
        this.text = in.readParcelable(TextConfiguration.class.getClassLoader());
    }

    public static final Creator<OperatorMessageConfiguration> CREATOR = new Creator<OperatorMessageConfiguration>() {
        @Override
        public OperatorMessageConfiguration createFromParcel(Parcel source) {
            return new OperatorMessageConfiguration(source);
        }

        @Override
        public OperatorMessageConfiguration[] newArray(int size) {
            return new OperatorMessageConfiguration[size];
        }
    };
    /* END: Parcelable related */
}
