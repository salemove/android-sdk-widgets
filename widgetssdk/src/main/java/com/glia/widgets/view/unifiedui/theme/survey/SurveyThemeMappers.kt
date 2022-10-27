package com.glia.widgets.view.unifiedui.theme.survey

import com.glia.widgets.view.unifiedui.config.survey.*
import com.glia.widgets.view.unifiedui.theme.base.updateFrom

internal fun ThemeOptionButton?.updateFrom(optionButtonRemoteConfig: OptionButtonRemoteConfig?): ThemeOptionButton? =
    optionButtonRemoteConfig?.let {
        ThemeOptionButton(
            normalText = this?.normalText.updateFrom(it.normalTextRemoteConfig),
            normalLayer = this?.normalLayer.updateFrom(it.normalLayerRemoteConfig),
            selectedText = this?.selectedText.updateFrom(it.selectedTextRemoteConfig),
            selectedLayer = this?.selectedLayer.updateFrom(it.selectedLayerRemoteConfig),
            highlightedText = this?.highlightedText.updateFrom(it.highlightedTextRemoteConfig),
            highlightedLayer = this?.highlightedLayer.updateFrom(it.highlightedLayerRemoteConfig),
            fontSize = it.fontRemoteConfig?.size?.value ?: this?.fontSize,
            fontStyle = it.fontRemoteConfig?.style?.style ?: this?.fontStyle
        )
    } ?: this

internal fun ThemeSurveyBooleanQuestion?.updateFrom(surveyBooleanQuestionRemoteConfig: SurveyBooleanQuestionRemoteConfig?): ThemeSurveyBooleanQuestion? =
    surveyBooleanQuestionRemoteConfig?.let {
        ThemeSurveyBooleanQuestion(
            title = this?.title.updateFrom(it.title),
            optionButton = this?.optionButton.updateFrom(it.optionButtonRemoteConfig)
        )
    } ?: this

internal fun ThemeSurveyScaleQuestion?.updateFrom(surveyScaleQuestionRemoteConfig: SurveyScaleQuestionRemoteConfig?): ThemeSurveyScaleQuestion? =
    surveyScaleQuestionRemoteConfig?.let {
        ThemeSurveyScaleQuestion(
            title = this?.title.updateFrom(it.title),
            optionButton = this?.optionButton.updateFrom(it.optionButtonRemoteConfig)
        )
    } ?: this

internal fun ThemeSurveySingleQuestion?.updateFrom(surveySingleQuestionRemoteConfig: SurveySingleQuestionRemoteConfig?): ThemeSurveySingleQuestion? =
    surveySingleQuestionRemoteConfig?.let {
        ThemeSurveySingleQuestion(
            title = this?.title.updateFrom(it.title),
            tintColor = this?.tintColor.updateFrom(it.tintColor),
            option = this?.option.updateFrom(it.option)
        )
    } ?: this

internal fun ThemeSurveyInputQuestion?.updateFrom(surveyInputQuestionRemoteConfig: SurveyInputQuestionRemoteConfig?): ThemeSurveyInputQuestion? =
    surveyInputQuestionRemoteConfig?.let {
        ThemeSurveyInputQuestion(
            title = this?.title.updateFrom(it.title),
            background = this?.background.updateFrom(it.background),
            text = this?.text.updateFrom(it.textRemoteConfig),
            option = this?.option.updateFrom(it.option)
        )
    } ?: this

internal fun SurveyTheme?.updateFrom(surveyRemoteConfig: SurveyRemoteConfig?): SurveyTheme? = surveyRemoteConfig?.let {
    SurveyTheme(
        layer = this?.layer.updateFrom(it.layerRemoteConfig),
        title = this?.title.updateFrom(it.title),
        submitButton = this?.submitButton.updateFrom(it.submitButtonRemoteConfig),
        cancelButton = this?.cancelButton.updateFrom(it.cancelButtonRemoteConfig),
        booleanQuestion = this?.booleanQuestion.updateFrom(it.booleanQuestion),
        scaleQuestion = this?.scaleQuestion.updateFrom(it.scaleQuestion),
        singleQuestion = this?.singleQuestion.updateFrom(it.singleQuestion),
        inputQuestion = this?.inputQuestion.updateFrom(it.inputQuestion)
    )
} ?: this