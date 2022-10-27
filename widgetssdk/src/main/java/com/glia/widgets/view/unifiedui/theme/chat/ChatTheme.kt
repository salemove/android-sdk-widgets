package com.glia.widgets.view.unifiedui.theme.chat

import android.os.Parcelable
import com.glia.widgets.view.unifiedui.theme.base.ThemeColor
import com.glia.widgets.view.unifiedui.theme.base.ThemeHeader
import com.glia.widgets.view.unifiedui.theme.base.ThemeLayer
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class ChatTheme(
    val background: ThemeLayer?,
    val header: ThemeHeader?,
    val operatorMessage: ThemeMessageBalloon?,
    val visitorMessage: ThemeMessageBalloon?,
    val connect: ThemeEngagementStates?,
    val input: ThemeInput?,
    val responseCard: ThemeResponseCard?,
    val audioUpgrade: ThemeUpgrade?,
    val videoUpgrade: ThemeUpgrade?,
    val bubble: ThemeBubble?,
    val attachmentsPopup: ThemeAttachmentsPopup?,
    val unreadIndicator: ThemeBubble?,
    val typingIndicator: ThemeColor?
) : Parcelable