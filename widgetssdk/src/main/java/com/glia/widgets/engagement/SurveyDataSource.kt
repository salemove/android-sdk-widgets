package com.glia.widgets.engagement

import com.glia.androidsdk.Engagement
import com.glia.androidsdk.engagement.Survey
import io.reactivex.Single

internal interface SurveyDataSource {
    fun fetchSurvey(engagement: Engagement): Single<Result<Survey>>
}

internal class SurveyDataSourceImpl : SurveyDataSource {
    override fun fetchSurvey(engagement: Engagement): Single<Result<Survey>> = Single.create {
        engagement.getSurvey { survey, gliaException ->
            it.onSuccess(survey?.let(Result.Companion::success) ?: Result.failure(gliaException ?: RuntimeException()))
        }
    }
}
