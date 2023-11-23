package com.glia.widgets.engagement.end.domain

import com.glia.androidsdk.Engagement
import com.glia.androidsdk.engagement.Survey
import com.glia.widgets.engagement.SurveyDataSource
import com.glia.widgets.engagement.end.EndEngagement
import com.glia.widgets.helper.Result

internal class LoadSurveyUseCase(private val dataSource: SurveyDataSource) {
    operator fun invoke(engagement: Engagement, isOperator: Boolean) = dataSource.fetchSurvey(engagement)
        .map { mapSurveyResult(it, isOperator) }
        .filter { it !is EndEngagement.Result.Visitor }

    private fun mapSurveyResult(result: Result<Survey>, isOperator: Boolean) = when {
        result is Result.Success -> EndEngagement.Result.Survey(result.value)
        isOperator -> EndEngagement.Result.Operator
        else -> EndEngagement.Result.Visitor
    }
}
