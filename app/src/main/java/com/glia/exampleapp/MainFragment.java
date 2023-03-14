package com.glia.exampleapp;

import static com.glia.androidsdk.visitor.Authentication.Behavior.FORBIDDEN_DURING_ENGAGEMENT;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.preference.PreferenceManager;

import com.glia.androidsdk.Glia;
import com.glia.androidsdk.screensharing.ScreenSharing;
import com.glia.androidsdk.visitor.Authentication;
import com.glia.widgets.GliaWidgets;
import com.glia.widgets.UiTheme;
import com.glia.widgets.call.CallActivity;
import com.glia.widgets.call.Configuration;
import com.glia.widgets.callvisualizer.EndScreenSharingActivity;
import com.glia.widgets.chat.ChatActivity;
import com.glia.widgets.core.callvisualizer.domain.CallVisualizer;
import com.glia.widgets.core.configuration.GliaSdkConfiguration;
import com.glia.widgets.messagecenter.MessageCenterActivity;
import com.glia.widgets.view.VisitorCodeView;
import com.glia.widgets.view.head.ChatHeadLayout;

public class MainFragment extends Fragment {

    @Nullable
    private ConstraintLayout containerView;

    @Nullable
    private ChatHeadLayout chatHeadLayout;

    private Authentication authentication;

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

        this.containerView = view.findViewById(R.id.constraint_layout);
        NavController navController = NavHostFragment.findNavController(this);
        setupAuthButtonsVisibility();
        view.findViewById(R.id.settings_button).setOnClickListener(view1 ->
                navController.navigate(R.id.settings));
        view.findViewById(R.id.chat_activity_button).setOnClickListener(v ->
                navigateToChat());
        view.findViewById(R.id.audio_call_button).setOnClickListener(v ->
                navigateToCall(GliaWidgets.MEDIA_TYPE_AUDIO));
        view.findViewById(R.id.video_call_button).setOnClickListener(v ->
                navigateToCall(GliaWidgets.MEDIA_TYPE_VIDEO));
        view.findViewById(R.id.message_center_activity_button).setOnClickListener(v ->
                navigateToMessageCenter());
        view.findViewById(R.id.end_engagement_button).setOnClickListener(v ->
                GliaWidgets.endEngagement());
        view.findViewById(R.id.initGliaWidgetsButton).setOnClickListener(v ->
                new Thread(this::initGliaWidgets).start()
        );
        view.findViewById(R.id.authenticationButton).setOnClickListener(v ->
                showAuthenticationDialog());
        view.findViewById(R.id.deauthenticationButton).setOnClickListener(v ->
                deauthenticate());
        view.findViewById(R.id.clear_session_button).setOnClickListener(v ->
                clearSession());
        view.findViewById(R.id.visitor_code_button).setOnClickListener(v ->
                showVisitorCode());
    }

    @Override
    public void onResume() {
        super.onResume();

        if (Glia.isInitialized() && authentication == null) {
            prepareAuthentication();
        }

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
                    public void onNavigateToEndScreenSharing() {
                        navigateToEndScreenSharing();
                    }

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

    private void setupAuthButtonsVisibility() {
        if (getActivity() == null || containerView == null) return;
        if (!Glia.isInitialized()) {
            getActivity().runOnUiThread(() -> {
                containerView.findViewById(R.id.initGliaWidgetsButton).setVisibility(View.VISIBLE);
                containerView.findViewById(R.id.authenticationButton).setVisibility(View.GONE);
                containerView.findViewById(R.id.deauthenticationButton).setVisibility(View.GONE);
                containerView.findViewById(R.id.visitor_code_button).setVisibility(View.GONE);
            });
            return;
        }
        getActivity().runOnUiThread(() -> {
            containerView.findViewById(R.id.visitor_code_button).setVisibility(View.VISIBLE);
        });
        if (authentication == null) return;

        if (authentication.isAuthenticated()) {
            getActivity().runOnUiThread(() -> {
                containerView.findViewById(R.id.initGliaWidgetsButton).setVisibility(View.GONE);
                containerView.findViewById(R.id.authenticationButton).setVisibility(View.GONE);
                containerView.findViewById(R.id.deauthenticationButton).setVisibility(View.VISIBLE);
            });
        } else {
            getActivity().runOnUiThread(() -> {
                containerView.findViewById(R.id.initGliaWidgetsButton).setVisibility(View.GONE);
                containerView.findViewById(R.id.authenticationButton).setVisibility(View.VISIBLE);
                containerView.findViewById(R.id.deauthenticationButton).setVisibility(View.GONE);
            });
        }
    }

    private void listenForCallVisualizerEngagements() {
        GliaWidgets.getCallVisualizer().onEngagementStart(() -> {
            showToast("Call Visualiser engagement established");
        });
    }

    private void navigateToChat() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
        Intent intent = ChatActivity.getIntent(
                getContext(),
                getContextAssetIdFromPrefs(sharedPreferences),
                getQueueIdFromPrefs(sharedPreferences));
        startActivity(intent);
    }

    private void navigateToEndScreenSharing() {
        Intent intent = new Intent(requireContext(), EndScreenSharingActivity.class);
        setNavigationIntentData(intent);
        startActivity(intent);
    }

    private void navigateToMessageCenter() {
        Intent intent = new Intent(requireContext(), MessageCenterActivity.class);
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

    private void showAuthenticationDialog() {
        if (getContext() == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        final EditText tokenInput = prepareTokenInputViewEditText(builder);
        builder.setPositiveButton(
                getString(R.string.authentication_dialog_authenticate_button),
                (dialog, which) -> authenticate(tokenInput));
        builder.setNegativeButton(
                R.string.authentication_dialog_cancel_button,
                (dialog, which) -> dialog.cancel());
        builder.setView(prepareDialogLayout(tokenInput));
        builder.show();
    }

    @NonNull
    private LinearLayout prepareDialogLayout(EditText tokenInput) {
        LinearLayout container = new LinearLayout(getContext());
        container.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        int marginInDp = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics());
        layoutParams.setMargins(marginInDp, 0, marginInDp, 0);
        tokenInput.setLayoutParams(layoutParams);
        tokenInput.setGravity(android.view.Gravity.TOP | Gravity.START);

        container.addView(tokenInput, layoutParams);
        return container;
    }

    @NonNull
    private EditText prepareTokenInputViewEditText(AlertDialog.Builder builder) {
        final EditText input = new EditText(getContext());
        input.setHint(R.string.authentication_dialog_token_input_hint);
        input.setSingleLine();
        input.setMaxLines(10);
        input.setHorizontallyScrolling(false);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setTitle(R.string.authentication_dialog_title);
        builder.setView(input);
        return input;
    }

    private void initGliaWidgets() {
        if (Glia.isInitialized()) {
            setupAuthButtonsVisibility();
            listenForCallVisualizerEngagements();
            return;
        }

        if (getActivity() == null) return;

        GliaWidgets.init(GliaWidgetsConfigManager.createDefaultConfig(getActivity().getApplicationContext()));
        prepareAuthentication();
        listenForCallVisualizerEngagements();
    }

    private void prepareAuthentication() {
        authentication = GliaWidgets.getAuthentication(FORBIDDEN_DURING_ENGAGEMENT);
        setupAuthButtonsVisibility();
    }

    private void authenticate(EditText input) {
        if (getActivity() == null || containerView == null) return;

        String jwt = input.getText().toString();
        authentication.authenticate((response, exception) -> {
            if (exception == null && authentication.isAuthenticated()) {
                setupAuthButtonsVisibility();
            } else {
                showToast("Error: " + exception);
            }
        }, jwt);
    }

    private void deauthenticate() {
        if (getActivity() == null || containerView == null) return;

        authentication.deauthenticate((response, exception) -> {
            if (exception == null && !authentication.isAuthenticated()) {
                setupAuthButtonsVisibility();
            } else {
                showToast("Error: " + exception);
            }
        });
    }

    private void clearSession() {
        GliaWidgets.clearVisitorSession();
        setupAuthButtonsVisibility();
    }

    private void showVisitorCode() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
        String visitorContext = getContextAssetIdFromPrefs(sharedPreferences);
        CallVisualizer cv = GliaWidgets.getCallVisualizer();
        if (visitorContext != null && !visitorContext.trim().isEmpty()) {
            cv.addVisitorContext(visitorContext);
        }
        cv.showVisitorCodeDialog(getContext());
    }

    // For testing the integrated Visitor Code solution
    private void showVisitorCodeInADedicatedView() {
        VisitorCodeView visitorCodeView = GliaWidgets.getCallVisualizer().createVisitorCodeView(getContext());
        CardView cv = containerView.findViewById(R.id.container);
        cv.addView(visitorCodeView);
        cv.setVisibility(View.VISIBLE);
    }

    private void showToast(String message) {
        if (getActivity() == null) return;

        getActivity().runOnUiThread(() -> Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show());
    }
}
