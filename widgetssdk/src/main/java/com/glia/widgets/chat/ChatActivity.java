package com.glia.widgets.chat;

import static com.glia.widgets.core.screensharing.data.GliaScreenSharingRepository.UNIQUE_RESULT_CODE;

import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.os.Parcelable;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.glia.androidsdk.Glia;
import com.glia.androidsdk.engagement.Survey;
import com.glia.widgets.GliaWidgets;
import com.glia.widgets.R;
import com.glia.widgets.UiTheme;
import com.glia.widgets.call.CallActivity;
import com.glia.widgets.call.Configuration;
import com.glia.widgets.core.configuration.GliaSdkConfiguration;
import com.glia.widgets.helper.Logger;
import com.glia.widgets.helper.Utils;
import com.glia.widgets.survey.SurveyActivity;
import com.glia.widgets.view.head.ChatHeadLayout;

public class ChatActivity extends AppCompatActivity {

    private final static String TAG = ChatActivity.class.getSimpleName();
    private ChatView chatView;

    private ActivityResultLauncher<Intent> startMediaProjection;
    private MediaProjectionManager mediaProjectionManager;

    private ChatView.OnBackClickedListener onBackClickedListener = () -> {
        if (chatView.backPressed()) finish();
    };

    private ChatView.OnNavigateToCallListener onNavigateToCallListener =
            (UiTheme theme, String mediaType) -> {
                navigateToCall(mediaType);
                chatView.navigateToCallSuccess();
            };
    private ChatView.OnNavigateToSurveyListener onNavigateToSurveyListener =
            (UiTheme theme, Survey survey) -> {
                navigateToSurvey(theme, survey);
                finish();
            };

    private ChatView.OnRequestScreenSharingPermissionCallback onRequestScreenSharingPermissionCallback =
            () -> {
                if (startMediaProjection != null && mediaProjectionManager != null) {
                    startMediaProjection.launch(mediaProjectionManager.createScreenCaptureIntent());
                    Logger.d(TAG, "Acquire a media projection token: launching permission request"
                    );
                }
            };

    private GliaSdkConfiguration configuration;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_activity);
        chatView = findViewById(R.id.chat_view);
        ChatHeadLayout chatHeadLayout = findViewById(R.id.chat_head_layout);
        chatHeadLayout.setIsChatView(true);

        chatView.setOnTitleUpdatedListener(this::setTitle);
        configuration = createConfiguration(getIntent());
        chatHeadLayout.setConfiguration(configuration);
        chatView.setConfiguration(configuration);
        chatView.setUiTheme(configuration.getRunTimeTheme());
        chatView.setOnBackClickedListener(onBackClickedListener);
        chatView.setOnBackToCallListener(this::backToCall);

        // In case the engagement ends, Activity is removed from the device's Recents menu
        // to avoid app users to accidentally start queueing for another call when they resume
        // the app from the Recents menu and the app's backstack was empty.
        chatView.setOnEndListener(this::finishAndRemoveTask);

        chatView.setOnMinimizeListener(this::finish);
        chatView.setOnNavigateToCallListener(onNavigateToCallListener);
        chatView.setOnNavigateToSurveyListener(onNavigateToSurveyListener);
        registerForMediaProjectionPermissionResult();
        chatView.setOnRequestScreenSharingPermissionCallback(onRequestScreenSharingPermissionCallback);
        chatView.startChat(
                configuration.getCompanyName(),
                configuration.getQueueId(),
                configuration.getContextAssetId(),
                configuration.getUseOverlay(),
                configuration.getScreenSharingMode()
        );
    }

    @Override
    protected void onResume() {
        chatView.onResume();
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        chatView.onPause();
    }

    @Override
    protected void onDestroy() {
        onBackClickedListener = null;
        onNavigateToCallListener = null;
        onNavigateToSurveyListener = null;
        chatView.onDestroyView(isFinishing());
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (chatView.backPressed()) super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        GliaWidgets.onActivityResult(requestCode, resultCode, data);
        chatView.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        GliaWidgets.onRequestPermissionsResult(requestCode, permissions, grantResults);
        chatView.onRequestPermissionsResult(requestCode, grantResults);
    }

    private void registerForMediaProjectionPermissionResult() {
        // Request a token that grants the app the ability to capture the display contents
        // See https://developer.android.com/guide/topics/large-screens/media-projection
        mediaProjectionManager = getSystemService(MediaProjectionManager.class);
        startMediaProjection = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    Logger.d(TAG, "Acquire a media projection token: result received");
                    if (result.getResultCode() == RESULT_OK) {
                        Logger.d(TAG,
                                "Acquire a media projection token: RESULT_OK, passing data to Glia Core SDK");
                        Glia.getCurrentEngagement().ifPresent(engagement -> engagement.onActivityResult(
                                UNIQUE_RESULT_CODE,
                                result.getResultCode(),
                                result.getData()
                        ));
                    }
                }
        );
    }

    private GliaSdkConfiguration createConfiguration(Intent intent) {
        return new GliaSdkConfiguration.Builder()
                .intent(intent)
                .build();
    }

    private void navigateToCall(String mediaType) {
        startActivity(
                CallActivity.getIntent(
                        getApplicationContext(),
                        getConfigurationBuilder().setMediaType(Utils.toMediaType(mediaType))
                                .setIsUpgradeToCall(true)
                                .build()
                )
        );
    }

    private void backToCall() {
        startActivity(CallActivity.getIntent(getApplicationContext(), getConfigurationBuilder().build()));

        chatView.navigateToCallSuccess();
    }

    private Configuration.Builder getConfigurationBuilder() {
        return new Configuration.Builder().setWidgetsConfiguration(configuration);
    }

    private void navigateToSurvey(UiTheme theme, Survey survey) {
        Intent newIntent = new Intent(getApplicationContext(), SurveyActivity.class);
        newIntent.putExtra(GliaWidgets.UI_THEME, theme);
        newIntent.putExtra(GliaWidgets.SURVEY, (Parcelable) survey);
        startActivity(newIntent);
    }
}
