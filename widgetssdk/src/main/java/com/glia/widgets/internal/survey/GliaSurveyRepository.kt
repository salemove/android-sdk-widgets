package com.glia.widgets.internal.survey

import com.glia.androidsdk.engagement.Survey
import com.glia.widgets.di.GliaCore
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.TAG

internal class GliaSurveyRepository(private val gliaCore: GliaCore) {
    // Core logs a failed submit, and the survey screen is closed by then, so the result is not used.
    fun submitSurveyAnswers(answers: List<Survey.Answer>, surveyId: String, engagementId: String) {
        Logger.i(TAG, "Submit survey answers")
        gliaCore.submitSurveyAnswers(answers, surveyId, engagementId) { }
    }
}
