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
import com.glia.widgets.di.Dependencies;

public class ChatActivity extends AppCompatActivity {

    private String companyName;
    private String queueId;
    private String contextUrl;
    private boolean useOverlays;
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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Dependencies.addActivityToBackStack(Constants.CHAT_ACTIVITY);
        setContentView(R.layout.chat_activity);

        Intent intent = getIntent();
        companyName = intent.getStringExtra(GliaWidgets.COMPANY_NAME);
        queueId = intent.getStringExtra(GliaWidgets.QUEUE_ID);
        UiTheme runtimeTheme = intent.getParcelableExtra(GliaWidgets.UI_THEME);
        contextUrl = intent.getStringExtra(GliaWidgets.CONTEXT_URL);
        useOverlays = intent.getBooleanExtra(GliaWidgets.USE_OVERLAY, true);

        chatView = findViewById(R.id.chat_view);
        chatView.setTheme(runtimeTheme);
        chatView.setOnBackClickedListener(onBackClickedListener);
        chatView.setOnEndListener(onEndListener);
        chatView.setOnNavigateToCallListener(onNavigateToCallListener);
        chatView.startChat(
                companyName,
                queueId,
                contextUrl,
                useOverlays,
                savedInstanceState
        );
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
        newIntent.putExtra(GliaWidgets.COMPANY_NAME, companyName);
        newIntent.putExtra(GliaWidgets.QUEUE_ID, queueId);
        newIntent.putExtra(GliaWidgets.CONTEXT_URL, contextUrl);
        newIntent.putExtra(GliaWidgets.UI_THEME, theme);
        newIntent.putExtra(GliaWidgets.USE_OVERLAY, useOverlays);
        newIntent.putExtra(GliaWidgets.MEDIA_TYPE, mediaType);
        startActivity(newIntent);
    }

    @Override
    protected void onStop() {
        super.onStop();
        chatView.onStop();
    }
}
