package com.glia.widgets.view.unifiedui.theme.gva

import com.glia.widgets.view.unifiedui.Mergeable
import com.glia.widgets.view.unifiedui.merge
import com.glia.widgets.view.unifiedui.theme.base.ButtonTheme

internal data class GvaTheme(
    val quickReplyTheme: ButtonTheme? = null,
    val persistentButtonTheme: GvaPersistentButtonTheme? = null,
    val galleryCardTheme: GvaGalleryCardTheme? = null
) : Mergeable<GvaTheme> {
    override fun merge(other: GvaTheme): GvaTheme = GvaTheme(
        quickReplyTheme = quickReplyTheme merge other.quickReplyTheme,
        persistentButtonTheme = persistentButtonTheme merge other.persistentButtonTheme,
        galleryCardTheme = galleryCardTheme merge other.galleryCardTheme
    )
}
