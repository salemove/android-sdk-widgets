package com.glia.widgets.snapshotutils

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.FontRes
import androidx.annotation.RawRes
import com.glia.widgets.R
import com.glia.widgets.UiTheme
import com.glia.widgets.helper.ResourceProvider
import com.glia.widgets.view.unifiedui.config.RemoteConfiguration
import com.glia.widgets.view.unifiedui.parse.RemoteConfigurationParser
import com.glia.widgets.view.unifiedui.theme.UnifiedTheme
import com.google.gson.Gson
import com.google.gson.JsonObject

internal interface SnapshotTheme : SnapshotContent {

    fun unifiedTheme(json: String): UnifiedTheme {
        val resourceProvider = ResourceProvider(context)
        return RemoteConfigurationParser(resourceProvider).defaultGson
            .fromJson(json, RemoteConfiguration::class.java).toUnifiedTheme()!!
    }

    fun unifiedTheme(@RawRes resId: Int, modifier: ((JsonObject) -> Unit)? = null): UnifiedTheme {
        val jsonRaw = rawRes(resId)
        if (modifier != null) {
            val jsonObject = Gson().fromJson(rawRes(R.raw.test_unified_config), JsonObject::class.java)
            modifier(jsonObject)
            return unifiedTheme(jsonObject.toString())
        }
        return unifiedTheme(jsonRaw)
    }

    fun unifiedThemeWithGlobalColors(): UnifiedTheme = unifiedTheme(R.raw.global_colors_unified_config)

    fun unifiedTheme(): UnifiedTheme = unifiedTheme(R.raw.test_unified_config)

    fun uiTheme(
        appBarTitle: String? = "Snapshot Test",
        @ColorRes brandPrimaryColor: Int? = R.color.brandPrimaryColor,
        @ColorRes baseLightColor: Int? = R.color.baseLightColor,
        @ColorRes baseDarkColor: Int? = R.color.baseDarkColor,
        @ColorRes baseNormalColor: Int? = R.color.baseNormalColor,
        @ColorRes baseShadeColor: Int? = R.color.baseShadeColor,
        @ColorRes systemAgentBubbleColor: Int? = R.color.systemAgentBubbleColor,
        @ColorRes systemNegativeColor: Int? = R.color.systemNegativeColor,
        @ColorRes visitorMessageBackgroundColor: Int? = R.color.visitorMessageBackgroundColor,
        @ColorRes visitorMessageTextColor: Int? = R.color.visitorMessageTextColor,
        @ColorRes operatorMessageBackgroundColor: Int? = R.color.operatorMessageBackgroundColor,
        @ColorRes operatorMessageTextColor: Int? = R.color.operatorMessageTextColor,
        @ColorRes newMessageDividerColor: Int? = R.color.newMessageDividerColor,
        @ColorRes newMessageDividerTextColor: Int? = R.color.newMessageDividerTextColor,
        @ColorRes botActionButtonBackgroundColor: Int? = R.color.botActionButtonBackgroundColor,
        @ColorRes botActionButtonTextColor: Int? = R.color.botActionButtonTextColor,
        @ColorRes botActionButtonSelectedBackgroundColor: Int? = R.color.botActionButtonSelectedBackgroundColor,
        @ColorRes botActionButtonSelectedTextColor: Int? = R.color.botActionButtonSelectedTextColor,
        @ColorRes sendMessageButtonTintColor: Int? = R.color.sendMessageButtonTintColor,
        @ColorRes gliaChatBackgroundColor: Int? = R.color.gliaChatBackgroundColor,
        @ColorRes gliaChatHeaderTitleTintColor: Int? = R.color.gliaChatHeaderTitleTintColor,
        @ColorRes gliaChatHeaderHomeButtonTintColor: Int? = R.color.gliaChatHeaderHomeButtonTintColor,
        @ColorRes gliaChatHeaderExitQueueButtonTintColor: Int? = R.color.gliaChatHeaderExitQueueButtonTintColor,
        @ColorRes visitorCodeTextColor: Int? = R.color.visitorCodeTextColor,
        @ColorRes visitorCodeBackgroundColor: Int? = R.color.visitorCodeBackgroundColor,
        @ColorRes visitorCodeBorderColor: Int? = R.color.visitorCodeBorderColor,
        @ColorRes gvaQuickReplyBackgroundColor: Int? = R.color.gvaQuickReplyBackgroundColor,
        @ColorRes gvaQuickReplyStrokeColor: Int? = R.color.gvaQuickReplyStrokeColor,
        @ColorRes gvaQuickReplyTextColor: Int? = R.color.gvaQuickReplyTextColor,
        @ColorRes endScreenShareTintColor: Int? = R.color.endScreenShareTintColor,
        @FontRes fontRes: Int? = R.font.expletus,
        @DrawableRes iconAppBarBack: Int? = R.drawable.test_ic_app_bar_back,
        @DrawableRes iconLeaveQueue: Int? = R.drawable.test_ic_leave_queue,
        @DrawableRes iconSendMessage: Int? = R.drawable.test_ic_send_message,
        @DrawableRes iconChatAudioUpgrade: Int? = R.drawable.test_ic_chat_audio_upgrade,
        @DrawableRes iconUpgradeAudioDialog: Int? = R.drawable.test_ic_upgrade_audio_dialog,
        @DrawableRes iconCallAudioOn: Int? = R.drawable.test_ic_call_audio_on,
        @DrawableRes iconChatVideoUpgrade: Int? = R.drawable.test_ic_chat_video_upgrade,
        @DrawableRes iconUpgradeVideoDialog: Int? = R.drawable.test_ic_upgrade_video_dialog,
        @DrawableRes iconScreenSharingDialog: Int? = R.drawable.test_ic_screen_sharing_dialog,
        @DrawableRes iconCallVideoOn: Int? = R.drawable.test_ic_call_video_on,
        @DrawableRes iconCallAudioOff: Int? = R.drawable.test_ic_call_audio_off,
        @DrawableRes iconCallVideoOff: Int? = R.drawable.test_ic_call_video_off,
        @DrawableRes iconCallChat: Int? = R.drawable.test_ic_call_chat,
        @DrawableRes iconCallSpeakerOn: Int? = R.drawable.test_ic_call_speaker_on,
        @DrawableRes iconCallSpeakerOff: Int? = R.drawable.test_ic_call_speaker_off,
        @DrawableRes iconCallMinimize: Int? = R.drawable.test_ic_call_minimize,
        @DrawableRes iconPlaceholder: Int? = R.drawable.test_ic_placeholder,
        @DrawableRes iconOnHold: Int? = R.drawable.test_ic_on_hold,
        @DrawableRes iconEndScreenShare: Int? = R.drawable.test_ic_end_screen_share,
        whiteLabel: Boolean? = true,
        gliaAlertDialogButtonUseVerticalAlignment: Boolean? = true
    ): UiTheme {
        return UiTheme(
            appBarTitle = "Snapshot Test",
            brandPrimaryColor = brandPrimaryColor,
            baseLightColor = baseLightColor,
            baseDarkColor = baseDarkColor,
            baseNormalColor = baseNormalColor,
            baseShadeColor = baseShadeColor,
            systemAgentBubbleColor = systemAgentBubbleColor,
            systemNegativeColor = systemNegativeColor,
            visitorMessageBackgroundColor = visitorMessageBackgroundColor,
            visitorMessageTextColor = visitorMessageTextColor,
            operatorMessageBackgroundColor = operatorMessageBackgroundColor,
            operatorMessageTextColor = operatorMessageTextColor,
            newMessageDividerColor = newMessageDividerColor,
            newMessageDividerTextColor = newMessageDividerTextColor,
            botActionButtonBackgroundColor = botActionButtonBackgroundColor,
            botActionButtonTextColor = botActionButtonTextColor,
            botActionButtonSelectedBackgroundColor = botActionButtonSelectedBackgroundColor,
            botActionButtonSelectedTextColor = botActionButtonSelectedTextColor,
            sendMessageButtonTintColor = sendMessageButtonTintColor,
            gliaChatBackgroundColor = gliaChatBackgroundColor,
            gliaChatHeaderTitleTintColor = gliaChatHeaderTitleTintColor,
            gliaChatHeaderHomeButtonTintColor = gliaChatHeaderHomeButtonTintColor,
            gliaChatHeaderExitQueueButtonTintColor = gliaChatHeaderExitQueueButtonTintColor,
            visitorCodeTextColor = visitorCodeTextColor,
            visitorCodeBackgroundColor = visitorCodeBackgroundColor,
            visitorCodeBorderColor = visitorCodeBorderColor,
            gvaQuickReplyBackgroundColor = gvaQuickReplyBackgroundColor,
            gvaQuickReplyStrokeColor = gvaQuickReplyStrokeColor,
            gvaQuickReplyTextColor = gvaQuickReplyTextColor,
            endScreenShareTintColor = endScreenShareTintColor,
            fontRes = fontRes,
            iconAppBarBack = iconAppBarBack,
            iconLeaveQueue = iconLeaveQueue,
            iconSendMessage = iconSendMessage,
            iconChatAudioUpgrade = iconChatAudioUpgrade,
            iconUpgradeAudioDialog = iconUpgradeAudioDialog,
            iconCallAudioOn = iconCallAudioOn,
            iconChatVideoUpgrade = iconChatVideoUpgrade,
            iconUpgradeVideoDialog = iconUpgradeVideoDialog,
            iconScreenSharingDialog = iconScreenSharingDialog,
            iconCallVideoOn = iconCallVideoOn,
            iconCallAudioOff = iconCallAudioOff,
            iconCallVideoOff = iconCallVideoOff,
            iconCallChat = iconCallChat,
            iconCallSpeakerOn = iconCallSpeakerOn,
            iconCallSpeakerOff = iconCallSpeakerOff,
            iconCallMinimize = iconCallMinimize,
            iconPlaceholder = iconPlaceholder,
            iconOnHold = iconOnHold,
            iconEndScreenShare = iconEndScreenShare,
            whiteLabel = whiteLabel,
            gliaAlertDialogButtonUseVerticalAlignment = gliaAlertDialogButtonUseVerticalAlignment
        )
    }
}
