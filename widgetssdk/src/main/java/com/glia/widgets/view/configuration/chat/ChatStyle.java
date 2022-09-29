package com.glia.widgets.view.configuration.chat;

import android.os.Parcel;
import android.os.Parcelable;

public class ChatStyle implements Parcelable {
    // TODO: will be done in the next task.
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

    public ChatStyle() {
    }

    protected ChatStyle(Parcel in) {
    }

    public static final Parcelable.Creator<ChatStyle> CREATOR = new Parcelable.Creator<ChatStyle>() {
        @Override
        public ChatStyle createFromParcel(Parcel source) {
            return new ChatStyle(source);
        }

        @Override
        public ChatStyle[] newArray(int size) {
            return new ChatStyle[size];
        }
    };
}
