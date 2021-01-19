package com.glia.exampleapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.preference.PreferenceManager;

import com.glia.widgets.GliaWidgets;
import com.glia.widgets.UiTheme;
import com.glia.widgets.chat.ChatActivity;
import com.glia.widgets.chat.ChatView;

public class MainFragment extends Fragment {

    private static final String STARTED = "started";

    private ChatView chatView;

    public MainFragment() {
    }

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
        FragmentActivity activity = requireActivity();
        view.findViewById(R.id.settings_button).setOnClickListener(view1 -> {
            NavController navController =
                    Navigation.findNavController(activity, R.id.nav_host_fragment);
            navController.navigate(R.id.settings);
        });
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
        chatView = view.findViewById(R.id.chat_view);
        view.findViewById(R.id.embed_button).setOnClickListener(view1 -> {
            if (chatView.isStarted()) {
                chatView.show();
            } else {
                chatView.startEmbeddedChat(
                        getStringFromPrefs(R.string.pref_company_name, getString(R.string.settings_value_default_company_name), sharedPreferences),
                        getStringFromPrefs(R.string.pref_queue_id, getString(R.string.queue_id), sharedPreferences),
                        getStringFromPrefs(R.string.pref_context_url, getString(R.string.queue_id), sharedPreferences));
            }
        });
        view.findViewById(R.id.activity_button).setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), ChatActivity.class);
            intent.putExtra(GliaWidgets.COMPANY_NAME,
                    getStringFromPrefs(R.string.pref_company_name, getString(R.string.settings_value_default_company_name), sharedPreferences));
            intent.putExtra(GliaWidgets.QUEUE_ID,
                    getStringFromPrefs(R.string.pref_queue_id, getString(R.string.queue_id), sharedPreferences));
            intent.putExtra(GliaWidgets.CONTEXT_URL,
                    getStringFromPrefs(R.string.pref_context_url, getString(R.string.queue_id), sharedPreferences));
            UiTheme uiTheme = getUiThemeByPrefs(sharedPreferences);
            intent.putExtra(GliaWidgets.UI_THEME, uiTheme);
            startActivity(intent);
        });

        if (savedInstanceState != null && savedInstanceState.containsKey(STARTED)) {
            chatView.startEmbeddedChat(
                    getStringFromPrefs(R.string.pref_company_name, getString(R.string.settings_value_default_company_name), sharedPreferences),
                    getStringFromPrefs(R.string.pref_queue_id, getString(R.string.queue_id), sharedPreferences),
                    getStringFromPrefs(R.string.pref_context_url, getString(R.string.queue_id), sharedPreferences));
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
        UiTheme theme = getUiThemeByPrefs(sharedPreferences);
        // Doing this here because user can navigate to settings and change values anytime
        chatView.setTheme(theme);
    }

    @Override
    public void onDestroyView() {
        chatView.onDestroyView();
        super.onDestroyView();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (chatView.isStarted()) {
            outState.putBoolean(STARTED, chatView.isStarted());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        GliaWidgets.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    String getStringFromPrefs(@StringRes int keyValue, String defaultValue, SharedPreferences sharedPreferences) {
        return sharedPreferences.getString(getString(keyValue), defaultValue);
    }

    UiTheme getUiThemeByPrefs(SharedPreferences sharedPreferences) {
        String title = getStringFromPrefs(R.string.pref_header_title, null, sharedPreferences);
        Integer baseLightColor = getColorValueFromPrefs(R.string.pref_base_light_color, sharedPreferences);
        Integer baseDarkColor = getColorValueFromPrefs(R.string.pref_base_dark_color, sharedPreferences);
        Integer baseNormalColor = getColorValueFromPrefs(R.string.pref_base_normal_color, sharedPreferences);
        Integer baseShadeColor = getColorValueFromPrefs(R.string.pref_base_shade_color, sharedPreferences);
        Integer brandPrimaryColor = getColorValueFromPrefs(R.string.pref_brand_primary_color, sharedPreferences);
        Integer systemAgentBubbleColor = getColorValueFromPrefs(R.string.pref_system_agent_bubble_color, sharedPreferences);
        Integer fontFamily = getTypefaceFromPrefs(sharedPreferences);
        Integer systemNegativeColor = getColorValueFromPrefs(R.string.pref_system_negative_color, sharedPreferences);
        UiTheme.UiThemeBuilder builder = new UiTheme.UiThemeBuilder();

        builder.setAppBarTitle(title);
        builder.setBaseLightColor(baseLightColor);
        builder.setBaseDarkColor(baseDarkColor);
        builder.setBaseNormalColor(baseNormalColor);
        builder.setBaseShadeColor(baseShadeColor);
        builder.setSystemAgentBubbleColor(systemAgentBubbleColor);
        builder.setBrandPrimaryColor(brandPrimaryColor);
        builder.setFontRes(fontFamily);
        builder.setSystemNegativeColor(systemNegativeColor);
        return builder.build();
    }

    Integer getColorValueFromPrefs(@StringRes int keyValue, SharedPreferences sharedPreferences) {
        String colorGrey = getString(R.string.color_grey_value);
        String colorRed = getString(R.string.color_red_value);
        String colorBlue = getString(R.string.color_blue_value);
        String colorWhite = getString(R.string.color_white_value);
        String colorBlack = getString(R.string.color_black_value);
        String defaultPrefValue = getString(R.string.settings_value_default);

        String colorValue = sharedPreferences.getString(getString(keyValue), defaultPrefValue);

        if (!colorValue.equals(defaultPrefValue)) {
            if (colorValue.equals(colorGrey)) {
                return R.color.color_grey;
            } else if (colorValue.equals(colorRed)) {
                return R.color.color_red;
            } else if (colorValue.equals(colorBlue)) {
                return R.color.color_blue;
            } else if (colorValue.equals(colorWhite)) {
                return R.color.color_white;
            } else if (colorValue.equals(colorBlack)) {
                return R.color.color_black;
            }
        }
        return null;
    }

    Integer getTypefaceFromPrefs(SharedPreferences sharedPreferences) {
        String delius = getString(R.string.font_delius_value);
        String expletus = getString(R.string.font_expletus_value);
        String tangerine = getString(R.string.font_tangerine_value);
        String defaultPrefValue = getString(R.string.settings_value_default);

        String typeFaceValue = sharedPreferences.getString(getString(R.string.pref_font_family), defaultPrefValue);

        if (!typeFaceValue.equals(defaultPrefValue)) {
            if (typeFaceValue.equals(delius)) {
                return R.font.delius;
            } else if (typeFaceValue.equals(expletus)) {
                return R.font.expletus;
            } else if (typeFaceValue.equals(tangerine)) {
                return R.font.tangerine;
            }
        }
        return null;
    }
}
