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
        val allCoreCauses: List<GliaException.Cause> = GliaException.Cause.entries
        val gliaCoreExceptions = allCoreCauses.map { GliaException("Test message", it) }

        val allWidgetsCauses: List<GliaWidgetsException.Cause> = GliaWidgetsException.Cause.entries
        val gliaWidgetsExceptions = allWidgetsCauses.map { GliaWidgetsException("Test message", it) }

        assertEquals(allCoreCauses.size, allWidgetsCauses.size)
        gliaCoreExceptions.forEachIndexed { index, item ->
            val widgetsException = item.toWidgetsType()

            assertNotNull(widgetsException)
            assertEquals("Test message", widgetsException.debugMessage)
            assertEquals(widgetsException.gliaCause, gliaWidgetsExceptions[index].gliaCause)
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
        val allWidgetsCauses: List<GliaWidgetsException.Cause> = GliaWidgetsException.Cause.entries
        val gliaWidgetsExceptions = allWidgetsCauses.map { GliaWidgetsException("Test message", it) }

        val allCoreCauses: List<GliaException.Cause> = GliaException.Cause.entries
        val gliaCoreExceptions = allCoreCauses.map { GliaException("Test message", it) }

        assertEquals(allCoreCauses.size, allWidgetsCauses.size)
        gliaWidgetsExceptions.forEachIndexed { index, item ->
            val coreException = item.toCoreType()

            assertNotNull(coreException)
            assertEquals("Test message", coreException.debugMessage)
            assertEquals(coreException.cause, gliaCoreExceptions[index].cause)
        }
    }
}
