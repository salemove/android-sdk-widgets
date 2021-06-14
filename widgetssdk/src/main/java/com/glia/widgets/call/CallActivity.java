package com.glia.widgets.call;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.glia.androidsdk.Engagement;
import com.glia.widgets.Constants;
import com.glia.widgets.GliaWidgets;
import com.glia.widgets.R;
import com.glia.widgets.UiTheme;
import com.glia.widgets.chat.ChatActivity;
import com.glia.widgets.di.Dependencies;
import com.glia.widgets.helper.Logger;

public class CallActivity extends Activity {

    private static final String TAG = "CallActivity";

    private String companyName;
    private String queueId;
    private UiTheme runtimeTheme;
    private String contextUrl;
    private boolean useOverlays;

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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Dependencies.addActivityToBackStack(Constants.CALL_ACTIVITY);
        setContentView(R.layout.call_activity);
        callView = findViewById(R.id.call_view);

        Intent intent = getIntent();
        companyName = intent.getStringExtra(GliaWidgets.COMPANY_NAME);
        queueId = intent.getStringExtra(GliaWidgets.QUEUE_ID);
        runtimeTheme = intent.getParcelableExtra(GliaWidgets.UI_THEME);
        contextUrl = intent.getStringExtra(GliaWidgets.CONTEXT_URL);
        useOverlays = intent.getBooleanExtra(GliaWidgets.USE_OVERLAY, true);
        callView.setTheme(runtimeTheme);
        callView.setOnBackClickedListener(onBackClickedListener);
        callView.setOnEndListener(onEndListener);
        callView.setOnNavigateToChatListener(onNavigateToChatListener);
        callView.startCall(companyName, queueId, contextUrl, useOverlays, getMediaType(intent));
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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        GliaWidgets.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void navigateToChat() {
        Logger.d(TAG, "navigateToChat");
        Intent newIntent = new Intent(getApplicationContext(), ChatActivity.class);
        newIntent.putExtra(GliaWidgets.COMPANY_NAME, companyName);
        newIntent.putExtra(GliaWidgets.QUEUE_ID, queueId);
        newIntent.putExtra(GliaWidgets.CONTEXT_URL, contextUrl);
        newIntent.putExtra(GliaWidgets.UI_THEME, runtimeTheme);
        newIntent.putExtra(GliaWidgets.USE_OVERLAY, useOverlays);
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
}
