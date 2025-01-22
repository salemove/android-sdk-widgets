package com.glia.widgets.engagement.completion

import com.glia.androidsdk.Engagement
import com.glia.widgets.engagement.State
import com.glia.widgets.engagement.SurveyState
import com.glia.widgets.engagement.domain.EngagementStateUseCase
import com.glia.widgets.engagement.domain.ReleaseResourcesUseCase
import com.glia.widgets.engagement.domain.SurveyUseCase
import com.glia.widgets.helper.OneTimeEvent
import com.glia.widgets.helper.asOneTimeStateFlowable
import com.glia.widgets.helper.unSafeSubscribe
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.processors.BehaviorProcessor

internal interface EngagementCompletionContract {

    interface Controller {
        val state: Flowable<OneTimeEvent<EngagementCompletionState>>
    }
}

internal class EngagementCompletionController(
    private val releaseResourcesUseCase: ReleaseResourcesUseCase,
    engagementStateUseCase: EngagementStateUseCase,
    surveyUseCase: SurveyUseCase
) : EngagementCompletionContract.Controller {

    private val _state: BehaviorProcessor<EngagementCompletionState> = BehaviorProcessor.create()
    override val state: Flowable<OneTimeEvent<EngagementCompletionState>> = _state.asOneTimeStateFlowable()

    init {
        surveyUseCase().unSafeSubscribe(::handleSurveyState)
        engagementStateUseCase().unSafeSubscribe(::handleEngagementState)
    }

    private fun handleEngagementState(state: State) {
        when (state) {
            is State.EngagementEnded -> {
                if (state.onEnd != Engagement.ActionOnEnd.RETAIN) {
                    // Even though we need to retain the chat screen we still need to reset view states and controllers and later setup them again
                    releaseResourcesUseCase()
                }
                _state.onNext(EngagementCompletionState.EngagementEnded(state.isEndedByVisitor, state.onEnd))
            }

            State.QueueUnstaffed -> {
                releaseResourcesUseCase()
                _state.onNext(EngagementCompletionState.QueueUnstaffed)
            }

            State.UnexpectedErrorHappened -> {
                releaseResourcesUseCase()
                _state.onNext(EngagementCompletionState.UnexpectedErrorHappened)
            }

            else -> {
                //no op
            }
        }
    }

    private fun handleSurveyState(surveyState: SurveyState) {
        when (surveyState) {
            is SurveyState.Value -> _state.onNext(EngagementCompletionState.SurveyLoaded(surveyState.survey))
            SurveyState.Empty -> {
                // no op
            }
        }
    }
}
