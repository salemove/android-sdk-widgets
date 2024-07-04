package com.glia.widgets.view.unifiedui.theme.call

import com.glia.widgets.view.unifiedui.Mergeable
import com.glia.widgets.view.unifiedui.merge

internal data class BarButtonStatesTheme(
    val disabled: BarButtonStyleTheme? = null,
    val enabled: BarButtonStyleTheme? = null,
    val activated: BarButtonStyleTheme? = null
) : Mergeable<BarButtonStatesTheme> {
    override fun merge(other: BarButtonStatesTheme): BarButtonStatesTheme = BarButtonStatesTheme(
        disabled = disabled merge other.disabled,
        enabled = enabled merge other.enabled,
        activated = activated merge other.activated
    )
}
