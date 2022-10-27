package com.glia.widgets.view.unifiedui.theme.bubble

import android.os.Parcelable
import com.glia.widgets.view.unifiedui.config.bubble.BubbleRemoteConfig
import com.glia.widgets.view.unifiedui.theme.base.ThemeButton
import com.glia.widgets.view.unifiedui.theme.base.ThemeColor
import com.glia.widgets.view.unifiedui.theme.base.updateFrom
import com.glia.widgets.view.unifiedui.theme.chat.ThemeUserImage
import com.glia.widgets.view.unifiedui.theme.chat.updateFrom
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class BubbleTheme(
    val userImage: ThemeUserImage?,
    val badge: ThemeButton?,
    val onHoldOverlay: ThemeColor?
) : Parcelable

internal fun BubbleTheme?.updateFrom(bubbleRemoteConfig: BubbleRemoteConfig?): BubbleTheme? = bubbleRemoteConfig?.let {
    BubbleTheme(
        userImage = this?.userImage.updateFrom(it.userImage),
        badge = this?.badge.updateFrom(it.badge),
        onHoldOverlay = this?.onHoldOverlay.updateFrom(it.onHoldOverlay?.color)
    )
} ?: this
