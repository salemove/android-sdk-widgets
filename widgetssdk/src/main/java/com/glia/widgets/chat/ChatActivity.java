package com.glia.widgets.chat;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.glia.widgets.Constants;
import com.glia.widgets.GliaWidgets;
import com.glia.widgets.R;
import com.glia.widgets.UiTheme;
import com.glia.widgets.call.CallActivity;
import com.glia.widgets.core.configuration.Configuration;
import com.glia.widgets.di.Dependencies;

public class ChatActivity extends AppCompatActivity {
    private ChatView chatView;
    private ChatView.OnBackClickedListener onBackClickedListener = () -> {
        if (chatView.backPressed()) finish();
    };
    private ChatView.OnEndListener onEndListener = this::finish;
    private ChatView.OnNavigateToCallListener onNavigateToCallListener =
            (UiTheme theme, String mediaType) -> {
                navigateToCall(theme, mediaType);
                chatView.navigateToCallSuccess();
            };

    private Configuration configuration;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Dependencies.addActivityToBackStack(Constants.CHAT_ACTIVITY);
        setContentView(R.layout.chat_activity);
        chatView = findViewById(R.id.chat_view);

        buildConfiguration();

        chatView.setTheme(configuration.getRunTimeTheme());
        chatView.setOnBackClickedListener(onBackClickedListener);
        chatView.setOnEndListener(onEndListener);
        chatView.setOnNavigateToCallListener(onNavigateToCallListener);
        chatView.startChat(
                configuration.getCompanyName(),
                configuration.getQueueId(),
                configuration.getContextUrl(),
                configuration.getUseOverlay(),
                savedInstanceState
        );
    }

    private void buildConfiguration() {
        configuration = new Configuration.Builder()
                .companyName(getCompanyName())
                .queueId(getQueueId())
                .runTimeTheme(getRunTimeUiTheme())
                .contextUrl(getContextUrl())
                .useOverlay(getUseOverlay())
                .build();
    }

    @Override
    protected void onResume() {
        chatView.onResume();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        onBackClickedListener = null;
        onEndListener = null;
        onNavigateToCallListener = null;
        chatView.onDestroyView();
        Dependencies.removeActivityFromBackStack(Constants.CHAT_ACTIVITY);
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
        chatView.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void navigateToCall(UiTheme theme, String mediaType) {
        Intent newIntent = new Intent(getApplicationContext(), CallActivity.class);
        newIntent.putExtra(GliaWidgets.COMPANY_NAME, configuration.getCompanyName());
        newIntent.putExtra(GliaWidgets.QUEUE_ID, configuration.getQueueId());
        newIntent.putExtra(GliaWidgets.CONTEXT_URL, configuration.getContextUrl());
        newIntent.putExtra(GliaWidgets.UI_THEME, theme);
        newIntent.putExtra(GliaWidgets.USE_OVERLAY, configuration.getUseOverlay());
        newIntent.putExtra(GliaWidgets.MEDIA_TYPE, mediaType);
        startActivity(newIntent);
    }

    @Override
    protected void onStop() {
        super.onStop();
        chatView.onStop();
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
