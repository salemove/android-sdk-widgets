package com.glia.widgets

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.glia.androidsdk.GliaConfig
import com.glia.androidsdk.SiteApiKey
import com.glia.androidsdk.omnibrowse.Omnibrowse
import com.glia.widgets.callvisualizer.controller.CallVisualizerController
import com.glia.widgets.di.ControllerFactory
import com.glia.widgets.di.Dependencies
import com.glia.widgets.di.GliaCore
import com.glia.widgets.di.RepositoryFactory
import org.junit.Assert
import org.junit.Before
import org.junit.ClassRule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
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
        Dependencies.setGlia(gliaCore)
        controllerFactory = mock()
        Dependencies.setControllerFactory(controllerFactory)
        repositoryFactory = mock()
        Dependencies.setRepositoryFactory(repositoryFactory)
    }

    @Test
    fun onAppCreate_setApplicationToGliaCore_whenCalled() {
        val application = RuntimeEnvironment.getApplication()
        GliaWidgets.onAppCreate(application)
        Mockito.verify(gliaCore).onAppCreate(eq(application))
    }

    @Test
    fun init_setConfigToGliaCore_whenCalled() {
        val siteApiKey = SiteApiKey("SiteApiId", "SiteApiSecret")
        val siteId = "SiteId"
        val region = "Region"
        val context = Mockito.mock(Context::class.java)
        val widgetsConfig = GliaWidgetsConfig.Builder()
            .setSiteApiKey(siteApiKey)
            .setSiteId(siteId)
            .setRegion(region)
            .setContext(context)
            .build()
        val callVisualizer = Mockito.mock(Omnibrowse::class.java)
        whenever(gliaCore.callVisualizer).thenReturn(callVisualizer)
        val callVisualizerController = Mockito.mock(CallVisualizerController::class.java)
        whenever(controllerFactory.callVisualizerController).thenReturn(callVisualizerController)
        whenever(repositoryFactory.engagementRepository) doReturn mock()
        GliaWidgets.init(widgetsConfig)
        val captor = argumentCaptor<GliaConfig>()
        Mockito.verify(gliaCore).init(captor.capture())
        val gliaConfig = captor.lastValue
        Assert.assertEquals(siteApiKey, gliaConfig.siteApiKey)
        Assert.assertEquals(siteId, gliaConfig.siteId)
        Assert.assertEquals(region, gliaConfig.region)
        Assert.assertEquals(context, gliaConfig.context)
    }

}
