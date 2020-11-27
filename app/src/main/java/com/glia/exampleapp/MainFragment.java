package com.glia.exampleapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.content.res.ResourcesCompat;
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
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.main_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Context context = view.getContext();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        view.findViewById(R.id.settings_button).setOnClickListener(view1 -> {
            FragmentActivity activity = getActivity();
            if (activity != null) {
                NavController navController = Navigation.findNavController(activity, R.id.nav_host_fragment);
                navController.navigate(R.id.settings);
            }
        });
        view.findViewById(R.id.embed_button).setOnClickListener(view1 -> {
            ChatView chatView = view.findViewById(R.id.chat_view);
            ColorStateList bgColor = getColorValueFromPrefs(R.string.pref_bg_color, sharedPreferences);
            ColorStateList receiverBgColor = getColorValueFromPrefs(R.string.pref_primary_color, sharedPreferences);
            ColorStateList senderBgColor = getColorValueFromPrefs(R.string.pref_sender_bg_color, sharedPreferences);
            Typeface fontFamily = getTypefaceFromPrefs(getContext(), sharedPreferences);
            ColorStateList primaryTextColor = getColorValueFromPrefs(R.string.pref_text_color, sharedPreferences);
            chatView.setupUiConfig(new UiTheme(bgColor, senderBgColor, receiverBgColor, fontFamily, primaryTextColor));
            chatView.start();
        });
    }

    ColorStateList getColorValueFromPrefs(@StringRes int keyValue, SharedPreferences sharedPreferences) {
        String colorGrey = getString(R.string.color_grey_value);
        String colorRed = getString(R.string.color_red_value);
        String colorBlue = getString(R.string.color_blue_value);
        String colorWhite = getString(R.string.color_white_value);
        String colorBlack = getString(R.string.color_black_value);
        String defaultPrefValue = getString(R.string.settings_value_default);

        String colorValue = sharedPreferences.getString(getString(keyValue), defaultPrefValue);

        if (!colorValue.equals(defaultPrefValue)) {
            if (colorValue.equals(colorGrey)) {
                return ResourcesCompat.getColorStateList(getResources(), R.color.color_grey, null);
            } else if (colorValue.equals(colorRed)) {
                return ResourcesCompat.getColorStateList(getResources(), R.color.color_red, null);
            } else if (colorValue.equals(colorBlue)) {
                return ResourcesCompat.getColorStateList(getResources(), R.color.color_blue, null);
            } else if (colorValue.equals(colorWhite)) {
                return ResourcesCompat.getColorStateList(getResources(), R.color.color_white, null);
            } else if (colorValue.equals(colorBlack)) {
                return ResourcesCompat.getColorStateList(getResources(), R.color.color_black, null);
            }
        }
        return null;
    }

    Typeface getTypefaceFromPrefs(Context context, SharedPreferences sharedPreferences) {
        String delius = getString(R.string.font_delius_value);
        String expletus = getString(R.string.font_expletus_value);
        String tangerine = getString(R.string.font_tangerine_value);
        String defaultPrefValue = getString(R.string.settings_value_default);

        String typeFaceValue = sharedPreferences.getString(getString(R.string.pref_font_family), defaultPrefValue);

        if (!typeFaceValue.equals(defaultPrefValue)) {
            if (typeFaceValue.equals(delius)) {
                return ResourcesCompat.getFont(context, R.font.delius);
            } else if (typeFaceValue.equals(expletus)) {
                return ResourcesCompat.getFont(context, R.font.expletus);
            } else if (typeFaceValue.equals(tangerine)) {
                return ResourcesCompat.getFont(context, R.font.tangerine);
            }
        }
        return null;
    }
}
