@file:Suppress("FunctionName")

package com.glia.widgets.view.unifiedui.theme.defaulttheme

import android.graphics.Color
import com.glia.widgets.view.unifiedui.composeIfAtLeastOneNotNull
import com.glia.widgets.view.unifiedui.theme.ColorPallet
import com.glia.widgets.view.unifiedui.theme.base.LayerTheme
import com.glia.widgets.view.unifiedui.theme.survey.OptionButtonTheme
import com.glia.widgets.view.unifiedui.theme.survey.SurveyBooleanQuestionTheme
import com.glia.widgets.view.unifiedui.theme.survey.SurveyInputQuestionTheme
import com.glia.widgets.view.unifiedui.theme.survey.SurveyScaleQuestionTheme
import com.glia.widgets.view.unifiedui.theme.survey.SurveySingleQuestionTheme
import com.glia.widgets.view.unifiedui.theme.survey.SurveyTheme

/**
 * Default theme for Survey screen
 */
internal fun SurveyTheme(pallet: ColorPallet): SurveyTheme =
    SurveyTheme(
        layer = LayerTheme(fill = pallet.lightColorTheme),
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
        darkColorTheme,
        normalColorTheme,
        primaryColorTheme,
        lightColorTheme,
        negativeColorTheme
    ) {
        OptionButtonTheme(
            normalText = BaseDarkColorTextTheme(this),
            normalLayer = LayerTheme(
                fill = lightColorTheme,
                stroke = normalColorTheme?.primaryColor
            ),
            selectedText = BaseLightColorTextTheme(this),
            selectedLayer = LayerTheme(fill = primaryColorTheme, stroke = Color.TRANSPARENT),
            highlightedText = BaseNegativeColorTextTheme(this),
            highlightedLayer = LayerTheme(
                fill = lightColorTheme,
                stroke = negativeColorTheme?.primaryColor
            )
        )
    }
}
