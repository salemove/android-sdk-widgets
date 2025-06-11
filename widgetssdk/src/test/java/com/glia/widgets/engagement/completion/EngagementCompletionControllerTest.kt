package com.glia.widgets.engagement.completion

import com.glia.androidsdk.engagement.Survey
import com.glia.widgets.engagement.EndAction
import com.glia.widgets.engagement.State
import com.glia.widgets.engagement.domain.EngagementStateUseCase
import com.glia.widgets.engagement.domain.ReleaseResourcesUseCase
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
    private val engagementStateProcessor = PublishProcessor.create<State>()

    private lateinit var releaseResourcesUseCase: ReleaseResourcesUseCase
    private lateinit var engagementStateUseCase: EngagementStateUseCase

    private lateinit var controller: EngagementCompletionContract.Controller

    @Before
    fun setUp() {
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }

        releaseResourcesUseCase = mockk(relaxUnitFun = true)
        engagementStateUseCase = mockk(relaxUnitFun = true)

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
    fun `State EngagementEnded should release resources and emit FinishActivities when endAction is ClearStateCallVisualizer`() {
        engagementStateProcessor.onNext(State.EngagementEnded(EndAction.ClearStateCallVisualizer))
        verify { releaseResourcesUseCase() }
        assertEquals(EngagementCompletionState.FinishActivities, controller.state.blockingFirst().value)
    }

    @Test
    fun `State EngagementEnded should release resources and emit FinishActivities when endAction is ClearStateRegular`() {
        engagementStateProcessor.onNext(State.EngagementEnded(EndAction.ClearStateRegular))
        verify { releaseResourcesUseCase() }
        assertEquals(EngagementCompletionState.FinishActivities, controller.state.blockingFirst().value)
    }

    @Test
    fun `State EngagementEnded should release resources and emit ShowEngagementEndedDialog when endAction is ShowEndDialog`() {
        engagementStateProcessor.onNext(State.EngagementEnded(EndAction.ShowEndDialog))
        verify { releaseResourcesUseCase() }
        assertEquals(EngagementCompletionState.ShowEngagementEndedDialog, controller.state.blockingFirst().value)
    }

    @Test
    fun `State EngagementEnded should release resources and emit ShowSurvey when endAction is ShowSurvey`() {
        val survey = mockk<Survey>()
        engagementStateProcessor.onNext(State.EngagementEnded(EndAction.ShowSurvey(survey)))
        verify { releaseResourcesUseCase() }
        assertEquals(EngagementCompletionState.ShowSurvey(survey), controller.state.blockingFirst().value)
    }

    @Test
    fun `State EngagementEnded should not release resources when endAction is Retain`() {
        engagementStateProcessor.onNext(State.EngagementEnded(EndAction.Retain))
        verify(exactly = 0) { releaseResourcesUseCase() }
        controller.state.test().assertNoValues()
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
