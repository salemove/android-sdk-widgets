package com.glia.widgets.view.unifiedui.theme.entrywidget

import com.glia.widgets.view.unifiedui.Mergeable
import com.glia.widgets.view.unifiedui.merge
import com.glia.widgets.view.unifiedui.theme.base.ButtonTheme
import com.glia.widgets.view.unifiedui.theme.base.LayerTheme
import com.glia.widgets.view.unifiedui.theme.base.TextTheme

internal data class EntryWidgetTheme(
    val background: LayerTheme? = null,
    val mediaTypeItems: MediaTypeItemsTheme? = null,
    val errorTitle: TextTheme? = null,
    val errorMessage: TextTheme? = null,
    val errorButton: ButtonTheme? = null
) : Mergeable<EntryWidgetTheme> {
    override fun merge(other: EntryWidgetTheme): EntryWidgetTheme = EntryWidgetTheme(
        background = background merge other.background,
        mediaTypeItems = mediaTypeItems merge other.mediaTypeItems,
        errorTitle = errorTitle merge other.errorTitle,
        errorMessage = errorMessage merge other.errorMessage,
        errorButton = errorButton merge other.errorButton
    )
}
