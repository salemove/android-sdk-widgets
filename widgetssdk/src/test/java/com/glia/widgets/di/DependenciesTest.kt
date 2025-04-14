package com.glia.widgets.di

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.glia.androidsdk.GliaException
import com.glia.androidsdk.RequestCallback
import com.glia.androidsdk.SiteApiKey
import com.glia.widgets.GliaWidgetsConfig
import org.junit.Before
import org.junit.ClassRule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@get:ClassRule
val rule: TestRule = InstantTaskExecutorRule()

@RunWith(RobolectricTestRunner::class)
class DependenciesTest {

    private lateinit var gliaCore: GliaCore
    private lateinit var controllerFactory: ControllerFactory
    private lateinit var repositoryFactory: RepositoryFactory

    @Before
    fun setUp() {
        gliaCore = Mockito.mock()
        controllerFactory = Mockito.mock()
        repositoryFactory = Mockito.mock()
        Dependencies.gliaCore = gliaCore
        Dependencies.controllerFactory = controllerFactory
        Dependencies.repositoryFactory = repositoryFactory
        Dependencies.localeProvider = Mockito.mock()
    }

    @Test
    fun `onSdkInit without callbacks should initialize controllerFactory, and repositoryFactory regardless of gliaCore init result`() {
        val siteApiKey = SiteApiKey("SiteApiId", "SiteApiSecret")
        val widgetsConfig = GliaWidgetsConfig.Builder()
            .setSiteApiKey(siteApiKey)
            .build()

        Dependencies.onSdkInit(widgetsConfig)

        verify(Dependencies.gliaCore).init(any())
        verify(Dependencies.controllerFactory).init()
        verify(Dependencies.repositoryFactory).initialize()
    }

    @Test
    fun `onSdkInit with callbacks should not initialize controllerFactory, and repositoryFactory until gliaCore init finishes`() {
        val siteApiKey = SiteApiKey("SiteApiId", "SiteApiSecret")
        val widgetsConfig = GliaWidgetsConfig.Builder()
            .setSiteApiKey(siteApiKey)
            .build()
        val callback = mock<RequestCallback<Boolean?>>()

        Dependencies.onSdkInit(gliaWidgetsConfig = widgetsConfig, callback = callback)

        verify(Dependencies.gliaCore).init(any(), any())
        verify(Dependencies.controllerFactory, never()).init()
        verify(Dependencies.repositoryFactory, never()).initialize()
    }

    @Test
    fun `onSdkInit with callbacks should initialize controllerFactory, and repositoryFactory when gliaCore init succeeds`() {
        val siteApiKey = SiteApiKey("SiteApiId", "SiteApiSecret")
        val widgetsConfig = GliaWidgetsConfig.Builder()
            .setSiteApiKey(siteApiKey)
            .build()
        val callback = mock<RequestCallback<Boolean?>>()
        val callbackCaptor = argumentCaptor<RequestCallback<Boolean?>>()

        whenever(gliaCore.init(any(), callbackCaptor.capture())).thenAnswer {
            // Simulate success by invoking the captured callback
            callbackCaptor.firstValue.onResult(true, null)
        }

        Dependencies.onSdkInit(gliaWidgetsConfig = widgetsConfig, callback = callback)

        verify(Dependencies.gliaCore).init(any(), any())
        verify(Dependencies.controllerFactory).init()
        verify(Dependencies.repositoryFactory).initialize()
    }

    @Test
    fun `onSdkInit with callbacks should not initialize controllerFactory, and repositoryFactory when gliaCore init fails`() {
        val siteApiKey = SiteApiKey("SiteApiId", "SiteApiSecret")
        val widgetsConfig = GliaWidgetsConfig.Builder()
            .setSiteApiKey(siteApiKey)
            .build()
        val callback = mock<RequestCallback<Boolean?>>()
        val callbackCaptor = argumentCaptor<RequestCallback<Boolean?>>()
        whenever(gliaCore.init(any(), callbackCaptor.capture())).thenAnswer {
            // Simulate error by invoking the captured callback
            callbackCaptor.firstValue.onResult(null, GliaException("Test Exception", GliaException.Cause.INVALID_INPUT))
        }

        Dependencies.onSdkInit(gliaWidgetsConfig = widgetsConfig, callback = callback)

        verify(Dependencies.gliaCore).init(any(), any())
        verify(Dependencies.controllerFactory, never()).init()
        verify(Dependencies.repositoryFactory, never()).initialize()
    }
}
