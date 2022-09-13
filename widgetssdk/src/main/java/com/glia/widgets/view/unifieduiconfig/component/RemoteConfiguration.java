package com.glia.widgets.view.unifieduiconfig.component;

import androidx.annotation.Nullable;

import com.glia.widgets.view.unifieduiconfig.component.call.CallScreenStyle;
import com.glia.widgets.view.unifieduiconfig.component.chat.ChatScreenStyle;
import com.google.gson.annotations.SerializedName;

public class RemoteConfiguration {

    @SerializedName("chatScreen")
    @Nullable
    private ChatScreenStyle chatScreenStyle;

    @SerializedName("callScreen")
    @Nullable
    private CallScreenStyle callScreenStyle;

    @Nullable
    public ChatScreenStyle getChatScreenStyle() {
        return chatScreenStyle;
    }

    @Nullable
    public CallScreenStyle getCallScreenStyle() {
        return callScreenStyle;
    }
}
