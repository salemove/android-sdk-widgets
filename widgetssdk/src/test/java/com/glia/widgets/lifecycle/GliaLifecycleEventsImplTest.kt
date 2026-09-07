package com.glia.widgets.lifecycle

import com.glia.widgets.engagement.MediaType
import com.glia.widgets.engagement.domain.GliaLifecycleEventUseCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.rxjava3.processors.PublishProcessor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GliaLifecycleEventsImplTest {

    private lateinit var gliaLifecycleEventUseCase: GliaLifecycleEventUseCase
    private lateinit var eventsProcessor: PublishProcessor<GliaEvent>
    private lateinit var gliaLifecycleEvents: GliaLifecycleEventsImpl

    @Before
    fun setUp() {
        eventsProcessor = PublishProcessor.create()
        gliaLifecycleEventUseCase = mockk()
        every { gliaLifecycleEventUseCase() } returns eventsProcessor
        gliaLifecycleEvents = GliaLifecycleEventsImpl(gliaLifecycleEventUseCase)
    }

    @Test
    fun `subscribe registers a listener and dispatches events`() {
        val listener: OnGliaEvent = mockk(relaxed = true)

        gliaLifecycleEvents.subscribe(listener)
        eventsProcessor.onNext(GliaEvent.EngagementStarted)

        verify { listener.onEvent(GliaEvent.EngagementStarted) }
        assertTrue(gliaLifecycleEvents.subscriptions.containsKey(listener.hashCode()))
    }

    @Test
    fun `subscribe does not duplicate the same listener instance`() {
        val listener: OnGliaEvent = mockk(relaxed = true)

        gliaLifecycleEvents.subscribe(listener)
        gliaLifecycleEvents.subscribe(listener)
        eventsProcessor.onNext(GliaEvent.EngagementStarted)

        verify(exactly = 1) { gliaLifecycleEventUseCase() }
        verify(exactly = 1) { listener.onEvent(GliaEvent.EngagementStarted) }
        assertEquals(1, gliaLifecycleEvents.subscriptions.size)
    }

    @Test
    fun `subscribe registers multiple distinct listeners independently`() {
        val firstListener: OnGliaEvent = mockk(relaxed = true)
        val secondListener: OnGliaEvent = mockk(relaxed = true)

        gliaLifecycleEvents.subscribe(firstListener)
        gliaLifecycleEvents.subscribe(secondListener)
        eventsProcessor.onNext(GliaEvent.EngagementOngoing(MediaType.AUDIO))

        verify { firstListener.onEvent(GliaEvent.EngagementOngoing(MediaType.AUDIO)) }
        verify { secondListener.onEvent(GliaEvent.EngagementOngoing(MediaType.AUDIO)) }
        assertEquals(2, gliaLifecycleEvents.subscriptions.size)
    }

    @Test
    fun `unsubscribe disposes the subscription and stops further events`() {
        val listener: OnGliaEvent = mockk(relaxed = true)
        gliaLifecycleEvents.subscribe(listener)

        gliaLifecycleEvents.unsubscribe(listener)
        eventsProcessor.onNext(GliaEvent.EngagementEnded)

        verify(exactly = 0) { listener.onEvent(any()) }
        assertFalse(gliaLifecycleEvents.subscriptions.containsKey(listener.hashCode()))
    }

    @Test
    fun `unsubscribe is a no-op for a listener that was never subscribed`() {
        val listener: OnGliaEvent = mockk(relaxed = true)

        gliaLifecycleEvents.unsubscribe(listener)

        assertTrue(gliaLifecycleEvents.subscriptions.isEmpty())
    }
}
