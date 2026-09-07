package com.glia.widgets.lifecycle

import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GliaLifecycleEventsImplTest {

    private lateinit var gliaLifecycleEvents: GliaLifecycleEventsImpl

    @Before
    fun setUp() {
        gliaLifecycleEvents = GliaLifecycleEventsImpl()
    }

    @Test
    fun `subscribe adds the listener`() {
        val listener: OnGliaEvent = mockk(relaxed = true)

        gliaLifecycleEvents.subscribe(listener)

        assertTrue(gliaLifecycleEvents.listeners.containsKey(listener.hashCode()))
    }

    @Test
    fun `subscribe does not duplicate the same listener instance`() {
        val listener: OnGliaEvent = mockk(relaxed = true)

        gliaLifecycleEvents.subscribe(listener)
        gliaLifecycleEvents.subscribe(listener)

        assertEquals(1, gliaLifecycleEvents.listeners.size)
    }

    @Test
    fun `subscribe registers multiple distinct listeners`() {
        val firstListener: OnGliaEvent = mockk(relaxed = true)
        val secondListener: OnGliaEvent = mockk(relaxed = true)

        gliaLifecycleEvents.subscribe(firstListener)
        gliaLifecycleEvents.subscribe(secondListener)

        assertEquals(2, gliaLifecycleEvents.listeners.size)
    }

    @Test
    fun `unsubscribe removes the listener`() {
        val listener: OnGliaEvent = mockk(relaxed = true)
        gliaLifecycleEvents.subscribe(listener)

        gliaLifecycleEvents.unsubscribe(listener)

        assertFalse(gliaLifecycleEvents.listeners.containsKey(listener.hashCode()))
    }

    @Test
    fun `unsubscribe is a no-op for a listener that was never subscribed`() {
        val listener: OnGliaEvent = mockk(relaxed = true)

        gliaLifecycleEvents.unsubscribe(listener)

        assertTrue(gliaLifecycleEvents.listeners.isEmpty())
    }
}
