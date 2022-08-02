package com.glia.exampleapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.preference.PreferenceManager;

import com.glia.androidsdk.Glia;
import com.glia.androidsdk.screensharing.ScreenSharing;
import com.glia.widgets.GliaWidgets;
import com.glia.widgets.UiTheme;
import com.glia.widgets.call.CallActivity;
import com.glia.widgets.call.Configuration;
import com.glia.widgets.chat.ChatActivity;
import com.glia.widgets.core.configuration.GliaSdkConfiguration;
import com.glia.widgets.view.head.ChatHeadLayout;

public class MainFragment extends Fragment {

    @Nullable
    private ConstraintLayout containerView;

    @Nullable
    private ChatHeadLayout chatHeadLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.main_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.containerView = (ConstraintLayout) view;
        NavController navController = NavHostFragment.findNavController(this);
        view.findViewById(R.id.settings_button).setOnClickListener(view1 ->
                navController.navigate(R.id.settings));
        view.findViewById(R.id.chat_activity_button).setOnClickListener(v ->
                navigateToChat());
        view.findViewById(R.id.audio_call_button).setOnClickListener(v ->
                navigateToCall(GliaWidgets.MEDIA_TYPE_AUDIO));
        view.findViewById(R.id.video_call_button).setOnClickListener(v ->
                navigateToCall(GliaWidgets.MEDIA_TYPE_VIDEO));
        view.findViewById(R.id.end_engagement_button).setOnClickListener(v ->
                GliaWidgets.endEngagement());
        view.findViewById(R.id.clear_session_button).setOnClickListener(v ->
                GliaWidgets.clearVisitorSession());
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!Glia.isInitialized() || chatHeadLayout != null) return;

        Context context = getContext();
        if (context == null) return;

        chatHeadLayout = new ChatHeadLayout(context);
        chatHeadLayout.setLayoutParams(new ConstraintLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                )
        );
        chatHeadLayout.setNavigationCallback(
                new ChatHeadLayout.NavigationCallback() {
                    @Override
                    public void onNavigateToChat() {
                        navigateToChat();
                    }

                    @Override
                    public void onNavigateToCall() {
                        navigateToCall(null);
                    }
                }
        );

        if (containerView != null) {
            containerView.addView(chatHeadLayout);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        GliaWidgets.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void navigateToChat() {
        Intent intent = new Intent(requireContext(), ChatActivity.class);
        setNavigationIntentData(intent);
        startActivity(intent);
    }

    private void navigateToCall(
            @Nullable String mediaType
    ) {

        Configuration.Builder configBuilder = Configuration.Builder
                .builder()
                .setWidgetsConfiguration(getConfiguration());

        if (!TextUtils.isEmpty(mediaType)) {
            configBuilder.setMediaType(mediaType);
        }

        Intent intent = CallActivity.getIntent(requireContext(), configBuilder.build());

        startActivity(intent);
    }

    private GliaSdkConfiguration getConfiguration() {
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(requireContext());

        return new GliaSdkConfiguration.Builder()
                .companyName(getCompanyNameFromPrefs(sharedPreferences))
                .contextAssetId(getContextAssetIdFromPrefs(sharedPreferences))
                .queueId(getQueueIdFromPrefs(sharedPreferences))
                .runTimeTheme(getRuntimeThemeFromPrefs(sharedPreferences))
                .screenSharingMode(getScreenSharingModeFromPrefs(sharedPreferences))
                .useOverlay(getUseOverlay(sharedPreferences))
                .build();
    }

    private void setNavigationIntentData(Intent intent) {
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(requireContext());

        intent.putExtra(
                GliaWidgets.COMPANY_NAME,
                getCompanyNameFromPrefs(sharedPreferences)
        );
        intent.putExtra(
                GliaWidgets.QUEUE_ID,
                getQueueIdFromPrefs(sharedPreferences)
        );
        intent.putExtra(
                GliaWidgets.CONTEXT_ASSET_ID,
                getContextAssetIdFromPrefs(sharedPreferences)
        );
        intent.putExtra(
                GliaWidgets.UI_THEME,
                getRuntimeThemeFromPrefs(sharedPreferences)
        );
        intent.putExtra(
                GliaWidgets.USE_OVERLAY,
                getUseOverlay(sharedPreferences)
        );
        intent.putExtra(
                GliaWidgets.SCREEN_SHARING_MODE,
                getScreenSharingModeFromPrefs(sharedPreferences)
        );
    }

    private Boolean getUseOverlay(SharedPreferences sharedPreferences) {
        return Utils.getUseOverlay(sharedPreferences, getResources());
    }

    private ScreenSharing.Mode getScreenSharingModeFromPrefs(SharedPreferences sharedPreferences) {
        return Utils.getScreenSharingModeFromPrefs(sharedPreferences, getResources());
    }

    private UiTheme getRuntimeThemeFromPrefs(SharedPreferences sharedPreferences) {
        return Utils.getUiThemeByPrefs(sharedPreferences, getResources());
    }

    private String getQueueIdFromPrefs(SharedPreferences sharedPreferences) {
        return Utils.getStringFromPrefs(
                R.string.pref_queue_id,
                getString(R.string.default_queue_id),
                sharedPreferences,
                getResources()
        );
    }

    private String getContextAssetIdFromPrefs(SharedPreferences sharedPreferences) {
        return Utils.getStringFromPrefs(
                R.string.pref_context_asset_id,
                null,
                sharedPreferences,
                getResources()
        );
    }

    private String getCompanyNameFromPrefs(SharedPreferences sharedPreferences) {
        return Utils.getStringFromPrefs(
                R.string.pref_company_name,
                "",
                sharedPreferences,
                getResources()
        );
    }
}
