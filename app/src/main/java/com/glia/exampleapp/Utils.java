package com.glia.exampleapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Resources;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;

import com.glia.androidsdk.Engagement;
import com.glia.androidsdk.screensharing.ScreenSharing;
import com.glia.androidsdk.visitor.Authentication;
import com.glia.widgets.GliaWidgets;
import com.glia.widgets.UiTheme;
import com.glia.widgets.view.configuration.ButtonConfiguration;
import com.glia.widgets.view.configuration.ChatHeadConfiguration;
import com.glia.widgets.view.configuration.TextConfiguration;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.InvalidParameterException;

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

    @Nullable
    public static UiTheme getRunTimeThemeByPrefs(SharedPreferences sharedPreferences, Resources resources) {
        boolean isRunTimeThemeEnabled = sharedPreferences.getBoolean(resources.getString(R.string.pref_runtime_theme), false);
        if (!isRunTimeThemeEnabled) {
            return null;
        }

        String title = Utils.getStringFromPrefs(R.string.pref_header_title, null, sharedPreferences, resources);
        Integer baseLightColor = getColorValueFromPrefs(R.string.pref_base_light_color, sharedPreferences, resources);
        Integer baseDarkColor = getColorValueFromPrefs(R.string.pref_base_dark_color, sharedPreferences, resources);
        Integer baseNormalColor = getColorValueFromPrefs(R.string.pref_base_normal_color, sharedPreferences, resources);
        Integer baseShadeColor = getColorValueFromPrefs(R.string.pref_base_shade_color, sharedPreferences, resources);
        Integer brandPrimaryColor = getColorValueFromPrefs(R.string.pref_brand_primary_color, sharedPreferences, resources);
        Integer systemAgentBubbleColor = getColorValueFromPrefs(R.string.pref_system_agent_bubble_color, sharedPreferences, resources);
        Integer chatSendMessageButtonTintColor = getColorValueFromPrefs(R.string.pref_send_message_button_tint_color, sharedPreferences, resources);
        Integer chatHeaderTitleTintColor = getColorValueFromPrefs(R.string.pref_chat_header_title_tint_color, sharedPreferences, resources);
        Integer chatHeaderHomeButtonTintColor = getColorValueFromPrefs(R.string.pref_chat_header_home_button_tint_color, sharedPreferences, resources);
        Integer chatHeaderExitQueueButtonTintColor = getColorValueFromPrefs(R.string.pref_chat_header_exit_queue_button_tint_color, sharedPreferences, resources);
        Integer chatStartingHeadingTextColorRes = getColorValueFromPrefs(R.string.pref_chat_starting_heading_text_color, sharedPreferences, resources);
        Integer chatStartingCaptionTextColorRes = getColorValueFromPrefs(R.string.pref_chat_starting_caption_text_color, sharedPreferences, resources);
        Integer chatStartedHeadingTextColorRes = getColorValueFromPrefs(R.string.pref_chat_started_heading_text_color, sharedPreferences, resources);
        Integer chatStartedCaptionTextColorRes = getColorValueFromPrefs(R.string.pref_chat_started_caption_text_color, sharedPreferences, resources);
        Integer fontFamily = getTypefaceFromPrefs(sharedPreferences, resources);
        Integer systemNegativeColor = getColorValueFromPrefs(R.string.pref_system_negative_color, sharedPreferences, resources);
        Integer visitorMessageBackgroundColor = getColorValueFromPrefs(R.string.pref_visitor_message_bg_color, sharedPreferences, resources);
        Integer visitorMessageTextColor = getColorValueFromPrefs(R.string.pref_visitor_message_txt_color, sharedPreferences, resources);
        Integer operatorMessageBackgroundColor = getColorValueFromPrefs(R.string.pref_operator_message_bg_color, sharedPreferences, resources);
        Integer operatorMessageTextColor = getColorValueFromPrefs(R.string.pref_operator_message_txt_color, sharedPreferences, resources);
        Integer botActionButtonBackgroundColor = getColorValueFromPrefs(R.string.pref_bot_action_button_bg_color, sharedPreferences, resources);
        Integer botActionButtonTextColor = getColorValueFromPrefs(R.string.pref_bot_action_button_txt_color, sharedPreferences, resources);
        Integer botActionButtonSelectedBackgroundColor = getColorValueFromPrefs(R.string.pref_bot_action_button_selected_bg_color, sharedPreferences, resources);
        Integer botActionButtonSelectedTextColor = getColorValueFromPrefs(R.string.pref_bot_action_button_selected_txt_color, sharedPreferences, resources);

        Boolean whiteLabel = sharedPreferences.getBoolean(resources.getString(R.string.pref_white_label), false);

        Boolean gliaAlertDialogButtonUseVerticalAlignment = sharedPreferences.getBoolean(
            resources.getString(R.string.pref_use_alert_dialog_button_vertical_alignment),
            false
        );

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
        builder.setVisitorMessageBackgroundColor(visitorMessageBackgroundColor);
        builder.setVisitorMessageTextColor(visitorMessageTextColor);
        builder.setOperatorMessageBackgroundColor(operatorMessageBackgroundColor);
        builder.setOperatorMessageTextColor(operatorMessageTextColor);

        builder.setWhiteLabel(whiteLabel);

        // choice card attributes
        builder.setChoiceCardContentTextConfiguration(null);
        builder.setBotActionButtonBackgroundColor(botActionButtonBackgroundColor);
        builder.setBotActionButtonTextColor(botActionButtonTextColor);
        builder.setBotActionButtonSelectedBackgroundColor(botActionButtonSelectedBackgroundColor);
        builder.setBotActionButtonSelectedTextColor(botActionButtonSelectedTextColor);

        // to set alert buttons to align vertically in runtime
        builder.setGliaAlertDialogButtonUseVerticalAlignment(gliaAlertDialogButtonUseVerticalAlignment);

        // here goes header end engagement button configuration
        builder.setHeaderEndButtonConfiguration(null);
        builder.setPositiveButtonConfiguration(null);
        builder.setNegativeButtonConfiguration(null);
        builder.setNeutralButtonConfiguration(null);

        builder.setChatHeadConfiguration(
            getChatHeadConfiguration(
                brandPrimaryColor,
                baseLightColor
            )
        );

        builder.setSendMessageButtonTintColor(chatSendMessageButtonTintColor);

        builder.setGliaChatHeaderTitleTintColor(chatHeaderTitleTintColor);
        builder.setGliaChatHeaderHomeButtonTintColor(chatHeaderHomeButtonTintColor);
        builder.setGliaChatHeaderExitQueueButtonTintColor(chatHeaderExitQueueButtonTintColor);

        builder.setChatStartingHeadingTextColor(chatStartingHeadingTextColorRes);
        builder.setChatStartingCaptionTextColor(chatStartingCaptionTextColorRes);
        builder.setChatStartedHeadingTextColor(chatStartedHeadingTextColorRes);
        builder.setChatStartedCaptionTextColor(chatStartedCaptionTextColorRes);
        return builder.build();
    }

    // Positive button configuration Example runtime
    private static ButtonConfiguration getPositiveButtonTestingConfiguration(Context context) {
        return ButtonConfiguration.builder()
                .backgroundColor(
                        ColorStateList.valueOf(ContextCompat.getColor(context, R.color.color_blue))
                )
                .textConfiguration(
                        TextConfiguration.builder()
                                .textColor(
                                        ColorStateList.valueOf(ContextCompat.getColor(context, R.color.color_black))
                                )
                                .build()
                )
                .build();
    }

    // Negative button configuration Example runtime
    private static ButtonConfiguration getNegativeButtonTestingConfiguration(Context context) {
        return ButtonConfiguration.builder()
                .backgroundColor(
                        ColorStateList.valueOf(ContextCompat.getColor(context, R.color.color_white))
                )
                .textConfiguration(
                        TextConfiguration.builder()
                                .textColor(
                                        ColorStateList.valueOf(ContextCompat.getColor(context, R.color.color_black))
                                )
                                .build()
                )
                .strokeWidth(1)
                .strokeColor(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.color_black)))
                .build();
    }

    private static ChatHeadConfiguration getChatHeadConfiguration(
            Integer colorPrimary,
            Integer colorBright
    ) {
        return ChatHeadConfiguration.builder()
                .backgroundColorRes(colorPrimary)
                .operatorPlaceholderIconTintList(colorBright)
                .operatorPlaceholderBackgroundColor(colorPrimary)
                .badgeTextColor(colorBright)
                .badgeBackgroundTintList(colorPrimary)
                .build();
    }

    private static TextConfiguration getChoiceCardContentTextConfiguration(Context context) {
        return TextConfiguration.builder()
                .textSize(16f)      // in sp
                .fontFamily(R.font.tangerine)
                .textColor(ContextCompat.getColorStateList(context, R.color.color_dark_cyan))
                .build();
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

    public static ScreenSharing.Mode getScreenSharingModeFromPrefs(
            SharedPreferences sharedPreferences,
            Resources resources
    ) {
        String unboundedValue = resources.getString(R.string.screen_sharing_mode_unbounded);
        String appBoundedValue = resources.getString(R.string.screen_sharing_mode_app_bounded);
        String valueFromPrefs = sharedPreferences.getString(
                resources.getString(R.string.pref_screen_sharing_mode),
                unboundedValue
        );

        if (valueFromPrefs.equals(appBoundedValue)) {
            return ScreenSharing.Mode.APP_BOUNDED;
        } else {
            return ScreenSharing.Mode.UNBOUNDED;
        }
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

        if (valueFromPrefs != null && valueFromPrefs.equals(allowedDuringEngagementValue)) {
            return Authentication.Behavior.ALLOWED_DURING_ENGAGEMENT;
        } else {
            return Authentication.Behavior.FORBIDDEN_DURING_ENGAGEMENT;
        }
    }

    public static Engagement.MediaType toMediaType(@NonNull String mediaType) {
        switch (mediaType) {
            case GliaWidgets.MEDIA_TYPE_VIDEO:
                return Engagement.MediaType.VIDEO;
            case GliaWidgets.MEDIA_TYPE_AUDIO:
                return Engagement.MediaType.AUDIO;
            default:
                throw new InvalidParameterException("Invalid Media Type");
        }
    }
}
