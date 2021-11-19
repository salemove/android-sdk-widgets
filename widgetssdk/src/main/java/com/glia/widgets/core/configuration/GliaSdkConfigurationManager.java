package com.glia.widgets.core.configuration;

public class GliaSdkConfigurationManager {
    private static final String TAG = GliaSdkConfigurationManager.class.getSimpleName();

    private boolean useOverlay = true;

    public GliaSdkConfigurationManager() {
    }

    public boolean isUseOverlay() {
        return this.useOverlay;
    }

    public void setUseOverlay(boolean enabled) {
        this.useOverlay = enabled;
    }
}
