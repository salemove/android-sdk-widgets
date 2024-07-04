package com.glia.widgets.view.unifiedui.theme.chat

import com.glia.widgets.view.unifiedui.Mergeable
import com.glia.widgets.view.unifiedui.merge
import com.glia.widgets.view.unifiedui.theme.base.ColorTheme

internal data class UserImageTheme(
    val placeholderColor: ColorTheme? = null,
    val placeholderBackgroundColor: ColorTheme? = null,
    val imageBackgroundColor: ColorTheme? = null
) : Mergeable<UserImageTheme> {
    override fun merge(other: UserImageTheme): UserImageTheme = UserImageTheme(
        placeholderColor = placeholderColor merge other.placeholderColor,
        placeholderBackgroundColor = placeholderBackgroundColor merge other.placeholderBackgroundColor,
        imageBackgroundColor = imageBackgroundColor merge other.imageBackgroundColor
    )
}
