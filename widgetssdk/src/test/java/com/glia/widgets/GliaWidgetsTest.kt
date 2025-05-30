package com.glia.widgets

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.glia.androidsdk.Glia
import com.glia.androidsdk.GliaConfig
import com.glia.androidsdk.GliaException
import com.glia.androidsdk.RequestCallback
import com.glia.androidsdk.SiteApiKey as CoreSiteApiKey
import com.glia.androidsdk.omnibrowse.Omnibrowse
import com.glia.widgets.callbacks.OnComplete
import com.glia.widgets.callbacks.OnError
import com.glia.widgets.callvisualizer.controller.CallVisualizerController
import com.glia.widgets.internal.queue.QueueRepository
import com.glia.widgets.di.ControllerFactory
import com.glia.widgets.di.Dependencies
import com.glia.widgets.di.GliaCore
import com.glia.widgets.di.RepositoryFactory
import com.glia.widgets.engagement.EngagementRepository
import org.junit.Assert
import org.junit.Before
import org.junit.ClassRule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

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

        // Reset the state before each test
        GliaWidgets::class.java.getDeclaredField("isInitialized").apply {
            isAccessible = true
            set(null, false)
        }
    }

    @Test
    fun `init sets config to glia core when called`() {
        val siteApiKey = SiteApiKey("SiteApiId", "SiteApiSecret")
        val siteId = "SiteId"
        val region = "Region"
        val applicationContext = mock<Context>()
        whenever(applicationContext.applicationContext).thenReturn(applicationContext)
        val context = mock<Context>()
        whenever(context.applicationContext).thenReturn(applicationContext)
        val widgetsConfig = GliaWidgetsConfig.Builder()
            .setSiteApiKey(siteApiKey)
            .setSiteId(siteId)
            .setRegion(region)
            .setContext(context)
            .build()
        val callVisualizer = mock<Omnibrowse>()
        whenever(gliaCore.callVisualizer).thenReturn(callVisualizer)
        val callVisualizerController = mock<CallVisualizerController>()
        whenever(controllerFactory.callVisualizerController).thenReturn(callVisualizerController)
        val engagementRepository = mock<EngagementRepository>()
        whenever(repositoryFactory.engagementRepository) doReturn engagementRepository
        val queueRepository = mock<QueueRepository>()
        whenever(repositoryFactory.queueRepository) doReturn queueRepository

        GliaWidgets.init(widgetsConfig)

        val captor = argumentCaptor<GliaConfig>()
        verify(gliaCore).init(captor.capture())
        verify(repositoryFactory).initialize()
        val gliaConfig = captor.lastValue
        Assert.assertEquals(siteApiKey.id, gliaConfig.siteApiKey.id)
        Assert.assertEquals(siteApiKey.secret, gliaConfig.siteApiKey.secret)
        Assert.assertEquals(siteId, gliaConfig.siteId)
        Assert.assertEquals(region, gliaConfig.region)
        Assert.assertEquals(applicationContext, gliaConfig.context)
        Assert.assertTrue(GliaWidgets.isInitialized())
    }

    @Test
    fun `onSdkInit sets ApiKey when deprecated setSiteApiKey is used`() {
        val siteApiKey = CoreSiteApiKey("SiteApiId", "SiteApiSecret")
        val siteId = "SiteId"
        val region = "Region"
        val applicationContext = mock<Context>()
        whenever(applicationContext.applicationContext).thenReturn(applicationContext)
        val context = mock<Context>()
        whenever(context.applicationContext).thenReturn(applicationContext)
        val widgetsConfig = GliaWidgetsConfig.Builder()
            .setSiteApiKey(siteApiKey)
            .setSiteId(siteId)
            .setRegion(region)
            .setContext(context)
            .build()
        val callVisualizer = mock<Omnibrowse>()
        whenever(gliaCore.callVisualizer).thenReturn(callVisualizer)
        val callVisualizerController = mock<CallVisualizerController>()
        whenever(controllerFactory.callVisualizerController).thenReturn(callVisualizerController)
        val engagementRepository = mock<EngagementRepository>()
        whenever(repositoryFactory.engagementRepository) doReturn engagementRepository
        val queueRepository = mock<QueueRepository>()
        whenever(repositoryFactory.queueRepository) doReturn queueRepository

        GliaWidgets.init(widgetsConfig)

        val captor = argumentCaptor<GliaConfig>()
        verify(gliaCore).init(captor.capture())
        val gliaConfig = captor.lastValue
        Assert.assertEquals(siteApiKey.id, gliaConfig.siteApiKey.id)
        Assert.assertEquals(siteApiKey.secret, gliaConfig.siteApiKey.secret)
    }

    @Test
    fun `init should invoke onComplete when initialization succeeds`() {
        val siteApiKey = SiteApiKey("SiteApiId", "SiteApiSecret")
        val gliaWidgetsConfig = GliaWidgetsConfig.Builder()
            .setSiteApiKey(siteApiKey)
            .setSiteId("SiteId")
            .setRegion("Region")
            .setContext(mock())
            .build()
        val onComplete = mock<OnComplete>()
        val onError = mock<OnError>()
        val callbackCaptor = argumentCaptor<RequestCallback<Boolean?>>()
        whenever(gliaCore.init(any(), callbackCaptor.capture())).thenAnswer {
            // Simulate success by invoking the captured callback
            callbackCaptor.firstValue.onResult(true, null)
        }

        GliaWidgets.init(gliaWidgetsConfig, onComplete, onError)

        verify(onComplete).onComplete()
        verify(onError, never()).onError(any())
        Assert.assertTrue(GliaWidgets.isInitialized())
    }

    @Test
    fun `init should invoke onError with specific exception when initialization fails`() {
        val siteApiKey = SiteApiKey("SiteApiId", "SiteApiSecret")
        val gliaWidgetsConfig = GliaWidgetsConfig.Builder()
            .setSiteApiKey(siteApiKey)
            .setSiteId("SiteId")
            .setRegion("Region")
            .setContext(mock())
            .build()
        val onComplete = mock<OnComplete>()
        val onError = mock<OnError>()
        val callbackCaptor = argumentCaptor<RequestCallback<Boolean?>>()
        whenever(gliaCore.init(any(), callbackCaptor.capture())).thenAnswer {
            // Simulate error by invoking the captured callback
            callbackCaptor.firstValue.onResult(null, GliaException("Glia Core SDK exception", GliaException.Cause.INVALID_INPUT))
        }

        GliaWidgets.init(gliaWidgetsConfig, onComplete, onError)

        verify(onComplete, never()).onComplete()
        argumentCaptor<GliaWidgetsException>().apply {
            verify(onError).onError(capture())
            Assert.assertEquals(GliaWidgetsException.Cause.INVALID_INPUT, firstValue.gliaCause)
            Assert.assertEquals("Failed to initialise Glia Widgets SDK. Please check credentials.", firstValue.debugMessage)
        }
    }

    @Test
    fun `isInitialized returns false before initialization`() {
        Assert.assertFalse(Glia.isInitialized())
        Assert.assertFalse(GliaWidgets.isInitialized())
    }

    @Test
    fun `isInitialized returns false if initialization fails`() {
        val siteApiKey = SiteApiKey("SiteApiId", "SiteApiSecret")
        val widgetsConfig = GliaWidgetsConfig.Builder()
            .setSiteApiKey(siteApiKey)
            .build()
        whenever(Dependencies.onSdkInit(widgetsConfig)).thenThrow(RuntimeException("Initialization failed"))

        try {
            GliaWidgets.init(widgetsConfig)
        } catch (e: Exception) {
            // Ignore exception
        }

        Assert.assertFalse(Glia.isInitialized())
        Assert.assertFalse(GliaWidgets.isInitialized())
    }

    @Test
    fun `isInitialized returns false if initialization with callbacks fails`() {
        val siteApiKey = SiteApiKey("SiteApiId", "SiteApiSecret")
        val widgetsConfig = GliaWidgetsConfig.Builder()
            .setSiteApiKey(siteApiKey)
            .build()
        whenever(Dependencies.onSdkInit(widgetsConfig)).thenThrow(RuntimeException("Initialization failed"))

        try {
            GliaWidgets.init(widgetsConfig, mock(), mock())
        } catch (e: Exception) {
            // Ignore exception
        }

        Assert.assertFalse(Glia.isInitialized())
        Assert.assertFalse(GliaWidgets.isInitialized())
    }
}
