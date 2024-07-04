package com.glia.widgets.view.unifiedui.theme.bubble

import com.glia.widgets.view.unifiedui.Mergeable
import com.glia.widgets.view.unifiedui.merge
import com.glia.widgets.view.unifiedui.theme.base.BadgeTheme
import com.glia.widgets.view.unifiedui.theme.chat.OnHoldOverlayTheme
import com.glia.widgets.view.unifiedui.theme.chat.UserImageTheme

internal data class BubbleTheme(
    val userImage: UserImageTheme? = null,
    val badge: BadgeTheme? = null,
    val onHoldOverlay: OnHoldOverlayTheme? = null
) : Mergeable<BubbleTheme> {
    override fun merge(other: BubbleTheme): BubbleTheme = BubbleTheme(
        userImage = userImage merge other.userImage,
        badge = badge merge other.badge,
        onHoldOverlay = onHoldOverlay merge other.onHoldOverlay
    )
}
