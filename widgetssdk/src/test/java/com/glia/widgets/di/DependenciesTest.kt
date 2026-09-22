package com.glia.widgets.di

import android.mockk
import android.unMockk
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.glia.widgets.AuthorizationMethod as WidgetsAuthorizationMethod
import com.glia.widgets.Region
import com.glia.telemetry_lib.EventAttribute
import com.glia.telemetry_lib.GliaLogger
import com.glia.telemetry_lib.GliaTelemetry
import com.glia.telemetry_lib.GlobalAttribute
import com.glia.telemetry_lib.LogEvents
import com.glia.telemetry_lib.StringAttribute
import com.glia.widgets.BuildConfig
import com.glia.widgets.GliaWidgetsConfig
import com.glia.widgets.GliaWidgetsException
import com.glia.widgets.apiKeyId
import com.glia.widgets.callbacks.OnComplete
import com.glia.widgets.callbacks.OnError
import com.glia.widgets.helper.orNotApplicable
import com.glia.widgets.helper.stringValue
import com.glia.widgets.launcher.ConfigurationManager
import com.glia.widgets.push.notifications.PushClickHandlerController
import com.glia.widgets.locale.LocaleProvider
import io.mockk.Ordering
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.mockk.verifyOrder
import junit.framework.TestCase.assertEquals
import org.junit.After
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.ClassRule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@get:ClassRule
val rule: TestRule = InstantTaskExecutorRule()

@RunWith(RobolectricTestRunner::class)
class DependenciesTest {

    private lateinit var gliaCore: GliaCore
    private lateinit var controllerFactory: ControllerFactory
    private lateinit var repositoryFactory: RepositoryFactory
    private lateinit var localeProvider: LocaleProvider
    private lateinit var configurationManager: ConfigurationManager
    private lateinit var onComplete: OnComplete
    private lateinit var onError: OnError
    private lateinit var pushClickHandlerController: PushClickHandlerController

    @Before
    fun setUp() {
        GliaTelemetry.mockk()
        GliaLogger.mockk()

        // create mocks
        gliaCore = mockk(relaxUnitFun = true)
        controllerFactory = mockk(relaxUnitFun = true)
        repositoryFactory = mockk(relaxUnitFun = true)
        localeProvider = mockk(relaxUnitFun = true)
        configurationManager = mockk(relaxUnitFun = true)
        onComplete = mockk(relaxUnitFun = true)
        onError = mockk(relaxUnitFun = true)
        pushClickHandlerController = mockk(relaxUnitFun = true)
        every { controllerFactory.pushClickHandlerController } returns pushClickHandlerController

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

        // Dependencies is a process-wide singleton - restore the real GliaCore so the strict mock does not leak into other test classes
        Dependencies.gliaCore = GliaCoreImpl()
    }

    @Test
    fun `onSdkInit(callbacks) passes onError to gliaCore and reports error when gliaCore init fails`() {
        val widgetsConfig = mockConfiguration()
        val coreOnErrorSlot = slot<OnError>()

        Dependencies.onSdkInit(widgetsConfig, onComplete, onError)

        verifyInitLogger(widgetsConfig)
        verifyNotInitialized()

        verify { gliaCore.init(widgetsConfig, any(), capture(coreOnErrorSlot)) }
        val initializationError = GliaWidgetsException("Failed to init core", GliaWidgetsException.Cause.INVALID_INPUT)
        coreOnErrorSlot.captured.onError(initializationError)

        verify { onError.onError(initializationError) }
        verify(exactly = 0) { onComplete.onComplete() }
        verifyNotInitialized()
    }

    @Test
    fun `onSdkInit(callbacks) initializes dependencies and reports completion when gliaCore init succeeds`() {
        val widgetsConfig = mockConfiguration()
        val coreOnCompleteSlot = slot<OnComplete>()

        Dependencies.onSdkInit(widgetsConfig, onComplete, onError)

        verifyInitLogger(widgetsConfig)
        verifyNotInitialized()

        verify { gliaCore.init(widgetsConfig, capture(coreOnCompleteSlot), any()) }
        coreOnCompleteSlot.captured.onComplete()

        verify { onComplete.onComplete() }
        verify(exactly = 0) { onError.onError(any()) }
        verifyInitialized(widgetsConfig)
    }

    @Test
    fun `onSdkInit logs UserApiKey ID in telemetry`() {
        val userApiKey = WidgetsAuthorizationMethod.UserApiKey("user-api-key-123", "secret")
        val config = GliaWidgetsConfig.Builder()
            .setContext(RuntimeEnvironment.getApplication())
            .setSiteId("test-site-id")
            .setRegion(Region.EU)
            .setAuthorizationMethod(userApiKey)
            .build()

        Dependencies.onSdkInit(config, onComplete, onError)

        val attributeSlot = slot<Map<StringAttribute, String>>()
        verify {
            GliaLogger.i(event = eq(LogEvents.WIDGETS_SDK_CONFIGURING), message = isNull<String>(), attributes = capture(attributeSlot))
        }

        val attributes = attributeSlot.captured
        assertEquals("user-api-key-123", attributes[EventAttribute.ApiKeyId])
    }

    private fun mockConfiguration(): GliaWidgetsConfig = mockk(relaxed = true)

    private fun verifyInitLogger(widgetsConfig: GliaWidgetsConfig) {
        val attributeSlot = slot<Map<StringAttribute, String>>()
        verify(Ordering.ORDERED) {
            GliaTelemetry.setGlobalAttribute(GlobalAttribute.SdkWidgetsVersion, BuildConfig.GLIA_WIDGETS_SDK_VERSION)
            GliaLogger.i(event = eq(LogEvents.WIDGETS_SDK_CONFIGURING), message = isNull<String>(), attributes = capture(attributeSlot))
        }

        val attributes = attributeSlot.captured

        assertEquals(widgetsConfig.authorizationMethod?.apiKeyId.orNotApplicable, attributes[EventAttribute.ApiKeyId])
        assertEquals(widgetsConfig.region?.stringValue ?: widgetsConfig.regionString.orNotApplicable, attributes[EventAttribute.Environment])
        assertEquals(widgetsConfig.manualLocaleOverride, attributes[EventAttribute.LocaleCode])
    }

    private fun verifyInitialized(widgetsConfig: GliaWidgetsConfig) {
        verifyOrder {
            controllerFactory.init()
            repositoryFactory.initialize()
            configurationManager.applyConfiguration(widgetsConfig)
            localeProvider.setCompanyName(widgetsConfig.companyName)
            pushClickHandlerController.onSdkInitialized()
            GliaLogger.i(LogEvents.WIDGETS_SDK_CONFIGURED)
        }
    }

    private fun verifyNotInitialized() {
        verify(exactly = 0) {
            controllerFactory.init()
            repositoryFactory.initialize()
            configurationManager.applyConfiguration(any())
            localeProvider.setCompanyName(any())
            pushClickHandlerController.onSdkInitialized()
        }

        verify(inverse = true) {
            GliaLogger.i(LogEvents.WIDGETS_SDK_CONFIGURED)
        }
    }

}
