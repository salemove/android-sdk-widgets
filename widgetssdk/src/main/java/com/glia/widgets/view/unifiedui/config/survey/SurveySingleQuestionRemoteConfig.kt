package com.glia.widgets.view.unifiedui.config.survey

import com.glia.widgets.view.unifiedui.config.base.ColorLayerRemoteConfig
import com.glia.widgets.view.unifiedui.config.base.TextRemoteConfig
import com.glia.widgets.view.unifiedui.theme.survey.SurveySingleQuestionTheme
import com.google.gson.annotations.SerializedName

internal data class SurveySingleQuestionRemoteConfig(

    @SerializedName("title")
    val title: TextRemoteConfig?,

    @SerializedName("tintColor")
    val tintColor: ColorLayerRemoteConfig?,

    @SerializedName("option")
    val option: TextRemoteConfig?,

    @SerializedName("error")
    val errorRemoteConfig: TextRemoteConfig?
) {
    fun toSurveySingleQuestionTheme(): SurveySingleQuestionTheme = SurveySingleQuestionTheme(
        title = title?.toTextTheme(),
        tintColor = tintColor?.toColorTheme(),
        option = option?.toTextTheme(),
        error = errorRemoteConfig?.toTextTheme()
    )
}
