package com.glia.exampleapp;

import android.content.SharedPreferences;
import android.content.res.Resources;

import androidx.annotation.StringRes;

import com.glia.widgets.UiTheme;

class Utils {

    public static String getStringFromPrefs(@StringRes int keyValue, String defaultValue, SharedPreferences sharedPreferences, Resources resources) {
        return sharedPreferences.getString(resources.getString(keyValue), defaultValue);
    }

    public static UiTheme getUiThemeByPrefs(SharedPreferences sharedPreferences, Resources resources) {
        String title = Utils.getStringFromPrefs(R.string.pref_header_title, null, sharedPreferences, resources);
        Integer baseLightColor = getColorValueFromPrefs(R.string.pref_base_light_color, sharedPreferences, resources);
        Integer baseDarkColor = getColorValueFromPrefs(R.string.pref_base_dark_color, sharedPreferences, resources);
        Integer baseNormalColor = getColorValueFromPrefs(R.string.pref_base_normal_color, sharedPreferences, resources);
        Integer baseShadeColor = getColorValueFromPrefs(R.string.pref_base_shade_color, sharedPreferences, resources);
        Integer brandPrimaryColor = getColorValueFromPrefs(R.string.pref_brand_primary_color, sharedPreferences, resources);
        Integer systemAgentBubbleColor = getColorValueFromPrefs(R.string.pref_system_agent_bubble_color, sharedPreferences, resources);
        Integer fontFamily = getTypefaceFromPrefs(sharedPreferences, resources);
        Integer systemNegativeColor = getColorValueFromPrefs(R.string.pref_system_negative_color, sharedPreferences, resources);
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

    public static Integer getColorValueFromPrefs(@StringRes int keyValue, SharedPreferences sharedPreferences, Resources resources) {
        String colorGrey = resources.getString(R.string.color_grey_value);
        String colorRed = resources.getString(R.string.color_red_value);
        String colorBlue = resources.getString(R.string.color_blue_value);
        String colorWhite = resources.getString(R.string.color_white_value);
        String colorBlack = resources.getString(R.string.color_black_value);
        String defaultPrefValue = resources.getString(R.string.settings_value_default);

        String colorValue = sharedPreferences.getString(resources.getString(keyValue), defaultPrefValue);

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

    public static Integer getTypefaceFromPrefs(SharedPreferences sharedPreferences, Resources resources) {
        String delius = resources.getString(R.string.font_delius_value);
        String expletus = resources.getString(R.string.font_expletus_value);
        String tangerine = resources.getString(R.string.font_tangerine_value);
        String defaultPrefValue = resources.getString(R.string.settings_value_default);

        String typeFaceValue = sharedPreferences.getString(resources.getString(R.string.pref_font_family), defaultPrefValue);

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
