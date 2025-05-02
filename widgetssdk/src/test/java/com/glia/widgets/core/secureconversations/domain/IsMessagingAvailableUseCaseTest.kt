package com.glia.widgets.core.secureconversations.domain

import android.COMMON_EXTENSIONS_CLASS_PATH
import com.glia.widgets.engagement.MediaType
import com.glia.widgets.queue.Queue
import com.glia.widgets.core.queue.QueueRepository
import com.glia.widgets.core.queue.QueuesState
import com.glia.widgets.engagement.EngagementRepository
import com.glia.widgets.engagement.State
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
    private lateinit var engagementRepository: EngagementRepository
    private lateinit var isMessagingAvailableUseCase: IsMessagingAvailableUseCase

    @Before
    fun setUp() {
        queueRepository = mockk(relaxUnitFun = true)
        engagementRepository = mockk(relaxUnitFun = true)
        isMessagingAvailableUseCase = IsMessagingAvailableUseCase(queueRepository, engagementRepository)
        mockkStatic(COMMON_EXTENSIONS_CLASS_PATH)
    }

    @After
    fun tearDown() {
        unmockkStatic(COMMON_EXTENSIONS_CLASS_PATH)
    }

    @Test
    fun `invoke returns true when transferred SC`() {
        val nonMessagingQueue = createQueueWithStatus(Queue.Status.OPEN, false)
        val queuesState = QueuesState.Queues(listOf(nonMessagingQueue))
        every { queueRepository.queuesState } returns Flowable.just(queuesState)
        every { engagementRepository.engagementState } returns Flowable.just(State.TransferredToSecureConversation)

        val testSubscriber = TestSubscriber<Boolean>()
        isMessagingAvailableUseCase().subscribe(testSubscriber)

        testSubscriber.assertValue(true)
    }

    @Test
    fun `invoke returns true when queue with messaging exists`() {
        val messagingQueue = createQueueWithStatus(Queue.Status.OPEN, true)
        val queuesState = QueuesState.Queues(listOf(messagingQueue))
        every { queueRepository.queuesState } returns Flowable.just(queuesState)
        every { engagementRepository.engagementState } returns Flowable.just(State.NoEngagement)

        val testSubscriber = TestSubscriber<Boolean>()
        isMessagingAvailableUseCase().subscribe(testSubscriber)

        testSubscriber.assertValue(true)
    }

    @Test
    fun `invoke returns false when no queue with messaging exists`() {
        val nonMessagingQueue = createQueueWithStatus(Queue.Status.OPEN, false)
        val queuesState = QueuesState.Queues(listOf(nonMessagingQueue))
        every { queueRepository.queuesState } returns Flowable.just(queuesState)
        every { engagementRepository.engagementState } returns Flowable.just(State.NoEngagement)

        val testSubscriber = TestSubscriber<Boolean>()
        isMessagingAvailableUseCase().subscribe(testSubscriber)

        testSubscriber.assertValue(false)
    }

    @Test
    fun `invoke returns false when queue state is closed`() {
        val closedQueue = createQueueWithStatus(Queue.Status.CLOSED, true)
        val queuesState = QueuesState.Queues(listOf(closedQueue))
        every { queueRepository.queuesState } returns Flowable.just(queuesState)
        every { engagementRepository.engagementState } returns Flowable.just(State.NoEngagement)

        val testSubscriber = TestSubscriber<Boolean>()
        isMessagingAvailableUseCase().subscribe(testSubscriber)

        testSubscriber.assertValue(false)
    }

    @Test
    fun `invoke returns false when queue state is unknown`() {
        val unknownQueue = createQueueWithStatus(Queue.Status.UNKNOWN, true)
        val queuesState = QueuesState.Queues(listOf(unknownQueue))
        every { queueRepository.queuesState } returns Flowable.just(queuesState)
        every { engagementRepository.engagementState } returns Flowable.just(State.NoEngagement)

        val testSubscriber = TestSubscriber<Boolean>()
        isMessagingAvailableUseCase().subscribe(testSubscriber)

        testSubscriber.assertValue(false)
    }

    @Test
    fun `invoke returns false when queue state is error`() {
        val errorState = QueuesState.Error(Throwable("Error"))
        every { queueRepository.queuesState } returns Flowable.just(errorState)
        every { engagementRepository.engagementState } returns Flowable.just(State.NoEngagement)

        val testSubscriber = TestSubscriber<Boolean>()
        isMessagingAvailableUseCase().subscribe(testSubscriber)

        testSubscriber.assertValue { !it }
    }

    private fun createQueueWithStatus(status: Queue.Status, supportsMessaging: Boolean): Queue {
        val queue = mockk<Queue>()
        every { queue.status } returns status
        every { queue.medias } returns if (supportsMessaging) listOf(MediaType.MESSAGING) else listOf(
            MediaType.TEXT,
            MediaType.AUDIO
        )
        return queue
    }
}
