package com.glia.widgets.view.unifiedui.theme.call

import com.glia.widgets.view.unifiedui.theme.SnackBarTheme
import com.glia.widgets.view.unifiedui.theme.base.HeaderTheme
import com.glia.widgets.view.unifiedui.theme.base.LayerTheme
import com.glia.widgets.view.unifiedui.theme.base.TextTheme
import com.glia.widgets.view.unifiedui.theme.chat.EngagementStatesTheme

internal data class CallTheme(
    val background: LayerTheme? = null,
    val bottomText: TextTheme? = null,
    val buttonBar: ButtonBarTheme? = null,
    val duration: TextTheme? = null,
    val header: HeaderTheme? = null,
    val operator: TextTheme? = null,
    val topText: TextTheme? = null,
    val connect: EngagementStatesTheme? = null,
    val snackBar: SnackBarTheme? = null
)
