package com.glia.exampleapp;

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

import com.glia.widgets.ChatView;
import com.glia.widgets.UiTheme;

public class MainFragment extends Fragment {

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
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
        FragmentActivity activity = getActivity();
        view.findViewById(R.id.settings_button).setOnClickListener(view1 -> {
            if (activity != null) {
                NavController navController =
                        Navigation.findNavController(activity, R.id.nav_host_fragment);
                navController.navigate(R.id.settings);
            }
        });
        ChatView chatView = view.findViewById(R.id.chat_view);
        chatView.setOnBackClickedListener(view2 -> chatView.stop());
        view.findViewById(R.id.embed_button).setOnClickListener(view1 -> {
            UiTheme theme = getUiThemeByPrefs(sharedPreferences);
            chatView.setTheme(theme);
            chatView.start();
        });
    }

    String getAppbarTitleFromPrefs(SharedPreferences sharedPreferences) {
        return sharedPreferences.getString(getString(R.string.pref_header_title), null);
    }

    UiTheme getUiThemeByPrefs(SharedPreferences sharedPreferences) {
        String title = getAppbarTitleFromPrefs(sharedPreferences);
        Integer bgColor = getColorValueFromPrefs(R.string.pref_bg_color, sharedPreferences);
        Integer primaryColor = getColorValueFromPrefs(R.string.pref_primary_color, sharedPreferences);
        Integer opearatorBgColor = getColorValueFromPrefs(R.string.pref_operator_bg_color, sharedPreferences);
        Integer fontFamily = getTypefaceFromPrefs(sharedPreferences);
        Integer primaryTextColor = getColorValueFromPrefs(R.string.pref_text_color, sharedPreferences);
        UiTheme.UiThemeBuilder builder = new UiTheme.UiThemeBuilder();
        builder.setTitle(title);
        builder.setBackgroundColorRes(bgColor);
        builder.setOperatorMessageBgColorRes(opearatorBgColor);
        builder.setPrimaryBrandColorRes(primaryColor);
        builder.setFontRes(fontFamily);
        builder.setPrimaryTextColorRes(primaryTextColor);
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
