package com.glia.widgets.view.unifiedui.theme.gva

import com.glia.widgets.view.unifiedui.Mergeable
import com.glia.widgets.view.unifiedui.merge
import com.glia.widgets.view.unifiedui.theme.base.ButtonTheme
import com.glia.widgets.view.unifiedui.theme.base.LayerTheme
import com.glia.widgets.view.unifiedui.theme.base.TextTheme

internal data class GvaGalleryCardTheme(
    val title: TextTheme? = null,
    val subtitle: TextTheme? = null,
    val image: LayerTheme? = null,
    val button: ButtonTheme? = null,
    val background: LayerTheme? = null
) : Mergeable<GvaGalleryCardTheme> {
    override fun merge(other: GvaGalleryCardTheme): GvaGalleryCardTheme = GvaGalleryCardTheme(
        title = title merge other.title,
        subtitle = subtitle merge other.subtitle,
        image = image merge other.image,
        button = button merge other.button,
        background = background merge other.background
    )
}
