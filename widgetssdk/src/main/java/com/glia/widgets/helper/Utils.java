package com.glia.widgets.helper;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.IBinder;
import android.util.TypedValue;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.AttrRes;
import androidx.annotation.StyleableRes;

import com.glia.widgets.Constants;
import com.glia.widgets.GliaWidgets;
import com.glia.widgets.R;
import com.glia.widgets.UiTheme;
import com.glia.widgets.call.CallActivity;
import com.glia.widgets.chat.ChatActivity;
import com.glia.widgets.model.ChatHeadInput;

import java.util.Locale;

public class Utils {
    public static float pxFromDp(final Context context, final float dp) {
        return dp * context.getResources().getDisplayMetrics().density;
    }

    public static String toMmSs(int seconds) {
        return String.format(Locale.getDefault(), "%02d:%02d", seconds / 60, seconds % 60);
    }

    public static String formatOperatorName(String operatorName) {
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
        defaultThemeBuilder.setWhiteLabel(
                getTypedArrayIntegerValue(
                        typedArray,
                        context,
                        R.styleable.GliaView_whiteLabel,
                        R.attr.gliaWhiteLabel
                )
        );
        return defaultThemeBuilder.build();
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
        Integer fontRes = newTheme.getFontRes() != null ? newTheme.getFontRes() : oldTheme.getFontRes();
        Integer systemNegativeColorRes = newTheme.getSystemNegativeColor() != null ?
                newTheme.getSystemNegativeColor() : oldTheme.getSystemNegativeColor();
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
        Integer iconCallAudioOn = newTheme.getIconCallAudioOn() != null ?
                newTheme.getIconCallAudioOn() : oldTheme.getIconCallAudioOn();
        Integer iconChatVideoUpgrade = newTheme.getIconChatVideoUpgrade() != null ?
                newTheme.getIconChatVideoUpgrade() : oldTheme.getIconChatVideoUpgrade();
        Integer iconUpgradeVideoDialog = newTheme.getIconUpgradeVideoDialog() != null ?
                newTheme.getIconUpgradeVideoDialog() : oldTheme.getIconUpgradeVideoDialog();
        Integer iconScreenSharingDialog = newTheme.getIconScreenSharingDialog() != null ?
                newTheme.getIconScreenSharingDialog() : oldTheme.getIconScreenSharingDialog();
        Integer iconCallVideoOn = newTheme.getIconCallVideoOn() != null ?
                newTheme.getIconCallVideoOn() : oldTheme.getIconCallVideoOn();
        Integer iconCallAudioOff = newTheme.getIconCallAudioOff() != null ?
                newTheme.getIconCallAudioOff() : oldTheme.getIconCallAudioOff();
        Integer iconCallVideoOff = newTheme.getIconCallVideoOff() != null ?
                newTheme.getIconCallVideoOff() : oldTheme.getIconCallVideoOff();
        Integer iconCallChat = newTheme.getIconCallChat() != null ?
                newTheme.getIconCallChat() : oldTheme.getIconCallChat();
        Integer iconCallSpeakerOn = newTheme.getIconCallSpeakerOn() != null ?
                newTheme.getIconCallSpeakerOn() : oldTheme.getIconCallSpeakerOn();
        Integer iconCallSpeakerOff = newTheme.getIconCallSpeakerOff() != null ?
                newTheme.getIconCallSpeakerOff() : oldTheme.getIconCallSpeakerOff();
        Integer iconCallMinimize = newTheme.getIconCallMinimize() != null ?
                newTheme.getIconCallMinimize() : oldTheme.getIconCallMinimize();
        Integer iconPlaceholder = newTheme.getIconPlaceholder() != null ?
                newTheme.getIconPlaceholder() : oldTheme.getIconPlaceholder();

        Integer whiteLabel = newTheme.getWhiteLabel() != null ? newTheme.getWhiteLabel() : oldTheme.getWhiteLabel();

        UiTheme.UiThemeBuilder builder = new UiTheme.UiThemeBuilder();
        builder.setAppBarTitle(title);
        builder.setBaseLightColor(baseLightColorRes);
        builder.setBaseDarkColor(baseDarkColorRes);
        builder.setBaseNormalColor(baseNormalColorRes);
        builder.setBaseShadeColor(baseShadeColorRes);
        builder.setBrandPrimaryColor(brandPriamryColorRes);
        builder.setSystemAgentBubbleColor(systemAgentBubbleColorRes);
        builder.setFontRes(fontRes);
        builder.setSystemNegativeColor(systemNegativeColorRes);
        builder.setIconAppBarBack(iconAppBarBack);
        builder.setIconLeaveQueue(iconLeaveQueue);
        builder.setIconSendMessage(iconSendMessage);
        builder.setIconChatAudioUpgrade(iconChatAudioUpgrade);
        builder.setIconUpgradeAudioDialog(iconUpgradeAudioDialog);
        builder.setIconCallAudioOn(iconCallAudioOn);
        builder.setIconChatVideoUpgrade(iconChatVideoUpgrade);
        builder.setIconUpgradeVideoDialog(iconUpgradeVideoDialog);
        builder.setIconScreenSharingDialog(iconScreenSharingDialog);
        builder.setIconCallVideoOn(iconCallVideoOn);
        builder.setIconCallAudioOff(iconCallAudioOff);
        builder.setIconCallVideoOff(iconCallVideoOff);
        builder.setIconCallChat(iconCallChat);
        builder.setIconCallSpeakerOn(iconCallSpeakerOn);
        builder.setIconCallSpeakerOff(iconCallSpeakerOff);
        builder.setIconCallMinimize(iconCallMinimize);
        builder.setIconPlaceholder(iconPlaceholder);
        builder.setWhiteLabel(whiteLabel);
        return builder.build();
    }

    public static Intent getReturnToEngagementIntent(
            Context context,
            ChatHeadInput chatHeadInput,
            String returnDestination
    ) {
        if (returnDestination.equals(Constants.CHAT_ACTIVITY)) {
            return getNavigationIntent(context, ChatActivity.class, chatHeadInput);
        } else {
            return getNavigationIntent(context, CallActivity.class, chatHeadInput);
        }
    }

    private static Intent getNavigationIntent(Context context, Class<?> cls, ChatHeadInput chatHeadInput) {
        Intent newIntent = new Intent(context, cls);
        newIntent.putExtra(GliaWidgets.COMPANY_NAME, chatHeadInput.companyName);
        newIntent.putExtra(GliaWidgets.QUEUE_ID, chatHeadInput.queueId);
        newIntent.putExtra(GliaWidgets.CONTEXT_URL, chatHeadInput.contextUrl);
        newIntent.putExtra(GliaWidgets.UI_THEME, chatHeadInput.uiTheme);
        newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return newIntent;
    }
}
