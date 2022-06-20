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
import com.glia.widgets.survey.SurveyActivity;

import java.security.InvalidParameterException;
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
                IntentReader.from(this)
                        .getConfiguration();

        if (!callView.shouldShowMediaEngagementView(configuration.isUpgradeToCall)) {
            finishAndRemoveTask();
            return;
        }

        callView.setOnTitleUpdatedListener(this::setTitle);
        callView.setConfiguration(configuration.sdkConfiguration);
        callView.setTheme(configuration.sdkConfiguration.getRunTimeTheme());
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
        switch (configuration.mediaType) {
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
        if (configuration.mediaType == Engagement.MediaType.VIDEO && missingPermission(Manifest.permission.CAMERA)) {
            missingPermissions.add(Manifest.permission.CAMERA);
        }
        if ((configuration.mediaType == Engagement.MediaType.VIDEO || configuration.mediaType == Engagement.MediaType.AUDIO)
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
        callView.startCall(
                configuration.sdkConfiguration.getCompanyName(),
                configuration.sdkConfiguration.getQueueId(),
                configuration.sdkConfiguration.getContextUrl(),
                configuration.sdkConfiguration.getUseOverlay(),
                configuration.sdkConfiguration.getScreenSharingMode(),
                configuration.mediaType
        );
    }

    private void navigateToChat() {
        Logger.d(TAG, "navigateToChat");
        Intent newIntent = new Intent(getApplicationContext(), ChatActivity.class);
        newIntent.putExtra(GliaWidgets.COMPANY_NAME, configuration.sdkConfiguration.getCompanyName());
        newIntent.putExtra(GliaWidgets.QUEUE_ID, configuration.sdkConfiguration.getQueueId());
        newIntent.putExtra(GliaWidgets.CONTEXT_URL, configuration.sdkConfiguration.getContextUrl());
        newIntent.putExtra(GliaWidgets.UI_THEME, configuration.sdkConfiguration.getRunTimeTheme());
        newIntent.putExtra(GliaWidgets.USE_OVERLAY, configuration.sdkConfiguration.getUseOverlay());
        newIntent.putExtra(GliaWidgets.SCREEN_SHARING_MODE, configuration.sdkConfiguration.getScreenSharingMode());
        startActivity(newIntent);
    }

    private void navigateToSurvey(Survey survey) {
        Intent newIntent = new Intent(getApplicationContext(), SurveyActivity.class);
        newIntent.putExtra(GliaWidgets.UI_THEME, configuration.sdkConfiguration.getRunTimeTheme());
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
                Configuration.Builder
                        .builder()
                        .setWidgetsConfiguration(sdkConfiguration)
                        .setMediaType(toMediaType(mediaType))
                        .build()
        );
    }

    public static Intent getIntent(
            Context context,
            CallActivity.Configuration configuration
    ) {
        return IntentBuilder
                .from(context)
                .setConfiguration(configuration)
                .getIntent();
    }

    public static Engagement.MediaType toMediaType(String mediaType) {
        switch (mediaType) {
            case GliaWidgets.MEDIA_TYPE_VIDEO:
                return Engagement.MediaType.VIDEO;
            case GliaWidgets.MEDIA_TYPE_AUDIO:
                return Engagement.MediaType.AUDIO;
            default:
                throw new InvalidParameterException("Invalid Media Type");
        }
    }

    public static class Configuration {
        private final GliaSdkConfiguration sdkConfiguration;
        private final Engagement.MediaType mediaType;
        private final boolean isUpgradeToCall;

        private Configuration(Builder builder) {
            this.sdkConfiguration = builder.widgetsConfiguration;
            this.mediaType = builder.mediaType;
            this.isUpgradeToCall = builder.isUpgradeToCall != null ? builder.isUpgradeToCall : false;
        }

        public GliaSdkConfiguration getSdkConfiguration() {
            return this.sdkConfiguration;
        }

        public Engagement.MediaType getMediaType() {
            return this.mediaType;
        }

        public boolean getIsUpgradeToCall() {
            return this.isUpgradeToCall;
        }

        public static class Builder {
            private GliaSdkConfiguration widgetsConfiguration;
            private Engagement.MediaType mediaType;
            private Boolean isUpgradeToCall;

            public Builder setWidgetsConfiguration(GliaSdkConfiguration configuration) {
                this.widgetsConfiguration = configuration;
                return this;
            }

            public Builder setMediaType(Engagement.MediaType mediaType) {
                this.mediaType = mediaType;
                return this;
            }

            public Builder setMediaType(String mediaType) {
                this.mediaType = toMediaType(mediaType);
                return this;
            }

            public Builder setIsUpgradeToCall(Boolean isUpgradeToCall) {
                this.isUpgradeToCall = isUpgradeToCall;
                return this;
            }

            public Builder() {
                this.isUpgradeToCall = false;
                this.mediaType = Engagement.MediaType.AUDIO;
            }

            public Configuration build() {
                return new Configuration(this);
            }

            public static Builder from(Configuration configuration) {
                return builder()
                        .setWidgetsConfiguration(configuration.sdkConfiguration)
                        .setMediaType(configuration.mediaType)
                        .setIsUpgradeToCall(configuration.isUpgradeToCall);
            }

            public static Builder builder() {
                return new Builder();
            }
        }
    }

    private static class IntentBuilder {
        private final Context context;
        private Configuration configuration;

        private IntentBuilder(@NonNull Context context) {
            this.context = context;
        }

        public static IntentBuilder from(@NonNull Context context) {
            return new IntentBuilder(context);
        }

        public IntentBuilder setConfiguration(@NonNull Configuration configuration) {
            this.configuration = configuration;
            return this;
        }

        public Intent getIntent() {
            if (configuration == null) throw new RuntimeException("Configuration " +
                    "missing");

            Intent intent = new Intent(context, CallActivity.class);
            intent.putExtra(GliaWidgets.COMPANY_NAME, configuration.sdkConfiguration.getCompanyName());
            intent.putExtra(GliaWidgets.QUEUE_ID, configuration.sdkConfiguration.getQueueId());
            intent.putExtra(GliaWidgets.CONTEXT_URL, configuration.sdkConfiguration.getContextUrl());
            intent.putExtra(GliaWidgets.UI_THEME, configuration.sdkConfiguration.getRunTimeTheme());
            intent.putExtra(GliaWidgets.USE_OVERLAY, configuration.sdkConfiguration.getUseOverlay());
            intent.putExtra(GliaWidgets.SCREEN_SHARING_MODE, configuration.sdkConfiguration.getScreenSharingMode());
            intent.putExtra(GliaWidgets.MEDIA_TYPE, configuration.getMediaType());
            intent.putExtra(GliaWidgets.IS_UPGRADE_TO_CALL, configuration.getIsUpgradeToCall());
            return intent;
        }
    }

    private static class IntentReader {
        private final AppCompatActivity activity;

        private IntentReader(@NonNull AppCompatActivity activity) {
            this.activity = activity;
        }

        public Configuration getConfiguration() {
            return Configuration.Builder
                    .builder()
                    .setWidgetsConfiguration(getSdkConfiguration())
                    .setMediaType(getMediaType())
                    .setIsUpgradeToCall(getIsUpgradeToCall())
                    .build();
        }

        public static IntentReader from(@NonNull AppCompatActivity activity) {
            return new IntentReader(activity);
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
}
