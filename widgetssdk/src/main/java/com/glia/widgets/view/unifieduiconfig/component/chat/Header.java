package com.glia.widgets.view.unifieduiconfig.component.chat;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

import com.glia.widgets.view.unifieduiconfig.component.Layer;
import com.glia.widgets.view.unifieduiconfig.component.Text;
import com.google.gson.annotations.SerializedName;

public class Header implements Parcelable {

    @SerializedName("text")
    @Nullable
    private final Text text;

    @SerializedName("background")
    @Nullable
    private final Layer color;

    @Nullable
    public Text getText() {
        return text;
    }

    @Nullable
    public Layer getColor() {
        return color;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.text, flags);
        dest.writeParcelable(this.color, flags);
    }

    protected Header(Parcel in) {
        this.text = in.readParcelable(Text.class.getClassLoader());
        this.color = in.readParcelable(Layer.class.getClassLoader());
    }

    public static final Parcelable.Creator<Header> CREATOR = new Parcelable.Creator<Header>() {
        @Override
        public Header createFromParcel(Parcel source) {
            return new Header(source);
        }

        @Override
        public Header[] newArray(int size) {
            return new Header[size];
        }
    };
}
