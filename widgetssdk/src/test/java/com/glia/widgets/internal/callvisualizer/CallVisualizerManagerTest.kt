package com.glia.widgets.internal.callvisualizer

import android.content.Context
import android.mockk
import android.unMockk
import com.glia.telemetry_lib.GliaLogger
import com.glia.widgets.callbacks.OnComplete
import com.glia.widgets.callvisualizer.controller.CallVisualizerContract
import com.glia.widgets.internal.callvisualizer.domain.VisitorCodeViewBuilderUseCase
import com.glia.widgets.view.VisitorCodeView
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.rxjava3.processors.PublishProcessor
import org.junit.After
import org.junit.Before
import org.junit.Test
import com.glia.widgets.engagement.State as EngagementState

internal class CallVisualizerManagerTest {

    private lateinit var buildVisitorCodeUseCase: VisitorCodeViewBuilderUseCase
    private lateinit var callVisualizerController: CallVisualizerContract.Controller
    private lateinit var engagementStartProcessor: PublishProcessor<EngagementState>
    private lateinit var engagementEndProcessor: PublishProcessor<EngagementState>

    private lateinit var callVisualizerManager: CallVisualizerManager

    @Before
    fun setUp() {
        GliaLogger.mockk()

        buildVisitorCodeUseCase = mockk()
        callVisualizerController = mockk(relaxUnitFun = true)
        engagementStartProcessor = PublishProcessor.create()
        engagementEndProcessor = PublishProcessor.create()

        every { callVisualizerController.engagementStartFlow } returns engagementStartProcessor
        every { callVisualizerController.engagementEndFlow } returns engagementEndProcessor

        callVisualizerManager = CallVisualizerManager(buildVisitorCodeUseCase, callVisualizerController)
    }

    @After
    fun tearDown() {
        GliaLogger.unMockk()
    }

    @Test
    fun `createVisitorCodeView delegates to VisitorCodeViewBuilderUseCase with closable false`() {
        val context = mockk<Context>()
        val view = mockk<VisitorCodeView>()
        every { buildVisitorCodeUseCase(context, false) } returns view

        val result = callVisualizerManager.createVisitorCodeView(context)

        assert(result === view)
    }

    @Test
    fun `showVisitorCodeDialog delegates to controller`() {
        callVisualizerManager.showVisitorCodeDialog()

        verify { callVisualizerController.showVisitorCodeDialog() }
    }

    @Test
    fun `hideVisitorCodeDialog delegates to controller dismissVisitorCodeDialog`() {
        callVisualizerManager.hideVisitorCodeDialog()

        verify { callVisualizerController.dismissVisitorCodeDialog() }
    }

    @Test
    fun `addVisitorContext delegates to controller with given assetId`() {
        callVisualizerManager.addVisitorContext("asset-id")

        verify { callVisualizerController.saveVisitorContextAssetId("asset-id") }
    }

    @Test
    fun `onEngagementStart callback is invoked when engagementStartFlow emits`() {
        var invoked = false

        callVisualizerManager.onEngagementStart(OnComplete { invoked = true })
        engagementStartProcessor.onNext(mockk())

        assert(invoked)
    }

    @Test
    fun `onEngagementEnd callback is invoked when engagementEndFlow emits`() {
        var invoked = false

        callVisualizerManager.onEngagementEnd(OnComplete { invoked = true })
        engagementEndProcessor.onNext(mockk())

        assert(invoked)
    }
}
