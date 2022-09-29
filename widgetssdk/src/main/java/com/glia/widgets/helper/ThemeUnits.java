package com.glia.widgets.helper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.glia.widgets.UiTheme;
import com.glia.widgets.view.unifieduiconfig.component.RemoteConfiguration;
import com.glia.widgets.view.unifieduiconfig.component.call.CallScreenStyle;
import com.glia.widgets.view.unifieduiconfig.component.chat.ChatScreenStyle;

public class ThemeUnits {

    @Nullable
    public static UiTheme apply(@Nullable UiTheme uiTheme, @Nullable RemoteConfiguration remoteConfiguration) {
        if (uiTheme == null && remoteConfiguration == null) {
            return null;
        }
        UiTheme.UiThemeBuilder builder = new UiTheme.UiThemeBuilder();
        if (uiTheme != null) {
            builder.setTheme(uiTheme);
        }

        if (remoteConfiguration != null) {
            applyChatScreen(remoteConfiguration, builder);
            applyCallScreen(remoteConfiguration, builder);
        }

        return builder.build();
    }

    private static void applyChatScreen(@NonNull RemoteConfiguration remoteConfiguration,
                                        @NonNull UiTheme.UiThemeBuilder builder) {
        ChatScreenStyle chatScreenStyle = remoteConfiguration.getChatScreenStyle();
        if (chatScreenStyle == null) {
            return;
        }
        // TODO: will be done in the next task
    }

    private static void applyCallScreen(@NonNull RemoteConfiguration remoteConfiguration,
                                        @NonNull UiTheme.UiThemeBuilder builder) {
        CallScreenStyle callScreenStyle = remoteConfiguration.getCallScreenStyle();
        if (callScreenStyle != null) {
            return;
        }
        // TODO: will be done in the next task
    }
}
