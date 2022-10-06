package com.glia.widgets.view.configuration.call;

import android.os.Parcel;
import android.os.Parcelable;

public class CallStyle implements Parcelable {
    // TODO: will be done in the next task
    // The idea is to move here corresponding fields from the UiTheme.

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }

    public void readFromParcel(Parcel source) {
    }

    public CallStyle() {
    }

    protected CallStyle(Parcel in) {
    }

    public static final Parcelable.Creator<CallStyle> CREATOR = new Parcelable.Creator<CallStyle>() {
        @Override
        public CallStyle createFromParcel(Parcel source) {
            return new CallStyle(source);
        }

        @Override
        public CallStyle[] newArray(int size) {
            return new CallStyle[size];
        }
    };
}
