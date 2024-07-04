package com.glia.widgets.view.unifiedui.theme.gva

import com.glia.widgets.view.unifiedui.Mergeable
import com.glia.widgets.view.unifiedui.merge
import com.glia.widgets.view.unifiedui.theme.base.ButtonTheme
import com.glia.widgets.view.unifiedui.theme.base.LayerTheme
import com.glia.widgets.view.unifiedui.theme.base.TextTheme

internal data class GvaPersistentButtonTheme(
    val title: TextTheme? = null,
    val background: LayerTheme? = null,
    val button: ButtonTheme? = null
) : Mergeable<GvaPersistentButtonTheme> {
    override fun merge(other: GvaPersistentButtonTheme): GvaPersistentButtonTheme = GvaPersistentButtonTheme(
        title = title merge other.title,
        background = background merge other.background,
        button = button merge other.button
    )
}
