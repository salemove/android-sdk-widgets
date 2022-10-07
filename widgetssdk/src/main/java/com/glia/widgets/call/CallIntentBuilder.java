package com.glia.widgets.call;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.glia.widgets.GliaWidgets;
import com.glia.widgets.core.configuration.GliaSdkConfiguration;

class CallIntentBuilder {
    private final Context context;
    private Configuration configuration;

    private CallIntentBuilder(@NonNull Context context) {
        this.context = context;
    }

    public static CallIntentBuilder from(@NonNull Context context) {
        return new CallIntentBuilder(context);
    }

    public CallIntentBuilder setConfiguration(@NonNull Configuration configuration) {
        this.configuration = configuration;
        return this;
    }

    public Intent getIntent() {
        validateActivityConfiguration();
        validateWidgetsSdkConfiguration();
        GliaSdkConfiguration sdkConfiguration = configuration.getSdkConfiguration();
        return new Intent(context, CallActivity.class)
                .putExtra(GliaWidgets.COMPANY_NAME, sdkConfiguration.getCompanyName())
                .putExtra(GliaWidgets.QUEUE_ID, sdkConfiguration.getQueueId())
                .putExtra(GliaWidgets.CONTEXT_ASSET_ID, sdkConfiguration.getContextAssetId())
                .putExtra(GliaWidgets.REMOTE_CONFIGURATION, sdkConfiguration.getRemoteConfiguration())
                .putExtra(GliaWidgets.UI_THEME, sdkConfiguration.getRunTimeTheme())
                .putExtra(GliaWidgets.USE_OVERLAY, sdkConfiguration.getUseOverlay())
                .putExtra(GliaWidgets.SCREEN_SHARING_MODE, sdkConfiguration.getScreenSharingMode())
                .putExtra(GliaWidgets.MEDIA_TYPE, configuration.getMediaType())
                .putExtra(GliaWidgets.IS_UPGRADE_TO_CALL, configuration.getIsUpgradeToCall());
    }

    private void validateActivityConfiguration() {
        if (configuration == null) {
            throw new RuntimeException("Configuration missing");
        }
    }

    private void validateWidgetsSdkConfiguration() {
        GliaSdkConfiguration sdkConfiguration = configuration.getSdkConfiguration();
        if (sdkConfiguration == null) {
            throw new RuntimeException("WidgetsSdk Configuration missing");
        }
    }
}
