package com.glia.widgets.engagement.completion

import com.glia.androidsdk.engagement.Survey
import com.glia.widgets.engagement.EngagementStateUseCase
import com.glia.widgets.engagement.SurveyState
import com.glia.widgets.engagement.SurveyUseCase
import com.glia.widgets.helper.OneTimeEvent
import com.glia.widgets.helper.unSafeSubscribe
import io.reactivex.Flowable
import io.reactivex.processors.BehaviorProcessor
import com.glia.widgets.engagement.State as EngagementState

internal class EngagementCompletionUseCase(
    private val engagementStateUseCase: EngagementStateUseCase,
    private val surveyUseCase: SurveyUseCase
) {
    private val _state: BehaviorProcessor<State> = BehaviorProcessor.create()

    init {
        subscribeToEvents()
    }

    operator fun invoke(): Flowable<OneTimeEvent<State>> = _state.map(::OneTimeEvent)

    private fun subscribeToEvents() {
        surveyUseCase().unSafeSubscribe(::handleSurveyState)
        engagementStateUseCase().unSafeSubscribe(::handleEngagementState)
    }

    private fun handleEngagementState(state: EngagementState) {
        when (state) {
            EngagementState.FinishedCallVisualizer,
            EngagementState.FinishedOmniCore -> _state.onNext(State.QueuingOrEngagementEnded)

            EngagementState.QueueUnstaffed -> {
                _state.onNext(State.QueuingOrEngagementEnded)
                _state.onNext(State.QueueUnstaffed)
            }

            EngagementState.UnexpectedErrorHappened -> {
                _state.onNext(State.QueuingOrEngagementEnded)
                _state.onNext(State.UnexpectedErrorHappened)
            }

            else -> {
                //no op
            }
        }
    }

    private fun handleSurveyState(surveyState: SurveyState) {
        when (surveyState) {
            SurveyState.EmptyFromOperatorRequest -> _state.onNext(State.OperatorEndedEngagement)
            is SurveyState.Value -> _state.onNext(State.SurveyLoaded(surveyState.survey))
            SurveyState.Empty -> {
                // no op
            }
        }
    }

    sealed interface State {
        object QueueUnstaffed : State
        object UnexpectedErrorHappened : State
        object OperatorEndedEngagement : State
        data class SurveyLoaded(val survey: Survey) : State
        object QueuingOrEngagementEnded : State
    }

}
