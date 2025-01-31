package com.glia.widgets.engagement.completion

import com.glia.widgets.engagement.EndedBy
import com.glia.widgets.engagement.State
import com.glia.widgets.engagement.domain.EngagementStateUseCase
import com.glia.widgets.engagement.domain.ReleaseResourcesUseCase
import com.glia.widgets.helper.OneTimeEvent
import com.glia.widgets.helper.asOneTimeStateFlowable
import com.glia.widgets.helper.isRetain
import com.glia.widgets.helper.isSurvey
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
    engagementStateUseCase: EngagementStateUseCase
) : EngagementCompletionContract.Controller {

    private val _state: BehaviorProcessor<EngagementCompletionState> = BehaviorProcessor.create()
    override val state: Flowable<OneTimeEvent<EngagementCompletionState>> = _state.asOneTimeStateFlowable()

    init {
        engagementStateUseCase().unSafeSubscribe(::handleEngagementState)
    }

    private fun handleEngagementState(state: State) {
        when (state) {
            is State.EngagementEnded -> handleEngagementEnded(state)

            State.QueueUnstaffed -> {
                releaseResourcesUseCase()
                _state.onNext(EngagementCompletionState.FinishActivities)
                _state.onNext(EngagementCompletionState.ShowNoOperatorsAvailableDialog)
            }

            State.UnexpectedErrorHappened -> {
                releaseResourcesUseCase()
                _state.onNext(EngagementCompletionState.FinishActivities)
                _state.onNext(EngagementCompletionState.ShowUnexpectedErrorDialog)
            }

            else -> {
                //no op
            }
        }
    }

    private fun handleEngagementEnded(state: State.EngagementEnded) {
        when {
            state.isCallVisualizer -> _state.onNext(EngagementCompletionState.FinishActivities)
            state.endedBy == EndedBy.CLEAR_STATE -> _state.onNext(EngagementCompletionState.FinishActivities)
            state.endedBy == EndedBy.VISITOR && !state.action.isSurvey -> _state.onNext(EngagementCompletionState.FinishActivities)
            // This check should be after the cases above, as it could potentially be a retain action in all of these cases
            state.action.isRetain -> return
            state.action.isSurvey -> {
                _state.onNext(EngagementCompletionState.FinishActivities)
                state.fetchSurveyCallback(
                    { survey -> _state.onNext(EngagementCompletionState.ShowSurvey(survey)) },
                    { if (state.endedBy == EndedBy.OPERATOR) _state.onNext(EngagementCompletionState.ShowEngagementEndedDialog) }
                )
            }

            else -> {
                _state.onNext(EngagementCompletionState.FinishActivities)
                _state.onNext(EngagementCompletionState.ShowEngagementEndedDialog)
            }
        }

        releaseResourcesUseCase()
    }
}
