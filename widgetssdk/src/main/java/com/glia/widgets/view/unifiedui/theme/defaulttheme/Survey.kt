@file:Suppress("FunctionName")

package com.glia.widgets.view.unifiedui.theme.defaulttheme

import android.graphics.Color
import com.glia.widgets.view.unifiedui.extensions.composeIfAtLeastOneNotNull
import com.glia.widgets.view.unifiedui.theme.ColorPallet
import com.glia.widgets.view.unifiedui.theme.base.LayerTheme
import com.glia.widgets.view.unifiedui.theme.base.TextTheme
import com.glia.widgets.view.unifiedui.theme.survey.*

/**
 * Default theme for Survey screen
 */
internal fun SurveyTheme(pallet: ColorPallet): SurveyTheme =
    SurveyTheme(
        layer = LayerTheme(fill = pallet.backgroundColorTheme),
        title = TextTheme(textColor = pallet.baseNormalColorTheme),
        submitButton = PositiveDefaultButtonTheme(pallet),
        cancelButton = NegativeDefaultButtonTheme(pallet),
        booleanQuestion = SurveyBooleanQuestionTheme(
            title = TextTheme(textColor = pallet.baseDarkColorTheme),
            SurveyOptionButtonTheme(pallet)
        ),
        scaleQuestion = SurveyScaleQuestionTheme(
            title = TextTheme(textColor = pallet.baseDarkColorTheme),
            SurveyOptionButtonTheme(pallet)
        ),
        singleQuestion = SurveySingleQuestionTheme(
            title = TextTheme(textColor = pallet.baseDarkColorTheme),
            tintColor = pallet.primaryColorTheme,
            option = TextTheme(textColor = pallet.baseDarkColorTheme)
        ),
        inputQuestion = SurveyInputQuestionTheme(
            title = TextTheme(textColor = pallet.baseDarkColorTheme),
            option = SurveyOptionButtonTheme(pallet),
            text = TextTheme(textColor = pallet.baseDarkColorTheme)
        )
    )

/**
 * Default theme for Survey option button
 */
internal fun SurveyOptionButtonTheme(pallet: ColorPallet): OptionButtonTheme? = pallet.run {
    composeIfAtLeastOneNotNull(
        baseDarkColorTheme,
        baseNormalColorTheme,
        primaryColorTheme,
        baseLightColorTheme,
        systemNegativeColorTheme
    ) {
        OptionButtonTheme(
            normalText = TextTheme(textColor = baseDarkColorTheme),
            normalLayer = LayerTheme(
                fill = backgroundColorTheme,
                stroke = baseNormalColorTheme?.primaryColor
            ),
            selectedText = TextTheme(textColor = baseLightColorTheme),
            selectedLayer = LayerTheme(fill = primaryColorTheme, stroke = Color.TRANSPARENT),
            highlightedText = TextTheme(textColor = systemNegativeColorTheme),
            highlightedLayer = LayerTheme(
                fill = backgroundColorTheme,
                stroke = systemNegativeColorTheme?.primaryColor
            )
        )
    }
}
