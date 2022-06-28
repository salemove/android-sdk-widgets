package com.glia.widgets.call;

import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.glia.androidsdk.Engagement;
import com.glia.widgets.GliaWidgets;
import com.glia.widgets.core.configuration.GliaSdkConfiguration;

class CallIntentReader {
    private final AppCompatActivity activity;

    private CallIntentReader(@NonNull AppCompatActivity activity) {
        this.activity = activity;
    }

    public CallActivity.Configuration getConfiguration() {
        return CallActivity.Configuration.Builder
                .builder()
                .setWidgetsConfiguration(getSdkConfiguration())
                .setMediaType(getMediaType())
                .setIsUpgradeToCall(getIsUpgradeToCall())
                .build();
    }

    public static CallIntentReader from(@NonNull AppCompatActivity activity) {
        return new CallIntentReader(activity);
    }

    @NonNull
    private Intent getIntent() {
        return activity.getIntent();
    }

    @Nullable
    private GliaSdkConfiguration getSdkConfiguration() {
        return new GliaSdkConfiguration
                .Builder()
                .intent(getIntent())
                .build();
    }

    private Engagement.MediaType getMediaType() {
        if (getIntent().hasExtra(GliaWidgets.MEDIA_TYPE)) {
            return (Engagement.MediaType) getIntent().getSerializableExtra(GliaWidgets.MEDIA_TYPE);
        }
        return Engagement.MediaType.AUDIO;
    }

    private Boolean getIsUpgradeToCall() {
        if (getIntent().hasExtra(GliaWidgets.IS_UPGRADE_TO_CALL)) {
            return getIntent().getBooleanExtra(GliaWidgets.IS_UPGRADE_TO_CALL, false);
        }
        return false;
    }
}
