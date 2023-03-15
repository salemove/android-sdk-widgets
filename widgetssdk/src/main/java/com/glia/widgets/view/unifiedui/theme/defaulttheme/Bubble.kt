@file:Suppress("FunctionName")

package com.glia.widgets.view.unifiedui.theme.defaulttheme

import com.glia.widgets.view.unifiedui.exstensions.composeIfAtLeastOneNotNull
import com.glia.widgets.view.unifiedui.theme.ColorPallet
import com.glia.widgets.view.unifiedui.theme.base.BadgeTheme
import com.glia.widgets.view.unifiedui.theme.base.LayerTheme
import com.glia.widgets.view.unifiedui.theme.bubble.BubbleTheme
import com.glia.widgets.view.unifiedui.theme.chat.UserImageTheme

/**
 * Default theme for Unread messages indicator
 */
internal fun BubbleTheme(pallet: ColorPallet): BubbleTheme? =
    pallet.run {
        val badge = BadgeTheme(this)
        val userImage = UserImageTheme(this)
        composeIfAtLeastOneNotNull(badge, userImage) {
            BubbleTheme(
                userImage = userImage,
                badge = badge,
            )
        }
    }

/**
 * Default theme for User image
 */
internal fun UserImageTheme(pallet: ColorPallet): UserImageTheme? = pallet.run {
    composeIfAtLeastOneNotNull(primaryColorTheme, baseLightColorTheme) {
        UserImageTheme(
            placeholderColor = baseLightColorTheme,
            placeholderBackgroundColor = primaryColorTheme,
            imageBackgroundColor = primaryColorTheme
        )
    }
}

/**
 * Default theme for Badge
 */
internal fun BadgeTheme(pallet: ColorPallet): BadgeTheme? = pallet.run {
    composeIfAtLeastOneNotNull(baseNormalColorTheme, primaryColorTheme) {
        BadgeTheme(
            textColor = baseNormalColorTheme,
            background = LayerTheme(fill = primaryColorTheme)

        )
    }
}

