package com.glia.widgets.view.unifieduiconfig.component;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

import com.glia.widgets.view.unifieduiconfig.component.call.CallScreenStyle;
import com.glia.widgets.view.unifieduiconfig.component.chat.ChatScreenStyle;
import com.google.gson.annotations.SerializedName;

public class RemoteConfiguration implements Parcelable {

    @SerializedName("chatScreen")
    @Nullable
    private final ChatScreenStyle chatScreenStyle;

    @SerializedName("callScreen")
    @Nullable
    private final CallScreenStyle callScreenStyle;

    @Nullable
    public ChatScreenStyle getChatScreenStyle() {
        return chatScreenStyle;
    }

    @Nullable
    public CallScreenStyle getCallScreenStyle() {
        return callScreenStyle;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.chatScreenStyle, flags);
        dest.writeParcelable(this.callScreenStyle, flags);
    }

    protected RemoteConfiguration(Parcel in) {
        this.chatScreenStyle = in.readParcelable(ChatScreenStyle.class.getClassLoader());
        this.callScreenStyle = in.readParcelable(CallScreenStyle.class.getClassLoader());
    }

    public static final Creator<RemoteConfiguration> CREATOR = new Creator<RemoteConfiguration>() {
        @Override
        public RemoteConfiguration createFromParcel(Parcel source) {
            return new RemoteConfiguration(source);
        }

        @Override
        public RemoteConfiguration[] newArray(int size) {
            return new RemoteConfiguration[size];
        }
    };
}
