package com.glia.widgets.view.unifiedui.config.survey

import com.glia.widgets.view.unifiedui.config.base.ButtonRemoteConfig
import com.glia.widgets.view.unifiedui.config.base.LayerRemoteConfig
import com.glia.widgets.view.unifiedui.config.base.TextRemoteConfig
import com.glia.widgets.view.unifiedui.theme.survey.SurveyTheme
import com.google.gson.annotations.SerializedName

internal data class SurveyRemoteConfig(

    @SerializedName("layer")
    val layerRemoteConfig: LayerRemoteConfig?,

    @SerializedName("title")
    val title: TextRemoteConfig?,

    @SerializedName("submitButton")
    val submitButtonRemoteConfig: ButtonRemoteConfig?,

    @SerializedName("cancelButton")
    val cancelButtonRemoteConfig: ButtonRemoteConfig?,

    @SerializedName("booleanQuestion")
    val booleanQuestion: SurveyBooleanQuestionRemoteConfig?,

    @SerializedName("scaleQuestion")
    val scaleQuestion: SurveyScaleQuestionRemoteConfig?,

    @SerializedName("singleQuestion")
    val singleQuestion: SurveySingleQuestionRemoteConfig?,

    @SerializedName("inputQuestion")
    val inputQuestion: SurveyInputQuestionRemoteConfig?
) {
    fun toSurveyTheme(): SurveyTheme = SurveyTheme(
        layer = layerRemoteConfig?.toLayerTheme(),
        title = title?.toTextTheme(),
        submitButton = submitButtonRemoteConfig?.toButtonTheme(),
        cancelButton = cancelButtonRemoteConfig?.toButtonTheme(),
        booleanQuestion = booleanQuestion?.toSurveyBooleanQuestionTheme(),
        scaleQuestion = scaleQuestion?.toSurveyScaleQuestionTheme(),
        singleQuestion = singleQuestion?.toSurveySingleQuestionTheme(),
        inputQuestion = inputQuestion?.toSurveyInputQuestionTheme()
    )
}
