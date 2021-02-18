package com.glia.widgets.call;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.ContextThemeWrapper;

import com.glia.widgets.GliaWidgets;
import com.glia.widgets.R;
import com.glia.widgets.UiTheme;
import com.glia.widgets.chat.ChatActivity;
import com.glia.widgets.head.ChatHeadService;
import com.glia.widgets.helper.Logger;

public class CallActivity extends Activity {

    private static final String TAG = "CallActivity";

    private String companyName;
    private String queueId;
    private UiTheme runtimeTheme;
    private String contextUrl;
    private String lastTypedText;

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
    private CallView.OnBubbleListener onBubbleListener =
            (String operatorProfileImgUrl, String returnDestination) -> {
                startChatHeadService(operatorProfileImgUrl, returnDestination);
                if (returnDestination != null) {
                    finish();
                }
            };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GliaWidgets.addActivityToBackStack(GliaWidgets.CALL_ACTIVITY);
        setContentView(R.layout.call_activity);
        callView = findViewById(R.id.call_view);

        Intent intent = getIntent();
        companyName = intent.getStringExtra(GliaWidgets.COMPANY_NAME);
        queueId = intent.getStringExtra(GliaWidgets.QUEUE_ID);
        runtimeTheme = intent.getParcelableExtra(GliaWidgets.UI_THEME);
        contextUrl = intent.getStringExtra(GliaWidgets.CONTEXT_URL);
        lastTypedText = intent.getStringExtra(ChatActivity.LAST_TYPED_TEXT);
        callView.setTheme(runtimeTheme);
        callView.setOnBackClickedListener(onBackClickedListener);
        callView.setOnEndListener(onEndListener);
        callView.setOnNavigateToChatListener(onNavigateToChatListener);
        callView.setOnBubbleListener(onBubbleListener);
        callView.startCall();
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
        onBackClickedListener = null;
        onEndListener = null;
        onNavigateToChatListener = null;
        onBubbleListener = null;
        callView.onDestroy();
        GliaWidgets.removeActivityFromBackStack(GliaWidgets.CALL_ACTIVITY);
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
    }

    private void navigateToChat() {
        Logger.d(TAG, "navigateToChat");
        Intent newIntent = new Intent(getApplicationContext(), ChatActivity.class);
        newIntent.putExtra(GliaWidgets.COMPANY_NAME, companyName);
        newIntent.putExtra(GliaWidgets.QUEUE_ID, queueId);
        newIntent.putExtra(GliaWidgets.CONTEXT_URL, contextUrl);
        newIntent.putExtra(GliaWidgets.UI_THEME, runtimeTheme);
        newIntent.putExtra(ChatActivity.LAST_TYPED_TEXT, lastTypedText);
        newIntent.putExtra(GliaWidgets.IS_ORIGIN_CALL, true);
        startActivity(newIntent);
    }

    private void startChatHeadService(String operatorProfileImgUrl, String returnDestination) {
        Logger.d(TAG, "startChatHeadService, returnDestination: " + returnDestination);
        Intent newIntent = new Intent(getApplicationContext(), ChatHeadService.class);
        newIntent.putExtra(GliaWidgets.COMPANY_NAME, companyName);
        newIntent.putExtra(GliaWidgets.QUEUE_ID, queueId);
        newIntent.putExtra(GliaWidgets.CONTEXT_URL, contextUrl);
        newIntent.putExtra(ChatActivity.LAST_TYPED_TEXT, lastTypedText);
        newIntent.putExtra(GliaWidgets.RETURN_DESTINATION, returnDestination);
        newIntent.putExtra(GliaWidgets.OPERATOR_PROFILE_IMG_URL, operatorProfileImgUrl);
        getApplicationContext().startService(newIntent);
    }
}
