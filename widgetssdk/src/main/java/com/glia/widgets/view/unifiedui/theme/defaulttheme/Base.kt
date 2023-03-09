@file:Suppress("FunctionName")

package com.glia.widgets.view.unifiedui.theme.defaulttheme

import com.glia.widgets.view.unifiedui.theme.ColorPallet
import com.glia.widgets.view.unifiedui.theme.UnifiedTheme
import com.glia.widgets.view.unifiedui.theme.base.*

/**
 * Default [UnifiedTheme] generated for [ColorPallet]
 */
internal fun DefaultTheme(pallet: ColorPallet?): UnifiedTheme? = pallet?.let {
    UnifiedTheme(
        callTheme = CallDefaultTheme(it),
        chatTheme = ChatDefaultTheme(it),
        alertTheme = AlertDefaultTheme(it),
        bubbleTheme = null,
        surveyTheme = null,
        callVisualizerTheme = null
    )
}