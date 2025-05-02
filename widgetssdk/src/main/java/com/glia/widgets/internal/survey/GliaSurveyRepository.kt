package com.glia.widgets.internal.survey

import com.glia.androidsdk.GliaException
import com.glia.androidsdk.engagement.Survey
import com.glia.widgets.di.GliaCore
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.TAG
import java.util.function.Consumer

internal class GliaSurveyRepository(private val gliaCore: GliaCore) {
    fun submitSurveyAnswers(
        answers: List<Survey.Answer>,
        surveyId: String,
        engagementId: String,
        callback: Consumer<GliaException?>
    ) {
        Logger.i(TAG, "Submit survey answers")
        gliaCore.submitSurveyAnswers(answers, surveyId, engagementId, callback)
    }
}
