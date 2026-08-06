package com.glia.widgets

import com.glia.androidsdk.GliaException
import com.glia.widgets.helper.Logger
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class GliaWidgetsExceptionTest {

    @Before
    fun setUp() {
        Logger.setIsDebug(false)
    }

    @Test
    fun toWidgetsType_withAllValidGliaException_returnsGliaWidgetsException() {
        GliaException.Cause.entries.forEach { coreCause ->
            val widgetsException = GliaException("Test message", coreCause).toWidgetsType()

            assertNotNull(widgetsException)
            assertEquals("Test message", widgetsException.debugMessage)
            val expectedCause = when (coreCause) {
                // ALREADY_INITIALIZED is intentionally not exposed in the Widgets API - it is reported as INVALID_INPUT
                GliaException.Cause.ALREADY_INITIALIZED -> GliaWidgetsException.Cause.INVALID_INPUT
                else -> GliaWidgetsException.Cause.valueOf(coreCause.name)
            }
            assertEquals(expectedCause, widgetsException.gliaCause)
        }
    }

    @Test
    fun toWidgetsType_withNullGliaException_returnsDefinedGliaWidgetsException() {
        val nothing: GliaException? = null
        val widgetsException = nothing.toWidgetsType("Test message", GliaWidgetsException.Cause.INVALID_INPUT)
        assertEquals("Test message", widgetsException.debugMessage)
        assertEquals(widgetsException.gliaCause, GliaWidgetsException.Cause.INVALID_INPUT)
    }

    @Test
    fun toCoreType_withAllValidGliaWidgetsException_returnsGliaException() {
        GliaWidgetsException.Cause.entries.forEach { widgetsCause ->
            val coreException = GliaWidgetsException("Test message", widgetsCause).toCoreType()

            assertNotNull(coreException)
            assertEquals("Test message", coreException.debugMessage)
            assertEquals(GliaException.Cause.valueOf(widgetsCause.name), coreException.cause)
        }
    }
}
