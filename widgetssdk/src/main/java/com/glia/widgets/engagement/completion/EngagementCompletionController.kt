package com.glia.widgets.engagement.completion

import com.glia.widgets.engagement.EndAction
import com.glia.widgets.engagement.State
import com.glia.widgets.engagement.domain.EngagementStateUseCase
import com.glia.widgets.engagement.domain.ReleaseResourcesUseCase
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
        when (state.endAction) {
            EndAction.ClearStateCallVisualizer, EndAction.ClearStateRegular -> _state.onNext(EngagementCompletionState.FinishActivities)
            EndAction.ShowEndDialog -> _state.onNext(EngagementCompletionState.ShowEngagementEndedDialog)
            is EndAction.ShowSurvey -> _state.onNext(EngagementCompletionState.ShowSurvey(state.endAction.survey))
            // No need to handle this action here, return to not release resources
            EndAction.Retain -> return
        }

        releaseResourcesUseCase()
    }
}
