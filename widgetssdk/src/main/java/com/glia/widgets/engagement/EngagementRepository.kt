package com.glia.widgets.engagement

import com.glia.androidsdk.Engagement
import com.glia.androidsdk.Glia
import com.glia.androidsdk.engagement.EngagementState
import com.glia.androidsdk.engagement.Survey
import com.glia.androidsdk.omnibrowse.OmnibrowseEngagement
import com.glia.androidsdk.omnicore.OmnicoreEngagement
import com.glia.widgets.di.GliaCore
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.TAG
import io.reactivex.Flowable
import io.reactivex.processors.PublishProcessor
import java.util.function.Consumer

internal interface EngagementRepository {
    val engagementState: Flowable<State>
    val survey: Flowable<SurveyState>
    val hasOngoingEngagement: Boolean
    val isCallVisualizerEngagement: Boolean
    fun initialize()
    fun reset()
    fun endEngagement(silently: Boolean)

    sealed interface State {
        object NoEngagement : State
        object Started : State
        object Finished : State
        data class Update(val value: Any) : State
    }

    sealed interface SurveyState {
        object Empty : SurveyState
        object EmptyFromOperatorRequest : SurveyState
        data class Value(val survey: Survey) : SurveyState
    }
}

internal class EngagementRepositoryImpl(
    private val core: GliaCore
) : EngagementRepository {
    private val omniCoreEngagementCallback = Consumer<OmnicoreEngagement> {
        handleOmniCoreEngagement(it)
    }

    private val callVisualizerEngagementCallback = Consumer<OmnibrowseEngagement> {
        handleCallVisualizerEngagement(it)
    }

    private val engagementStateCallback: Consumer<EngagementState> = Consumer {
        handleEngagementState(it)
    }

    private val engagementEndCallback: Runnable = Runnable {
        handleEngagementEnd()
    }

    private var currentEngagement: Engagement? = null

    private val _engagementState: PublishProcessor<EngagementRepository.State> = PublishProcessor.create()
    override val engagementState: Flowable<EngagementRepository.State> = _engagementState

    private val _survey: PublishProcessor<EngagementRepository.SurveyState> = PublishProcessor.create()
    override val survey: Flowable<EngagementRepository.SurveyState> = _survey

    override val hasOngoingEngagement: Boolean
        get() = currentEngagement != null

    override val isCallVisualizerEngagement: Boolean
        get() = currentEngagement is OmnibrowseEngagement

    override fun initialize() {
        core.on(Glia.Events.ENGAGEMENT, omniCoreEngagementCallback)
//        core.callVisualizer.on(Omnibrowse.Events.ENGAGEMENT, callVisualizerEngagementCallback) TODO implement on next step, I'm not sure this needs to be unified with omnicore engagement
    }

    override fun reset() {
        _survey.onNext(EngagementRepository.SurveyState.Empty)
        _engagementState.onNext(EngagementRepository.State.NoEngagement)
        currentEngagement?.also(::unsubscribeFromEngagementEvents)
    }

    override fun endEngagement(silently: Boolean) {
        currentEngagement?.also {
            unsubscribeFromEngagementEvents(it)
            it.end { Logger.d(TAG, "Ending engagement failed") }
            _engagementState.onNext(EngagementRepository.State.Finished)
            if (silently) {
                _survey.onNext(EngagementRepository.SurveyState.Empty)
            } else {
                fetchSurvey(it, false)
            }
            currentEngagement = null
        }
    }

    private fun fetchSurvey(engagement: Engagement, isOperator: Boolean) {
        engagement.getSurvey { survey, _ ->
            when {
                survey != null -> _survey.onNext(EngagementRepository.SurveyState.Value(survey))
                isOperator -> _survey.onNext(EngagementRepository.SurveyState.EmptyFromOperatorRequest)
                else -> _survey.onNext(EngagementRepository.SurveyState.Empty)
            }
        }
    }

    private fun handleOmniCoreEngagement(engagement: OmnicoreEngagement) {
        currentEngagement = engagement
        subscribeToEngagementEvents(engagement)
        _engagementState.onNext(EngagementRepository.State.Started)
    }

    private fun handleCallVisualizerEngagement(engagement: OmnibrowseEngagement) {
        currentEngagement = engagement
        subscribeToEngagementEvents(engagement)
        _engagementState.onNext(EngagementRepository.State.Started)
    }

    private fun subscribeToEngagementEvents(engagement: Engagement) {
        engagement.on(Engagement.Events.END, engagementEndCallback)
        engagement.on(Engagement.Events.STATE_UPDATE, engagementStateCallback)
    }

    private fun unsubscribeFromEngagementEvents(engagement: Engagement) {
        engagement.off(Engagement.Events.END, engagementEndCallback)
        engagement.off(Engagement.Events.STATE_UPDATE, engagementStateCallback)
    }

    private fun handleEngagementState(state: EngagementState) {
        _engagementState.onNext(EngagementRepository.State.Update(state))
    }

    private fun handleEngagementEnd() {
        _engagementState.onNext(EngagementRepository.State.Finished)
        fetchSurvey(currentEngagement ?: return, true)
        currentEngagement = null
    }

}
