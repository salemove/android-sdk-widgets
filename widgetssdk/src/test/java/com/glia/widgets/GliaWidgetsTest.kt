package com.glia.widgets

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.glia.widgets.callbacks.OnComplete
import com.glia.widgets.callbacks.OnError
import com.glia.widgets.callvisualizer.controller.CallVisualizerController
import com.glia.widgets.di.ControllerFactory
import com.glia.widgets.di.Dependencies
import com.glia.widgets.di.GliaCore
import com.glia.widgets.di.GliaCoreImpl
import com.glia.widgets.di.RepositoryFactory
import com.glia.widgets.engagement.EngagementRepository
import com.glia.widgets.internal.queue.QueueRepository
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.ClassRule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

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
    fun `deprecated init initializes dependencies synchronously`() {
        val gliaWidgetsConfig = widgetsConfig()
        mockWidgetsInitialization()

        GliaWidgets.init(gliaWidgetsConfig)

        verify(gliaCore).init(eq(gliaWidgetsConfig))
        verify(controllerFactory).init()
        verify(repositoryFactory).initialize()
    }

    @Test
    fun `deprecated init throws when core initialization fails`() {
        val gliaWidgetsConfig = widgetsConfig()
        val initializationError = GliaWidgetsException("Invalid configuration", GliaWidgetsException.Cause.INVALID_INPUT)
        whenever(gliaCore.init(eq(gliaWidgetsConfig))) doThrow initializationError

        val exception = Assert.assertThrows(GliaWidgetsException::class.java) {
            GliaWidgets.init(gliaWidgetsConfig)
        }

        Assert.assertEquals(initializationError, exception)
        verify(controllerFactory, never()).init()
        verify(repositoryFactory, never()).initialize()
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
        .setSiteApiKey(SiteApiKey("SiteApiId", "SiteApiSecret"))
        .setSiteId("SiteId")
        .setRegion(GliaWidgetsConfig.Regions.EU)
        .setContext(RuntimeEnvironment.getApplication())
        .build()

    private fun mockWidgetsInitialization() {
        val callVisualizerController = mock<CallVisualizerController>()
        whenever(controllerFactory.callVisualizerController).thenReturn(callVisualizerController)
        val engagementRepository = mock<EngagementRepository>()
        whenever(repositoryFactory.engagementRepository) doReturn engagementRepository
        val queueRepository = mock<QueueRepository>()
        whenever(repositoryFactory.queueRepository) doReturn queueRepository
    }
}
