package com.glia.widgets.core.configuration;

import com.glia.androidsdk.screensharing.ScreenSharing;
import com.glia.widgets.R;
import com.glia.widgets.StringProvider;
import com.glia.widgets.UiTheme;

import org.jetbrains.annotations.Nullable;

public class GliaSdkConfigurationManager {

    StringProvider stringProvider;

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
        fetchRemoteCompanyName();
        return companyName;
    }

    private void fetchRemoteCompanyName() {
        if (stringProvider == null) {
            return;
        }
        String remoteCompanyName = stringProvider.getRemoteString(R.string.general_company_name);
        if (remoteCompanyName != null && !remoteCompanyName.isEmpty()) {
            companyName = remoteCompanyName;
        }
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
        return screenSharingMode;
    }

    public void setScreenSharingMode(ScreenSharing.Mode screenSharingMode) {
        this.screenSharingMode = screenSharingMode;
    }

    @Nullable
    public GliaSdkConfiguration createWidgetsConfiguration() {
        return new GliaSdkConfiguration.Builder()
                .companyName(companyName)
                .screenSharingMode(screenSharingMode)
                .useOverlay(useOverlay)
                .runTimeTheme(uiTheme)
                .build();
    }

    public void setStringProvider(StringProvider stringProvider) {
        this.stringProvider = stringProvider;
    }
}
