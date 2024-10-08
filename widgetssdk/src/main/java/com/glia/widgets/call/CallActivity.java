package com.glia.widgets.call;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.glia.widgets.GliaWidgets;
import com.glia.widgets.R;
import com.glia.widgets.base.FadeTransitionActivity;
import com.glia.widgets.chat.ChatActivity;
import com.glia.widgets.core.configuration.EngagementConfiguration;
import com.glia.widgets.di.Dependencies;
import com.glia.widgets.helper.Logger;
import com.glia.widgets.helper.Utils;
import com.glia.widgets.locale.LocaleString;
import com.glia.widgets.webbrowser.WebBrowserActivity;

import java.util.ArrayList;
import java.util.Objects;

/**
 * This activity is used for engagements that include audio and/or video calls.
 * <p>
 * Main features:
 * - Requests required permissions and enqueues for audio and/or video engagements if no ongoing engagements are found.
 * - Provides video feeds from operator and visitor cameras.
 * - Provides controls for managing ongoing engagements, including video and audio.
 * - Allows switching between chat and call activities.
 * <p>
 * Before this activity is launched, make sure that Glia Widgets SDK is set up correctly.
 * <p>
 * Data that can be passed together with the Activity intent:
 * - {@link GliaWidgets#QUEUE_IDS}: IDs list of the queues you would like to use for your engagements.
 * For a full list of optional parameters, see the constants defined in {@link GliaWidgets}.
 * <p>
 * Code example:
 * <pre>
 * Intent intent = new Intent(requireContext(), CallActivity.class);
 * intent.putExtra(GliaWidgets.QUEUE_IDS, new ArrayList<>(List.of("AUDIO_QUEUE_ID")));
 * intent.putExtra(GliaWidgets.MEDIA_TYPE, Engagement.MediaType.VIDEO);
 * startActivity(intent);
 * </pre>
 */
public final class CallActivity extends FadeTransitionActivity {
    private static final String TAG = CallActivity.class.getSimpleName();

    private CallConfiguration callConfiguration;

    private CallView callView;
    private CallView.OnBackClickedListener onBackClickedListener = this::finish;
    private CallView.OnNavigateToChatListener onNavigateToChatListener = () -> {
        navigateToChat();
        finish();
    };
    private final CallView.OnNavigateToWebBrowserListener onNavigateToWebBrowserListener = this::navigateToWebBrowser;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.i(TAG, "Create Call screen");
        setContentView(R.layout.call_activity);
        callView = findViewById(R.id.call_view);

        // Legacy company name support
        Dependencies.getSdkConfigurationManager().setLegacyCompanyName(getIntent().getStringExtra(GliaWidgets.COMPANY_NAME));

        callConfiguration = CallActivityIntentHelper.readConfiguration(this);
        if (this.getIntent().hasExtra(GliaWidgets.USE_OVERLAY)) {
            // Integrator has passed a deprecated GliaWidgets.USE_OVERLAY parameter with Intent
            // Override bubble configuration with USE_OVERLAY value
            boolean useOverlay = this.getIntent().getBooleanExtra(GliaWidgets.USE_OVERLAY, true);
            Dependencies.getSdkConfigurationManager().setLegacyUseOverlay(useOverlay);
        }

        if (!callView.shouldShowMediaEngagementView(callConfiguration.isUpgradeToCall)) {
            finishAndRemoveTask();
            return;
        }

        callView.setOnTitleUpdatedListener(this::setTitle);
        callView.setEngagementConfiguration(callConfiguration.engagementConfiguration);
        callView.setUiTheme(callConfiguration.engagementConfiguration.getRunTimeTheme());
        callView.setOnBackClickedListener(onBackClickedListener);

        // In case the engagement ends, Activity is removed from the device's Recents menu
        // to avoid app users to accidentally start queueing for another call when they resume
        // the app from the Recents menu and the app's backstack was empty.
        callView.setOnEndListener(this::finishAndRemoveTask);

        callView.setOnMinimizeListener(this::finish);
        callView.setOnNavigateToChatListener(onNavigateToChatListener);
        callView.setOnNavigateToWebBrowserListener(onNavigateToWebBrowserListener);

        if (savedInstanceState == null) {
            startCall();
        }
    }

    @Override
    protected void onResume() {
        callView.onResume();
        super.onResume();
    }

    @Override
    protected void onPause() {
        callView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        Logger.i(TAG, "Destroy Call screen");
        onBackClickedListener = null;
        onNavigateToChatListener = null;
        callView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        callView.onUserInteraction();
    }

    private void startCall() {
        EngagementConfiguration engagementConfiguration = Objects.requireNonNull(callConfiguration.engagementConfiguration);
        callView.startCall(
            Objects.requireNonNull(engagementConfiguration.getCompanyName()),
            engagementConfiguration.getQueueIds(),
            engagementConfiguration.getContextAssetId(),
            Objects.requireNonNull(engagementConfiguration.getScreenSharingMode()),
            callConfiguration.isUpgradeToCall,
            callConfiguration.mediaType
        );
    }

    private void navigateToChat() {
        Logger.d(TAG, "navigateToChat");
        EngagementConfiguration sdkConfiguration = Objects.requireNonNull(callConfiguration.engagementConfiguration);
        ArrayList<String> queueIds;
        if (sdkConfiguration.getQueueIds() != null) {
            queueIds = new ArrayList<>(sdkConfiguration.getQueueIds());
        } else {
            queueIds = null;
        }
        Intent newIntent = new Intent(getApplicationContext(), ChatActivity.class)
            .putExtra(GliaWidgets.QUEUE_IDS, queueIds)
            .putExtra(GliaWidgets.CONTEXT_ASSET_ID, sdkConfiguration.getContextAssetId())
            .putExtra(GliaWidgets.UI_THEME, sdkConfiguration.getRunTimeTheme())
            .putExtra(GliaWidgets.SCREEN_SHARING_MODE, sdkConfiguration.getScreenSharingMode());
        startActivity(newIntent);
    }

    private void navigateToWebBrowser(LocaleString title, String url) {
        Intent newIntent = WebBrowserActivity.Companion.intent(this, title, url);
        startActivity(newIntent);
    }

    /**
     * Creates and fills out Intent for starting CallActivity
     * @deprecated use {@link #getIntent(Context, CallConfiguration)} since 1.8.2
     * @param applicationContext - application context
     * @param engagementConfiguration - widgets sdk configuration
     * @param mediaType - media type that should be started (in case media engagement not ongoing)
     * @return Intent for starting CallActivity
     */
    @Deprecated
    public static Intent getIntent(
        Context applicationContext,
        EngagementConfiguration engagementConfiguration,
        String mediaType
    ) {
        Logger.logDeprecatedMethodUse(TAG, "getIntent(Context, GliaSdkConfiguration, String)");
        return getIntent(applicationContext,
                new CallConfiguration.Builder()
                        .setEngagementConfiguration(engagementConfiguration)
                        .setMediaType(Utils.toMediaType(mediaType))
                        .build()
        );
    }

    /**
     * Creates and fills out Intent for starting CallActivity
     * @param context - Context object
     * @param callConfiguration - CallActivity configuration
     * @return - Intent for Starting CallActivity
     */
    public static Intent getIntent(
        Context context,
        CallConfiguration callConfiguration
    ) {
        return CallActivityIntentHelper.createIntent(context, callConfiguration);
    }
}
