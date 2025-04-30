package com.glia.exampleapp;

import android.content.SharedPreferences;
import android.content.res.Resources;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.glia.widgets.authentication.Authentication;

import org.json.JSONException;
import org.json.JSONObject;

class Utils {

    public static String getStringFromPrefs(@StringRes int keyValue, String defaultValue, SharedPreferences sharedPreferences, Resources resources) {
        return sharedPreferences.getString(resources.getString(keyValue), defaultValue);
    }

    public static void putStringToPrefs(@StringRes int keyValue, String value, SharedPreferences sharedPreferences, Resources resources) {
        sharedPreferences.edit().putString(resources.getString(keyValue), value).apply();
    }

    @Nullable
    public static String getRemoteThemeByPrefs(SharedPreferences sharedPreferences, Resources resources) throws JSONException {
        boolean isRemoteThemeEnabled = sharedPreferences.getBoolean(resources.getString(R.string.pref_remote_theme), false);
        if (!isRemoteThemeEnabled) {
            return null;
        }

        Integer brandPrimaryColorId = getColorValueFromPrefs(R.string.pref_brand_primary_color, sharedPreferences, resources);
        Integer brandSecondaryColorId = getColorValueFromPrefs(R.string.pref_brand_secondary_color, sharedPreferences, resources);
        Integer baseNormalColorId = getColorValueFromPrefs(R.string.pref_base_normal_color, sharedPreferences, resources);
        Integer baseLightColorId = getColorValueFromPrefs(R.string.pref_base_light_color, sharedPreferences, resources);
        Integer baseDarkColorId = getColorValueFromPrefs(R.string.pref_base_dark_color, sharedPreferences, resources);
        Integer baseShadeColorId = getColorValueFromPrefs(R.string.pref_base_shade_color, sharedPreferences, resources);
        Integer backgroundColorId = getColorValueFromPrefs(R.string.pref_background_color, sharedPreferences, resources);
        Integer systemNegativeColorId = getColorValueFromPrefs(R.string.pref_system_negative_color, sharedPreferences, resources);

        JSONObject remoteThemeJson = new JSONObject()
                .put("globalColors", new JSONObject()
                        .put("primary", toHexColor(brandPrimaryColorId, resources))
                        .put("secondary", toHexColor(brandSecondaryColorId, resources))
                        .put("baseNormal", toHexColor(baseNormalColorId, resources))
                        .put("baseLight", toHexColor(baseLightColorId, resources))
                        .put("baseDark", toHexColor(baseDarkColorId, resources))
                        .put("baseShade", toHexColor(baseShadeColorId, resources))
                        .put("background", toHexColor(backgroundColorId, resources))
                        .put("systemNegative", toHexColor(systemNegativeColorId, resources))
                );
        return remoteThemeJson.toString();
    }

    private static String toHexColor(Integer intColor, Resources resources) {
        if (intColor == null) {
            return null;
        }

        // %08X gives you zero-padded hex that is 8 chars long)
        return String.format("#%08X", resources.getColor(intColor, null));
    }
    public static Integer getColorValueFromPrefs(@StringRes int keyValue, SharedPreferences sharedPreferences, Resources resources) {
        String colorGrey = resources.getString(R.string.color_grey_value);
        String colorRed = resources.getString(R.string.color_red_value);
        String colorBlue = resources.getString(R.string.color_blue_value);
        String colorWhite = resources.getString(R.string.color_white_value);
        String colorBlack = resources.getString(R.string.color_black_value);
        String colorDarkGrayishBlue = resources.getString(R.string.color_dark_grayish_blue);
        String colorPureYellow = resources.getString(R.string.color_pure_yellow);
        String colorDarkCyan = resources.getString(R.string.color_dark_cyan);
        String colorVeryDarkBlue = resources.getString(R.string.color_very_dark_blue);
        String colorVeryDarkGrayishBlue = resources.getString(R.string.color_very_dark_grayish_blue);
        String colorVeryLightGray = resources.getString(R.string.color_very_light_gray);

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
            } else if (colorValue.equals(colorDarkGrayishBlue)) {
                return R.color.color_dark_grayish_blue;
            } else if (colorValue.equals(colorPureYellow)) {
                return R.color.color_pure_yellow;
            } else if (colorValue.equals(colorDarkCyan)) {
                return R.color.color_dark_cyan;
            } else if (colorValue.equals(colorVeryDarkBlue)) {
                return R.color.color_very_dark_blue;
            } else if (colorValue.equals(colorVeryDarkGrayishBlue)) {
                return R.color.color_very_dark_grayish_blue;
            } else if (colorValue.equals(colorVeryLightGray)) {
                return R.color.color_very_light_gray;
            }
        }
        return null;
    }

    public static Authentication.Behavior getAuthenticationBehaviorFromPrefs(
        SharedPreferences sharedPreferences,
        Resources resources
    ) {
        String allowedDuringEngagementValue = resources.getString(R.string.authentication_behavior_allowed_during_engagement);
        String valueFromPrefs = sharedPreferences.getString(
            resources.getString(R.string.pref_authentication_behavior),
            allowedDuringEngagementValue
        );

        if (valueFromPrefs.equals(allowedDuringEngagementValue)) {
            return Authentication.Behavior.ALLOWED_DURING_ENGAGEMENT;
        } else {
            return Authentication.Behavior.FORBIDDEN_DURING_ENGAGEMENT;
        }
    }
}
