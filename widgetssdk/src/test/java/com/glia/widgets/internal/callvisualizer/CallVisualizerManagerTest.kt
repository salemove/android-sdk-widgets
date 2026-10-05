package com.glia.widgets.internal.callvisualizer

import android.assertRethrownOnMainThread
import android.content.Context
import android.mockk
import android.runOnCoreThread
import android.unMockk
import com.glia.telemetry_lib.GliaLogger
import com.glia.widgets.callbacks.OnComplete
import com.glia.widgets.callvisualizer.controller.CallVisualizerContract
import com.glia.widgets.internal.callvisualizer.domain.VisitorCodeViewBuilderUseCase
import com.glia.widgets.view.VisitorCodeView
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.rxjava3.functions.Consumer
import io.reactivex.rxjava3.plugins.RxJavaPlugins
import io.reactivex.rxjava3.processors.PublishProcessor
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import com.glia.widgets.engagement.State as EngagementState

@RunWith(RobolectricTestRunner::class)
internal class CallVisualizerManagerTest {

    private lateinit var buildVisitorCodeUseCase: VisitorCodeViewBuilderUseCase
    private lateinit var callVisualizerController: CallVisualizerContract.Controller
    private lateinit var engagementStartProcessor: PublishProcessor<EngagementState>
    private lateinit var engagementEndProcessor: PublishProcessor<EngagementState>

    private lateinit var callVisualizerManager: CallVisualizerManager

    private var previousRxErrorHandler: Consumer<in Throwable>? = null
    private val rxErrors: MutableList<Throwable> = mutableListOf()

    @Before
    fun setUp() {
        GliaLogger.mockk()
        previousRxErrorHandler = RxJavaPlugins.getErrorHandler()
        RxJavaPlugins.setErrorHandler { rxErrors.add(it) }

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
        RxJavaPlugins.setErrorHandler(previousRxErrorHandler)
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

    @Test
    fun `onEngagementStart keeps listening and re-throws on the main thread when the integrator callback throws`() {
        val integratorBug = IllegalStateException("integrator bug")
        var calls = 0
        callVisualizerManager.onEngagementStart { calls++; throw integratorBug }

        runOnCoreThread {
            engagementStartProcessor.onNext(mockk())
            engagementStartProcessor.onNext(mockk())
        }

        assertEquals(2, calls)
        assertTrue(engagementStartProcessor.hasSubscribers())
        assertEquals(emptyList<Throwable>(), rxErrors)
        assertRethrownOnMainThread(integratorBug)
    }

    @Test
    fun `onEngagementEnd keeps listening and re-throws on the main thread when the integrator callback throws`() {
        val integratorBug = IllegalStateException("integrator bug")
        var calls = 0
        callVisualizerManager.onEngagementEnd { calls++; throw integratorBug }

        runOnCoreThread {
            engagementEndProcessor.onNext(mockk())
            engagementEndProcessor.onNext(mockk())
        }

        assertEquals(2, calls)
        assertTrue(engagementEndProcessor.hasSubscribers())
        assertEquals(emptyList<Throwable>(), rxErrors)
        assertRethrownOnMainThread(integratorBug)
    }
}
