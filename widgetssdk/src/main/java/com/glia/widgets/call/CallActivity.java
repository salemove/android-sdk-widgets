package com.glia.widgets.call;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;

import com.glia.androidsdk.Engagement;
import com.glia.widgets.Constants;
import com.glia.widgets.GliaWidgets;
import com.glia.widgets.R;
import com.glia.widgets.UiTheme;
import com.glia.widgets.chat.ChatActivity;
import com.glia.widgets.core.configuration.GliaSdkConfiguration;
import com.glia.widgets.di.Dependencies;
import com.glia.widgets.helper.Logger;

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
    private CallView.OnBackClickedListener onBackClickedListener = () -> {
        callView.backPressed();
        finish();
    };
    private CallView.OnEndListener onEndListener = this::finish;
    private CallView.OnNavigateToChatListener onNavigateToChatListener = () -> {
        navigateToChat();
        finish();
    };
    private final PublishSubject<Pair<Integer, Integer[]>> permissionSubject = PublishSubject.create();
    private Disposable permissionSubjectDisposable;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Dependencies.addActivityToBackStack(Constants.CALL_ACTIVITY);
        setContentView(R.layout.call_activity);
        callView = findViewById(R.id.call_view);
        if (!callView.shouldShowMediaEngagementView()) {
            finish();
            return;
        }

        buildConfiguration();

        callView.setTheme(configuration.getRunTimeTheme());
        callView.setOnBackClickedListener(onBackClickedListener);
        callView.setOnEndListener(onEndListener);
        callView.setOnNavigateToChatListener(onNavigateToChatListener);

        startCallWithPermissions(
                configuration.getCompanyName(),
                configuration.getQueueId(),
                configuration.getContextUrl(),
                configuration.getUseOverlay(),
                getMediaType(getIntent())
        );
    }

    private void buildConfiguration() {
        configuration = new GliaSdkConfiguration.Builder()
                .companyName(getCompanyName())
                .queueId(getQueueId())
                .runTimeTheme(getRunTimeUiTheme())
                .contextUrl(getContextUrl())
                .useOverlay(getUseOverlay())
                .build();
    }

    private void startCallWithPermissions(String companyName,
                                          String queueId,
                                          String contextUrl,
                                          boolean useOverlays,
                                          Engagement.MediaType mediaType) {

        List<String> missingPermissions = new ArrayList<>();
        if (mediaType == Engagement.MediaType.VIDEO && !hasPermission(Manifest.permission.CAMERA)) {
            missingPermissions.add(Manifest.permission.CAMERA);
        }
        if ((mediaType == Engagement.MediaType.VIDEO || mediaType == Engagement.MediaType.AUDIO) && !hasPermission(Manifest.permission.RECORD_AUDIO)) {
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
                                    onCallPermissionsAvailable(companyName, queueId, contextUrl, useOverlays, mediaType);
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
            onCallPermissionsAvailable(companyName, queueId, contextUrl, useOverlays, mediaType);
        }
    }

    private boolean isPermissionDataMediaRequestCode(Pair<Integer, Integer[]> permissionData) {
        return permissionData != null && permissionData.first != null && permissionData.first == MEDIA_PERMISSION_REQUEST_CODE;
    }

    private boolean hasPermission(String permission) {
        return ContextCompat.checkSelfPermission(getApplicationContext(), permission) == PackageManager.PERMISSION_GRANTED;
    }

    private void onCallPermissionsAvailable(String companyName,
                                            String queueId,
                                            String contextUrl,
                                            boolean useOverlays,
                                            Engagement.MediaType mediaType) {
        callView.startCall(companyName, queueId, contextUrl, useOverlays, mediaType);
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
    public void onBackPressed() {
        callView.backPressed();
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        if (permissionSubjectDisposable != null) {
            permissionSubjectDisposable.dispose();
        }
        onBackClickedListener = null;
        onEndListener = null;
        onNavigateToChatListener = null;
        callView.onDestroy();
        Dependencies.removeActivityFromBackStack(Constants.CALL_ACTIVITY);
        super.onDestroy();
    }

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        callView.onUserInteraction();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        GliaWidgets.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Integer[] convertedGrantResults = IntStream.of(grantResults).boxed().toArray(Integer[]::new);
        permissionSubject.onNext(new Pair<>(requestCode, convertedGrantResults));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        GliaWidgets.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void navigateToChat() {
        Logger.d(TAG, "navigateToChat");
        Intent newIntent = new Intent(getApplicationContext(), ChatActivity.class);
        newIntent.putExtra(GliaWidgets.COMPANY_NAME, configuration.getCompanyName());
        newIntent.putExtra(GliaWidgets.QUEUE_ID, configuration.getQueueId());
        newIntent.putExtra(GliaWidgets.CONTEXT_URL, configuration.getContextUrl());
        newIntent.putExtra(GliaWidgets.UI_THEME, configuration.getRunTimeTheme());
        newIntent.putExtra(GliaWidgets.USE_OVERLAY, configuration.getUseOverlay());
        startActivity(newIntent);
    }

    private Engagement.MediaType getMediaType(Intent intent) {
        String mediaType = intent.getStringExtra(GliaWidgets.MEDIA_TYPE);
        if (mediaType != null && mediaType.equals(GliaWidgets.MEDIA_TYPE_VIDEO)) {
            return Engagement.MediaType.VIDEO;
        } else {
            return Engagement.MediaType.AUDIO;
        }
    }

    private String getCompanyName() {
        return getIntent().getStringExtra(GliaWidgets.COMPANY_NAME);
    }

    private String getQueueId() {
        return getIntent().getStringExtra(GliaWidgets.QUEUE_ID);
    }

    private String getContextUrl() {
        return getIntent().getStringExtra(GliaWidgets.CONTEXT_URL);
    }

    private UiTheme getRunTimeUiTheme() {
        return getIntent().getParcelableExtra(GliaWidgets.UI_THEME);
    }

    private boolean getUseOverlay() {
        return getIntent().getBooleanExtra(
                GliaWidgets.USE_OVERLAY,
                Dependencies.getSdkConfigurationManager().isUseOverlay()
        );
    }
}
