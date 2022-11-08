package com.glia.widgets.view.unifiedui.theme.survey

import com.glia.widgets.view.configuration.OptionButtonConfiguration
import com.glia.widgets.view.configuration.survey.BooleanQuestionConfiguration
import com.glia.widgets.view.configuration.survey.InputQuestionConfiguration
import com.glia.widgets.view.configuration.survey.ScaleQuestionConfiguration
import com.glia.widgets.view.configuration.survey.SingleQuestionConfiguration
import com.glia.widgets.view.configuration.survey.SurveyStyle
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

internal fun ThemeOptionButton?.updateFrom(optionButtonConfiguration: OptionButtonConfiguration?): ThemeOptionButton? =
    optionButtonConfiguration?.let {
        ThemeOptionButton(
            normalText = this?.normalText.updateFrom(it.normalText),
            normalLayer = this?.normalLayer.updateFrom(it.normalLayer),
            selectedText = this?.selectedText.updateFrom(it.selectedText),
            selectedLayer = this?.selectedLayer.updateFrom(it.selectedLayer),
            highlightedText = this?.highlightedText.updateFrom(it.highlightedText),
            highlightedLayer = this?.highlightedLayer.updateFrom(it.highlightedLayer),
            fontSize = this?.fontSize,
            fontStyle = this?.fontStyle
        )
    } ?: this

//mappers from old configs
internal fun ThemeSurveyBooleanQuestion?.updateFrom(booleanQuestionConfiguration: BooleanQuestionConfiguration?): ThemeSurveyBooleanQuestion? =
    booleanQuestionConfiguration?.let {
        ThemeSurveyBooleanQuestion(
            title = this?.title.updateFrom(it.title),
            optionButton = this?.optionButton.updateFrom(it.optionButton)
        )
    } ?: this

internal fun ThemeSurveyScaleQuestion?.updateFrom(scaleQuestionConfiguration: ScaleQuestionConfiguration?): ThemeSurveyScaleQuestion? =
    scaleQuestionConfiguration?.let {
        ThemeSurveyScaleQuestion(
            title = this?.title.updateFrom(it.title),
            optionButton = this?.optionButton.updateFrom(it.optionButton)
        )
    } ?: this

internal fun ThemeSurveySingleQuestion?.updateFrom(singleQuestionConfiguration: SingleQuestionConfiguration?): ThemeSurveySingleQuestion? =
    singleQuestionConfiguration?.let {
        ThemeSurveySingleQuestion(
            title = this?.title.updateFrom(it.title),
            tintColor = this?.tintColor.updateFrom(it.tintColor),
            option = this?.option.updateFrom(it.optionText)
        )
    } ?: this

internal fun ThemeSurveyInputQuestion?.updateFrom(inputQuestionConfiguration: InputQuestionConfiguration?): ThemeSurveyInputQuestion? =
    inputQuestionConfiguration?.let {
        ThemeSurveyInputQuestion(
            title = this?.title.updateFrom(it.title),
            background = this?.background,
            text = this?.text.updateFrom(it.title),
            option = this?.option.updateFrom(it.optionButton)
        )
    } ?: this


internal fun SurveyTheme?.updateFrom(surveyStyle: SurveyStyle?): SurveyTheme? = surveyStyle?.let {
    SurveyTheme(
        layer = this?.layer.updateFrom(it.layer),
        title = this?.title.updateFrom(it.title),
        submitButton = this?.submitButton.updateFrom(it.submitButton),
        cancelButton = this?.cancelButton.updateFrom(it.cancelButton),
        booleanQuestion = this?.booleanQuestion.updateFrom(it.booleanQuestion),
        scaleQuestion = this?.scaleQuestion.updateFrom(it.scaleQuestion),
        singleQuestion = this?.singleQuestion.updateFrom(it.singleQuestion),
        inputQuestion = this?.inputQuestion.updateFrom(it.inputQuestion)
    )
} ?: this