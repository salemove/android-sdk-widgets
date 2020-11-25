package com.glia.exampleapp;

import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
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

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        view.findViewById(R.id.settings_button).setOnClickListener(view1 -> {
            NavController navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
            navController.navigate(R.id.settings);
        });
        view.findViewById(R.id.embed_button).setOnClickListener(view1 -> {
            ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(
                    ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT);
            ChatView chatView = new ChatView(getContext());
            ((ConstraintLayout) view).addView(chatView, params);
            ColorStateList bgColor = getColorValueFromPrefs(R.string.pref_bg_color, sharedPreferences);
            ColorStateList receiverBgColor = getColorValueFromPrefs(R.string.pref_primary_color, sharedPreferences);
            ColorStateList senderBgColor = getColorValueFromPrefs(R.string.pref_sender_bg_color, sharedPreferences);
            chatView.setupUiConfig(new UiTheme(bgColor, senderBgColor, receiverBgColor));
        });
    }

    ColorStateList getColorValueFromPrefs(@StringRes int keyValue, SharedPreferences sharedPreferences) {
        String colorGrey = getString(R.string.color_grey_value);
        String colorRed = getString(R.string.color_red_value);
        String colorBlue = getString(R.string.color_blue_value);
        String defaultPrefValue = getString(R.string.settings_value_default);

        String colorValue = sharedPreferences.getString(getString(keyValue), defaultPrefValue);

        if (!colorValue.equals(defaultPrefValue)) {
            if (colorValue.equals(colorGrey)) {
                return ResourcesCompat.getColorStateList(getResources(), R.color.color_grey, null);
            } else if (colorValue.equals(colorRed)) {
                return ResourcesCompat.getColorStateList(getResources(), R.color.color_red, null);
            } else if (colorValue.equals(colorBlue)) {
                return ResourcesCompat.getColorStateList(getResources(), R.color.color_blue, null);
            }
        }
        return null;
    }
}
