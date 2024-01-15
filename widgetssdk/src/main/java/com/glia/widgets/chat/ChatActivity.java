package com.glia.widgets.chat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.glia.widgets.GliaWidgets;
import com.glia.widgets.R;
import com.glia.widgets.UiTheme;
import com.glia.widgets.call.CallActivity;
import com.glia.widgets.call.Configuration;
import com.glia.widgets.core.configuration.GliaSdkConfiguration;
import com.glia.widgets.helper.Logger;
import com.glia.widgets.helper.Utils;

import java.util.Objects;

public class ChatActivity extends AppCompatActivity {
    private static final String TAG = ChatActivity.class.getSimpleName();
    private ChatView chatView;

    private GliaSdkConfiguration configuration;

    /**
     * Creates and fills out Intent for starting ChatActivity
     *
     * @param context   - Context object
     * @param contextId - Context asset ID
     * @param queueId   - Queue ID
     * @return - Intent for Starting ChatActivity
     */
    public static Intent getIntent(
        Context context,
        String contextId,
        String queueId
    ) {
        return new Intent(context, ChatActivity.class)
            .putExtra(GliaWidgets.CONTEXT_ASSET_ID, contextId)
            .putExtra(GliaWidgets.QUEUE_ID, queueId);
    }

    /**
     * Creates and fills out Intent for starting ChatActivity
     *
     * @param context   - Context object
     * @param contextId - Context asset ID
     * @param queueId   - Queue ID
     * @param chatType  - Type of chat screen
     * @return - Intent for Starting ChatActivity
     */
    public static Intent getIntent(
        Context context,
        String contextId,
        String queueId,
        ChatType chatType
    ) {
        return new Intent(context, ChatActivity.class)
            .putExtra(GliaWidgets.CONTEXT_ASSET_ID, contextId)
            .putExtra(GliaWidgets.QUEUE_ID, queueId)
            .putExtra(GliaWidgets.CHAT_TYPE, (Parcelable) chatType);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.i(TAG, "Create Chat screen");
        setContentView(R.layout.chat_activity);
        chatView = findViewById(R.id.chat_view);

        chatView.setOnTitleUpdatedListener(this::setTitle);
        configuration = createConfiguration(getIntent());

        if (!chatView.shouldShow()) {
            finishAndRemoveTask();
            return;
        }

        chatView.setConfiguration(configuration);
        chatView.setUiTheme(configuration.getRunTimeTheme());
        chatView.setOnBackClickedListener(this::finish);
        chatView.setOnBackToCallListener(this::backToCallScreen);

        // In case the engagement ends, Activity is removed from the device's Recents menu
        // to avoid app users to accidentally start queueing for another call when they resume
        // the app from the Recents menu and the app's backstack was empty.
        chatView.setOnEndListener(this::finishAndRemoveTask);

        chatView.setOnMinimizeListener(this::finish);
        chatView.setOnNavigateToCallListener(this::startCallScreen);
        chatView.startChat(
            configuration.getCompanyName(),
            configuration.getQueueId(),
            configuration.getContextAssetId(),
            configuration.getUseOverlay(),
            configuration.getScreenSharingMode(),
            Objects.requireNonNull(configuration.getChatType())
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
        chatView.onDestroyView();
        Logger.i(TAG, "Destroy Chat screen");
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        chatView.onBackPressed();
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

    private void startCallScreen(UiTheme theme, String mediaType) {
        startActivity(
            CallActivity.getIntent(
                getApplicationContext(),
                getConfigurationBuilder().setMediaType(Utils.toMediaType(mediaType))
                    .setIsUpgradeToCall(true)
                    .build()
            )
        );
        finish();
    }

    private void backToCallScreen() {
        startActivity(CallActivity.getIntent(getApplicationContext(), getConfigurationBuilder().build()));
        finish();
    }

    private Configuration.Builder getConfigurationBuilder() {
        return new Configuration.Builder().setWidgetsConfiguration(configuration);
    }
}
