package com.glia.widgets.view.unifiedui.config.survey

import android.os.Parcelable
import com.glia.widgets.view.unifiedui.config.base.ButtonRemoteConfig
import com.glia.widgets.view.unifiedui.config.base.LayerRemoteConfig
import com.glia.widgets.view.unifiedui.config.base.TextRemoteConfig
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
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
): Parcelable
