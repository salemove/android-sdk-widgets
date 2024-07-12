package com.glia.widgets.core.secureconversations.domain

import android.annotation.SuppressLint
import com.glia.androidsdk.Engagement
import com.glia.androidsdk.LogLevel
import com.glia.androidsdk.LoggingAdapter
import com.glia.androidsdk.queuing.Queue
import com.glia.androidsdk.queuing.QueueState
import com.glia.widgets.core.engagement.GliaEngagementConfigRepository
import com.glia.widgets.core.queue.GliaQueueRepository
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.rx.Schedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.TestScheduler
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class GetAvailableQueueIdsForSecureMessagingUseCaseTest {
    private lateinit var engagementConfigRepository: GliaEngagementConfigRepository
    private lateinit var queueRepository: GliaQueueRepository
    private lateinit var isMessagingAvailableUseCase: IsMessagingAvailableUseCase
    private lateinit var testScheduler: TestScheduler
    private lateinit var loggingAdapter: LoggingAdapter

    private lateinit var useCase: GetAvailableQueueIdsForSecureMessagingUseCase

    // Queues
    private val queue1 = Queue(
        "1068c08b-4155-4ee6-93d2-5baebee72ca6",
        "Queue1",
        QueueState.Status.OPEN,
        arrayOf(Engagement.MediaType.TEXT, Engagement.MediaType.MESSAGING),
        false
    )
    private val queue2 = Queue(
        "60b040be-03dd-4c78-a0e2-834e8b8903dc",
        "Queue2",
        QueueState.Status.OPEN,
        arrayOf(Engagement.MediaType.TEXT, Engagement.MediaType.MESSAGING),
        true
    )
    private val queue3 = Queue(
        "f8dcaccb-1874-4c1c-ba26-79eee699578c",
        "Queue3",
        QueueState.Status.OPEN,
        arrayOf(Engagement.MediaType.TEXT, Engagement.MediaType.MESSAGING),
        true
    )
    private val queues = listOf(queue1, queue2, queue3)
    private val queueIds = queues.map { it.id }

    private val defaultQueues = listOf(queue2, queue3)
    private val defaultQueuesIds = defaultQueues.map { it.id }

    @Before
    fun setUp() {
        loggingAdapter = mock()
        Logger.addAdapter(loggingAdapter)
        Logger.setIsDebug(false)

        engagementConfigRepository = mock()
        queueRepository = mock()
        isMessagingAvailableUseCase = mock()

        testScheduler = TestScheduler()
        val schedulers = mock<Schedulers>()
        whenever(schedulers.mainScheduler) doReturn testScheduler
        whenever(schedulers.computationScheduler) doReturn testScheduler

        useCase = GetAvailableQueueIdsForSecureMessagingUseCase(
            engagementConfigRepository,
            queueRepository,
            isMessagingAvailableUseCase,
            schedulers
        )
    }

    @Test
    fun `invoke() returns queue IDs when isMessagingAvailableUseCase returns true`() {
        whenever(engagementConfigRepository.queueIds) doReturn queueIds
        whenever(queueRepository.queues) doReturn Single.just(queues.toTypedArray())
        whenever(isMessagingAvailableUseCase(queues.toList())) doReturn true

        var isCallbackInvoked = false
        useCase { result, error ->
            assertEquals(queueIds, result)
            assertNull(error)

            isCallbackInvoked = true
        }

        testScheduler.triggerActions()

        assertTrue(isCallbackInvoked)

        verify(loggingAdapter).log(
            LogLevel.INFO,
            "Glia Widgets: GetAvailableQueueIdsForSecureMessagingUseCase",
            "Secure Messaging is available in queues with IDs: $queueIds.",
            emptyMap()
        )
    }

    @Test
    fun `invoke() returns null when isMessagingAvailableUseCase returns false`() {
        val queueIds = listOf(queue1.id, "d9470299-030d-45d2-b21c-bcf6ad8e4415")
        whenever(engagementConfigRepository.queueIds) doReturn queueIds
        whenever(queueRepository.queues) doReturn Single.just(queues.toTypedArray())
        whenever(isMessagingAvailableUseCase(listOf(queue1))) doReturn false

        var isCallbackInvoked = false
        useCase { result, error ->
            assertNull(result)
            assertNull(error)

            isCallbackInvoked = true
        }

        testScheduler.triggerActions()

        assertTrue(isCallbackInvoked)

        verify(loggingAdapter).log(
            LogLevel.WARN,
            "Glia Widgets: GetAvailableQueueIdsForSecureMessagingUseCase",
            "Provided queue IDs do not match with queues that have status other than closed and support messaging.",
            emptyMap()
        )
    }

    @Test
    fun `invoke() returns default queue IDs when configured queues list is empty`() {
        whenever(engagementConfigRepository.queueIds) doReturn emptyList()
        whenever(queueRepository.queues) doReturn Single.just(queues.toTypedArray())
        whenever(isMessagingAvailableUseCase(defaultQueues)) doReturn true

        var isCallbackInvoked = false
        useCase { result, error ->
            assertEquals(defaultQueuesIds, result)
            assertNull(error)

            isCallbackInvoked = true
        }

        testScheduler.triggerActions()

        assertTrue(isCallbackInvoked)

        verify(loggingAdapter, never()).log(
            LogLevel.WARN,
            "Glia Widgets: GetAvailableQueueIdsForSecureMessagingUseCase",
            "Provided queue IDs do not match with any queue.",
            emptyMap()
        )

        verify(loggingAdapter).log(
            LogLevel.INFO,
            "Glia Widgets: GetAvailableQueueIdsForSecureMessagingUseCase",
            "Secure Messaging is available using queues that are set as **Default**.",
            emptyMap()
        )
    }

    @Test
    fun `invoke() returns default queue IDs when configured queues list not match with site queues`() {
        whenever(engagementConfigRepository.queueIds) doReturn listOf("6d0d7251-1ef5-4741-88cf-c1dec9563197", "57256db7-1abf-412d-9d84-b0afd47e15b1")
        whenever(queueRepository.queues) doReturn Single.just(queues.toTypedArray())
        whenever(isMessagingAvailableUseCase(defaultQueues)) doReturn true

        var isCallbackInvoked = false
        useCase { result, error ->
            assertEquals(defaultQueuesIds, result)
            assertNull(error)

            isCallbackInvoked = true
        }

        testScheduler.triggerActions()

        assertTrue(isCallbackInvoked)

        verify(loggingAdapter).log(
            LogLevel.WARN,
            "Glia Widgets: GetAvailableQueueIdsForSecureMessagingUseCase",
            "Provided queue IDs do not match with any queue.",
            emptyMap()
        )

        verify(loggingAdapter).log(
            LogLevel.INFO,
            "Glia Widgets: GetAvailableQueueIdsForSecureMessagingUseCase",
            "Secure Messaging is available using queues that are set as **Default**.",
            emptyMap()
        )
    }

    @Test
    fun `invoke() returns null when default queue IDs not available for messaging`() {
        whenever(engagementConfigRepository.queueIds) doReturn emptyList()
        whenever(queueRepository.queues) doReturn Single.just(queues.toTypedArray())
        whenever(isMessagingAvailableUseCase(defaultQueues)) doReturn false

        var isCallbackInvoked = false
        useCase { result, error ->
            assertNull(result)
            assertNull(error)

            isCallbackInvoked = true
        }

        testScheduler.triggerActions()

        assertTrue(isCallbackInvoked)

        verify(loggingAdapter).log(
            LogLevel.WARN,
            "Glia Widgets: GetAvailableQueueIdsForSecureMessagingUseCase",
            "No default queues that have status other than closed and support messaging were found.",
            emptyMap()
        )
    }

    @Test
    fun `invoke() returns error when get queues returns error`() {
        val exception = Exception("Error")
        whenever(queueRepository.queues) doReturn Single.error(exception)

        var isCallbackInvoked = false
        useCase { result, error ->
            assertNull(result)
            assertNotNull(error)

            isCallbackInvoked = true
        }

        testScheduler.triggerActions()

        assertTrue(isCallbackInvoked)

        verify(loggingAdapter, never()).log(any(), any(), any(), any())
    }

    @SuppressLint("CheckResult")
    @Test
    fun `invoke() logs invalid IDs when some queue IDs are invalid`() {
        val queuesIds = listOf("invalidId1", "f8dcaccb-1874-4c1c-ba26-79eee699578c", "invalidId2")
        whenever(engagementConfigRepository.queueIds) doReturn queuesIds
        whenever(queueRepository.queues) doReturn Single.just(queues.toTypedArray())
        whenever(isMessagingAvailableUseCase(any())) doReturn false

        useCase().test()

        testScheduler.triggerActions()

        verify(loggingAdapter).log(
            LogLevel.WARN,
            "Glia Widgets: GetAvailableQueueIdsForSecureMessagingUseCase",
            "Queue ID array for Secure Messaging contains invalid queue IDs: [invalidId1, invalidId2].",
            emptyMap()
        )
    }
}
