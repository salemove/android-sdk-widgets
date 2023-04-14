@file:Suppress("FunctionName")

package com.glia.widgets.view.unifiedui.theme.defaulttheme

import android.graphics.Color
import com.glia.widgets.view.unifiedui.extensions.composeIfAtLeastOneNotNull
import com.glia.widgets.view.unifiedui.theme.ColorPallet
import com.glia.widgets.view.unifiedui.theme.base.LayerTheme
import com.glia.widgets.view.unifiedui.theme.survey.*

/**
 * Default theme for Survey screen
 */
internal fun SurveyTheme(pallet: ColorPallet): SurveyTheme =
    SurveyTheme(
        layer = LayerTheme(fill = pallet.backgroundColorTheme),
        title = BaseNormalColorTextTheme(pallet),
        submitButton = PositiveDefaultButtonTheme(pallet),
        cancelButton = NegativeDefaultButtonTheme(pallet),
        booleanQuestion = SurveyBooleanQuestionTheme(
            title = BaseDarkColorTextTheme(pallet),
            SurveyOptionButtonTheme(pallet)
        ),
        scaleQuestion = SurveyScaleQuestionTheme(
            title = BaseDarkColorTextTheme(pallet),
            SurveyOptionButtonTheme(pallet)
        ),
        singleQuestion = SurveySingleQuestionTheme(
            title = BaseDarkColorTextTheme(pallet),
            tintColor = pallet.primaryColorTheme,
            option = BaseDarkColorTextTheme(pallet)
        ),
        inputQuestion = SurveyInputQuestionTheme(
            title = BaseDarkColorTextTheme(pallet),
            option = SurveyOptionButtonTheme(pallet),
            text = BaseDarkColorTextTheme(pallet)
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
            normalText = BaseDarkColorTextTheme(this),
            normalLayer = LayerTheme(
                fill = backgroundColorTheme,
                stroke = baseNormalColorTheme?.primaryColor
            ),
            selectedText = BaseLightColorTextTheme(this),
            selectedLayer = LayerTheme(fill = primaryColorTheme, stroke = Color.TRANSPARENT),
            highlightedText = BaseNegativeColorTextTheme(this),
            highlightedLayer = LayerTheme(
                fill = backgroundColorTheme,
                stroke = systemNegativeColorTheme?.primaryColor
            )
        )
    }
}
