package com.glia.widgets.core.configuration;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

import com.glia.androidsdk.screensharing.ScreenSharing;
import com.glia.widgets.GliaWidgetsConfig;
import com.glia.widgets.UiTheme;
import com.glia.widgets.di.Dependencies;
import com.glia.widgets.helper.Logger;
import com.glia.widgets.helper.ResourceProvider;

import org.jetbrains.annotations.Nullable;

/**
 * @hide
 */
public class GliaSdkConfigurationManager {

    private static final String TAG = GliaSdkConfigurationManager.class.getSimpleName();

    private ScreenSharing.Mode screenSharingMode = null;
    private String companyName = null;
    private String legacyCompanyName = null;
    private boolean enableBubbleOutsideApp = true; // default values
    private boolean enableBubbleInsideApp = true; // default values

    private UiTheme uiTheme = null;

    public void fromConfiguration(@NonNull GliaWidgetsConfig configuration) {
        this.screenSharingMode = configuration.screenSharingMode;
        this.companyName = configuration.companyName;
        this.uiTheme = configuration.uiTheme;

        Boolean enableBubbleOutsideApp = configuration.enableBubbleOutsideApp;
        if (enableBubbleOutsideApp != null) {
            this.enableBubbleOutsideApp = enableBubbleOutsideApp;
        }

        Boolean enableBubbleInsideApp = configuration.enableBubbleInsideApp;
        if (enableBubbleInsideApp != null) {
            this.enableBubbleInsideApp = enableBubbleInsideApp;
        }
    }

    public boolean isEnableBubbleOutsideApp() {
        return this.enableBubbleOutsideApp;
    }

    public boolean isEnableBubbleInsideApp() {
        return this.enableBubbleInsideApp;
    }

    /**
     * @deprecated Should be removed together with GliaWidgetsConfig.USE_OVERLAY
     */
    @Deprecated
    public void setLegacyUseOverlay(boolean useOverlay) {
        Logger.logDeprecatedMethodUse(TAG, "setLegacyUseOverlay()");
        this.enableBubbleOutsideApp = useOverlay;
        this.enableBubbleInsideApp = useOverlay;
    }

    public void setLegacyCompanyName(String companyName) {
        this.legacyCompanyName = companyName;
    }

    public String getCompanyName() {
        if (companyName == null && legacyCompanyName != null) {
            // Legacy company name configuration method used before local default
            companyName = legacyCompanyName;
        }
        return companyName;
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
    public EngagementConfiguration buildEngagementConfiguration() {
        return new EngagementConfiguration(
            companyName,
            null,
            null,
            null,
            uiTheme,
            screenSharingMode
        );
    }
}
