package com.glia.widgets.view.unifiedui.theme.chat

import com.glia.widgets.view.unifiedui.theme.base.ColorTheme
import com.glia.widgets.view.unifiedui.theme.base.HeaderTheme
import com.glia.widgets.view.unifiedui.theme.base.LayerTheme
import com.glia.widgets.view.unifiedui.theme.base.TextTheme
import com.glia.widgets.view.unifiedui.theme.bubble.BubbleTheme
import com.glia.widgets.view.unifiedui.theme.gva.GvaTheme

internal data class ChatTheme(
    val background: LayerTheme? = null,
    val header: HeaderTheme? = null,
    val operatorMessage: MessageBalloonTheme? = null,
    val visitorMessage: MessageBalloonTheme? = null,
    val connect: EngagementStatesTheme? = null,
    val input: InputTheme? = null,
    val responseCard: ResponseCardTheme? = null,
    val audioUpgrade: MediaUpgradeTheme? = null,
    val videoUpgrade: MediaUpgradeTheme? = null,
    val bubble: BubbleTheme? = null,
    val attachmentsPopup: AttachmentsPopupTheme? = null,
    val unreadIndicator: UnreadIndicatorTheme? = null,
    val typingIndicator: ColorTheme? = null,
    val newMessagesDividerColorTheme: ColorTheme? = null,
    val newMessagesDividerTextTheme: TextTheme? = null,
    val gva: GvaTheme? = null
)
