package com.glia.widgets.view.unifiedui.theme.chat

import com.glia.widgets.view.unifiedui.Mergeable
import com.glia.widgets.view.unifiedui.merge
import com.glia.widgets.view.unifiedui.theme.base.ColorTheme
import com.glia.widgets.view.unifiedui.theme.base.HeaderTheme
import com.glia.widgets.view.unifiedui.theme.base.LayerTheme
import com.glia.widgets.view.unifiedui.theme.base.TextTheme
import com.glia.widgets.view.unifiedui.theme.bubble.BubbleTheme
import com.glia.widgets.view.unifiedui.theme.gva.GvaTheme
import com.glia.widgets.view.unifiedui.theme.securemessaging.SecureMessagingTheme

internal data class ChatTheme(
    val background: LayerTheme? = null,
    val header: HeaderTheme? = null,
    val operatorMessage: MessageBalloonTheme? = null,
    val visitorMessage: MessageBalloonTheme? = null,
    val connect: EngagementStatesTheme? = null,
    val input: InputTheme? = null,
    val inputDisabled: InputTheme? = null,
    val responseCard: ResponseCardTheme? = null,
    val audioUpgrade: MediaUpgradeTheme? = null,
    val videoUpgrade: MediaUpgradeTheme? = null,
    val bubble: BubbleTheme? = null,
    val attachmentsPopup: AttachmentsPopupTheme? = null,
    val unreadIndicator: UnreadIndicatorTheme? = null,
    val typingIndicator: ColorTheme? = null,
    val newMessagesDividerColorTheme: ColorTheme? = null,
    val newMessagesDividerTextTheme: TextTheme? = null,
    val gva: GvaTheme? = null,
    val secureMessaging: SecureMessagingTheme? = null
) : Mergeable<ChatTheme> {
    override fun merge(other: ChatTheme): ChatTheme = ChatTheme(
        background = background merge other.background,
        header = header merge other.header,
        operatorMessage = operatorMessage merge other.operatorMessage,
        visitorMessage = visitorMessage merge other.visitorMessage,
        connect = connect merge other.connect,
        input = input merge other.input,
        inputDisabled = inputDisabled merge other.inputDisabled,
        responseCard = responseCard merge other.responseCard,
        audioUpgrade = audioUpgrade merge other.audioUpgrade,
        videoUpgrade = videoUpgrade merge other.videoUpgrade,
        bubble = bubble merge other.bubble,
        attachmentsPopup = attachmentsPopup merge other.attachmentsPopup,
        unreadIndicator = unreadIndicator merge other.unreadIndicator,
        typingIndicator = typingIndicator merge other.typingIndicator,
        newMessagesDividerColorTheme = newMessagesDividerColorTheme merge other.newMessagesDividerColorTheme,
        newMessagesDividerTextTheme = newMessagesDividerTextTheme merge other.newMessagesDividerTextTheme,
        gva = gva merge other.gva,
        secureMessaging = secureMessaging merge other.secureMessaging
    )
}
