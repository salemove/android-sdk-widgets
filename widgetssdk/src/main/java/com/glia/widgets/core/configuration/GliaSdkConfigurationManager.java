package com.glia.widgets.core.configuration;

import static com.glia.widgets.core.configuration.GliaSdkConfiguration.DEFAULT_SCREEN_SHARING_MODE;

import com.glia.androidsdk.screensharing.ScreenSharing;
import com.glia.widgets.UiTheme;

public class GliaSdkConfigurationManager {

    private boolean useOverlay = false;
    private ScreenSharing.Mode screenSharingMode = null;
    private String companyName = null;

    private UiTheme uiTheme = null;

    public boolean isUseOverlay() {
        return this.useOverlay;
    }

    public void setUseOverlay(boolean enabled) {
        this.useOverlay = enabled;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public UiTheme getUiTheme() {
        return uiTheme;
    }

    public void setUiTheme(UiTheme uiTheme) {
        this.uiTheme = uiTheme;
    }

    public ScreenSharing.Mode getScreenSharingMode() {
        if (screenSharingMode == null) {
            return DEFAULT_SCREEN_SHARING_MODE;
        } else {
            return screenSharingMode;
        }
    }

    public void setScreenSharingMode(ScreenSharing.Mode screenSharingMode) {
        this.screenSharingMode = screenSharingMode;
    }
}
