package com.glia.widgets.view.unifiedui.config.chat

import com.glia.widgets.view.unifiedui.config.base.ColorLayerRemoteConfig
import com.glia.widgets.view.unifiedui.config.base.HeaderRemoteConfig
import com.glia.widgets.view.unifiedui.config.base.LayerRemoteConfig
import com.glia.widgets.view.unifiedui.config.base.TextRemoteConfig
import com.glia.widgets.view.unifiedui.config.bubble.BubbleRemoteConfig
import com.glia.widgets.view.unifiedui.config.gva.GvaRemoteConfig
import com.glia.widgets.view.unifiedui.config.securemessaging.SecureMessagingRemoteConfig
import com.glia.widgets.view.unifiedui.theme.chat.ChatTheme
import com.google.gson.annotations.SerializedName

internal data class ChatRemoteConfig(
    @SerializedName("background")
    val background: LayerRemoteConfig?,

    @SerializedName("header")
    val headerRemoteConfig: HeaderRemoteConfig?,

    @SerializedName("operatorMessage")
    val operatorMessage: MessageBalloonRemoteConfig?,

    @SerializedName("visitorMessage")
    val visitorMessage: MessageBalloonRemoteConfig?,

    @SerializedName("connect")
    val connect: EngagementStatesRemoteConfig?,

    @SerializedName("input")
    val inputRemoteConfig: InputRemoteConfig?,

    @SerializedName("inputDisabled")
    val inputDisabledRemoteConfig: InputRemoteConfig?,

    @SerializedName("responseCard")
    val responseCardRemoteConfig: ResponseCardRemoteConfig?,

    @SerializedName("audioUpgrade")
    val audioUpgradeRemoteConfig: UpgradeRemoteConfig?,

    @SerializedName("videoUpgrade")
    val videoUpgradeRemoteConfig: UpgradeRemoteConfig?,

    @SerializedName("bubble")
    val bubbleRemoteConfig: BubbleRemoteConfig?,

    @SerializedName("attachmentSourceList")
    val attachmentSourceListRemoteConfig: AttachmentSourceListRemoteConfig?,

    @SerializedName("unreadIndicator")
    val unreadIndicator: UnreadIndicatorRemoteConfig?,

    @SerializedName("typingIndicator")
    val typingIndicator: ColorLayerRemoteConfig?,

    @SerializedName("newMessagesDividerColor")
    val newMessagesDividerColorRemoteConfig: ColorLayerRemoteConfig?,

    @SerializedName("newMessagesDividerText")
    val newMessagesDividerTextRemoteConfig: TextRemoteConfig?,

    @SerializedName("gva")
    val gvaRemoteConfig: GvaRemoteConfig?,

    @SerializedName("secureMessaging")
    val secureMessagingRemoteConfig: SecureMessagingRemoteConfig?
) {
    fun toChatTheme(): ChatTheme = ChatTheme(
        background = background?.toLayerTheme(),
        header = headerRemoteConfig?.toHeaderTheme(),
        operatorMessage = operatorMessage?.toMessageBalloonTheme(),
        visitorMessage = visitorMessage?.toMessageBalloonTheme(),
        connect = connect?.toEngagementStatesTheme(),
        input = inputRemoteConfig?.toInputTheme(),
        inputDisabled = inputDisabledRemoteConfig?.toInputTheme(),
        responseCard = responseCardRemoteConfig?.toResponseCardTheme(),
        audioUpgrade = audioUpgradeRemoteConfig?.toUpgradeTheme(),
        videoUpgrade = videoUpgradeRemoteConfig?.toUpgradeTheme(),
        bubble = bubbleRemoteConfig?.toBubbleTheme(),
        attachmentsPopup = attachmentSourceListRemoteConfig?.toAttachmentsPopupTheme(),
        unreadIndicator = unreadIndicator?.toUnreadIndicatorTheme(),
        typingIndicator = typingIndicator?.toColorTheme(),
        newMessagesDividerColorTheme = newMessagesDividerColorRemoteConfig?.toColorTheme(),
        newMessagesDividerTextTheme = newMessagesDividerTextRemoteConfig?.toTextTheme(),
        gva = gvaRemoteConfig?.toGvaTheme(),
        secureMessaging = secureMessagingRemoteConfig?.toSecureMessagingTheme()
    )
}
