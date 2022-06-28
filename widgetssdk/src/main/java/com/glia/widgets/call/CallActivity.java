package com.glia.widgets.call;

import android.Manifest;
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

import com.glia.androidsdk.Engagement;
import com.glia.androidsdk.engagement.Survey;
import com.glia.widgets.GliaWidgets;
import com.glia.widgets.R;
import com.glia.widgets.chat.ChatActivity;
import com.glia.widgets.core.configuration.GliaSdkConfiguration;
import com.glia.widgets.helper.Logger;
import com.glia.widgets.helper.Utils;
import com.glia.widgets.survey.SurveyActivity;

import java.util.ArrayList;
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
        configuration =
                CallIntentReader.from(this)
                        .getConfiguration();

        if (!callView.shouldShowMediaEngagementView(configuration.getIsUpgradeToCall())) {
            finishAndRemoveTask();
            return;
        }

        callView.setOnTitleUpdatedListener(this::setTitle);
        callView.setConfiguration(configuration.getSdkConfiguration());
        callView.setTheme(configuration.getSdkConfiguration().getRunTimeTheme());
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
        callView.onDestroy(isFinishing());
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
        List<String> missingPermissions = new ArrayList<>();
        if (configuration.getMediaType() == Engagement.MediaType.VIDEO && missingPermission(Manifest.permission.CAMERA)) {
            missingPermissions.add(Manifest.permission.CAMERA);
        }
        if ((configuration.getMediaType() == Engagement.MediaType.VIDEO || configuration.getMediaType() == Engagement.MediaType.AUDIO)
                && missingPermission(Manifest.permission.RECORD_AUDIO)) {
            missingPermissions.add(Manifest.permission.RECORD_AUDIO);
        }
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

    private boolean missingPermission(String permission) {
        return ContextCompat.checkSelfPermission(getApplicationContext(), permission) != PackageManager.PERMISSION_GRANTED;
    }

    private boolean isPermissionDataMediaRequestCode(Pair<Integer, Integer[]> permissionData) {
        return permissionData != null && permissionData.first != null && permissionData.first == MEDIA_PERMISSION_REQUEST_CODE;
    }

    private void onCallPermissionsAvailable() {
        GliaSdkConfiguration sdkConfiguration = configuration.getSdkConfiguration();
        callView.startCall(
                sdkConfiguration.getCompanyName(),
                sdkConfiguration.getQueueId(),
                sdkConfiguration.getContextUrl(),
                sdkConfiguration.getUseOverlay(),
                sdkConfiguration.getScreenSharingMode(),
                configuration.getMediaType()
        );
    }

    private void navigateToChat() {
        Logger.d(TAG, "navigateToChat");
        GliaSdkConfiguration sdkConfiguration = configuration.getSdkConfiguration();
        Intent newIntent = new Intent(getApplicationContext(), ChatActivity.class);
        newIntent.putExtra(GliaWidgets.COMPANY_NAME, sdkConfiguration.getCompanyName());
        newIntent.putExtra(GliaWidgets.QUEUE_ID, sdkConfiguration.getQueueId());
        newIntent.putExtra(GliaWidgets.CONTEXT_URL, sdkConfiguration.getContextUrl());
        newIntent.putExtra(GliaWidgets.UI_THEME, sdkConfiguration.getRunTimeTheme());
        newIntent.putExtra(GliaWidgets.USE_OVERLAY, sdkConfiguration.getUseOverlay());
        newIntent.putExtra(GliaWidgets.SCREEN_SHARING_MODE, sdkConfiguration.getScreenSharingMode());
        startActivity(newIntent);
    }

    private void navigateToSurvey(Survey survey) {
        Intent newIntent = new Intent(getApplicationContext(), SurveyActivity.class);
        newIntent.putExtra(GliaWidgets.UI_THEME, configuration.getSdkConfiguration().getRunTimeTheme());
        newIntent.putExtra(GliaWidgets.SURVEY, (Parcelable) survey);
        startActivity(newIntent);
    }

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
