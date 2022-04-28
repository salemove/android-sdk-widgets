package com.glia.widgets.call;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;

public class CallActivity extends AppCompatActivity {
    private static final int MEDIA_PERMISSION_REQUEST_CODE = 2001;

    private static final String TAG = CallActivity.class.getSimpleName();

    private GliaSdkConfiguration configuration;

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
        if (!callView.shouldShowMediaEngagementView()) {
            finishAndRemoveTask();
            return;
        }

        configuration = createConfiguration(getIntent());
        callView.setConfiguration(configuration);
        callView.setTheme(configuration.getRunTimeTheme());
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

    private GliaSdkConfiguration createConfiguration(Intent intent) {
        return new GliaSdkConfiguration.Builder()
                .intent(intent)
                .build();
    }

    private void startCallWithPermissions() {
        List<String> missingPermissions = new ArrayList<>();
        Engagement.MediaType mediaType = getMediaType();
        if (mediaType == Engagement.MediaType.VIDEO && missingPermission(Manifest.permission.CAMERA)) {
            missingPermissions.add(Manifest.permission.CAMERA);
        }
        if ((mediaType == Engagement.MediaType.VIDEO || mediaType == Engagement.MediaType.AUDIO)
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
                configuration.getCompanyName(),
                configuration.getQueueId(),
                configuration.getContextUrl(),
                configuration.getUseOverlay(),
                configuration.getScreenSharingMode(),
                getMediaType()
        );
    }

    private Engagement.MediaType getMediaType() {
        String mediaType = getIntent().getStringExtra(GliaWidgets.MEDIA_TYPE);
        if (mediaType != null && mediaType.equals(GliaWidgets.MEDIA_TYPE_VIDEO)) {
            return Engagement.MediaType.VIDEO;
        } else {
            return Engagement.MediaType.AUDIO;
        }
    }

    private void navigateToChat() {
        Logger.d(TAG, "navigateToChat");
        Intent newIntent = new Intent(getApplicationContext(), ChatActivity.class);
        newIntent.putExtra(GliaWidgets.COMPANY_NAME, configuration.getCompanyName());
        newIntent.putExtra(GliaWidgets.QUEUE_ID, configuration.getQueueId());
        newIntent.putExtra(GliaWidgets.CONTEXT_URL, configuration.getContextUrl());
        newIntent.putExtra(GliaWidgets.UI_THEME, configuration.getRunTimeTheme());
        newIntent.putExtra(GliaWidgets.USE_OVERLAY, configuration.getUseOverlay());
        newIntent.putExtra(GliaWidgets.SCREEN_SHARING_MODE, configuration.getScreenSharingMode());
        startActivity(newIntent);
    }

    private void navigateToSurvey(Survey survey) {
        Intent newIntent = new Intent(getApplicationContext(), SurveyActivity.class);
        newIntent.putExtra(GliaWidgets.UI_THEME, configuration.getRunTimeTheme());
        newIntent.putExtra(GliaWidgets.SURVEY, (Parcelable) survey);
        startActivity(newIntent);
    }
}
