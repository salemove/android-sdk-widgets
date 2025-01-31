package com.glia.widgets.engagement.completion

import com.glia.androidsdk.Engagement
import com.glia.androidsdk.engagement.Survey
import com.glia.widgets.engagement.EndedBy
import com.glia.widgets.engagement.FetchSurveyCallback
import com.glia.widgets.engagement.State
import com.glia.widgets.engagement.domain.EngagementStateUseCase
import com.glia.widgets.engagement.domain.ReleaseResourcesUseCase
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.reactivex.rxjava3.android.plugins.RxAndroidPlugins
import io.reactivex.rxjava3.processors.PublishProcessor
import io.reactivex.rxjava3.schedulers.Schedulers
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class EngagementCompletionControllerTest {
    private val engagementStateProcessor = PublishProcessor.create<State>()

    private lateinit var releaseResourcesUseCase: ReleaseResourcesUseCase
    private lateinit var engagementStateUseCase: EngagementStateUseCase
    private lateinit var fetchSurveyCallback: FetchSurveyCallback

    private lateinit var controller: EngagementCompletionContract.Controller

    @Before
    fun setUp() {
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }

        releaseResourcesUseCase = mockk(relaxUnitFun = true)
        engagementStateUseCase = mockk(relaxUnitFun = true)
        fetchSurveyCallback = mockk<FetchSurveyCallback>(relaxed = true)

        every { engagementStateUseCase() } returns engagementStateProcessor

        controller = EngagementCompletionController(releaseResourcesUseCase, engagementStateUseCase)

        verify { engagementStateUseCase() }
    }

    @After
    fun tearDown() {
        confirmVerified(releaseResourcesUseCase, engagementStateUseCase)

        RxAndroidPlugins.reset()
    }

    @Test
    fun `State EngagementEnded should release resources and emit FinishActivities when engagement is CV`() {
        engagementStateProcessor.onNext(
            State.EngagementEnded(
                true,
                EndedBy.CLEAR_STATE,
                Engagement.ActionOnEnd.END_NOTIFICATION,
                fetchSurveyCallback
            )
        )

        verify { releaseResourcesUseCase() }
        verify(exactly = 0) { fetchSurveyCallback.invoke(any(), any()) }
        assertEquals(EngagementCompletionState.FinishActivities, controller.state.blockingFirst().value)
    }

    @Test
    fun `State EngagementEnded should release resources and emit FinishActivities when omnicore engagement is ended by integrator`() {
        engagementStateProcessor.onNext(
            State.EngagementEnded(
                false,
                EndedBy.CLEAR_STATE,
                Engagement.ActionOnEnd.END_NOTIFICATION,
                fetchSurveyCallback
            )
        )

        verify { releaseResourcesUseCase() }
        verify(exactly = 0) { fetchSurveyCallback.invoke(any(), any()) }
        assertEquals(EngagementCompletionState.FinishActivities, controller.state.blockingFirst().value)
    }

    @Test
    fun `State EngagementEnded should release resources and emit FinishActivities when omnicore engagement is ended by visitor and no survey`() {
        engagementStateProcessor.onNext(State.EngagementEnded(false, EndedBy.VISITOR, Engagement.ActionOnEnd.END_NOTIFICATION, fetchSurveyCallback))

        verify { releaseResourcesUseCase() }
        verify(exactly = 0) { fetchSurveyCallback.invoke(any(), any()) }
        assertEquals(EngagementCompletionState.FinishActivities, controller.state.blockingFirst().value)
    }

    @Test
    fun `State EngagementEnded should do nothing when omnicore enngagement end action is RETAIN`() {
        engagementStateProcessor.onNext(State.EngagementEnded(false, EndedBy.OPERATOR, Engagement.ActionOnEnd.RETAIN, fetchSurveyCallback))

        verify(exactly = 0) { releaseResourcesUseCase() }
        verify(exactly = 0) { fetchSurveyCallback.invoke(any(), any()) }
        controller.state.test().assertNotComplete().assertNoValues()
    }

    @Test
    fun `State EngagementEnded should fetchSurvey when omnicore enngagement end action is SURVEY`() {
        val state = controller.state.map { it.value }
        engagementStateProcessor.onNext(State.EngagementEnded(false, EndedBy.VISITOR, Engagement.ActionOnEnd.SHOW_SURVEY, fetchSurveyCallback))

        val onSuccessCallbackSlot = slot<(Survey) -> Unit>()

        verify { releaseResourcesUseCase() }
        verify { fetchSurveyCallback.invoke(capture(onSuccessCallbackSlot), any()) }

        state.test().assertNotComplete().assertValues(EngagementCompletionState.FinishActivities)

        val survey = mockk<Survey>()

        onSuccessCallbackSlot.captured.invoke(survey)
        state.test().assertNotComplete().assertValues(EngagementCompletionState.ShowSurvey(survey))
    }

    @Test
    fun `State EngagementEnded should emit EngagementEndedDialog when fetching survey is failed and engagement ended by operator`() {
        val state = controller.state.map { it.value }
        engagementStateProcessor.onNext(State.EngagementEnded(false, EndedBy.OPERATOR, Engagement.ActionOnEnd.SHOW_SURVEY, fetchSurveyCallback))

        val onFailureCallback = slot<() -> Unit>()

        verify { releaseResourcesUseCase() }
        verify { fetchSurveyCallback.invoke(any(), capture(onFailureCallback)) }

        state.test().assertNotComplete().assertValues(EngagementCompletionState.FinishActivities)

        onFailureCallback.captured.invoke()
        state.test().assertNotComplete().assertValues(EngagementCompletionState.ShowEngagementEndedDialog)
    }

    @Test
    fun `State EngagementEnded should not emit EngagementEndedDialog when fetching survey is failed and engagement ended by visitor`() {
        val state = controller.state.map { it.value }
        engagementStateProcessor.onNext(State.EngagementEnded(false, EndedBy.VISITOR, Engagement.ActionOnEnd.SHOW_SURVEY, fetchSurveyCallback))

        val onFailureCallback = slot<() -> Unit>()

        verify { releaseResourcesUseCase() }
        verify { fetchSurveyCallback.invoke(any(), capture(onFailureCallback)) }

        state.test().assertNotComplete().assertValues(EngagementCompletionState.FinishActivities)

        onFailureCallback.captured.invoke()
        state.test().assertNotComplete().assertValues(EngagementCompletionState.FinishActivities)
    }

    @Test
    fun `State EngagementEnded should emit EngagementEndedDialog when engagement ended by Operator and no action is END_NOTIFICATION`() {
        val state = controller.state.map { it.value }.test()
        engagementStateProcessor.onNext(State.EngagementEnded(false, EndedBy.OPERATOR, Engagement.ActionOnEnd.END_NOTIFICATION, fetchSurveyCallback))

        verify { releaseResourcesUseCase() }
        verify(exactly = 0) { fetchSurveyCallback.invoke(any(), any()) }

        state.assertNotComplete().assertValues(EngagementCompletionState.FinishActivities, EngagementCompletionState.ShowEngagementEndedDialog)
    }

    @Test
    fun `State EngagementEnded should emit EngagementEndedDialog when engagement ended by Operator and no action is UNKNOWN`() {
        val state = controller.state.map { it.value }.test()
        engagementStateProcessor.onNext(State.EngagementEnded(false, EndedBy.OPERATOR, Engagement.ActionOnEnd.UNKNOWN, fetchSurveyCallback))

        verify { releaseResourcesUseCase() }
        verify(exactly = 0) { fetchSurveyCallback.invoke(any(), any()) }

        state.assertNotComplete().assertValues(EngagementCompletionState.FinishActivities, EngagementCompletionState.ShowEngagementEndedDialog)
    }

    @Test
    fun `State QueueUnstaffed should release resources and emit ShowNoOperatorsAvailableDialog`() {
        val testState = controller.state.map { it.value }.test()

        engagementStateProcessor.onNext(State.QueueUnstaffed)

        verify { releaseResourcesUseCase() }

        testState.assertValues(EngagementCompletionState.FinishActivities, EngagementCompletionState.ShowNoOperatorsAvailableDialog)
    }

    @Test
    fun `State UnexpectedErrorHappened should release resources and emit ShowUnexpectedErrorDialog`() {
        val testState = controller.state.map { it.value }.test()

        engagementStateProcessor.onNext(State.UnexpectedErrorHappened)

        verify { releaseResourcesUseCase() }

        testState.assertValues(EngagementCompletionState.FinishActivities, EngagementCompletionState.ShowUnexpectedErrorDialog)
    }
}
