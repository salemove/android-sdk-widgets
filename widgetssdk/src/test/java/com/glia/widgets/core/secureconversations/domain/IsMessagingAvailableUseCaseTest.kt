package com.glia.widgets.core.secureconversations.domain

import android.COMMON_EXTENSIONS_CLASS_PATH
import com.glia.androidsdk.queuing.Queue
import com.glia.androidsdk.queuing.QueueState
import com.glia.widgets.core.queue.QueueRepository
import com.glia.widgets.core.queue.QueuesState
import com.glia.widgets.helper.supportMessaging
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.subscribers.TestSubscriber
import org.junit.After
import org.junit.Before
import org.junit.Test

class IsMessagingAvailableUseCaseTest {

    private lateinit var queueRepository: QueueRepository
    private lateinit var isMessagingAvailableUseCase: IsMessagingAvailableUseCase

    @Before
    fun setUp() {
        queueRepository = mockk()
        isMessagingAvailableUseCase = IsMessagingAvailableUseCase(queueRepository)
        mockkStatic(COMMON_EXTENSIONS_CLASS_PATH)
    }

    @After
    fun tearDown() {
        unmockkStatic(COMMON_EXTENSIONS_CLASS_PATH)
    }

    @Test
    fun `invoke returns true when queue with messaging exists`() {
        val messagingQueue = createQueueWithStatus(QueueState.Status.OPEN, true)
        val queuesState = QueuesState.Queues(listOf(messagingQueue))
        every { queueRepository.queuesState } returns Flowable.just(queuesState)

        val testSubscriber = TestSubscriber<Result<Boolean>>()
        isMessagingAvailableUseCase().subscribe(testSubscriber)

        testSubscriber.assertValue(Result.success(true))
    }

    @Test
    fun `invoke returns false when no queue with messaging exists`() {
        val nonMessagingQueue = createQueueWithStatus(QueueState.Status.OPEN, false)
        val queuesState = QueuesState.Queues(listOf(nonMessagingQueue))
        every { queueRepository.queuesState } returns Flowable.just(queuesState)

        val testSubscriber = TestSubscriber<Result<Boolean>>()
        isMessagingAvailableUseCase().subscribe(testSubscriber)

        testSubscriber.assertValue(Result.success(false))
    }

    @Test
    fun `invoke returns false when queue state is closed`() {
        val closedQueue = createQueueWithStatus(QueueState.Status.CLOSED, true)
        val queuesState = QueuesState.Queues(listOf(closedQueue))
        every { queueRepository.queuesState } returns Flowable.just(queuesState)

        val testSubscriber = TestSubscriber<Result<Boolean>>()
        isMessagingAvailableUseCase().subscribe(testSubscriber)

        testSubscriber.assertValue(Result.success(false))
    }

    @Test
    fun `invoke returns false when queue state is unknown`() {
        val unknownQueue = createQueueWithStatus(QueueState.Status.UNKNOWN, true)
        val queuesState = QueuesState.Queues(listOf(unknownQueue))
        every { queueRepository.queuesState } returns Flowable.just(queuesState)

        val testSubscriber = TestSubscriber<Result<Boolean>>()
        isMessagingAvailableUseCase().subscribe(testSubscriber)

        testSubscriber.assertValue(Result.success(false))
    }

    @Test
    fun `invoke returns error when queue state is error`() {
        val errorState = QueuesState.Error(Throwable("Error"))
        every { queueRepository.queuesState } returns Flowable.just(errorState)

        val testSubscriber = TestSubscriber<Result<Boolean>>()
        isMessagingAvailableUseCase().subscribe(testSubscriber)

        testSubscriber.assertValue { it.isFailure }
    }

    private fun createQueueWithStatus(status: QueueState.Status, supportsMessaging: Boolean): Queue {
        val queue = mockk<Queue>()
        every { queue.state.status } returns status
        every { any<Queue>().supportMessaging() } returns supportsMessaging
        return queue
    }
}
