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
import com.glia.widgets.chat.head.ChatHeadService;

public class ChatActivity extends AppCompatActivity {

    public static final String LAST_TYPED_TEXT = "last_typed_text";

    private String companyName;
    private String queueId;
    private UiTheme uiTheme;
    private String contextUrl;
    private ChatView chatView;
    private ChatView.OnBackClickedListener onBackClickedListener = () -> {
        chatView.backPressed();
        finish();
    };
    private ChatView.OnEndListener onEndListener = this::finish;
    private ChatView.OnNavigateToCallListener onNavigateToCallListener = (String lastTypedText) -> {
        navigateToCall(lastTypedText);
        chatView.navigateToCallSuccess();
    };
    private ChatView.OnBubbleListener onBubbleListener = new ChatView.OnBubbleListener() {
        @Override
        public void call(UiTheme theme, String lastTypedText, boolean isVisible) {
            Intent newIntent = new Intent(getApplicationContext(), ChatHeadService.class);
            newIntent.putExtra(GliaWidgets.COMPANY_NAME, companyName);
            newIntent.putExtra(GliaWidgets.QUEUE_ID, queueId);
            newIntent.putExtra(GliaWidgets.CONTEXT_URL, contextUrl);
            newIntent.putExtra(GliaWidgets.UI_THEME, theme);
            newIntent.putExtra(ChatHeadService.IS_VISIBLE, isVisible);
            newIntent.putExtra(ChatActivity.LAST_TYPED_TEXT, lastTypedText);
            newIntent.putExtra(GliaWidgets.RETURN_DESTINATION, GliaWidgets.DESTINATION_CHAT);
            getApplicationContext().startService(newIntent);
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_activity);

        Intent intent = getIntent();
        companyName = intent.getStringExtra(GliaWidgets.COMPANY_NAME);
        queueId = intent.getStringExtra(GliaWidgets.QUEUE_ID);
        uiTheme = intent.getParcelableExtra(GliaWidgets.UI_THEME);
        contextUrl = intent.getStringExtra(GliaWidgets.CONTEXT_URL);
        boolean isOriginCall = intent.getBooleanExtra(GliaWidgets.IS_ORIGIN_CALL, false);
        String lastTypedText = intent.getStringExtra(LAST_TYPED_TEXT);

        chatView = findViewById(R.id.chat_view);
        if (uiTheme != null) {
            chatView.setTheme(uiTheme);
        }
        chatView.setOnBackClickedListener(onBackClickedListener);
        chatView.setOnEndListener(onEndListener);
        chatView.setOnNavigateToCallListener(onNavigateToCallListener);
        chatView.setOnBubbleListener(onBubbleListener);
        chatView.startChatActivityChat(
                this,
                companyName,
                queueId,
                contextUrl,
                lastTypedText,
                isOriginCall);
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
        onBubbleListener = null;
        chatView.onDestroyView();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        chatView.backPressed();
        super.onBackPressed();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        GliaWidgets.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void navigateToCall(String lastTypedText) {
        Intent newIntent = new Intent(getApplicationContext(), CallActivity.class);
        newIntent.putExtra(GliaWidgets.COMPANY_NAME, companyName);
        newIntent.putExtra(GliaWidgets.QUEUE_ID, queueId);
        newIntent.putExtra(GliaWidgets.CONTEXT_URL, contextUrl);
        newIntent.putExtra(GliaWidgets.UI_THEME, uiTheme);
        newIntent.putExtra(ChatActivity.LAST_TYPED_TEXT, lastTypedText);
        startActivity(newIntent);
    }
}
