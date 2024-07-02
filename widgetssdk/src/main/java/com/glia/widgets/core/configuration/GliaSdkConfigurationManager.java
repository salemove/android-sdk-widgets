package com.glia.widgets.core.configuration;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

import com.glia.androidsdk.Glia;
import com.glia.androidsdk.screensharing.ScreenSharing;
import com.glia.widgets.GliaWidgetsConfig;
import com.glia.widgets.R;
import com.glia.widgets.UiTheme;
import com.glia.widgets.di.Dependencies;
import com.glia.widgets.helper.ResourceProvider;

import org.jetbrains.annotations.Nullable;

/**
 * @hide
 */
public class GliaSdkConfigurationManager {

    private ScreenSharing.Mode screenSharingMode = null;
    private String companyName = null;
    private String legacyCompanyName = null;
    private boolean useOverlay = true;

    private UiTheme uiTheme = null;

    public void fromConfiguration(@NonNull GliaWidgetsConfig configuration) {
        this.screenSharingMode = configuration.screenSharingMode;
        this.companyName = configuration.companyName;
        this.uiTheme = configuration.uiTheme;

        Boolean useOverlay = configuration.isUseOverlay();
        if (useOverlay != null) {
            this.useOverlay = useOverlay;
        }
    }

    public boolean isUseOverlay() {
        return this.useOverlay;
    }

    public void setUseOverlay(boolean useOverlay) {
        this.useOverlay = useOverlay;
    }

    public void setLegacyCompanyName(String companyName) {
        this.legacyCompanyName = companyName;
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
        } else if (legacyCompanyName != null) {
            // Legacy company name configuration method used before local default
            companyName = legacyCompanyName;
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
            Dependencies.getStringProvider().reportImproperInitialisation(e);
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
