package com.glia.widgets.core.configuration;

import android.content.Intent;

import com.glia.androidsdk.screensharing.ScreenSharing;
import com.glia.widgets.GliaWidgets;
import com.glia.widgets.UiTheme;
import com.glia.widgets.view.unifieduiconfig.component.RemoteConfiguration;

public class GliaSdkConfiguration {

    private static final boolean DEFAULT_USE_OVERLAY = true;
    private static final ScreenSharing.Mode DEFAULT_SCREEN_SHARING_MODE =
            ScreenSharing.Mode.UNBOUNDED;

    private final String companyName;
    private final String queueId;
    private final String contextAssetId;
    @Deprecated
    private final String contextUrl;
    private final RemoteConfiguration remoteConfiguration;
    private final UiTheme runTimeTheme;
    private final boolean useOverlay;
    private final ScreenSharing.Mode screenSharingMode;

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

    public RemoteConfiguration getRemoteConfiguration() {
        return remoteConfiguration;
    }

    public UiTheme getRunTimeTheme() {
        return this.runTimeTheme;
    }

    public boolean getUseOverlay() {
        return this.useOverlay;
    }

    public ScreenSharing.Mode getScreenSharingMode() {
        return screenSharingMode;
    }

    public static class Builder {
        private String companyName;
        private String queueId;
        private String contextAssetId;
        @Deprecated
        private String contextUrl;
        private RemoteConfiguration remoteConfiguration;
        private UiTheme runTimeTheme;
        private boolean useOverlay;
        private ScreenSharing.Mode screenSharingMode;

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
            this.companyName = intent.getStringExtra(GliaWidgets.COMPANY_NAME);
            this.queueId = intent.getStringExtra(GliaWidgets.QUEUE_ID);
            this.remoteConfiguration = intent.getParcelableExtra(GliaWidgets.REMOTE_CONFIGURATION);
            this.runTimeTheme = intent.getParcelableExtra(GliaWidgets.UI_THEME);
            this.contextAssetId = intent.getStringExtra(GliaWidgets.CONTEXT_ASSET_ID);
            this.useOverlay = intent.getBooleanExtra(GliaWidgets.USE_OVERLAY, DEFAULT_USE_OVERLAY);
            this.screenSharingMode = intent.hasExtra(GliaWidgets.SCREEN_SHARING_MODE)
                    ? (ScreenSharing.Mode) intent.getSerializableExtra(GliaWidgets.SCREEN_SHARING_MODE)
                    : DEFAULT_SCREEN_SHARING_MODE;
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
        this.remoteConfiguration = builder.remoteConfiguration;
        this.runTimeTheme = builder.runTimeTheme;
        this.useOverlay = builder.useOverlay;
        this.screenSharingMode = builder.screenSharingMode;
    }
}
