package com.glia.widgets.chat;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.glia.androidsdk.engagement.Survey;
import com.glia.widgets.GliaWidgets;
import com.glia.widgets.R;
import com.glia.widgets.UiTheme;
import com.glia.widgets.call.CallActivity;
import com.glia.widgets.core.configuration.GliaSdkConfiguration;
import com.glia.widgets.helper.Utils;
import com.glia.widgets.survey.SurveyActivity;
import com.glia.widgets.view.head.ChatHeadLayout;

public class ChatActivity extends AppCompatActivity {
    private ChatView chatView;
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
        chatView.setTheme(configuration.getRunTimeTheme());
        chatView.setOnBackClickedListener(onBackClickedListener);

        // In case the engagement ends, Activity is removed from the device's Recents menu
        // to avoid app users to accidentally start queueing for another call when they resume
        // the app from the Recents menu and the app's backstack was empty.
        chatView.setOnEndListener(this::finishAndRemoveTask);

        chatView.setOnMinimizeListener(this::finish);
        chatView.setOnNavigateToCallListener(onNavigateToCallListener);
        chatView.setOnNavigateToSurveyListener(onNavigateToSurveyListener);
        chatView.startChat(
                configuration.getCompanyName(),
                configuration.getQueueId(),
                configuration.getContextUrl(),
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

    private GliaSdkConfiguration createConfiguration(Intent intent) {
        return new GliaSdkConfiguration.Builder()
                .intent(intent)
                .build();
    }

    private void navigateToCall(String mediaType) {
        startActivity(
                CallActivity.getIntent(
                        getApplicationContext(),
                        CallActivity.Configuration.Builder
                                .builder()
                                .setWidgetsConfiguration(configuration)
                                .setMediaType(Utils.toMediaType(mediaType))
                                .setIsUpgradeToCall(true)
                                .build()
                )
        );
    }

    private void navigateToSurvey(UiTheme theme, Survey survey) {
        Intent newIntent = new Intent(getApplicationContext(), SurveyActivity.class);
        newIntent.putExtra(GliaWidgets.UI_THEME, theme);
        newIntent.putExtra(GliaWidgets.SURVEY, (Parcelable) survey);
        startActivity(newIntent);
    }
}
