@file:Suppress("FunctionName")

package com.glia.widgets.view.unifiedui.theme.defaulttheme

import com.glia.widgets.view.unifiedui.theme.ColorPallet
import com.glia.widgets.view.unifiedui.theme.UnifiedTheme

/**
 * Default [UnifiedTheme] generated for [ColorPallet]
 */
internal fun DefaultTheme(pallet: ColorPallet?): UnifiedTheme? = pallet?.let {
    UnifiedTheme(
        alertTheme = AlertTheme(it),
        bubbleTheme = BubbleTheme(it),
        callTheme = CallTheme(it),
        chatTheme = ChatTheme(it),
        surveyTheme = SurveyTheme(it),
        callVisualizerTheme = CallVisualizerTheme(it),
        secureConversationsWelcomeScreenTheme = SecureConversationsWelcomeScreenTheme(it),
        secureConversationsConfirmationScreenTheme = SecureConversationsConfirmationScreenTheme(it),
        snackBarTheme = DefaultSnackBarTheme(it)
    )
}
