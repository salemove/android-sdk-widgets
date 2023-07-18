@file:Suppress("FunctionName")

package com.glia.widgets.view.unifiedui.theme.defaulttheme

import com.glia.widgets.view.unifiedui.theme.ColorPallet
import com.glia.widgets.view.unifiedui.theme.gva.GvaTheme

/**
 * Default Theme for Gva
 */
internal fun GvaTheme(pallet: ColorPallet): GvaTheme = GvaTheme(
    quickReplyTheme = pallet.primaryColorTheme?.let { OutlinedButtonTheme(it, it) }
)
