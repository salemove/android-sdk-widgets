package com.glia.widgets.core.configuration;

import androidx.annotation.VisibleForTesting;

import com.glia.androidsdk.Glia;
import com.glia.androidsdk.screensharing.ScreenSharing;
import com.glia.widgets.R;
import com.glia.widgets.UiTheme;
import com.glia.widgets.di.Dependencies;
import com.glia.widgets.helper.Logger;
import com.glia.widgets.helper.ResourceProvider;

import org.jetbrains.annotations.Nullable;

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

    /** @noinspection StatementWithEmptyBody*/
    public String getCompanyName() {
        String remoteCompanyName = fetchRemoteCompanyName();
        boolean isCompanyNameSetFromGliaHub = remoteCompanyName != null && !remoteCompanyName.trim().isEmpty();

        if (isCompanyNameSetFromGliaHub) {
            // Apply company name from Glia Hub
            companyName = remoteCompanyName;
        } else if (isCompanyNameSetFromWidgetsConfig()){
            // No need to replace company name. Continue using it.
        } else {
            // Company name was not set neither from Glia Hub nor from GliaWidgetsConfig.
            // Apply local default company name.
            companyName = getResourceProvider().getString(R.string.general_company_name);
        }
        return companyName;
    }

    @VisibleForTesting
    public boolean isCompanyNameSetFromWidgetsConfig() {
        return companyName != null;
    }

    @VisibleForTesting
    public @Nullable String fetchRemoteCompanyName() {
        String remoteCompanyName = null;
        try {
            remoteCompanyName = Glia.getRemoteString(getResourceProvider().getResourceKey(R.string.general_company_name));
        } catch (Exception e) {
            // Falling back on SDK configuration
            Logger.e("StringProvider", "**** ATTENTION **** \n An engagement view was opened immediately after Glia was initialized. \n It is strongly suggested to keep the initialization and actual engagement start separated by a little more time to allow custom locales feature to work properly.\n For further information See the  Custom Locales migration guide", e);
        }
        return remoteCompanyName;
    }

    @VisibleForTesting
    public ResourceProvider getResourceProvider() {
        return Dependencies.getResourceProvider();
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
}
