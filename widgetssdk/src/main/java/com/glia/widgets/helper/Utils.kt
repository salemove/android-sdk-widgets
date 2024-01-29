package com.glia.widgets.helper;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.TypedValue;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleableRes;
import androidx.core.content.res.ResourcesCompat;

import com.glia.androidsdk.Engagement;
import com.glia.widgets.GliaWidgets;
import com.glia.widgets.R;
import com.glia.widgets.UiTheme;

import org.jetbrains.annotations.NotNull;

import java.security.InvalidParameterException;

public class Utils {

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

    @NotNull
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
        defaultThemeBuilder.setNewMessagesDividerColor(
                getTypedArrayIntegerValue(typedArray, context, R.styleable.GliaView_newMessagesDividerColor, R.attr.gliaNewMessagesDividerColor)
        );
        defaultThemeBuilder.setNewMessagesDividerTextColor(
                getTypedArrayIntegerValue(typedArray, context, R.styleable.GliaView_newMessagesDividerTextColor, R.attr.gliaNewMessagesDividerTextColor)
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
                        androidx.appcompat.R.attr.fontFamily
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
        defaultThemeBuilder.setIconCallAudioOn(
                getTypedArrayIntegerValue(
                        typedArray,
                        context,
                        R.styleable.GliaView_iconCallAudioOn,
                        R.attr.gliaIconCallAudioOn
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
        defaultThemeBuilder.setIconCallVideoOn(
                getTypedArrayIntegerValue(
                        typedArray,
                        context,
                        R.styleable.GliaView_iconCallVideoOn,
                        R.attr.gliaIconCallVideoOn
                )
        );
        defaultThemeBuilder.setIconCallAudioOff(
                getTypedArrayIntegerValue(
                        typedArray,
                        context,
                        R.styleable.GliaView_iconCallAudioOff,
                        R.attr.gliaIconCallAudioOff
                )
        );
        defaultThemeBuilder.setIconCallVideoOff(
                getTypedArrayIntegerValue(
                        typedArray,
                        context,
                        R.styleable.GliaView_iconCallVideoOff,
                        R.attr.gliaIconCallVideoOff
                )
        );
        defaultThemeBuilder.setIconCallChat(
                getTypedArrayIntegerValue(
                        typedArray,
                        context,
                        R.styleable.GliaView_iconCallChat,
                        R.attr.gliaIconCallChat
                )
        );
        defaultThemeBuilder.setIconCallSpeakerOn(
                getTypedArrayIntegerValue(
                        typedArray,
                        context,
                        R.styleable.GliaView_iconCallSpeakerOn,
                        R.attr.gliaIconCallSpeakerOn
                )
        );
        defaultThemeBuilder.setIconCallSpeakerOff(
                getTypedArrayIntegerValue(
                        typedArray,
                        context,
                        R.styleable.GliaView_iconCallSpeakerOff,
                        R.attr.gliaIconCallSpeakerOff
                )
        );
        defaultThemeBuilder.setIconCallMinimize(
                getTypedArrayIntegerValue(
                        typedArray,
                        context,
                        R.styleable.GliaView_iconCallMinimize,
                        R.attr.gliaIconCallMinimize
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
        defaultThemeBuilder.setIconEndScreenShare(
                getTypedArrayIntegerValue(
                        typedArray,
                        context,
                        R.styleable.GliaView_iconEndScreenShare,
                        R.attr.gliaIconEndScreenShare
                )
        );
        defaultThemeBuilder.setEndScreenShareTintColor(
                getTypedArrayIntegerValue(
                        typedArray,
                        context,
                        R.styleable.GliaView_endScreenShareTintColor,
                        R.attr.gliaEndScreenShareTintColor
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
        return defaultThemeBuilder.build();
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

    @Nullable
    public static Typeface getFont(TypedArray typedArray, Context context) {
        int resId = getTypedArrayIntegerValue(
                typedArray, context, R.styleable.GliaView_android_fontFamily, androidx.appcompat.R.attr.fontFamily
        );

        if (resId > 0) {
            return ResourcesCompat.getFont(context, resId);
        }

        return null;
    }
}
