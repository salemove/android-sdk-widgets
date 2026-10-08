package com.glia.widgets.lifecycle

import com.glia.widgets.engagement.MediaType
import com.glia.widgets.engagement.domain.LifecycleEventUseCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.rxjava3.processors.PublishProcessor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class LifecycleEventsImplTest {

    private lateinit var lifecycleEventUseCase: LifecycleEventUseCase
    private lateinit var eventsProcessor: PublishProcessor<LifecycleEvent>
    private lateinit var gliaLifecycleEvents: LifecycleEventsImpl

    @Before
    fun setUp() {
        eventsProcessor = PublishProcessor.create()
        lifecycleEventUseCase = mockk()
        every { lifecycleEventUseCase() } returns eventsProcessor
        gliaLifecycleEvents = LifecycleEventsImpl(lifecycleEventUseCase)
    }

    @Test
    fun `subscribe registers a listener and dispatches events`() {
        val listener: OnLifecycleEvent = mockk(relaxed = true)

        gliaLifecycleEvents.subscribe(listener)
        eventsProcessor.onNext(LifecycleEvent.EngagementStarted)

        verify { listener.onEvent(LifecycleEvent.EngagementStarted) }
        assertTrue(gliaLifecycleEvents.subscriptions.containsKey(listener.hashCode()))
    }

    @Test
    fun `subscribe does not duplicate the same listener instance`() {
        val listener: OnLifecycleEvent = mockk(relaxed = true)

        gliaLifecycleEvents.subscribe(listener)
        gliaLifecycleEvents.subscribe(listener)
        eventsProcessor.onNext(LifecycleEvent.EngagementStarted)

        verify(exactly = 1) { lifecycleEventUseCase() }
        verify(exactly = 1) { listener.onEvent(LifecycleEvent.EngagementStarted) }
        assertEquals(1, gliaLifecycleEvents.subscriptions.size)
    }

    @Test
    fun `subscribe registers multiple distinct listeners independently`() {
        val firstListener: OnLifecycleEvent = mockk(relaxed = true)
        val secondListener: OnLifecycleEvent = mockk(relaxed = true)

        gliaLifecycleEvents.subscribe(firstListener)
        gliaLifecycleEvents.subscribe(secondListener)
        eventsProcessor.onNext(LifecycleEvent.EngagementOngoing(MediaType.AUDIO))

        verify { firstListener.onEvent(LifecycleEvent.EngagementOngoing(MediaType.AUDIO)) }
        verify { secondListener.onEvent(LifecycleEvent.EngagementOngoing(MediaType.AUDIO)) }
        assertEquals(2, gliaLifecycleEvents.subscriptions.size)
    }

    @Test
    fun `unsubscribe disposes the subscription and stops further events`() {
        val listener: OnLifecycleEvent = mockk(relaxed = true)
        gliaLifecycleEvents.subscribe(listener)

        gliaLifecycleEvents.unsubscribe(listener)
        eventsProcessor.onNext(LifecycleEvent.EngagementEnded)

        verify(exactly = 0) { listener.onEvent(any()) }
        assertFalse(gliaLifecycleEvents.subscriptions.containsKey(listener.hashCode()))
    }

    @Test
    fun `unsubscribe is a no-op for a listener that was never subscribed`() {
        val listener: OnLifecycleEvent = mockk(relaxed = true)

        gliaLifecycleEvents.unsubscribe(listener)

        assertTrue(gliaLifecycleEvents.subscriptions.isEmpty())
    }
}
