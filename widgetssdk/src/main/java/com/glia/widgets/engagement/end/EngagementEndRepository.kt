package com.glia.widgets.engagement.end

import com.glia.widgets.engagement.end.domain.DestroyControllersUseCase
import com.glia.widgets.engagement.end.domain.EngagementEndEventUseCase
import com.glia.widgets.engagement.end.domain.EngagementEndReasonUseCase
import com.glia.widgets.engagement.end.domain.LoadSurveyUseCase
import com.glia.widgets.engagement.end.domain.ResetEndEngagementReasonUseCase
import io.reactivex.Flowable

internal interface EngagementEndRepository {
    val engagementEndResult: Flowable<EndEngagement.Result>
}

internal class EngagementEndRepositoryImpl(
    private val loadSurveyUseCase: LoadSurveyUseCase,
    private val destroyControllersUseCase: DestroyControllersUseCase,
    private val resetEndEngagementReasonUseCase: ResetEndEngagementReasonUseCase,
    engagementEndEventUseCase: EngagementEndEventUseCase,
    engagementEndReasonUseCase: EngagementEndReasonUseCase
) : EngagementEndRepository {

    private val preliminaryResult: Flowable<EndEngagement.Result> = engagementEndReasonUseCase()
        .filter(EndEngagement.Reason::isVisitor)
        .map { EndEngagement.Result.Visitor }


    private val isOperatorReason = engagementEndReasonUseCase()
        .filter(EndEngagement.Reason::shouldRequestSurvey)
        .map(EndEngagement.Reason::isOperator)

    override val engagementEndResult: Flowable<EndEngagement.Result> = engagementEndEventUseCase().withLatestFrom(isOperatorReason, ::Pair)
        .doOnNext { tryToDestroyControllers(it.second) }
        .switchMapMaybe { loadSurveyUseCase(it.first, it.second) }
        .mergeWith(preliminaryResult)
        .doOnNext { resetEndEngagementReasonUseCase() }

    private fun tryToDestroyControllers(isOperator: Boolean) {
        //For visitor, we already destroyed all controllers
        if (isOperator) {
            destroyControllersUseCase()
        }
    }

}
