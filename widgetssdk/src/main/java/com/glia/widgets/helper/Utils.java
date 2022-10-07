package com.glia.widgets.helper;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.provider.OpenableColumns;
import android.util.TypedValue;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleableRes;

import com.glia.androidsdk.Engagement;
import com.glia.androidsdk.Operator;
import com.glia.androidsdk.chat.AttachmentFile;
import com.glia.widgets.GliaWidgets;
import com.glia.widgets.R;
import com.glia.widgets.UiTheme;
import com.glia.widgets.core.fileupload.model.FileAttachment;
import com.glia.widgets.view.configuration.ButtonConfiguration;
import com.glia.widgets.view.configuration.ChatHeadConfiguration;
import com.glia.widgets.view.configuration.ColorConfiguration;
import com.glia.widgets.view.configuration.LayerConfiguration;
import com.glia.widgets.view.configuration.TextConfiguration;
import com.glia.widgets.view.configuration.call.BarButtonConfiguration;
import com.glia.widgets.view.configuration.call.BarButtonStatesConfiguration;
import com.glia.widgets.view.configuration.call.ButtonBarConfiguration;
import com.glia.widgets.view.configuration.call.CallStyle;
import com.glia.widgets.view.configuration.survey.SurveyStyle;
import com.glia.widgets.view.unifieduiconfig.component.RemoteConfiguration;

import java.io.File;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class Utils {
    public static String getOperatorImageUrl(Operator operator) {
        return operator.getPicture() != null ? operator.getPicture().getURL().orElse(null) : null;
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

    public static float pxFromDp(final Context context, final float dp) {
        return dp * context.getResources().getDisplayMetrics().density;
    }

    public static String toMmSs(int seconds) {
        return String.format(Locale.getDefault(), "%02d:%02d", seconds / 60, seconds % 60);
    }

    public static String toMmSs(long milliseconds) {
        return toMmSs(Long.valueOf(TimeUnit.MILLISECONDS.toSeconds(milliseconds)).intValue());
    }

    public static float pxToSp(Context context, float pixels) {
        return pixels / context.getResources().getDisplayMetrics().scaledDensity;
    }

    public static String formatOperatorName(String operatorName) {
        if (operatorName == null) return "";
        int i = operatorName.indexOf(' ');
        if (i != -1) {
            return operatorName.substring(0, i);
        } else {
            return operatorName;
        }
    }

    public static UiTheme getThemeFromTypedArray(TypedArray typedArray, Context context) {
        UiTheme.UiThemeBuilder defaultThemeBuilder = new UiTheme.UiThemeBuilder();
        defaultThemeBuilder.setAppBarTitle(getAppBarTitleValue(typedArray));
        defaultThemeBuilder.setBrandPrimaryColor(
                getTypedArrayIntegerValue(
                        typedArray,
                        context,
                        R.styleable.GliaView_brandPrimaryColor,
                        R.attr.gliaBrandPrimaryColor));
        defaultThemeBuilder.setBaseLightColor(
                getTypedArrayIntegerValue(
                        typedArray,
                        context,
                        R.styleable.GliaView_baseLightColor,
                        R.attr.gliaBaseLightColor
                )
        );
        defaultThemeBuilder.setBaseDarkColor(
                getTypedArrayIntegerValue(
                        typedArray,
                        context,
                        R.styleable.GliaView_baseDarkColor,
                        R.attr.gliaBaseDarkColor
                )
        );
        defaultThemeBuilder.setBaseNormalColor(
                getTypedArrayIntegerValue(
                        typedArray,
                        context,
                        R.styleable.GliaView_baseNormalColor,
                        R.attr.gliaBaseNormalColor
                )
        );
        defaultThemeBuilder.setBaseShadeColor(
                getTypedArrayIntegerValue(
                        typedArray,
                        context,
                        R.styleable.GliaView_baseShadeColor,
                        R.attr.gliaBaseShadeColor
                )
        );
        defaultThemeBuilder.setSystemAgentBubbleColor(
                getTypedArrayIntegerValue(
                        typedArray,
                        context,
                        R.styleable.GliaView_systemAgentBubbleColor,
                        R.attr.gliaSystemAgentBubbleColor
                )
        );
        defaultThemeBuilder.setSystemNegativeColor(
                getTypedArrayIntegerValue(
                        typedArray,
                        context,
                        R.styleable.GliaView_systemNegativeColor,
                        R.attr.gliaSystemNegativeColor
                )
        );
        defaultThemeBuilder.setVisitorMessageBackgroundColor(
                getTypedArrayIntegerValue(
                        typedArray,
                        context,
                        R.styleable.GliaView_visitorMessageBackgroundColor,
                        R.attr.gliaVisitorMessageBackgroundColor
                )
        );
        defaultThemeBuilder.setVisitorMessageTextColor(
                getTypedArrayIntegerValue(
                        typedArray,
                        context,
                        R.styleable.GliaView_visitorMessageTextColor,
                        R.attr.gliaVisitorMessageTextColor
                )
        );
        defaultThemeBuilder.setOperatorMessageBackgroundColor(
                getTypedArrayIntegerValue(
                        typedArray,
                        context,
                        R.styleable.GliaView_operatorMessageBackgroundColor,
                        R.attr.gliaOperatorMessageBackgroundColor
                )
        );
        defaultThemeBuilder.setOperatorMessageTextColor(
                getTypedArrayIntegerValue(
                        typedArray,
                        context,
                        R.styleable.GliaView_operatorMessageTextColor,
                        R.attr.operatorMessageTextColor
                )
        );
        defaultThemeBuilder.setBotActionButtonBackgroundColor(
                getTypedArrayIntegerValue(
                        typedArray,
                        context,
                        R.styleable.GliaView_botActionButtonBackgroundColor,
                        R.attr.gliaBotActionButtonBackgroundColor
                )
        );
        defaultThemeBuilder.setBotActionButtonTextColor(
                getTypedArrayIntegerValue(
                        typedArray,
                        context,
                        R.styleable.GliaView_botActionButtonTextColor,
                        R.attr.gliaBotActionButtonTextColor
                )
        );
        defaultThemeBuilder.setBotActionButtonSelectedBackgroundColor(
                getTypedArrayIntegerValue(
                        typedArray,
                        context,
                        R.styleable.GliaView_botActionButtonSelectedBackgroundColor,
                        R.attr.gliaBotActionButtonSelectedBackgroundColor
                )
        );
        defaultThemeBuilder.setBotActionButtonSelectedTextColor(
                getTypedArrayIntegerValue(
                        typedArray,
                        context,
                        R.styleable.GliaView_botActionButtonSelectedTextColor,
                        R.attr.gliaBotActionButtonSelectedTextColor
                )
        );
        defaultThemeBuilder.setSendMessageButtonTintColor(
                getTypedArrayIntegerValue(
                        typedArray,
                        context,
                        R.styleable.GliaView_chatSendMessageButtonTintColor,
                        R.attr.gliaSendMessageButtonTintColor
                )
        );
        defaultThemeBuilder.setGliaChatBackgroundColor(
                getTypedArrayIntegerValue(
                        typedArray,
                        context,
                        R.styleable.GliaView_gliaChatBackgroundColor,
                        R.attr.gliaChatBackgroundColor
                )
        );
        defaultThemeBuilder.setGliaChatHeaderTitleTintColor(
                getTypedArrayIntegerValue(
                        typedArray,
                        context,
                        R.styleable.GliaView_chatHeaderTitleTintColor,
                        R.attr.gliaChatHeaderTitleTintColor
                )
        );
        defaultThemeBuilder.setGliaChatHeaderHomeButtonTintColor(
                getTypedArrayIntegerValue(
                        typedArray,
                        context,
                        R.styleable.GliaView_chatHeaderHomeButtonTintColor,
                        R.attr.gliaChatHeaderHomeButtonTintColor
                )
        );
        defaultThemeBuilder.setGliaChatHeaderExitQueueButtonTintColor(
                getTypedArrayIntegerValue(
                        typedArray,
                        context,
                        R.styleable.GliaView_chatHeaderExitQueueButtonTintColor,
                        R.attr.gliaChatHeaderExitQueueButtonTintColor
                )
        );
        defaultThemeBuilder.setChatStartedHeadingTextColor(
                getTypedArrayIntegerValue(
                        typedArray,
                        context,
                        R.styleable.GliaView_chatStartedHeadingTextColor,
                        R.attr.chatStartedHeadingTextColor
                )
        );
        defaultThemeBuilder.setChatStartedCaptionTextColor(
                getTypedArrayIntegerValue(
                        typedArray,
                        context,
                        R.styleable.GliaView_chatStartedCaptionTextColor,
                        R.attr.chatStartedCaptionTextColor
                )
        );
        defaultThemeBuilder.setChatStartingHeadingTextColor(
                getTypedArrayIntegerValue(
                        typedArray,
                        context,
                        R.styleable.GliaView_chatStartingHeadingTextColor,
                        R.attr.chatStartingHeadingTextColor
                )
        );
        defaultThemeBuilder.setChatStartingCaptionTextColor(
                getTypedArrayIntegerValue(
                        typedArray,
                        context,
                        R.styleable.GliaView_chatStartingCaptionTextColor,
                        R.attr.chatStartingCaptionTextColor
                )
        );
        defaultThemeBuilder.setFontRes(
                getTypedArrayIntegerValue(
                        typedArray,
                        context,
                        R.styleable.GliaView_android_fontFamily,
                        R.attr.fontFamily
                )
        );
        defaultThemeBuilder.setIconAppBarBack(
                getTypedArrayIntegerValue(
                        typedArray,
                        context,
                        R.styleable.GliaView_iconAppBarBack,
                        R.attr.gliaIconAppBarBack
                )
        );
        defaultThemeBuilder.setIconLeaveQueue(
                getTypedArrayIntegerValue(
                        typedArray,
                        context,
                        R.styleable.GliaView_iconLeaveQueue,
                        R.attr.gliaIconLeaveQueue
                )
        );
        defaultThemeBuilder.setIconSendMessage(
                getTypedArrayIntegerValue(
                        typedArray,
                        context,
                        R.styleable.GliaView_iconSendMessage,
                        R.attr.gliaIconSendMessage
                )
        );
        defaultThemeBuilder.setIconChatAudioUpgrade(
                getTypedArrayIntegerValue(
                        typedArray,
                        context,
                        R.styleable.GliaView_iconChatAudioUpgrade,
                        R.attr.gliaIconChatAudioUpgrade
                )
        );
        defaultThemeBuilder.setIconUpgradeAudioDialog(
                getTypedArrayIntegerValue(
                        typedArray,
                        context,
                        R.styleable.GliaView_iconUpgradeAudioDialog,
                        R.attr.gliaIconUpgradeAudioDialog
                )
        );
        defaultThemeBuilder.setIconChatVideoUpgrade(
                getTypedArrayIntegerValue(
                        typedArray,
                        context,
                        R.styleable.GliaView_iconChatVideoUpgrade,
                        R.attr.gliaIconChatVideoUpgrade
                )
        );
        defaultThemeBuilder.setIconUpgradeVideoDialog(
                getTypedArrayIntegerValue(
                        typedArray,
                        context,
                        R.styleable.GliaView_iconUpgradeVideoDialog,
                        R.attr.gliaIconUpgradeVideoDialog
                )
        );
        defaultThemeBuilder.setIconScreenSharingDialog(
                getTypedArrayIntegerValue(
                        typedArray,
                        context,
                        R.styleable.GliaView_iconScreenSharingDialog,
                        R.attr.gliaIconScreenSharingDialog
                )
        );
        defaultThemeBuilder.setIconPlaceholder(
                getTypedArrayIntegerValue(
                        typedArray,
                        context,
                        R.styleable.GliaView_iconPlaceholder,
                        R.attr.gliaIconPlaceholder
                )
        );
        defaultThemeBuilder.setIconOnHold(
                getTypedArrayIntegerValue(
                        typedArray,
                        context,
                        R.styleable.GliaView_iconOnHold,
                        R.attr.gliaIconOnHold
                )
        );
        defaultThemeBuilder.setWhiteLabel(
                getTypedArrayBooleanValue(
                        typedArray,
                        R.styleable.GliaView_whiteLabel
                )
        );
        defaultThemeBuilder.setGliaAlertDialogButtonUseVerticalAlignment(
                getTypedArrayBooleanValue(
                        typedArray,
                        R.styleable.GliaView_gliaAlertDialogButtonUseVerticalAlignment
                )
        );

        defaultThemeBuilder.setCallStyle(getCallStyleFromTypedArray(typedArray, context));

        return defaultThemeBuilder.build();
    }

    private static CallStyle getCallStyleFromTypedArray(TypedArray typedArray, Context context) {
        CallStyle.Builder builder = new CallStyle.Builder();

        builder.setButtonBar(getButtonBarConfigurationFromTypedArray(typedArray, context));

        return builder.build();
    }

    public static ButtonBarConfiguration getButtonBarConfigurationFromTypedArray(TypedArray typedArray, Context context) {
        ButtonBarConfiguration.Builder builder = new ButtonBarConfiguration.Builder();

        BarButtonStatesConfiguration.Builder chatButtonStatesBuilder = new BarButtonStatesConfiguration.Builder();
        BarButtonConfiguration.Builder chatButtonActiveBuilder = new BarButtonConfiguration.Builder();
        chatButtonActiveBuilder.setImageRes(
                getTypedArrayIntegerValue(
                        typedArray,
                        context,
                        R.styleable.GliaView_iconCallChat,
                        R.attr.gliaIconCallChat
                )
        );
        chatButtonStatesBuilder.setActive(chatButtonActiveBuilder.build());
        builder.setChatButton(chatButtonStatesBuilder.build());

        BarButtonStatesConfiguration.Builder minimizeButtonStatesBuilder = new BarButtonStatesConfiguration.Builder();
        BarButtonConfiguration.Builder minimizeButtonActiveBuilder = new BarButtonConfiguration.Builder();
        minimizeButtonActiveBuilder.setImageRes(
                getTypedArrayIntegerValue(
                        typedArray,
                        context,
                        R.styleable.GliaView_iconCallMinimize,
                        R.attr.gliaIconCallMinimize
                )
        );
        minimizeButtonStatesBuilder.setActive(minimizeButtonActiveBuilder.build());
        builder.setMinimizeButton(minimizeButtonStatesBuilder.build());

        BarButtonStatesConfiguration.Builder muteButtonStatesBuilder = new BarButtonStatesConfiguration.Builder();
        BarButtonConfiguration.Builder muteButtonActiveBuilder = new BarButtonConfiguration.Builder();
        muteButtonActiveBuilder.setImageRes(
                getTypedArrayIntegerValue(
                        typedArray,
                        context,
                        R.styleable.GliaView_iconCallAudioOn,
                        R.attr.gliaIconCallAudioOn
                )
        );
        muteButtonStatesBuilder.setActive(muteButtonActiveBuilder.build());
        BarButtonConfiguration.Builder muteButtonSelectedBuilder = new BarButtonConfiguration.Builder();
        muteButtonSelectedBuilder.setImageRes(
                getTypedArrayIntegerValue(
                        typedArray,
                        context,
                        R.styleable.GliaView_iconCallAudioOff,
                        R.attr.gliaIconCallAudioOff
                )
        );
        muteButtonStatesBuilder.setSelected(muteButtonSelectedBuilder.build());
        builder.setMuteButton(muteButtonStatesBuilder.build());

        BarButtonStatesConfiguration.Builder speakerButtonStatesBuilder = new BarButtonStatesConfiguration.Builder();
        BarButtonConfiguration.Builder speakerButtonActiveBuilder = new BarButtonConfiguration.Builder();
        speakerButtonActiveBuilder.setImageRes(
                getTypedArrayIntegerValue(
                        typedArray,
                        context,
                        R.styleable.GliaView_iconCallSpeakerOff,
                        R.attr.gliaIconCallSpeakerOff
                )
        );
        speakerButtonStatesBuilder.setActive(speakerButtonActiveBuilder.build());
        BarButtonConfiguration.Builder speakerButtonSelectedBuilder = new BarButtonConfiguration.Builder();
        speakerButtonSelectedBuilder.setImageRes(
                getTypedArrayIntegerValue(
                        typedArray,
                        context,
                        R.styleable.GliaView_iconCallSpeakerOn,
                        R.attr.gliaIconCallSpeakerOn
                )
        );
        speakerButtonStatesBuilder.setSelected(speakerButtonSelectedBuilder.build());
        builder.setSpeakerButton(speakerButtonStatesBuilder.build());

        BarButtonStatesConfiguration.Builder videoButtonStatesBuilder = new BarButtonStatesConfiguration.Builder();
        BarButtonConfiguration.Builder videoButtonActiveBuilder = new BarButtonConfiguration.Builder();
        videoButtonActiveBuilder.setImageRes(
                getTypedArrayIntegerValue(
                        typedArray,
                        context,
                        R.styleable.GliaView_iconCallVideoOff,
                        R.attr.gliaIconCallVideoOff
                )
        );
        videoButtonStatesBuilder.setActive(videoButtonActiveBuilder.build());
        BarButtonConfiguration.Builder videoButtonSelectedBuilder = new BarButtonConfiguration.Builder();
        videoButtonSelectedBuilder.setImageRes(
                getTypedArrayIntegerValue(
                        typedArray,
                        context,
                        R.styleable.GliaView_iconCallVideoOn,
                        R.attr.gliaIconCallVideoOn
                )
        );
        videoButtonStatesBuilder.setSelected(videoButtonSelectedBuilder.build());
        builder.setVideoButton(videoButtonStatesBuilder.build());

        return builder.build();
    }

    public static Boolean getGliaAlertDialogButtonUseVerticalAlignment(UiTheme theme) {
        return theme.getGliaAlertDialogButtonUseVerticalAlignment() != null ?
                theme.getGliaAlertDialogButtonUseVerticalAlignment() :
                false;
    }

    private static Boolean getTypedArrayBooleanValue(TypedArray typedArray, int index) {
        return typedArray.hasValue(index) && typedArray.getBoolean(index, false);
    }

    private static String getAppBarTitleValue(TypedArray typedArray) {
        if (typedArray.hasValue(R.styleable.GliaView_appBarTitle)) {
            return typedArray.getString(R.styleable.GliaView_appBarTitle);
        } else {
            return null;
        }
    }

    public static String getTypedArrayStringValue(TypedArray typedArray,
                                                  @StyleableRes int index) {
        if (typedArray.hasValue(index)) {
            return typedArray.getString(index);
        }
        return null;
    }

    public static Integer getTypedArrayIntegerValue(TypedArray typedArray,
                                                    Context context,
                                                    @StyleableRes int index,
                                                    @AttrRes int defaultValue) {
        if (typedArray.hasValue(index)) {
            return typedArray.getResourceId(index, 0);
        } else {
            return getAttrResourceId(context, defaultValue);
        }
    }

    public static Integer getAttrResourceId(Context context,
                                            @AttrRes int attrId) {
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = context.getTheme();
        theme.resolveAttribute(attrId, typedValue, true);
        return typedValue.resourceId;
    }

    public static Activity getActivity(Context context) {
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity) context;
            }
            context = ((ContextWrapper) context).getBaseContext();
        }
        return null;
    }

    public static void hideSoftKeyboard(Context context, IBinder windowToken) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(windowToken, 0);
    }

    private static <T> T getConfiguration(
            T newConf,
            T oldConf
    ) {
        return newConf != null ? newConf : oldConf;
    }

    public static UiTheme getFullHybridTheme(RemoteConfiguration remoteConfiguration, UiTheme newTheme, UiTheme oldTheme) {
        UiTheme defaultUiTheme = UiTheme.getDefaultUiTheme();
        UiTheme fullHybridOldTheme = getFullHybridTheme(oldTheme, defaultUiTheme);
        UiTheme theme = getFullHybridTheme(newTheme, fullHybridOldTheme);
        return ThemeUnits.apply(theme, remoteConfiguration);
    }

    public static UiTheme getFullHybridTheme(UiTheme newTheme, UiTheme oldTheme) {
        String title = newTheme.getAppBarTitle() != null ? newTheme.getAppBarTitle() : oldTheme.getAppBarTitle();
        Integer baseLightColorRes = newTheme.getBaseLightColor() != null ?
                newTheme.getBaseLightColor() : oldTheme.getBaseLightColor();
        Integer baseDarkColorRes = newTheme.getBaseDarkColor() != null ?
                newTheme.getBaseDarkColor() : oldTheme.getBaseDarkColor();
        Integer baseNormalColorRes = newTheme.getBaseNormalColor() != null ?
                newTheme.getBaseNormalColor() : oldTheme.getBaseNormalColor();
        Integer baseShadeColorRes = newTheme.getBaseShadeColor() != null ?
                newTheme.getBaseShadeColor() : oldTheme.getBaseShadeColor();
        Integer brandPriamryColorRes = newTheme.getBrandPrimaryColor() != null ?
                newTheme.getBrandPrimaryColor() : oldTheme.getBrandPrimaryColor();
        Integer systemAgentBubbleColorRes = newTheme.getSystemAgentBubbleColor() != null ?
                newTheme.getSystemAgentBubbleColor() : oldTheme.getSystemAgentBubbleColor();
        Integer chatSendMessageButtonTintColor = newTheme.getSendMessageButtonTintColor() != null ?
                newTheme.getSendMessageButtonTintColor() : oldTheme.getSendMessageButtonTintColor();
        Integer gliaChatBackgroundColorRes = newTheme.getGliaChatBackgroundColor() != null ?
                newTheme.getGliaChatBackgroundColor() : oldTheme.getGliaChatBackgroundColor();
        Integer chatHeaderTitleColor = newTheme.getGliaChatHeaderTitleTintColor() != null ?
                newTheme.getGliaChatHeaderTitleTintColor() : oldTheme.getGliaChatHeaderTitleTintColor();
        Integer chatHeaderHomeButtonTintColor = newTheme.getGliaChatHeaderHomeButtonTintColor() != null ?
                newTheme.getGliaChatHeaderHomeButtonTintColor() : oldTheme.getGliaChatHeaderHomeButtonTintColor();
        Integer chatHeaderExitQueueButtonTintColor = newTheme.getGliaChatHeaderExitQueueButtonTintColor() != null ?
                newTheme.getGliaChatHeaderExitQueueButtonTintColor() : oldTheme.getGliaChatHeaderExitQueueButtonTintColor();
        Integer fontRes = newTheme.getFontRes() != null ? newTheme.getFontRes() : oldTheme.getFontRes();
        Integer systemNegativeColorRes = newTheme.getSystemNegativeColor() != null ?
                newTheme.getSystemNegativeColor() : oldTheme.getSystemNegativeColor();
        Integer visitorMessageBackgroundColorRes = newTheme.getVisitorMessageBackgroundColor() != null ?
                newTheme.getVisitorMessageBackgroundColor() : oldTheme.getVisitorMessageBackgroundColor();
        Integer visitorMessageTextColorRes = newTheme.getVisitorMessageTextColor() != null ?
                newTheme.getVisitorMessageTextColor() : oldTheme.getVisitorMessageTextColor();
        Integer operatorMessageBackgroundColorRes = newTheme.getOperatorMessageBackgroundColor() != null ?
                newTheme.getOperatorMessageBackgroundColor() : oldTheme.getOperatorMessageBackgroundColor();
        Integer operatorMessageTextColorRes = newTheme.getOperatorMessageTextColor() != null ?
                newTheme.getOperatorMessageTextColor() : oldTheme.getOperatorMessageTextColor();
        Integer botActionButtonBackgroundColorRes = newTheme.getBotActionButtonBackgroundColor() != null ?
                newTheme.getBotActionButtonBackgroundColor() : oldTheme.getBotActionButtonBackgroundColor();
        Integer botActionButtonTextColorRes = newTheme.getBotActionButtonTextColor() != null ?
                newTheme.getBotActionButtonTextColor() : oldTheme.getBotActionButtonTextColor();
        Integer botActionButtonSelectedBackgroundColorRes = newTheme.getBotActionButtonSelectedBackgroundColor() != null ?
                newTheme.getBotActionButtonSelectedBackgroundColor() : oldTheme.getBotActionButtonSelectedBackgroundColor();
        Integer botActionButtonSelectedTextColorRes = newTheme.getBotActionButtonSelectedTextColor() != null ?
                newTheme.getBotActionButtonSelectedTextColor() : oldTheme.getBotActionButtonSelectedTextColor();
        Integer chatStartingHeadingTextColorRes = newTheme.getGliaChatStartingHeadingTextColor() != null ?
                newTheme.getGliaChatStartingHeadingTextColor() : oldTheme.getGliaChatStartingHeadingTextColor();
        Integer chatStartingCaptionTextColorRes = newTheme.getGliaChatStartingCaptionTextColor() != null ?
                newTheme.getGliaChatStartingCaptionTextColor() : oldTheme.getGliaChatStartingCaptionTextColor();
        Integer chatStartedCaptionTextColorRes = newTheme.getGliaChatStartedCaptionTextColor() != null ?
                newTheme.getGliaChatStartedCaptionTextColor() : oldTheme.getGliaChatStartedCaptionTextColor();
        Integer chatStartedHeadingTextColorRes = newTheme.getGliaChatStartedHeadingTextColor() != null ?
                newTheme.getGliaChatStartedHeadingTextColor() : oldTheme.getGliaChatStartedHeadingTextColor();
        Integer iconAppBarBack = newTheme.getIconAppBarBack() != null ?
                newTheme.getIconAppBarBack() : oldTheme.getIconAppBarBack();
        Integer iconLeaveQueue = newTheme.getIconLeaveQueue() != null ?
                newTheme.getIconLeaveQueue() : oldTheme.getIconLeaveQueue();
        Integer iconSendMessage = newTheme.getIconSendMessage() != null ?
                newTheme.getIconSendMessage() : oldTheme.getIconSendMessage();
        Integer iconChatAudioUpgrade = newTheme.getIconChatAudioUpgrade() != null ?
                newTheme.getIconChatAudioUpgrade() : oldTheme.getIconChatAudioUpgrade();
        Integer iconUpgradeAudioDialog = newTheme.getIconUpgradeAudioDialog() != null ?
                newTheme.getIconUpgradeAudioDialog() : oldTheme.getIconUpgradeAudioDialog();
        Integer iconChatVideoUpgrade = newTheme.getIconChatVideoUpgrade() != null ?
                newTheme.getIconChatVideoUpgrade() : oldTheme.getIconChatVideoUpgrade();
        Integer iconUpgradeVideoDialog = newTheme.getIconUpgradeVideoDialog() != null ?
                newTheme.getIconUpgradeVideoDialog() : oldTheme.getIconUpgradeVideoDialog();
        Integer iconScreenSharingDialog = newTheme.getIconScreenSharingDialog() != null ?
                newTheme.getIconScreenSharingDialog() : oldTheme.getIconScreenSharingDialog();
        Integer iconPlaceholder = newTheme.getIconPlaceholder() != null ?
                newTheme.getIconPlaceholder() : oldTheme.getIconPlaceholder();
        Integer iconOnHold = newTheme.getIconOnHold() != null ?
                newTheme.getIconOnHold() : oldTheme.getIconOnHold();

        Boolean whiteLabel = newTheme.getWhiteLabel() != null ? newTheme.getWhiteLabel() : oldTheme.getWhiteLabel();
        Boolean isUseAlertDialogButtonVerticalAlignment =
                newTheme.getGliaAlertDialogButtonUseVerticalAlignment() != null ?
                        newTheme.getGliaAlertDialogButtonUseVerticalAlignment() :
                        oldTheme.getGliaAlertDialogButtonUseVerticalAlignment();

        ButtonConfiguration endButtonConfiguration =
                getConfiguration(
                        newTheme.getGliaEndButtonConfiguration(),
                        oldTheme.getGliaEndButtonConfiguration()
                );

        ButtonConfiguration positiveButtonConfiguration =
                getConfiguration(
                        newTheme.getGliaPositiveButtonConfiguration(),
                        oldTheme.getGliaPositiveButtonConfiguration()
                );

        ButtonConfiguration negativeButtonConfiguration =
                getConfiguration(
                        newTheme.getGliaNegativeButtonConfiguration(),
                        oldTheme.getGliaNegativeButtonConfiguration()
                );

        ButtonConfiguration neutralButtonConfiguration =
                getConfiguration(
                        newTheme.getGliaNeutralButtonConfiguration(),
                        oldTheme.getGliaNeutralButtonConfiguration()
                );

        TextConfiguration choiceCardContentTextConfiguration =
                getConfiguration(
                        newTheme.getGliaChoiceCardContentTextConfiguration(),
                        oldTheme.getGliaChoiceCardContentTextConfiguration()
                );

        ChatHeadConfiguration chatHeadConfiguration =
                getConfiguration(
                        newTheme.getChatHeadConfiguration(),
                        oldTheme.getChatHeadConfiguration()
                );

        SurveyStyle surveyStyle =
                getConfiguration(
                        newTheme.getSurveyStyle(),
                        oldTheme.getSurveyStyle()
                );

        CallStyle callStyle = getHybridCallStyle(oldTheme.getCallStyle(), newTheme.getCallStyle());

        UiTheme.UiThemeBuilder builder = new UiTheme.UiThemeBuilder();
        builder.setAppBarTitle(title);
        builder.setBaseLightColor(baseLightColorRes);
        builder.setBaseDarkColor(baseDarkColorRes);
        builder.setBaseNormalColor(baseNormalColorRes);
        builder.setBaseShadeColor(baseShadeColorRes);
        builder.setBrandPrimaryColor(brandPriamryColorRes);
        builder.setSystemAgentBubbleColor(systemAgentBubbleColorRes);
        builder.setSendMessageButtonTintColor(chatSendMessageButtonTintColor);
        builder.setGliaChatHeaderHomeButtonTintColor(chatHeaderHomeButtonTintColor);
        builder.setGliaChatHeaderExitQueueButtonTintColor(chatHeaderExitQueueButtonTintColor);
        builder.setChatStartingHeadingTextColor(chatStartingHeadingTextColorRes);
        builder.setChatStartingCaptionTextColor(chatStartingCaptionTextColorRes);
        builder.setChatStartedHeadingTextColor(chatStartedHeadingTextColorRes);
        builder.setChatStartedCaptionTextColor(chatStartedCaptionTextColorRes);
        builder.setFontRes(fontRes);
        builder.setSystemNegativeColor(systemNegativeColorRes);
        builder.setVisitorMessageBackgroundColor(visitorMessageBackgroundColorRes);
        builder.setVisitorMessageTextColor(visitorMessageTextColorRes);
        builder.setOperatorMessageBackgroundColor(operatorMessageBackgroundColorRes);
        builder.setGliaChatHeaderTitleTintColor(chatHeaderTitleColor);
        builder.setGliaChatBackgroundColor(gliaChatBackgroundColorRes);
        builder.setOperatorMessageTextColor(operatorMessageTextColorRes);
        builder.setBotActionButtonBackgroundColor(botActionButtonBackgroundColorRes);
        builder.setBotActionButtonTextColor(botActionButtonTextColorRes);
        builder.setBotActionButtonSelectedBackgroundColor(botActionButtonSelectedBackgroundColorRes);
        builder.setBotActionButtonSelectedTextColor(botActionButtonSelectedTextColorRes);
        builder.setIconAppBarBack(iconAppBarBack);
        builder.setIconLeaveQueue(iconLeaveQueue);
        builder.setIconSendMessage(iconSendMessage);
        builder.setIconChatAudioUpgrade(iconChatAudioUpgrade);
        builder.setIconUpgradeAudioDialog(iconUpgradeAudioDialog);
        builder.setIconChatVideoUpgrade(iconChatVideoUpgrade);
        builder.setIconUpgradeVideoDialog(iconUpgradeVideoDialog);
        builder.setIconScreenSharingDialog(iconScreenSharingDialog);
        builder.setIconPlaceholder(iconPlaceholder);
        builder.setIconOnHold(iconOnHold);
        builder.setWhiteLabel(whiteLabel);
        builder.setGliaAlertDialogButtonUseVerticalAlignment(isUseAlertDialogButtonVerticalAlignment);
        builder.setHeaderEndButtonConfiguration(endButtonConfiguration);
        builder.setPositiveButtonConfiguration(positiveButtonConfiguration);
        builder.setNegativeButtonConfiguration(negativeButtonConfiguration);
        builder.setNeutralButtonConfiguration(neutralButtonConfiguration);
        builder.setChoiceCardContentTextConfiguration(choiceCardContentTextConfiguration);
        builder.setChatHeadConfiguration(chatHeadConfiguration);
        builder.setSurveyStyle(surveyStyle);
        builder.setCallStyle(callStyle);
        return builder.build();
    }

    public static CallStyle getHybridCallStyle(@NonNull CallStyle newStyle, @Nullable CallStyle oldStyle) {
        CallStyle.Builder builder = new CallStyle.Builder();
        if (oldStyle != null) {
            builder.setCallStyle(oldStyle);
        }

        if (newStyle.getBackground() != null) {
            builder.setBackground(getHybridLayerConfiguration(
                    newStyle.getBackground(),
                    oldStyle != null ? newStyle.getBackground() : null
            ));
        }
        if (newStyle.getBottomText() != null) {
            builder.setBottomText(getHybridTextConfiguration(
                    newStyle.getBottomText(),
                    oldStyle != null ? oldStyle.getBottomText() : null
            ));
        }
        if (newStyle.getBottomText() != null) {
            builder.setBottomText(getHybridTextConfiguration(
                    newStyle.getBottomText(),
                    oldStyle != null ? oldStyle.getBottomText() : null
            ));
        }
        if (newStyle.getButtonBar() != null) {
            builder.setButtonBar(getHybridButtonBarConfiguration(
                    newStyle.getButtonBar(),
                    oldStyle != null ? oldStyle.getButtonBar() : null
            ));
        }
        if (newStyle.getDuration() != null) {
            builder.setDuration(getHybridTextConfiguration(
                    newStyle.getDuration(),
                    oldStyle != null ? oldStyle.getDuration() : null
            ));
        }
        if (newStyle.getOperator() != null) {
            builder.setOperator(getHybridTextConfiguration(
                    newStyle.getOperator(),
                    oldStyle != null ? oldStyle.getOperator() : null
            ));
        }
        if (newStyle.getTopText() != null) {
            builder.setTopText(getHybridTextConfiguration(
                    newStyle.getTopText(),
                    oldStyle != null ? oldStyle.getTopText() : null
            ));
        }
        return builder.build();
    }

    private static ButtonBarConfiguration getHybridButtonBarConfiguration(@NonNull ButtonBarConfiguration newConfiguration,
                                                                          @Nullable ButtonBarConfiguration oldConfiguration) {
        ButtonBarConfiguration.Builder builder = new ButtonBarConfiguration.Builder();
        if (oldConfiguration != null) {
            builder.setButtonBarConfiguration(oldConfiguration);
        }

        if (newConfiguration.getChatButton() != null) {
            builder.setChatButton(getHybridBarButtonStatesConfiguration(
                    newConfiguration.getChatButton(),
                    oldConfiguration != null ? oldConfiguration.getChatButton() : null
            ));
        }
        if (newConfiguration.getMinimizeButton() != null) {
            builder.setMinimizeButton(getHybridBarButtonStatesConfiguration(
                    newConfiguration.getMinimizeButton(),
                    oldConfiguration != null ? oldConfiguration.getMinimizeButton() : null
            ));
        }
        if (newConfiguration.getMuteButton() != null) {
            builder.setMuteButton(getHybridBarButtonStatesConfiguration(
                    newConfiguration.getMuteButton(),
                    oldConfiguration != null ? oldConfiguration.getMuteButton() : null
            ));
        }
        if (newConfiguration.getSpeakerButton() != null) {
            builder.setSpeakerButton(getHybridBarButtonStatesConfiguration(
                    newConfiguration.getSpeakerButton(),
                    oldConfiguration != null ? oldConfiguration.getSpeakerButton() : null
            ));
        }
        if (newConfiguration.getVideoButton() != null) {
            builder.setVideoButton(getHybridBarButtonStatesConfiguration(
                    newConfiguration.getVideoButton(),
                    oldConfiguration != null ? oldConfiguration.getVideoButton() : null
            ));
        }
        return builder.build();
    }

    private static BarButtonStatesConfiguration getHybridBarButtonStatesConfiguration(@NonNull BarButtonStatesConfiguration newConfiguration,
                                                                                      @Nullable BarButtonStatesConfiguration oldConfiguration) {
        BarButtonStatesConfiguration.Builder builder = new BarButtonStatesConfiguration.Builder();
        if (oldConfiguration != null) {
            builder.setBarButtonStatesConfiguration(oldConfiguration);
        }

        if (newConfiguration.getInactive() != null) {
            builder.setInactive(getHybridBarButtonConfiguration(
                    newConfiguration.getInactive(),
                    oldConfiguration != null ? oldConfiguration.getInactive() : null
            ));
        }
        if (newConfiguration.getActive() != null) {
            builder.setActive(getHybridBarButtonConfiguration(
                    newConfiguration.getActive(),
                    oldConfiguration != null ? oldConfiguration.getActive() : null
            ));
        }
        if (newConfiguration.getSelected() != null) {
            builder.setSelected(getHybridBarButtonConfiguration(
                    newConfiguration.getSelected(),
                    oldConfiguration != null ? oldConfiguration.getSelected() : null
            ));
        }
        return builder.build();
    }

    private static BarButtonConfiguration getHybridBarButtonConfiguration(@NonNull BarButtonConfiguration newConfiguration,
                                                                          @Nullable BarButtonConfiguration oldConfiguration) {
        BarButtonConfiguration.Builder builder = new BarButtonConfiguration.Builder();
        if (oldConfiguration != null) {
            builder.setBarButtonConfiguration(oldConfiguration);
        }

        if (newConfiguration.getBackground() != null) {
            builder.setBackground(newConfiguration.getBackground());
        }
        if (newConfiguration.getImageColor() != null) {
            builder.setImageColor(newConfiguration.getImageColor());
        }
        if (newConfiguration.getTitle() != null) {
            builder.setTitle(getHybridTextConfiguration(
                    newConfiguration.getTitle(),
                    oldConfiguration != null ? oldConfiguration.getTitle() : null
            ));
        }
        if (newConfiguration.getImageRes() != null) {
            builder.setImageRes(newConfiguration.getImageRes());
        }
        return builder.build();
    }

    private static TextConfiguration getHybridTextConfiguration(@NonNull TextConfiguration newConfiguration,
                                                                @Nullable TextConfiguration oldConfiguration) {
        TextConfiguration.Builder builder = new TextConfiguration.Builder();
        if (oldConfiguration != null) {
            builder.textConfiguration(oldConfiguration);
        }

        if (newConfiguration.getTextSize() != null) {
            builder.textSize(newConfiguration.getTextSize());
        }
        if (newConfiguration.getTextTypeFaceStyle() != null) {
            builder.textTypeFaceStyle(newConfiguration.getTextTypeFaceStyle());
        }
        if (newConfiguration.getTextColor() != null) {
            builder.textColor(newConfiguration.getTextColor());
        }
        if (newConfiguration.getBackgroundColor() != null) {
            builder.backgroundColor(newConfiguration.getBackgroundColor());
        }
        if (newConfiguration.getTextAlignment() != null) {
            builder.textAlignment(newConfiguration.getTextAlignment());
        }
        if (newConfiguration.getHintColor() != null) {
            builder.hintColor(newConfiguration.getHintColor());
        }
        if (newConfiguration.getTextColorLink() != null) {
            builder.textColorLink(newConfiguration.getTextColorLink());
        }
        if (newConfiguration.getTextColorHighlight() != null) {
            builder.textColorHighlight(newConfiguration.getTextColorHighlight());
        }
        if (newConfiguration.getFontFamily() != null) {
            builder.fontFamily(newConfiguration.getFontFamily());
        }
        if (newConfiguration.isBold() != null) {
            builder.bold(newConfiguration.isBold());
        }
        if (newConfiguration.isAllCaps() != null) {
            builder.allCaps(newConfiguration.isAllCaps());
        }
        return builder.build();
    }

    private static LayerConfiguration getHybridLayerConfiguration(@NonNull LayerConfiguration newConfiguration,
                                                                  @Nullable LayerConfiguration oldConfiguration) {
        LayerConfiguration.Builder builder = new LayerConfiguration.Builder();
        if (oldConfiguration != null) {
            builder.layerConfiguration(oldConfiguration);
        }

        if (newConfiguration.getBackgroundColorConfiguration() != null) {
            builder.backgroundColor(newConfiguration.getBackgroundColorConfiguration());
        }
        if (newConfiguration.getBorderColorConfiguration() != null) {
            builder.borderColor(newConfiguration.getBorderColorConfiguration());
        }
        if (newConfiguration.getBorderWidth() != null) {
            builder.borderWidth(newConfiguration.getBorderWidth());
        }
        if (newConfiguration.getCornerRadius() != null) {
            builder.cornerRadius(newConfiguration.getCornerRadius());
        }
        return builder.build();
    }

    public static File createTempPhotoFile(Context context) throws IOException {
        File directoryStorage = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        directoryStorage.deleteOnExit();
        return File.createTempFile(generatePhotoFileName(), ".jpg", directoryStorage);
    }

    private static String generatePhotoFileName() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd_HHmmss_", Locale.getDefault());
        Date date = new Date(System.currentTimeMillis());
        return "IMG_" + formatter.format(date);
    }

    public static FileAttachment mapUriToFileAttachment(ContentResolver contentResolver, Uri uri) {
        Cursor returnCursor = contentResolver.query(uri, null, null, null, null);
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
        returnCursor.moveToFirst();
        String displayName = returnCursor.getString(nameIndex);
        String mimeType = contentResolver.getType(uri);
        long size = returnCursor.getLong(sizeIndex);
        returnCursor.close();
        return new FileAttachment(uri, displayName, size, mimeType);
    }

    public static Optional<String> getExtensionByStringHandling(String filename) {
        return Optional.ofNullable(filename)
                .filter(f -> f.contains("."))
                .map(f -> f.substring(filename.lastIndexOf(".") + 1).toUpperCase());
    }

    public static String toString(AttachmentFile attachmentFile) {
        return "{" +
                "id=" + attachmentFile.getId() +
                ", size=" + attachmentFile.getSize() +
                ", contentType=" + attachmentFile.getContentType() +
                ", isDeleted=" + attachmentFile.isDeleted() +
                ", name=" + attachmentFile.getName() +
                " }";
    }

    @ColorInt
    public static int applyAlpha(@ColorInt int color, float alpha) {
        int alphaOfColor = Math.round(Color.alpha(color) * alpha);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(alphaOfColor, red, green, blue);
    }
}
