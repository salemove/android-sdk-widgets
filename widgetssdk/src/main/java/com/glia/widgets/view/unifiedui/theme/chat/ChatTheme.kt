package com.glia.widgets.view.unifiedui.theme.chat

import com.glia.widgets.view.unifiedui.theme.base.ColorTheme
import com.glia.widgets.view.unifiedui.theme.base.HeaderTheme
import com.glia.widgets.view.unifiedui.theme.base.LayerTheme
import com.glia.widgets.view.unifiedui.theme.bubble.BubbleTheme

internal data class ChatTheme(
    val background: LayerTheme?,
    val header: HeaderTheme?,
    val operatorMessage: MessageBalloonTheme?,
    val visitorMessage: MessageBalloonTheme?,
    val connect: EngagementStatesTheme?,
    val input: InputTheme?,
    val responseCard: ResponseCardTheme?,
    val audioUpgrade: UpgradeTheme?,
    val videoUpgrade: UpgradeTheme?,
    val bubble: BubbleTheme?,
    val attachmentsPopup: AttachmentsPopupTheme?,
    val unreadIndicator: BubbleTheme?,
    val typingIndicator: ColorTheme?
)