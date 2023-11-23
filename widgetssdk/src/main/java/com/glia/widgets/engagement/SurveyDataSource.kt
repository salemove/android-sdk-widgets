package com.glia.widgets.engagement

import com.glia.androidsdk.Engagement
import com.glia.androidsdk.engagement.Survey
import com.glia.widgets.helper.Result
import io.reactivex.Single

internal interface SurveyDataSource {
    fun fetchSurvey(engagement: Engagement): Single<Result<Survey>>
}

internal class SurveyDataSourceImpl : SurveyDataSource {
    override fun fetchSurvey(engagement: Engagement): Single<Result<Survey>> = Single.create {
        engagement.getSurvey { survey, ex ->
            it.onSuccess(survey?.let { Result.Success(it) } ?: Result.Failure(ex))
        }
    }
}
