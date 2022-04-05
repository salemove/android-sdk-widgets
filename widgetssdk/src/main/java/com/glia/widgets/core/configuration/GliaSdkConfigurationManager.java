package com.glia.widgets.core.configuration;

import com.glia.androidsdk.screensharing.ScreenSharing;

public class GliaSdkConfigurationManager {

    private boolean useOverlay = false;
    private ScreenSharing.Mode screenSharingMode = null;

    public boolean isUseOverlay() {
        return this.useOverlay;
    }

    public void setUseOverlay(boolean enabled) {
        this.useOverlay = enabled;
    }

    public ScreenSharing.Mode getScreenSharingMode() {
        return screenSharingMode;
    }

    public void setScreenSharingMode(ScreenSharing.Mode screenSharingMode) {
        this.screenSharingMode = screenSharingMode;
    }
}
