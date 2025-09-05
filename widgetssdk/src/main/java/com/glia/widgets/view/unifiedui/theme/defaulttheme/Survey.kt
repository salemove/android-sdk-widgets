@file:Suppress("FunctionName")

package com.glia.widgets.view.unifiedui.theme.defaulttheme

import android.graphics.Color
import com.glia.widgets.view.unifiedui.composeIfAtLeastOneNotNull
import com.glia.widgets.view.unifiedui.theme.ColorPallet
import com.glia.widgets.view.unifiedui.theme.base.LayerTheme
import com.glia.widgets.view.unifiedui.theme.survey.SurveyOptionTheme
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
            SurveyOptionTheme(pallet)
        ),
        scaleQuestion = SurveyScaleQuestionTheme(
            title = BaseDarkColorTextTheme(pallet),
            SurveyOptionTheme(pallet)
        ),
        singleQuestion = SurveySingleQuestionTheme(
            title = BaseDarkColorTextTheme(pallet),
            tintColor = pallet.primaryColorTheme,
            option = BaseDarkColorTextTheme(pallet)
        ),
        inputQuestion = SurveyInputQuestionTheme(
            title = BaseDarkColorTextTheme(pallet),
            inputField = SurveyInputOptionTheme(pallet),
        )
    )

/**
 * Default theme for Survey option
 */
internal fun SurveyOptionTheme(pallet: ColorPallet): SurveyOptionTheme? = pallet.run {
    composeIfAtLeastOneNotNull(
        darkColorTheme,
        normalColorTheme,
        primaryColorTheme,
        lightColorTheme,
        negativeColorTheme
    ) {
        SurveyOptionTheme(
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

/**
 * Default theme for Survey input option
 */
internal fun SurveyInputOptionTheme(pallet: ColorPallet): SurveyOptionTheme? = pallet.run {
    composeIfAtLeastOneNotNull(
        darkColorTheme,
        normalColorTheme,
        primaryColorTheme,
        lightColorTheme,
        negativeColorTheme
    ) {
        SurveyOptionTheme(
            normalText = BaseDarkColorTextTheme(this),
            normalLayer = LayerTheme(
                fill = lightColorTheme,
                stroke = normalColorTheme?.primaryColor
            ),
            selectedText = BaseDarkColorTextTheme(this),
            selectedLayer = LayerTheme(fill = lightColorTheme, stroke = normalColorTheme?.primaryColor),
            highlightedText = BaseNegativeColorTextTheme(this),
            highlightedLayer = LayerTheme(
                fill = lightColorTheme,
                stroke = negativeColorTheme?.primaryColor
            )
        )
    }
}
