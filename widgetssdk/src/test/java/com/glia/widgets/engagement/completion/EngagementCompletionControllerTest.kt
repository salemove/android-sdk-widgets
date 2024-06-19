package com.glia.widgets.engagement.completion

import com.glia.androidsdk.Engagement
import com.glia.androidsdk.engagement.Survey
import com.glia.widgets.engagement.EngagementUpdateState
import com.glia.widgets.engagement.State
import com.glia.widgets.engagement.SurveyState
import com.glia.widgets.engagement.domain.EngagementStateUseCase
import com.glia.widgets.engagement.domain.ReleaseResourcesUseCase
import com.glia.widgets.engagement.domain.SurveyUseCase
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.rxjava3.android.plugins.RxAndroidPlugins
import io.reactivex.rxjava3.processors.PublishProcessor
import io.reactivex.rxjava3.schedulers.Schedulers
import junit.framework.TestCase.assertEquals
import org.junit.After
import org.junit.Before
import org.junit.Test

class EngagementCompletionControllerTest {
    private val surveyStateProcessor = PublishProcessor.create<SurveyState>()
    private val engagementStateProcessor = PublishProcessor.create<State>()

    private lateinit var releaseResourcesUseCase: ReleaseResourcesUseCase
    private lateinit var engagementStateUseCase: EngagementStateUseCase
    private lateinit var surveyUseCase: SurveyUseCase

    private lateinit var controller: EngagementCompletionContract.Controller

    @Before
    fun setUp() {
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }

        releaseResourcesUseCase = mockk(relaxUnitFun = true)
        engagementStateUseCase = mockk(relaxUnitFun = true)
        surveyUseCase = mockk(relaxUnitFun = true)

        every { engagementStateUseCase() } returns engagementStateProcessor
        every { surveyUseCase() } returns surveyStateProcessor

        controller = EngagementCompletionController(releaseResourcesUseCase, engagementStateUseCase, surveyUseCase)

        verify { surveyUseCase() }
        verify { engagementStateUseCase() }
    }

    @After
    fun tearDown() {
        confirmVerified(releaseResourcesUseCase, engagementStateUseCase, surveyUseCase)

        RxAndroidPlugins.reset()
    }

    @Test
    fun `State FinishedCallVisualizer should release resources and emit QueuingOrEngagementEnded`() {
        engagementStateProcessor.onNext(State.FinishedCallVisualizer)

        verify { releaseResourcesUseCase() }
        assertEquals(EngagementCompletionState.QueuingOrEngagementEnded, controller.state.blockingFirst().value)
    }

    @Test
    fun `State FinishedOmniCore should release resources and emit QueuingOrEngagementEnded`() {
        engagementStateProcessor.onNext(State.FinishedOmniCore)

        verify { releaseResourcesUseCase() }
        assertEquals(EngagementCompletionState.QueuingOrEngagementEnded, controller.state.blockingFirst().value)
    }

    @Test
    fun `State QueueUnstaffed should release resources and emit QueuingOrEngagementEnded and QueueUnstaffed`() {
        val testState = controller.state.map { it.value }.test()

        engagementStateProcessor.onNext(State.QueueUnstaffed)

        verify { releaseResourcesUseCase() }

        testState.assertValues(
            EngagementCompletionState.QueuingOrEngagementEnded,
            EngagementCompletionState.QueueUnstaffed
        )
    }

    @Test
    fun `State UnexpectedErrorHappened should release resources and emit QueuingOrEngagementEnded and UnexpectedErrorHappened`() {
        val testState = controller.state.map { it.value }.test()

        engagementStateProcessor.onNext(State.UnexpectedErrorHappened)

        verify { releaseResourcesUseCase() }

        testState.assertValues(
            EngagementCompletionState.QueuingOrEngagementEnded,
            EngagementCompletionState.UnexpectedErrorHappened
        )
    }

    @Test
    fun `State other than FinishedCallVisualizer, FinishedOmniCore, QueueUnstaffed, UnexpectedErrorHappened should not release resources`() {
        val testState = controller.state.test()

        engagementStateProcessor.onNext(State.NoEngagement)
        engagementStateProcessor.onNext(State.PreQueuing(listOf("queueId"), Engagement.MediaType.TEXT))
        engagementStateProcessor.onNext(State.Queuing(listOf("queueId"), "queueTicketId", Engagement.MediaType.TEXT))
        engagementStateProcessor.onNext(State.QueueingCanceled)
        engagementStateProcessor.onNext(State.StartedOmniCore)
        engagementStateProcessor.onNext(State.StartedCallVisualizer)
        engagementStateProcessor.onNext(State.Update(mockk(), EngagementUpdateState.Transferring))

        verify(exactly = 0) { releaseResourcesUseCase() }
        testState.assertNoValues()
    }

    @Test
    fun `SurveyState EmptyFromOperatorRequest should produce OperatorEndedEngagement`() {
        val testState = controller.state.map { it.value }.test()

        surveyStateProcessor.onNext(SurveyState.EmptyFromOperatorRequest)

        testState.assertValues(EngagementCompletionState.OperatorEndedEngagement)
    }

    @Test
    fun `SurveyState Value should produce SurveyLoaded`() {
        val survey = mockk<Survey>()
        val testState = controller.state.map { it.value }.test()

        surveyStateProcessor.onNext(SurveyState.Value(survey))

        testState.assertValues(EngagementCompletionState.SurveyLoaded(survey))
    }

    @Test
    fun `SurveyState Empty should not produce any state`() {
        val testState = controller.state.test()

        surveyStateProcessor.onNext(SurveyState.Empty)

        testState.assertNoValues()
    }
}
