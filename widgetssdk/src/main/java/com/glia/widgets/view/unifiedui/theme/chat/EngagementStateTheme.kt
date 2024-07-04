package com.glia.widgets.view.unifiedui.theme.chat

import com.glia.widgets.view.unifiedui.Mergeable
import com.glia.widgets.view.unifiedui.merge
import com.glia.widgets.view.unifiedui.theme.base.TextTheme

internal data class EngagementStateTheme(
    val title: TextTheme?,
    val description: TextTheme?
) : Mergeable<EngagementStateTheme> {
    override fun merge(other: EngagementStateTheme): EngagementStateTheme = EngagementStateTheme(
        title = title merge other.title,
        description = description merge other.description
    )
}
