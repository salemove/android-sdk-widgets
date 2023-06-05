@file:Suppress("FunctionName")

package com.glia.widgets.view.unifiedui.theme.defaulttheme

import com.glia.widgets.view.unifiedui.theme.ColorPallet
import com.glia.widgets.view.unifiedui.theme.UnifiedTheme

/**
 * Default [UnifiedTheme] generated for [ColorPallet]
 */
internal fun DefaultTheme(pallet: ColorPallet?): UnifiedTheme? = pallet?.let {
    UnifiedTheme(
        callTheme = CallTheme(it),
        chatTheme = ChatTheme(it),
        alertTheme = AlertTheme(it),
        bubbleTheme = BubbleTheme(it),
        surveyTheme = SurveyTheme(it),
        callVisualizerTheme = CallVisualizerTheme(it),
        secureConversationsWelcomeScreenTheme = SecureConversationsWelcomeScreenTheme(it),
        secureConversationsConfirmationScreenTheme = SecureConversationsConfirmationScreenTheme(it)
    )
}
