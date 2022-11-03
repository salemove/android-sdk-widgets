package com.glia.widgets.view.unifiedui.theme.call

import android.os.Parcelable
import com.glia.widgets.view.unifiedui.theme.base.ThemeButton
import com.glia.widgets.view.unifiedui.theme.base.ThemeHeader
import com.glia.widgets.view.unifiedui.theme.base.ThemeLayer
import com.glia.widgets.view.unifiedui.theme.base.ThemeText
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class CallTheme(
    val background: ThemeLayer?,
    val bottomText: ThemeText?,
    val buttonBar: ThemeButtonBar?,
    val duration: ThemeText?,
    val endButton: ThemeButton?,
    val header: ThemeHeader?,
    val operator: ThemeText?,
    val topText: ThemeText?
) : Parcelable
