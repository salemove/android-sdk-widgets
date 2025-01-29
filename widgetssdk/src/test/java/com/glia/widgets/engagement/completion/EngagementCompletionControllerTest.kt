package com.glia.widgets.engagement.completion

import com.glia.androidsdk.Engagement
import com.glia.androidsdk.engagement.Survey
import com.glia.widgets.engagement.EngagementType
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
    fun `State EngagementEnded(CallVisualizer) should release resources and emit completion event EngagementEnded`() {
        engagementStateProcessor.onNext(State.EngagementEnded(EngagementType.CallVisualizer, true, Engagement.ActionOnEnd.UNKNOWN))

        verify { releaseResourcesUseCase() }
        assertEquals(EngagementCompletionState.EngagementEnded(true, true, Engagement.ActionOnEnd.UNKNOWN), controller.state.blockingFirst().value)
    }

    @Test
    fun `State EngagementEnded(OmniCore) should release resources and emit completion event EngagementEnded`() {
        engagementStateProcessor.onNext(State.EngagementEnded(EngagementType.OmniCore, true, Engagement.ActionOnEnd.UNKNOWN))

        verify { releaseResourcesUseCase() }
        assertEquals(EngagementCompletionState.EngagementEnded(true, false, Engagement.ActionOnEnd.UNKNOWN), controller.state.blockingFirst().value)
    }

    @Test
    fun `State QueueUnstaffed should release resources and emit QueueUnstaffed`() {
        val testState = controller.state.map { it.value }.test()

        engagementStateProcessor.onNext(State.QueueUnstaffed)

        verify { releaseResourcesUseCase() }

        testState.assertValues(
            EngagementCompletionState.QueueUnstaffed
        )
    }

    @Test
    fun `State UnexpectedErrorHappened should release resources and emit UnexpectedErrorHappened`() {
        val testState = controller.state.map { it.value }.test()

        engagementStateProcessor.onNext(State.UnexpectedErrorHappened)

        verify { releaseResourcesUseCase() }

        testState.assertValues(
            EngagementCompletionState.UnexpectedErrorHappened
        )
    }

    @Test
    fun `State other than EngagementEnded, QueueUnstaffed, UnexpectedErrorHappened should not release resources`() {
        val testState = controller.state.test()

        engagementStateProcessor.onNext(State.NoEngagement)
        engagementStateProcessor.onNext(State.PreQueuing(Engagement.MediaType.TEXT))
        engagementStateProcessor.onNext(State.Queuing("queueTicketId", Engagement.MediaType.TEXT))
        engagementStateProcessor.onNext(State.QueueingCanceled)
        engagementStateProcessor.onNext(State.EngagementStarted(EngagementType.CallVisualizer))
        engagementStateProcessor.onNext(State.EngagementStarted(EngagementType.OmniCore))
        engagementStateProcessor.onNext(State.Update(mockk(), EngagementUpdateState.Transferring))

        verify(exactly = 0) { releaseResourcesUseCase() }
        testState.assertNoValues()
    }

    @Test
    fun `Engagement State EngagementEnded() should produce completion EngagementEnded event with same properties`() {
        val testState = controller.state.map { it.value }.test()

        engagementStateProcessor.onNext(State.EngagementEnded(EngagementType.OmniCore, false, Engagement.ActionOnEnd.UNKNOWN))

        verify { releaseResourcesUseCase() }

        testState.assertValues(EngagementCompletionState.EngagementEnded(false, false, Engagement.ActionOnEnd.UNKNOWN))
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
