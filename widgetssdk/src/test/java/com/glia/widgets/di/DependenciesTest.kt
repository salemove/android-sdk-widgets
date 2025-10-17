package com.glia.widgets.di

import android.COMMON_EXTENSIONS_CLASS_PATH
import android.mockk
import android.unMockk
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.glia.androidsdk.CoreConfiguration
import com.glia.androidsdk.GliaException
import com.glia.androidsdk.RequestCallback
import com.glia.telemetry_lib.EventAttribute
import com.glia.telemetry_lib.GliaLogger
import com.glia.telemetry_lib.GliaTelemetry
import com.glia.telemetry_lib.GlobalAttribute
import com.glia.telemetry_lib.LogEvents
import com.glia.telemetry_lib.StringAttribute
import com.glia.widgets.BuildConfig
import com.glia.widgets.GliaWidgetsConfig
import com.glia.widgets.helper.orNotApplicable
import com.glia.widgets.helper.stringValue
import com.glia.widgets.helper.toCoreType
import com.glia.widgets.launcher.ConfigurationManager
import com.glia.widgets.locale.LocaleProvider
import io.mockk.Ordering
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkStatic
import io.mockk.verify
import io.mockk.verifyOrder
import junit.framework.TestCase.assertEquals
import org.junit.After
import org.junit.Before
import org.junit.ClassRule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@get:ClassRule
val rule: TestRule = InstantTaskExecutorRule()

@RunWith(RobolectricTestRunner::class)
class DependenciesTest {

    private lateinit var gliaCore: GliaCore
    private lateinit var controllerFactory: ControllerFactory
    private lateinit var repositoryFactory: RepositoryFactory
    private lateinit var localeProvider: LocaleProvider
    private lateinit var configurationManager: ConfigurationManager

    @Before
    fun setUp() {
        GliaTelemetry.mockk()
        GliaLogger.mockk()
        mockkStatic(COMMON_EXTENSIONS_CLASS_PATH)

        // create mocks
        gliaCore = mockk(relaxUnitFun = true)
        controllerFactory = mockk(relaxUnitFun = true)
        repositoryFactory = mockk(relaxUnitFun = true)
        localeProvider = mockk(relaxUnitFun = true)
        configurationManager = mockk(relaxUnitFun = true)

        // assign mocks to Dependencies
        Dependencies.gliaCore = gliaCore
        Dependencies.controllerFactory = controllerFactory
        Dependencies.repositoryFactory = repositoryFactory
        Dependencies.localeProvider = localeProvider
        Dependencies.configurationManager = configurationManager
    }

    @After
    fun tearDown() {
        GliaTelemetry.unMockk()
        GliaLogger.unMockk()
        unmockkStatic(COMMON_EXTENSIONS_CLASS_PATH)
    }

    @Test(expected = GliaException::class)
    fun `onSdkInit fails when configuration is invalid`() {
        val widgetsConfig: GliaWidgetsConfig = mockk()
        every { any<GliaWidgetsConfig>().toCoreType() } throws GliaException("Invalid configuration", GliaException.Cause.INVALID_INPUT)

        Dependencies.onSdkInit(gliaWidgetsConfig = widgetsConfig)
    }

    @Test(expected = GliaException::class)
    fun `onSdkInit(callback) fails when configuration is invalid`() {
        val widgetsConfig: GliaWidgetsConfig = mockk()
        every { any<GliaWidgetsConfig>().toCoreType() } throws GliaException("Invalid configuration", GliaException.Cause.INVALID_INPUT)

        Dependencies.onSdkInit(gliaWidgetsConfig = widgetsConfig, null)
    }

    @Test(expected = GliaException::class)
    fun `onSdkInit fails when gliaCore init fails`() {
        every { gliaCore.init(any()) } throws GliaException("Failed to init core", GliaException.Cause.INVALID_INPUT)

        Dependencies.onSdkInit(gliaWidgetsConfig = mockConfiguration())
    }

    @Test
    fun `onSdkInit(callback) fails when gliaCore init fails`() {
        val widgetsConfig = mockConfiguration()
        val coreCallbackSlot = slot<RequestCallback<Boolean?>>()
        val widgetsCallback: RequestCallback<Boolean?> = mockk(relaxUnitFun = true)

        Dependencies.onSdkInit(gliaWidgetsConfig = widgetsConfig, widgetsCallback)

        verifyInitLogger(widgetsConfig)
        verifyNotInitialized()

        verify { gliaCore.init(any(), capture(coreCallbackSlot)) }
        val coreException = GliaException("Failed to init core", GliaException.Cause.INVALID_INPUT)
        coreCallbackSlot.captured.onResult(null, coreException)

        verify { widgetsCallback.onResult(null, coreException) }
        verifyNotInitialized(excludeLogger = true)
    }

    @Test
    fun `onSdkInit succeeds when all required fields are present`() {
        val widgetsConfig = mockConfiguration()

        Dependencies.onSdkInit(gliaWidgetsConfig = widgetsConfig)
        verifyInitLogger(widgetsConfig)
        verifyInitialized(widgetsConfig)
    }

    @Test
    fun `onSdkInit(callback) succeeds when all required fields are present and gliaCore init succeeds`() {
        val widgetsConfig = mockConfiguration()
        val coreCallbackSlot = slot<RequestCallback<Boolean?>>()
        val widgetsCallback: RequestCallback<Boolean?> = mockk(relaxUnitFun = true)

        Dependencies.onSdkInit(gliaWidgetsConfig = widgetsConfig, widgetsCallback)

        verifyInitLogger(widgetsConfig)
        verifyNotInitialized()

        verify { gliaCore.init(any(), capture(coreCallbackSlot)) }
        coreCallbackSlot.captured.onResult(true, null)

        verify { widgetsCallback.onResult(true, null) }
        verifyInitialized(widgetsConfig)
    }

    @Test
    fun `onSdkInit(callback) initialized all the required controllers when callback is null and gliaCore init succeeds`() {
        val widgetsConfig = mockConfiguration()
        val coreCallbackSlot = slot<RequestCallback<Boolean?>>()

        Dependencies.onSdkInit(gliaWidgetsConfig = widgetsConfig, null)

        verifyInitLogger(widgetsConfig)
        verifyNotInitialized()

        verify { gliaCore.init(any(), capture(coreCallbackSlot)) }
        coreCallbackSlot.captured.onResult(true, null)

        verifyInitialized(widgetsConfig)
    }

    private fun mockConfiguration(): GliaWidgetsConfig {
        val widgetsConfig: GliaWidgetsConfig = mockk(relaxed = true)
        val coreConfig: CoreConfiguration = mockk(relaxed = true)
        every { any<GliaWidgetsConfig>().toCoreType() } returns coreConfig
        return widgetsConfig
    }

    private fun verifyInitLogger(widgetsConfig: GliaWidgetsConfig) {
        val attributeSlot = slot<Map<StringAttribute, String>>()
        verify(Ordering.ORDERED) {
            GliaTelemetry.setGlobalAttribute(GlobalAttribute.SdkWidgetsVersion, BuildConfig.GLIA_WIDGETS_SDK_VERSION)
            GliaLogger.i(event = eq(LogEvents.WIDGETS_SDK_CONFIGURING), message = isNull<String>(), attributes = capture(attributeSlot))
        }

        val attributes = attributeSlot.captured

        assertEquals(widgetsConfig.siteApiKey?.id.orNotApplicable, attributes[EventAttribute.ApiKeyId])
        assertEquals(widgetsConfig.region?.stringValue ?: widgetsConfig.regionString.orNotApplicable, attributes[EventAttribute.Environment])
        assertEquals(widgetsConfig.manualLocaleOverride, attributes[EventAttribute.LocaleCode])
    }

    private fun verifyInitialized(widgetsConfig: GliaWidgetsConfig) {
        verifyOrder {
            controllerFactory.init()
            repositoryFactory.initialize()
            configurationManager.applyConfiguration(widgetsConfig)
            localeProvider.setCompanyName(widgetsConfig.companyName)
            GliaLogger.i(LogEvents.WIDGETS_SDK_CONFIGURED)
        }
    }

    private fun verifyNotInitialized(excludeLogger: Boolean = false) {
        verify(exactly = 0) {
            controllerFactory.init()
            repositoryFactory.initialize()
            configurationManager.applyConfiguration(any())
            localeProvider.setCompanyName(any())
        }

        verify(inverse = !excludeLogger) {
            GliaLogger.i(LogEvents.WIDGETS_SDK_CONFIGURED)
        }
    }

}
