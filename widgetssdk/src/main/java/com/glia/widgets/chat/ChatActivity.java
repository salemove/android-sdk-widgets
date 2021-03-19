package com.glia.widgets.chat;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.glia.widgets.GliaWidgets;
import com.glia.widgets.R;
import com.glia.widgets.UiTheme;
import com.glia.widgets.call.CallActivity;

public class ChatActivity extends AppCompatActivity {

    private static final String TAG = "ChatActivity";

    private String companyName;
    private String queueId;
    private UiTheme runtimeTheme;
    private String contextUrl;
    private ChatView chatView;
    private ChatView.OnBackClickedListener onBackClickedListener = () -> {
        chatView.backPressed();
        finish();
    };
    private ChatView.OnEndListener onEndListener = this::finish;
    private ChatView.OnNavigateToCallListener onNavigateToCallListener =
            (UiTheme theme) -> {
                navigateToCall(theme);
                chatView.navigateToCallSuccess();
            };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        GliaWidgets.addActivityToBackStack(GliaWidgets.CHAT_ACTIVITY);
        setContentView(R.layout.chat_activity);

        Intent intent = getIntent();
        companyName = intent.getStringExtra(GliaWidgets.COMPANY_NAME);
        queueId = intent.getStringExtra(GliaWidgets.QUEUE_ID);
        runtimeTheme = intent.getParcelableExtra(GliaWidgets.UI_THEME);
        contextUrl = intent.getStringExtra(GliaWidgets.CONTEXT_URL);
        boolean useOverlays = intent.getBooleanExtra(GliaWidgets.USE_OVERLAY, true);

        chatView = findViewById(R.id.chat_view);
        if (runtimeTheme != null) {
            chatView.setTheme(runtimeTheme);
        }
        chatView.setOnBackClickedListener(onBackClickedListener);
        chatView.setOnEndListener(onEndListener);
        chatView.setOnNavigateToCallListener(onNavigateToCallListener);
        chatView.startChatActivityChat(
                this,
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
        GliaWidgets.removeActivityFromBackStack(GliaWidgets.CHAT_ACTIVITY);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        chatView.backPressed();
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        GliaWidgets.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        GliaWidgets.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void navigateToCall(UiTheme theme) {
        Intent newIntent = new Intent(getApplicationContext(), CallActivity.class);
        newIntent.putExtra(GliaWidgets.COMPANY_NAME, companyName);
        newIntent.putExtra(GliaWidgets.QUEUE_ID, queueId);
        newIntent.putExtra(GliaWidgets.CONTEXT_URL, contextUrl);
        newIntent.putExtra(GliaWidgets.UI_THEME, theme);
        startActivity(newIntent);
    }
}
