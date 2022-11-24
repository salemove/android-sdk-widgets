package com.glia.widgets.view.unifiedui.theme.call

import com.glia.widgets.view.unifiedui.theme.base.HeaderTheme
import com.glia.widgets.view.unifiedui.theme.base.LayerTheme
import com.glia.widgets.view.unifiedui.theme.base.TextTheme
import com.glia.widgets.view.unifiedui.theme.chat.EngagementStatesTheme

internal data class CallTheme(
    val background: LayerTheme?,
    val bottomText: TextTheme?,
    val buttonBar: ButtonBarTheme?,
    val duration: TextTheme?,
    val header: HeaderTheme?,
    val operator: TextTheme?,
    val topText: TextTheme?,
    val connect: EngagementStatesTheme?
)
