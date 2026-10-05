package com.glia.widgets

import android.assertRethrownOnMainThread
import android.runOnCoreThread
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.glia.androidsdk.GliaException
import com.glia.androidsdk.RequestCallback
import com.glia.widgets.callbacks.OnComplete
import com.glia.widgets.callbacks.OnError
import com.glia.widgets.callbacks.OnResult
import com.glia.widgets.callvisualizer.controller.CallVisualizerController
import com.glia.widgets.di.ControllerFactory
import com.glia.widgets.di.Dependencies
import com.glia.widgets.di.GliaCore
import com.glia.widgets.push.notifications.PushClickHandlerController
import com.glia.widgets.di.GliaCoreImpl
import com.glia.widgets.di.RepositoryFactory
import com.glia.widgets.engagement.EngagementRepository
import com.glia.widgets.internal.queue.QueueRepository
import com.glia.widgets.queue.Queue
import com.glia.widgets.visitor.VisitorInfo
import com.glia.widgets.visitor.VisitorInfoUpdateRequest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.ClassRule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import java.util.function.Consumer
import com.glia.androidsdk.queuing.Queue as CoreQueue
import com.glia.androidsdk.visitor.VisitorInfo as CoreVisitorInfo

@get:ClassRule
val rule: TestRule = InstantTaskExecutorRule()

@RunWith(RobolectricTestRunner::class)
class GliaWidgetsTest {

    private lateinit var gliaCore: GliaCore
    private lateinit var controllerFactory: ControllerFactory
    private lateinit var repositoryFactory: RepositoryFactory

    @Before
    fun setUp() {
        gliaCore = mock()
        controllerFactory = mock()
        repositoryFactory = mock()
        Dependencies.gliaCore = gliaCore
        Dependencies.controllerFactory = controllerFactory
        Dependencies.repositoryFactory = repositoryFactory
        Dependencies.localeProvider = mock()
    }

    @After
    fun tearDown() {
        // Dependencies is a process-wide singleton - restore the real GliaCore so the mock does not leak into other test classes
        Dependencies.gliaCore = GliaCoreImpl()
    }

    @Test
    fun `init passes config to glia core`() {
        val gliaWidgetsConfig = widgetsConfig()

        GliaWidgets.init(gliaWidgetsConfig, {}, {})

        verify(gliaCore).init(eq(gliaWidgetsConfig), any(), any())
    }

    @Test
    fun `init should invoke onComplete when initialization succeeds`() {
        val gliaWidgetsConfig = widgetsConfig()
        val onComplete = mock<OnComplete>()
        val onError = mock<OnError>()
        mockWidgetsInitialization()
        whenever(gliaCore.init(any(), any(), any())).thenAnswer {
            // Simulate successful Core SDK initialization by invoking the onComplete callback
            (it.arguments[1] as OnComplete).onComplete()
        }

        GliaWidgets.init(gliaWidgetsConfig, onComplete, onError)

        verify(onComplete).onComplete()
        verify(onError, never()).onError(any())
        verify(controllerFactory).init()
        verify(repositoryFactory).initialize()
    }

    @Test
    fun `init should invoke onError when core initialization fails`() {
        val gliaWidgetsConfig = widgetsConfig()
        val onComplete = mock<OnComplete>()
        val onError = mock<OnError>()
        val initializationError = GliaWidgetsException(
            "Network timeout. Please check the Internet connection.",
            GliaWidgetsException.Cause.NETWORK_TIMEOUT
        )
        whenever(gliaCore.init(any(), any(), any())).thenAnswer {
            (it.arguments[2] as OnError).onError(initializationError)
        }

        GliaWidgets.init(gliaWidgetsConfig, onComplete, onError)

        verify(onComplete, never()).onComplete()
        verify(onError).onError(initializationError)
        verify(controllerFactory, never()).init()
    }

    @Test
    fun `init initializes widgets and re-throws on the main thread when the integrator onComplete throws`() {
        val integratorBug = IllegalStateException("integrator bug")
        val onError = mock<OnError>()
        val coreOnComplete = argumentCaptor<OnComplete>()
        mockWidgetsInitialization()
        GliaWidgets.init(widgetsConfig(), { throw integratorBug }, onError)
        verify(gliaCore).init(any(), coreOnComplete.capture(), any())

        runOnCoreThread { coreOnComplete.firstValue.onComplete() }

        verify(controllerFactory).init()
        verify(repositoryFactory).initialize()
        verify(onError, never()).onError(any())
        assertRethrownOnMainThread(integratorBug)
    }

    @Test
    fun `getQueues re-throws on the main thread when the integrator onResult throws`() {
        val integratorBug = IllegalStateException("integrator bug")
        val onError = mock<OnError>()
        val coreOnResult = argumentCaptor<(Array<CoreQueue>) -> Unit>()
        GliaWidgets.getQueues({ throw integratorBug }, onError)
        verify(gliaCore).getQueues(coreOnResult.capture(), any())

        runOnCoreThread { coreOnResult.firstValue(emptyArray()) }

        verify(onError, never()).onError(any())
        assertRethrownOnMainThread(integratorBug)
    }

    @Test
    fun `getQueues re-throws on the main thread when the integrator onError throws`() {
        val integratorBug = IllegalStateException("integrator bug")
        val onResult = mock<OnResult<Collection<Queue>>>()
        val coreOnError = argumentCaptor<(GliaException?) -> Unit>()
        GliaWidgets.getQueues(onResult) { throw integratorBug }
        verify(gliaCore).getQueues(any(), coreOnError.capture())

        runOnCoreThread { coreOnError.firstValue(GliaException("error", GliaException.Cause.NETWORK_TIMEOUT)) }

        verify(onResult, never()).onResult(any())
        assertRethrownOnMainThread(integratorBug)
    }

    @Test
    fun `getVisitorInfo re-throws on the main thread when the integrator onError throws`() {
        val integratorBug = IllegalStateException("integrator bug")
        val onResult = mock<OnResult<VisitorInfo>>()
        val coreCallback = argumentCaptor<RequestCallback<CoreVisitorInfo?>>()
        GliaWidgets.getVisitorInfo(onResult) { throw integratorBug }
        verify(gliaCore).getVisitorInfo(coreCallback.capture())

        runOnCoreThread { coreCallback.firstValue.onResult(null, GliaException("error", GliaException.Cause.NETWORK_TIMEOUT)) }

        verify(onResult, never()).onResult(any())
        assertRethrownOnMainThread(integratorBug)
    }

    @Test
    fun `updateVisitorInfo re-throws on the main thread when the integrator onComplete throws`() {
        val integratorBug = IllegalStateException("integrator bug")
        val onError = mock<OnError>()
        val coreCallback = argumentCaptor<Consumer<GliaException?>>()
        GliaWidgets.updateVisitorInfo(VisitorInfoUpdateRequest(), { throw integratorBug }, onError)
        verify(gliaCore).updateVisitorInfo(any(), coreCallback.capture())

        runOnCoreThread { coreCallback.firstValue.accept(null) }

        verify(onError, never()).onError(any())
        assertRethrownOnMainThread(integratorBug)
    }

    @Test
    fun `isInitialized delegates to glia core`() {
        whenever(gliaCore.isInitialized) doReturn false
        Assert.assertFalse(GliaWidgets.isInitialized())

        whenever(gliaCore.isInitialized) doReturn true
        Assert.assertTrue(GliaWidgets.isInitialized())
    }

    @Test
    fun `isInitializationInProgress delegates to glia core`() {
        whenever(gliaCore.isInitializationInProgress) doReturn false
        Assert.assertFalse(GliaWidgets.isInitializationInProgress())

        whenever(gliaCore.isInitializationInProgress) doReturn true
        Assert.assertTrue(GliaWidgets.isInitializationInProgress())
    }

    private fun widgetsConfig(): GliaWidgetsConfig = GliaWidgetsConfig.Builder()
        .setAuthorizationMethod(AuthorizationMethod.UserApiKey("SiteApiId", "SiteApiSecret"))
        .setSiteId("SiteId")
        .setRegion(Region.EU)
        .setContext(RuntimeEnvironment.getApplication())
        .build()

    private fun mockWidgetsInitialization() {
        val callVisualizerController = mock<CallVisualizerController>()
        whenever(controllerFactory.callVisualizerController).thenReturn(callVisualizerController)
        whenever(controllerFactory.pushClickHandlerController) doReturn mock<PushClickHandlerController>()
        val engagementRepository = mock<EngagementRepository>()
        whenever(repositoryFactory.engagementRepository) doReturn engagementRepository
        val queueRepository = mock<QueueRepository>()
        whenever(repositoryFactory.queueRepository) doReturn queueRepository
    }
}
