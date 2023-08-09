package com.glia.widgets.core.configuration;

import android.content.Intent;

import com.glia.androidsdk.screensharing.ScreenSharing;
import com.glia.widgets.GliaWidgets;
import com.glia.widgets.UiTheme;
import com.glia.widgets.chat.ChatType;
import com.glia.widgets.di.Dependencies;

public class GliaSdkConfiguration {
    private static final ChatType DEFAULT_CHAT_TYPE = ChatType.LIVE_CHAT;

    private final String companyName;
    private final String queueId;
    private final String contextAssetId;
    private final String contextUrl;
    private final UiTheme runTimeTheme;
    private final boolean useOverlay;
    private final ScreenSharing.Mode screenSharingMode;
    private final ChatType chatType;

    public String getCompanyName() {
        return this.companyName;
    }

    public String getQueueId() {
        return this.queueId;
    }

    @Deprecated
    public String getContextUrl() {
        return this.contextUrl;
    }

    public String getContextAssetId() {
        return this.contextAssetId;
    }

    public UiTheme getRunTimeTheme() {
        return this.runTimeTheme != null ? this.runTimeTheme : Dependencies.getSdkConfigurationManager().getUiTheme();
    }

    public boolean getUseOverlay() {
        return this.useOverlay;
    }

    public ScreenSharing.Mode getScreenSharingMode() {
        return screenSharingMode != null ? this.screenSharingMode : Dependencies.getSdkConfigurationManager().getScreenSharingMode();
    }

    public ChatType getChatType() {
        return chatType;
    }

    public static class Builder {
        private String companyName;
        private String queueId;
        private String contextAssetId;
        private String contextUrl;
        private UiTheme runTimeTheme;
        private boolean useOverlay;
        private ScreenSharing.Mode screenSharingMode;
        private ChatType chatType;

        public Builder companyName(String companyName) {
            this.companyName = companyName;
            return this;
        }

        public Builder queueId(String queueId) {
            this.queueId = queueId;
            return this;
        }

        @Deprecated
        public Builder contextUrl(String contextUrl) {
            this.contextUrl = contextUrl;
            return this;
        }

        public Builder contextAssetId(String contextAssetId) {
            this.contextAssetId = contextAssetId;
            return this;
        }

        public Builder runTimeTheme(UiTheme runTimeTheme) {
            this.runTimeTheme = runTimeTheme;
            return this;
        }

        public Builder useOverlay(boolean useOverlay) {
            this.useOverlay = useOverlay;
            return this;
        }

        public Builder screenSharingMode(ScreenSharing.Mode screenSharingMode) {
            this.screenSharingMode = screenSharingMode;
            return this;
        }

        public Builder intent(Intent intent) {
            this.companyName = Dependencies.getSdkConfigurationManager().getCompanyName();
            this.queueId = intent.getStringExtra(GliaWidgets.QUEUE_ID);
            UiTheme tempTheme = intent.getParcelableExtra(GliaWidgets.UI_THEME);
            this.runTimeTheme = tempTheme != null ? tempTheme : Dependencies.getSdkConfigurationManager().getUiTheme();
            this.contextAssetId = intent.getStringExtra(GliaWidgets.CONTEXT_ASSET_ID);
            this.useOverlay = intent.getBooleanExtra(GliaWidgets.USE_OVERLAY, Dependencies.getSdkConfigurationManager().isUseOverlay());
            ScreenSharing.Mode tempMode = intent.hasExtra(GliaWidgets.SCREEN_SHARING_MODE)
                    ? (ScreenSharing.Mode) intent.getSerializableExtra(GliaWidgets.SCREEN_SHARING_MODE)
                    : Dependencies.getSdkConfigurationManager().getScreenSharingMode();
            this.screenSharingMode = tempMode != null ? tempMode : Dependencies.getSdkConfigurationManager().getScreenSharingMode();
            this.chatType = intent.hasExtra(GliaWidgets.CHAT_TYPE)
                    ? intent.getParcelableExtra(GliaWidgets.CHAT_TYPE)
                    : DEFAULT_CHAT_TYPE;
            return this;
        }

        public GliaSdkConfiguration build() {
            return new GliaSdkConfiguration(this);
        }
    }

    private GliaSdkConfiguration(Builder builder) {
        this.companyName = builder.companyName;
        this.queueId = builder.queueId;
        this.contextAssetId = builder.contextAssetId;
        this.contextUrl = builder.contextUrl;
        this.runTimeTheme = builder.runTimeTheme;
        this.useOverlay = builder.useOverlay;
        this.screenSharingMode = builder.screenSharingMode;
        this.chatType = builder.chatType;
    }
}
