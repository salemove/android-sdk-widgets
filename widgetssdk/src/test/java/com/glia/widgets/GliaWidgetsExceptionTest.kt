package com.glia.widgets

import com.glia.androidsdk.GliaException
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class GliaWidgetsExceptionTest {

    @Test
    fun testFrom_withAllValidGliaException_returnsGliaWidgetsException() {
        val allCoreCauses: List<GliaException.Cause> = GliaException.Cause.entries
        val gliaCoreExceptions = allCoreCauses.map { GliaException("Test message", it) }

        val allWidgetsCauses: List<GliaWidgetsException.Cause> = GliaWidgetsException.Cause.entries
        val gliaWidgetsExceptions = allWidgetsCauses.map { GliaWidgetsException("Test message", it) }

        assertEquals(allCoreCauses.size, allWidgetsCauses.size)
        gliaCoreExceptions.forEachIndexed { index, item ->
            val widgetsException = GliaWidgetsException.from(item)

            assertNotNull(widgetsException)
            assertEquals("Test message", widgetsException?.debugMessage)
            assertEquals(widgetsException?.gliaCause, gliaWidgetsExceptions[index].gliaCause)
        }
    }

    @Test
    fun testFrom_withNullGliaException_returnsNull() {
        val widgetsException = GliaWidgetsException.from(null)
        assertNull(widgetsException)
    }

    @Test
    fun testFrom_withUnknownCause_returnsGliaWidgetsExceptionWithNullCause() {
        val unknownCause = mockk<GliaException.Cause>()
        every { unknownCause.name } returns "UNKNOWN_CAUSE"

        val coreException = GliaException("Test message", unknownCause)
        val widgetsException = GliaWidgetsException.from(coreException)

        assertNotNull(widgetsException)
        assertEquals("Test message", widgetsException?.debugMessage)
        assertNull(widgetsException?.gliaCause)
    }
}
