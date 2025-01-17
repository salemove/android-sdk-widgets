package com.glia.widgets.view.unifiedui.theme.entrywidget

import com.glia.widgets.view.unifiedui.Mergeable
import com.glia.widgets.view.unifiedui.merge
import com.glia.widgets.view.unifiedui.theme.base.ColorTheme

internal data class MediaTypeItemsTheme(
    val mediaTypeItem: MediaTypeItemTheme? = null,
    val dividerColor: ColorTheme? = null
) : Mergeable<MediaTypeItemsTheme> {
    override fun merge(other: MediaTypeItemsTheme): MediaTypeItemsTheme = MediaTypeItemsTheme(
        mediaTypeItem = mediaTypeItem merge other.mediaTypeItem,
        dividerColor = dividerColor merge other.dividerColor
    )
}
