package com.glia.widgets.engagement.end

import android.annotation.SuppressLint
import com.glia.androidsdk.Engagement
import com.glia.widgets.di.Dependencies
import com.glia.widgets.engagement.EngagementDataSource
import com.glia.widgets.engagement.SurveyDataSource
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.processors.BehaviorProcessor

internal interface EngagementEndRepository {
    val engagementEndResult: Flowable<EndEngagement.Result>
    fun endEngagement()
    fun endEngagementSilently()
}

internal class EngagementEndRepositoryImpl(
    private val engagementDataSource: EngagementDataSource,
    private val surveyDataSource: SurveyDataSource
) : EngagementEndRepository {
    private val engagementEndReason: BehaviorProcessor<EndEngagement.Reason> = BehaviorProcessor.createDefault(EndEngagement.Reason.OPERATOR)

    private val preliminaryResult: Flowable<EndEngagement.Result> = engagementEndReason
        .filter(EndEngagement.Reason::isVisitor)
        .map { EndEngagement.Result.Visitor }

    private val currentEngagement = engagementDataSource.subscribeToEngagementStart()

    private val engagementEnd: Flowable<Engagement> = currentEngagement.flatMapSingle(engagementDataSource::subscribeToEngagementEnd)

    private val isOperatorReason = engagementEndReason.filter(EndEngagement.Reason::shouldRequestSurvey).map(EndEngagement.Reason::isOperator)

    override val engagementEndResult: Flowable<EndEngagement.Result> = engagementEnd.withLatestFrom(isOperatorReason, ::Pair)
        .subscribeOn(AndroidSchedulers.mainThread())
        .doOnNext { tryToDestroyControllers(it.second) }
        .switchMapMaybe { loadSurvey(it.first, it.second) }
        .mergeWith(preliminaryResult)
        .doOnNext { engagementEndReason.onNext(EndEngagement.Reason.OPERATOR) }

    private fun loadSurvey(engagement: Engagement, isOperator: Boolean): Maybe<EndEngagement.Result> =
        surveyDataSource.fetchSurvey(engagement).map { result ->
            when {
                result.isSuccess -> Result.success(EndEngagement.Result.Survey(result.getOrThrow()))
                isOperator -> Result.success(EndEngagement.Result.Operator)
                else -> Result.failure(RuntimeException())
            }
        }.filter { it.isSuccess }.map { it.getOrThrow() }

    private fun tryToDestroyControllers(isOperator: Boolean) {
        //For visitor, we already destroyed all controllers
        if (isOperator) {
            destroyControllers()
        }
    }

    @SuppressLint("CheckResult")
    private fun endEngagementLocally() {
        currentEngagement.singleElement().subscribe({
            engagementDataSource end it
        }, {}, {})
    }

    private fun destroyControllers() {
        Dependencies.getControllerFactory().destroyControllers()
    }

    override fun endEngagement() {
        engagementEndReason.onNext(EndEngagement.Reason.VISITOR)
        endEngagementLocally()
        destroyControllers()
    }

    override fun endEngagementSilently() {
        engagementEndReason.onNext(EndEngagement.Reason.VISITOR_SILENTLY)
        endEngagementLocally()
        destroyControllers()
    }

}
