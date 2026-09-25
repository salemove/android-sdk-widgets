package com.glia.widgets

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.glia.androidsdk.GliaException
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
import com.glia.widgets.internal.secureconversations.SecureConversationsRepository
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
import org.mockito.kotlin.inOrder
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

    @Test
    fun `clearVisitorSession defaults to ending the engagement and keeping pushes`() {
        val (engagementRepository, secureConversationsRepository) = mockClearVisitorSessionDependencies(isQueueingOrLiveEngagement = true)

        GliaWidgets.clearVisitorSession()

        verify(engagementRepository).reset()
        verify(secureConversationsRepository).unsubscribeAndResetData()
        verify(gliaCore).clearVisitorSession(false)
    }

    @Test
    fun `clearVisitorSession passes stopPushNotifications to core`() {
        mockClearVisitorSessionDependencies()

        GliaWidgets.clearVisitorSession(stopPushNotifications = true)

        verify(gliaCore).clearVisitorSession(true)
    }

    @Test
    fun `clearVisitorSession throws FORBIDDEN and touches nothing when engagement is ongoing and endEngagementIfPresent is false`() {
        val (engagementRepository, secureConversationsRepository) = mockClearVisitorSessionDependencies(isQueueingOrLiveEngagement = true)

        val exception = Assert.assertThrows(GliaWidgetsException::class.java) {
            GliaWidgets.clearVisitorSession(endEngagementIfPresent = false)
        }

        Assert.assertEquals(GliaWidgetsException.Cause.FORBIDDEN, exception.gliaCause)
        verify(engagementRepository, never()).reset()
        verify(secureConversationsRepository, never()).unsubscribeAndResetData()
        verify(gliaCore, never()).clearVisitorSession(any())
    }

    @Test
    fun `clearVisitorSession with endEngagementIfPresent false clears when nothing is ongoing`() {
        val (engagementRepository, secureConversationsRepository) = mockClearVisitorSessionDependencies(isQueueingOrLiveEngagement = false)

        GliaWidgets.clearVisitorSession(endEngagementIfPresent = false, stopPushNotifications = true)

        verify(engagementRepository).reset()
        verify(secureConversationsRepository).unsubscribeAndResetData()
        verify(gliaCore).clearVisitorSession(true)
    }

    @Test
    fun `clearVisitorSession resets local state before Core, in order`() {
        val (engagementRepository, secureConversationsRepository) = mockClearVisitorSessionDependencies()

        GliaWidgets.clearVisitorSession()

        inOrder(engagementRepository, secureConversationsRepository, gliaCore) {
            verify(engagementRepository).reset()
            verify(secureConversationsRepository).unsubscribeAndResetData()
            verify(gliaCore).clearVisitorSession(false)
        }
    }

    @Test
    fun `clearVisitorSession maps a Core exception and still resets local state`() {
        val (engagementRepository, secureConversationsRepository) = mockClearVisitorSessionDependencies()
        whenever(gliaCore.clearVisitorSession(any())) doThrow GliaException("Not set up", GliaException.Cause.INVALID_INPUT)

        val exception = Assert.assertThrows(GliaWidgetsException::class.java) {
            GliaWidgets.clearVisitorSession()
        }

        Assert.assertEquals(GliaWidgetsException.Cause.INVALID_INPUT, exception.gliaCause)
        verify(engagementRepository).reset()
        verify(secureConversationsRepository).unsubscribeAndResetData()
    }

    @Test
    fun `clearVisitorSession never ends the engagement itself`() {
        val (engagementRepository, _) = mockClearVisitorSessionDependencies(isQueueingOrLiveEngagement = true)

        GliaWidgets.clearVisitorSession()

        // Core ends it as the outgoing visitor; ending it here too would fire an integrator-style end.
        verify(engagementRepository, never()).endEngagement()
        verify(engagementRepository, never()).terminateEngagement()
    }

    private fun mockClearVisitorSessionDependencies(
        isQueueingOrLiveEngagement: Boolean = false
    ): Pair<EngagementRepository, SecureConversationsRepository> {
        val engagementRepository = mock<EngagementRepository> {
            on { this.isQueueingOrLiveEngagement } doReturn isQueueingOrLiveEngagement
        }
        val secureConversationsRepository = mock<SecureConversationsRepository>()
        whenever(repositoryFactory.engagementRepository) doReturn engagementRepository
        whenever(repositoryFactory.secureConversationsRepository) doReturn secureConversationsRepository
        return engagementRepository to secureConversationsRepository
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
