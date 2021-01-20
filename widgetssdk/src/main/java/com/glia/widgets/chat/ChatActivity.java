package com.glia.widgets.chat;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.glia.widgets.GliaWidgets;
import com.glia.widgets.R;
import com.glia.widgets.UiTheme;

public class ChatActivity extends AppCompatActivity {

    public static final String LAST_TYPED_TEXT = "last_typed_text";

    private ChatView chatView;
    private ChatView.OnBackClickedListener onBackClickedListener = () -> {
        chatView.backPressed();
        finish();
    };
    private ChatView.OnEndListener onEndListener = this::finish;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_activity);

        Intent intent = getIntent();
        String companyName = intent.getStringExtra(GliaWidgets.COMPANY_NAME);
        String queueId = intent.getStringExtra(GliaWidgets.QUEUE_ID);
        UiTheme uiTheme = intent.getParcelableExtra(GliaWidgets.UI_THEME);
        String contextUrl = intent.getStringExtra(GliaWidgets.CONTEXT_URL);
        String lastTypedText = intent.getStringExtra(LAST_TYPED_TEXT);

        chatView = findViewById(R.id.chat_view);
        if (uiTheme != null) {
            chatView.setTheme(uiTheme);
        }
        chatView.setOnBackClickedListener(onBackClickedListener);
        chatView.setOnEndListener(onEndListener);
        chatView.startChatActivityChat(this, companyName, queueId, contextUrl, lastTypedText);
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
}
