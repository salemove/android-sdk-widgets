package com.glia.widgets.lifecycle

import android.assertRethrownOnMainThread
import android.runOnCoreThread
import com.glia.widgets.engagement.MediaType
import com.glia.widgets.engagement.domain.LifecycleEventUseCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.rxjava3.functions.Consumer
import io.reactivex.rxjava3.plugins.RxJavaPlugins
import io.reactivex.rxjava3.processors.PublishProcessor
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class LifecycleEventsImplTest {

    private lateinit var lifecycleEventUseCase: LifecycleEventUseCase
    private lateinit var eventsProcessor: PublishProcessor<LifecycleEvent>
    private lateinit var gliaLifecycleEvents: LifecycleEventsImpl

    private var previousRxErrorHandler: Consumer<in Throwable>? = null
    private val rxErrors: MutableList<Throwable> = mutableListOf()

    @Before
    fun setUp() {
        previousRxErrorHandler = RxJavaPlugins.getErrorHandler()
        RxJavaPlugins.setErrorHandler { rxErrors.add(it) }
        eventsProcessor = PublishProcessor.create()
        lifecycleEventUseCase = mockk()
        every { lifecycleEventUseCase() } returns eventsProcessor
        gliaLifecycleEvents = LifecycleEventsImpl(lifecycleEventUseCase)
    }

    @After
    fun tearDown() {
        RxJavaPlugins.setErrorHandler(previousRxErrorHandler)
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

    @Test
    fun `subscribe keeps listening and re-throws on the main thread when the integrator listener throws`() {
        val integratorBug = IllegalStateException("integrator bug")
        val receivedEvents = mutableListOf<LifecycleEvent>()
        val listener = OnLifecycleEvent { receivedEvents.add(it); throw integratorBug }
        gliaLifecycleEvents.subscribe(listener)

        runOnCoreThread {
            eventsProcessor.onNext(LifecycleEvent.EngagementStarted)
            eventsProcessor.onNext(LifecycleEvent.EngagementEnded)
        }

        assertEquals(listOf(LifecycleEvent.EngagementStarted, LifecycleEvent.EngagementEnded), receivedEvents)
        assertTrue(gliaLifecycleEvents.subscriptions.containsKey(listener.hashCode()))
        assertTrue(eventsProcessor.hasSubscribers())
        assertEquals(emptyList<Throwable>(), rxErrors)
        assertRethrownOnMainThread(integratorBug)
    }
}
