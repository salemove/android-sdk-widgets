package com.glia.widgets.view.unifieduiconfig.component.call;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

public class BarButtonStates implements Parcelable {

    @SerializedName("inactive")
    @Nullable
    private final BarButtonStyle inactive;

    @SerializedName("active")
    @Nullable
    private final BarButtonStyle active;

    @Nullable
    public BarButtonStyle getInactive() {
        return inactive;
    }

    @Nullable
    public BarButtonStyle getActive() {
        return active;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.inactive, flags);
        dest.writeParcelable(this.active, flags);
    }

    protected BarButtonStates(Parcel in) {
        this.inactive = in.readParcelable(BarButtonStyle.class.getClassLoader());
        this.active = in.readParcelable(BarButtonStyle.class.getClassLoader());
    }

    public static final Parcelable.Creator<BarButtonStates> CREATOR = new Parcelable.Creator<BarButtonStates>() {
        @Override
        public BarButtonStates createFromParcel(Parcel source) {
            return new BarButtonStates(source);
        }

        @Override
        public BarButtonStates[] newArray(int size) {
            return new BarButtonStates[size];
        }
    };
}
