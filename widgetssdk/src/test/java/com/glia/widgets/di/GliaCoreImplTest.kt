package com.glia.widgets.di

import android.mockk
import android.unMockk
import com.glia.androidsdk.AuthorizationMethod
import com.glia.androidsdk.CoreConfiguration
import com.glia.androidsdk.Glia
import com.glia.androidsdk.GliaException
import com.glia.telemetry_lib.GliaLogger
import com.glia.telemetry_lib.LogEvents
import com.glia.widgets.GliaWidgetsConfig
import com.glia.widgets.GliaWidgetsException
import com.glia.widgets.Region
import com.glia.widgets.SiteApiKey
import com.glia.widgets.helper.toCoreType
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkStatic
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import java.util.function.Consumer

@RunWith(RobolectricTestRunner::class)
class GliaCoreImplTest {

    private lateinit var gliaCore: GliaCoreImpl

    @Before
    fun setUp() {
        mockkStatic(Glia::class)
        GliaLogger.mockk()
        gliaCore = GliaCoreImpl()
    }

    @After
    fun tearDown() {
        unmockkStatic(Glia::class)
        GliaLogger.unMockk()
    }

    @Test
    fun `init passes converted config to core`() {
        every { Glia.init(any<CoreConfiguration>(), any(), any()) } just Runs
        val configSlot = slot<CoreConfiguration>()
        val siteApiKey = SiteApiKey("SiteApiId", "SiteApiSecret")
        val widgetsConfig = widgetsConfig(siteApiKey)

        gliaCore.init(widgetsConfig, {}) {}

        verify { Glia.init(capture(configSlot), any(), any()) }
        val coreConfig = configSlot.captured
        val authorizationMethod = coreConfig.authorizationMethod as AuthorizationMethod.SiteApiKey
        assertEquals(siteApiKey.id, authorizationMethod.id)
        assertEquals(siteApiKey.secret, authorizationMethod.secret)
        assertEquals("SiteId", coreConfig.siteId)
        assertEquals(Region.EU.toCoreType(), coreConfig.region)
        assertEquals(RuntimeEnvironment.getApplication(), coreConfig.applicationContext)
    }

    @Test
    fun `init converts UserApiKey authorization method`() {
        every { Glia.init(any<CoreConfiguration>(), any(), any()) } just Runs
        val configSlot = slot<CoreConfiguration>()
        val userApiKey = com.glia.widgets.AuthorizationMethod.UserApiKey("UserApiId", "UserApiSecret")
        val widgetsConfig = GliaWidgetsConfig.Builder()
            .setContext(RuntimeEnvironment.getApplication())
            .setSiteId("SiteId")
            .setRegion(Region.EU)
            .setAuthorizationMethod(userApiKey)
            .build()

        gliaCore.init(widgetsConfig, {}) {}

        verify { Glia.init(capture(configSlot), any(), any()) }
        val authorizationMethod = configSlot.captured.authorizationMethod as AuthorizationMethod.UserApiKey
        assertEquals("UserApiId", authorizationMethod.id)
        assertEquals("UserApiSecret", authorizationMethod.secret)
    }

    @Test
    fun `init invokes onComplete when core initialization succeeds`() {
        every { Glia.init(any<CoreConfiguration>(), any(), any()) } answers { secondArg<Runnable>().run() }
        var completed = false

        gliaCore.init(widgetsConfig(), { completed = true }) { fail("onError should not be invoked") }

        assertTrue(completed)
    }

    @Test
    fun `init reports error when core initialization throws`() {
        every { Glia.init(any<CoreConfiguration>(), any(), any()) } throws
            GliaException("Invalid configuration", GliaException.Cause.INVALID_INPUT)
        var error: GliaWidgetsException? = null

        gliaCore.init(widgetsConfig(), { fail("onComplete should not be invoked") }) { error = it }

        assertEquals(GliaWidgetsException.Cause.INVALID_INPUT, requireNotNull(error).gliaCause)
    }

    @Test
    fun `init reports internal error when an unexpected exception is thrown`() {
        every { Glia.init(any<CoreConfiguration>(), any(), any()) } throws RuntimeException("Unexpected failure")
        var error: GliaWidgetsException? = null

        gliaCore.init(widgetsConfig(), { fail("onComplete should not be invoked") }) { error = it }

        assertEquals(GliaWidgetsException.Cause.INTERNAL_ERROR, requireNotNull(error).gliaCause)
    }

    @Test
    fun `init logs failure when core reports error`() {
        initializationError(GliaException.Cause.NETWORK_TIMEOUT)

        verify { GliaLogger.e(LogEvents.WIDGETS_SDK_UNCATEGORIZED, "Glia Widgets SDK initialization failed", any<GliaException>()) }
    }

    @Test
    fun `init logs failure when core initialization throws`() {
        every { Glia.init(any<CoreConfiguration>(), any(), any()) } throws
            GliaException("Invalid configuration", GliaException.Cause.INVALID_INPUT)

        gliaCore.init(widgetsConfig(), { fail("onComplete should not be invoked") }) { }

        verify { GliaLogger.e(LogEvents.WIDGETS_SDK_UNCATEGORIZED, "Glia Widgets SDK initialization failed", any<GliaException>()) }
    }

    @Test
    fun `init does not log failure when core initialization succeeds`() {
        every { Glia.init(any<CoreConfiguration>(), any(), any()) } answers { secondArg<Runnable>().run() }

        gliaCore.init(widgetsConfig(), {}) { fail("onError should not be invoked") }

        verify(exactly = 0) { GliaLogger.e(LogEvents.WIDGETS_SDK_UNCATEGORIZED, any<String>(), any<Throwable>()) }
    }

    @Test
    fun `deprecated init passes converted config to core`() {
        every { Glia.init(any<CoreConfiguration>()) } just Runs
        val configSlot = slot<CoreConfiguration>()
        val siteApiKey = SiteApiKey("SiteApiId", "SiteApiSecret")

        gliaCore.init(widgetsConfig(siteApiKey))

        verify { Glia.init(capture(configSlot)) }
        val coreConfig = configSlot.captured
        val authorizationMethod = coreConfig.authorizationMethod as AuthorizationMethod.SiteApiKey
        assertEquals(siteApiKey.id, authorizationMethod.id)
        assertEquals(siteApiKey.secret, authorizationMethod.secret)
        assertEquals("SiteId", coreConfig.siteId)
    }

    @Test
    fun `deprecated init throws GliaWidgetsException when core initialization throws`() {
        every { Glia.init(any<CoreConfiguration>()) } throws
            GliaException("Glia SDK is already initialized", GliaException.Cause.ALREADY_INITIALIZED)

        val exception = assertThrows(GliaWidgetsException::class.java) {
            gliaCore.init(widgetsConfig())
        }

        assertEquals(GliaWidgetsException.Cause.INVALID_INPUT, exception.gliaCause)
    }

    @Test
    fun `init maps ALREADY_INITIALIZED error to INVALID_INPUT`() {
        val error = initializationError(GliaException.Cause.ALREADY_INITIALIZED)

        assertEquals(GliaWidgetsException.Cause.INVALID_INPUT, error.gliaCause)
        assertEquals("Glia Widgets SDK is already initialized or initialization is already in progress.", error.debugMessage)
    }

    @Test
    fun `init maps NETWORK_TIMEOUT error to NETWORK_TIMEOUT`() {
        val error = initializationError(GliaException.Cause.NETWORK_TIMEOUT)

        assertEquals(GliaWidgetsException.Cause.NETWORK_TIMEOUT, error.gliaCause)
        assertEquals("Network timeout. Please check the Internet connection.", error.debugMessage)
    }

    @Test
    fun `init maps INVALID_INPUT error to INVALID_INPUT`() {
        val error = initializationError(GliaException.Cause.INVALID_INPUT)

        assertEquals(GliaWidgetsException.Cause.INVALID_INPUT, error.gliaCause)
        assertEquals("Failed to initialise Glia Widgets SDK. Invalid input. Please check credentials.", error.debugMessage)
    }

    @Test
    fun `init maps AUTHENTICATION_ERROR error to AUTHENTICATION_ERROR`() {
        val error = initializationError(GliaException.Cause.AUTHENTICATION_ERROR)

        assertEquals(GliaWidgetsException.Cause.AUTHENTICATION_ERROR, error.gliaCause)
        assertEquals("Failed to initialise Glia Widgets SDK. Authentication error. Please check credentials.", error.debugMessage)
    }

    @Test
    fun `init maps FORBIDDEN error to INVALID_INPUT`() {
        val error = initializationError(GliaException.Cause.FORBIDDEN)

        assertEquals(GliaWidgetsException.Cause.INVALID_INPUT, error.gliaCause)
        assertEquals("Failed to initialise Glia Widgets SDK. Forbidden. Please check credentials.", error.debugMessage)
    }

    @Test
    fun `init maps unexpected error to INVALID_INPUT`() {
        val error = initializationError(GliaException.Cause.INTERNAL_ERROR)

        assertEquals(GliaWidgetsException.Cause.INVALID_INPUT, error.gliaCause)
        assertEquals("Failed to initialise Glia Widgets SDK. Please check logs.", error.debugMessage)
    }

    @Test
    fun `isInitialized delegates to core`() {
        every { Glia.isInitialized() } returns true
        assertTrue(gliaCore.isInitialized)

        every { Glia.isInitialized() } returns false
        assertFalse(gliaCore.isInitialized)
    }

    @Test
    fun `isInitializationInProgress delegates to core`() {
        every { Glia.isInitInProgress() } returns true
        assertTrue(gliaCore.isInitializationInProgress)

        every { Glia.isInitInProgress() } returns false
        assertFalse(gliaCore.isInitializationInProgress)
    }

    private fun widgetsConfig(siteApiKey: SiteApiKey = SiteApiKey("SiteApiId", "SiteApiSecret")): GliaWidgetsConfig =
        GliaWidgetsConfig.Builder()
            .setSiteApiKey(siteApiKey)
            .setSiteId("SiteId")
            .setRegion(GliaWidgetsConfig.Regions.EU)
            .setContext(RuntimeEnvironment.getApplication())
            .build()

    private fun initializationError(coreCause: GliaException.Cause): GliaWidgetsException {
        every { Glia.init(any<CoreConfiguration>(), any(), any()) } answers {
            thirdArg<Consumer<GliaException>>().accept(GliaException("Core error", coreCause))
        }
        var error: GliaWidgetsException? = null

        gliaCore.init(widgetsConfig(), { fail("onComplete should not be invoked") }) { error = it }

        return requireNotNull(error) { "onError was not invoked" }
    }
}
