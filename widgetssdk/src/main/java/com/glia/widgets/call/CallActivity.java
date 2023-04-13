package com.glia.widgets.call;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;

import com.glia.androidsdk.engagement.Survey;
import com.glia.widgets.GliaWidgets;
import com.glia.widgets.R;
import com.glia.widgets.chat.ChatActivity;
import com.glia.widgets.core.configuration.GliaSdkConfiguration;
import com.glia.widgets.helper.Logger;
import com.glia.widgets.helper.Utils;
import com.glia.widgets.survey.SurveyActivity;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;

public class CallActivity extends AppCompatActivity {
    private static final int MEDIA_PERMISSION_REQUEST_CODE = 2001;

    private static final String TAG = CallActivity.class.getSimpleName();

    private Configuration configuration;

    private CallView callView;
    private CallView.OnBackClickedListener onBackClickedListener = this::finish;
    private CallView.OnNavigateToChatListener onNavigateToChatListener = () -> {
        navigateToChat();
        finish();
    };
    private final CallView.OnNavigateToSurveyListener onNavigateToSurveyListener = (Survey survey) -> {
        navigateToSurvey(survey);
        finish();
    };
    private final PublishSubject<Pair<Integer, Integer[]>> permissionSubject = PublishSubject.create();
    private Disposable permissionSubjectDisposable;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.call_activity);
        callView = findViewById(R.id.call_view);
        configuration = CallIntentReader.from(this).getConfiguration();

        if (!callView.shouldShowMediaEngagementView(configuration.getIsUpgradeToCall())) {
            finishAndRemoveTask();
            return;
        }

        callView.setOnTitleUpdatedListener(this::setTitle);
        callView.setConfiguration(configuration.getSdkConfiguration());
        callView.setUiTheme(configuration.getSdkConfiguration().getRunTimeTheme());
        callView.setOnBackClickedListener(onBackClickedListener);

        // In case the engagement ends, Activity is removed from the device's Recents menu
        // to avoid app users to accidentally start queueing for another call when they resume
        // the app from the Recents menu and the app's backstack was empty.
        callView.setOnEndListener(this::finishAndRemoveTask);

        callView.setOnMinimizeListener(this::finish);
        callView.setOnNavigateToChatListener(onNavigateToChatListener);
        callView.setOnNavigateToSurveyListener(onNavigateToSurveyListener);

        if (savedInstanceState == null) {
            startCallWithPermissions();
        }

        setTitle(getTitleText());
    }

    @StringRes
    private int getTitleText() {
        switch (configuration.getMediaType()) {
            case VIDEO:
                return R.string.glia_call_video_app_bar_title;
            case AUDIO:
            default:
                return R.string.glia_call_audio_app_bar_title;
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
        if (permissionSubjectDisposable != null) {
            permissionSubjectDisposable.dispose();
        }
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        GliaWidgets.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        GliaWidgets.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Integer[] convertedGrantResults = IntStream.of(grantResults).boxed().toArray(Integer[]::new);
        permissionSubject.onNext(new Pair<>(requestCode, convertedGrantResults));
    }

    private void startCallWithPermissions() {
        if (!configuration.getIsUpgradeToCall()) { // In upgrading to call, permissions handle on the Core SDK side.
            callView.checkForPermissions(
                    getApplicationContext(),
                    configuration.getMediaType(),
                    this::handleMissingPermissions);
        } else {
            onCallPermissionsAvailable();
        }
    }

    private void handleMissingPermissions(List<String> missingPermissions) {
        if (missingPermissions.size() > 0) {
            permissionSubjectDisposable = permissionSubject
                    .filter(this::isPermissionDataMediaRequestCode)
                    .firstOrError()
                    .map(permissionData -> permissionData.second)
                    .map(permissionResultCodeArray -> Arrays
                            .stream(permissionResultCodeArray)
                            .allMatch(code -> code == PackageManager.PERMISSION_GRANTED))
                    .subscribe(
                            (isPermissionRequestSuccessful) -> {
                                if (isPermissionRequestSuccessful) {
                                    onCallPermissionsAvailable();
                                } else {
                                    callView.showMissingPermissionsDialog();
                                }
                            },
                            error -> callView.showMissingPermissionsDialog()
                    );
            requestPermissions(
                    missingPermissions.toArray(new String[0]),
                    MEDIA_PERMISSION_REQUEST_CODE
            );
        } else {
            onCallPermissionsAvailable();
        }
    }

    public interface MissingPermissionsCallBack {
        void onMissingPermissionsGathered(List<String> missingPermissions);
    }

    private boolean isPermissionDataMediaRequestCode(Pair<Integer, Integer[]> permissionData) {
        return permissionData != null && permissionData.first != null && permissionData.first == MEDIA_PERMISSION_REQUEST_CODE;
    }

    private void onCallPermissionsAvailable() {
        GliaSdkConfiguration sdkConfiguration = configuration.getSdkConfiguration();
        callView.startCall(
                sdkConfiguration.getCompanyName(),
                sdkConfiguration.getQueueId(),
                sdkConfiguration.getContextAssetId(),
                sdkConfiguration.getUseOverlay(),
                sdkConfiguration.getScreenSharingMode(),
                configuration.getMediaType()
        );
    }

    private void navigateToChat() {
        Logger.d(TAG, "navigateToChat");
        GliaSdkConfiguration sdkConfiguration = configuration.getSdkConfiguration();
        Intent newIntent = new Intent(getApplicationContext(), ChatActivity.class)
                .putExtra(GliaWidgets.COMPANY_NAME, sdkConfiguration.getCompanyName())
                .putExtra(GliaWidgets.QUEUE_ID, sdkConfiguration.getQueueId())
                .putExtra(GliaWidgets.CONTEXT_ASSET_ID, sdkConfiguration.getContextAssetId())
                .putExtra(GliaWidgets.UI_THEME, sdkConfiguration.getRunTimeTheme())
                .putExtra(GliaWidgets.USE_OVERLAY, sdkConfiguration.getUseOverlay())
                .putExtra(GliaWidgets.SCREEN_SHARING_MODE, sdkConfiguration.getScreenSharingMode());
        startActivity(newIntent);
    }

    private void navigateToSurvey(Survey survey) {
        Intent newIntent = new Intent(getApplicationContext(), SurveyActivity.class)
                .putExtra(GliaWidgets.UI_THEME, configuration.getSdkConfiguration().getRunTimeTheme())
                .putExtra(GliaWidgets.SURVEY, (Parcelable) survey);
        startActivity(newIntent);
    }

    /**
     * Creates and fills out Intent for starting CallActivity
     * @deprecated use {@link #getIntent(Context, Configuration)} since 1.8.2
     * @param applicationContext - application context
     * @param sdkConfiguration - widgets sdk configuration
     * @param mediaType - media type that should be started (in case media engagement not ongoing)
     * @return Intent for starting CallActivity
     */
    @Deprecated
    public static Intent getIntent(
            Context applicationContext,
            GliaSdkConfiguration sdkConfiguration,
            String mediaType
    ) {
        return getIntent(applicationContext,
                new Configuration.Builder()
                        .setWidgetsConfiguration(sdkConfiguration)
                        .setMediaType(Utils.toMediaType(mediaType))
                        .build()
        );
    }

    /**
     * Creates and fills out Intent for starting CallActivity
     * @param context - Context object
     * @param configuration - CallActivity configuration
     * @return - Intent for Starting CallActivity
     */
    public static Intent getIntent(
            Context context,
            Configuration configuration
    ) {
        return CallIntentBuilder
                .from(context)
                .setConfiguration(configuration)
                .getIntent();
    }
}
