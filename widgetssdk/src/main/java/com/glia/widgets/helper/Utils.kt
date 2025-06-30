package com.glia.widgets.helper

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Typeface
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.annotation.StyleableRes
import androidx.core.content.res.ResourcesCompat
import com.glia.widgets.R
import com.glia.widgets.UiTheme
import com.glia.widgets.UiTheme.UiThemeBuilder

internal object Utils {

    fun getThemeFromTypedArray(typedArray: TypedArray, context: Context): UiTheme {
        val defaultThemeBuilder = UiThemeBuilder().apply {
            setBrandPrimaryColor(
                getTypedArrayIntegerValue(
                    typedArray,
                    context,
                    R.styleable.GliaView_brandPrimaryColor,
                    R.attr.gliaBrandPrimaryColor
                )
            )
            setBaseLightColor(
                getTypedArrayIntegerValue(
                    typedArray,
                    context,
                    R.styleable.GliaView_baseLightColor,
                    R.attr.gliaBaseLightColor
                )
            )
            setBaseDarkColor(
                getTypedArrayIntegerValue(
                    typedArray,
                    context,
                    R.styleable.GliaView_baseDarkColor,
                    R.attr.gliaBaseDarkColor
                )
            )
            setBaseNormalColor(
                getTypedArrayIntegerValue(
                    typedArray,
                    context,
                    R.styleable.GliaView_baseNormalColor,
                    R.attr.gliaBaseNormalColor
                )
            )
            setBaseShadeColor(
                getTypedArrayIntegerValue(
                    typedArray,
                    context,
                    R.styleable.GliaView_baseShadeColor,
                    R.attr.gliaBaseShadeColor
                )
            )
            setSystemAgentBubbleColor(
                getTypedArrayIntegerValue(
                    typedArray,
                    context,
                    R.styleable.GliaView_systemAgentBubbleColor,
                    R.attr.gliaSystemAgentBubbleColor
                )
            )
            setSystemNegativeColor(
                getTypedArrayIntegerValue(
                    typedArray,
                    context,
                    R.styleable.GliaView_systemNegativeColor,
                    R.attr.gliaSystemNegativeColor
                )
            )
            setVisitorMessageBackgroundColor(
                getTypedArrayIntegerValue(
                    typedArray,
                    context,
                    R.styleable.GliaView_visitorMessageBackgroundColor,
                    R.attr.gliaVisitorMessageBackgroundColor
                )
            )
            setVisitorMessageTextColor(
                getTypedArrayIntegerValue(
                    typedArray,
                    context,
                    R.styleable.GliaView_visitorMessageTextColor,
                    R.attr.gliaVisitorMessageTextColor
                )
            )
            setOperatorMessageBackgroundColor(
                getTypedArrayIntegerValue(
                    typedArray,
                    context,
                    R.styleable.GliaView_operatorMessageBackgroundColor,
                    R.attr.gliaOperatorMessageBackgroundColor
                )
            )
            setOperatorMessageTextColor(
                getTypedArrayIntegerValue(
                    typedArray,
                    context,
                    R.styleable.GliaView_operatorMessageTextColor,
                    R.attr.operatorMessageTextColor
                )
            )
            setNewMessagesDividerColor(
                getTypedArrayIntegerValue(
                    typedArray,
                    context,
                    R.styleable.GliaView_newMessagesDividerColor,
                    R.attr.gliaNewMessagesDividerColor
                )
            )
            setNewMessagesDividerTextColor(
                getTypedArrayIntegerValue(
                    typedArray,
                    context,
                    R.styleable.GliaView_newMessagesDividerTextColor,
                    R.attr.gliaNewMessagesDividerTextColor
                )
            )
            setBotActionButtonBackgroundColor(
                getTypedArrayIntegerValue(
                    typedArray,
                    context,
                    R.styleable.GliaView_botActionButtonBackgroundColor,
                    R.attr.gliaBotActionButtonBackgroundColor
                )
            )
            setBotActionButtonTextColor(
                getTypedArrayIntegerValue(
                    typedArray,
                    context,
                    R.styleable.GliaView_botActionButtonTextColor,
                    R.attr.gliaBotActionButtonTextColor
                )
            )
            setBotActionButtonSelectedBackgroundColor(
                getTypedArrayIntegerValue(
                    typedArray,
                    context,
                    R.styleable.GliaView_botActionButtonSelectedBackgroundColor,
                    R.attr.gliaBotActionButtonSelectedBackgroundColor
                )
            )
            setBotActionButtonSelectedTextColor(
                getTypedArrayIntegerValue(
                    typedArray,
                    context,
                    R.styleable.GliaView_botActionButtonSelectedTextColor,
                    R.attr.gliaBotActionButtonSelectedTextColor
                )
            )
            setSendMessageButtonTintColor(
                getTypedArrayIntegerValue(
                    typedArray,
                    context,
                    R.styleable.GliaView_chatSendMessageButtonTintColor,
                    R.attr.gliaSendMessageButtonTintColor
                )
            )
            setGliaChatBackgroundColor(
                getTypedArrayIntegerValue(
                    typedArray,
                    context,
                    R.styleable.GliaView_gliaChatBackgroundColor,
                    R.attr.gliaChatBackgroundColor
                )
            )
            setGliaChatHeaderTitleTintColor(
                getTypedArrayIntegerValue(
                    typedArray,
                    context,
                    R.styleable.GliaView_chatHeaderTitleTintColor,
                    R.attr.gliaChatHeaderTitleTintColor
                )
            )
            setGliaChatHeaderHomeButtonTintColor(
                getTypedArrayIntegerValue(
                    typedArray,
                    context,
                    R.styleable.GliaView_chatHeaderHomeButtonTintColor,
                    R.attr.gliaChatHeaderHomeButtonTintColor
                )
            )
            setGliaChatHeaderExitQueueButtonTintColor(
                getTypedArrayIntegerValue(
                    typedArray,
                    context,
                    R.styleable.GliaView_chatHeaderExitQueueButtonTintColor,
                    R.attr.gliaChatHeaderExitQueueButtonTintColor
                )
            )
            setChatStartedHeadingTextColor(
                getTypedArrayIntegerValue(
                    typedArray,
                    context,
                    R.styleable.GliaView_chatStartedHeadingTextColor,
                    R.attr.chatStartedHeadingTextColor
                )
            )
            setChatStartedCaptionTextColor(
                getTypedArrayIntegerValue(
                    typedArray,
                    context,
                    R.styleable.GliaView_chatStartedCaptionTextColor,
                    R.attr.chatStartedCaptionTextColor
                )
            )
            setChatStartingHeadingTextColor(
                getTypedArrayIntegerValue(
                    typedArray,
                    context,
                    R.styleable.GliaView_chatStartingHeadingTextColor,
                    R.attr.chatStartingHeadingTextColor
                )
            )
            setChatStartingCaptionTextColor(
                getTypedArrayIntegerValue(
                    typedArray,
                    context,
                    R.styleable.GliaView_chatStartingCaptionTextColor,
                    R.attr.chatStartingCaptionTextColor
                )
            )
            setFontRes(
                getTypedArrayIntegerValue(
                    typedArray,
                    context,
                    R.styleable.GliaView_android_fontFamily,
                    androidx.appcompat.R.attr.fontFamily
                )
            )
            setIconAppBarBack(
                getTypedArrayIntegerValue(
                    typedArray,
                    context,
                    R.styleable.GliaView_iconAppBarBack,
                    R.attr.gliaIconAppBarBack
                )
            )
            setIconLeaveQueue(
                getTypedArrayIntegerValue(
                    typedArray,
                    context,
                    R.styleable.GliaView_iconLeaveQueue,
                    R.attr.gliaIconLeaveQueue
                )
            )
            setIconSendMessage(
                getTypedArrayIntegerValue(
                    typedArray,
                    context,
                    R.styleable.GliaView_iconSendMessage,
                    R.attr.gliaIconSendMessage
                )
            )
            setIconChatAudioUpgrade(
                getTypedArrayIntegerValue(
                    typedArray,
                    context,
                    R.styleable.GliaView_iconChatAudioUpgrade,
                    R.attr.gliaIconChatAudioUpgrade
                )
            )
            setIconUpgradeAudioDialog(
                getTypedArrayIntegerValue(
                    typedArray,
                    context,
                    R.styleable.GliaView_iconUpgradeAudioDialog,
                    R.attr.gliaIconUpgradeAudioDialog
                )
            )
            setIconCallAudioOn(
                getTypedArrayIntegerValue(
                    typedArray,
                    context,
                    R.styleable.GliaView_iconCallAudioOn,
                    R.attr.gliaIconCallAudioOn
                )
            )
            setIconChatVideoUpgrade(
                getTypedArrayIntegerValue(
                    typedArray,
                    context,
                    R.styleable.GliaView_iconChatVideoUpgrade,
                    R.attr.gliaIconChatVideoUpgrade
                )
            )
            setIconUpgradeVideoDialog(
                getTypedArrayIntegerValue(
                    typedArray,
                    context,
                    R.styleable.GliaView_iconUpgradeVideoDialog,
                    R.attr.gliaIconUpgradeVideoDialog
                )
            )
            setIconCallVideoOn(
                getTypedArrayIntegerValue(
                    typedArray,
                    context,
                    R.styleable.GliaView_iconCallVideoOn,
                    R.attr.gliaIconCallVideoOn
                )
            )
            setIconCallAudioOff(
                getTypedArrayIntegerValue(
                    typedArray,
                    context,
                    R.styleable.GliaView_iconCallAudioOff,
                    R.attr.gliaIconCallAudioOff
                )
            )
            setIconCallVideoOff(
                getTypedArrayIntegerValue(
                    typedArray,
                    context,
                    R.styleable.GliaView_iconCallVideoOff,
                    R.attr.gliaIconCallVideoOff
                )
            )
            setIconCallChat(
                getTypedArrayIntegerValue(
                    typedArray,
                    context,
                    R.styleable.GliaView_iconCallChat,
                    R.attr.gliaIconCallChat
                )
            )
            setIconCallSpeakerOn(
                getTypedArrayIntegerValue(
                    typedArray,
                    context,
                    R.styleable.GliaView_iconCallSpeakerOn,
                    R.attr.gliaIconCallSpeakerOn
                )
            )
            setIconCallSpeakerOff(
                getTypedArrayIntegerValue(
                    typedArray,
                    context,
                    R.styleable.GliaView_iconCallSpeakerOff,
                    R.attr.gliaIconCallSpeakerOff
                )
            )
            setIconCallMinimize(
                getTypedArrayIntegerValue(
                    typedArray,
                    context,
                    R.styleable.GliaView_iconCallMinimize,
                    R.attr.gliaIconCallMinimize
                )
            )
            setIconPlaceholder(
                getTypedArrayIntegerValue(
                    typedArray,
                    context,
                    R.styleable.GliaView_iconPlaceholder,
                    R.attr.gliaIconPlaceholder
                )
            )
            setIconOnHold(
                getTypedArrayIntegerValue(
                    typedArray,
                    context,
                    R.styleable.GliaView_iconOnHold,
                    R.attr.gliaIconOnHold
                )
            )
            setWhiteLabel(
                getTypedArrayBooleanValue(
                    typedArray,
                    R.styleable.GliaView_whiteLabel
                )
            )
            setGliaAlertDialogButtonUseVerticalAlignment(
                getTypedArrayBooleanValue(
                    typedArray,
                    R.styleable.GliaView_gliaAlertDialogButtonUseVerticalAlignment
                )
            )
        }

        return defaultThemeBuilder.build()
    }

    private fun getTypedArrayBooleanValue(typedArray: TypedArray, index: Int): Boolean {
        return typedArray.hasValue(index) && typedArray.getBoolean(index, false)
    }

    fun getTypedArrayStringValue(
        typedArray: TypedArray,
        @StyleableRes index: Int
    ): String? {
        return if (typedArray.hasValue(index)) {
            typedArray.getString(index)
        } else null
    }

    fun getTypedArrayIntegerValue(
        typedArray: TypedArray,
        context: Context,
        @StyleableRes index: Int,
        @AttrRes defaultValue: Int
    ): Int {
        return if (typedArray.hasValue(index)) {
            typedArray.getResourceId(index, 0)
        } else {
            getAttrResourceId(context, defaultValue)
        }
    }

    fun getAttrResourceId(
        context: Context,
        @AttrRes attrId: Int
    ): Int {
        val typedValue = TypedValue()
        val theme = context.theme
        theme.resolveAttribute(attrId, typedValue, true)
        return typedValue.resourceId
    }

    fun getFont(typedArray: TypedArray, context: Context): Typeface? {
        val resId = getTypedArrayIntegerValue(
            typedArray,
            context,
            R.styleable.GliaView_android_fontFamily,
            androidx.appcompat.R.attr.fontFamily
        )
        return if (resId > 0) {
            ResourcesCompat.getFont(context, resId)
        } else null
    }
}
