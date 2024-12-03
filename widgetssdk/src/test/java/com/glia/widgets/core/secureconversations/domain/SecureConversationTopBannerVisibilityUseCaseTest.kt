package com.glia.widgets.core.secureconversations.domain

import android.assertCurrentValue
import com.glia.androidsdk.Engagement
import com.glia.androidsdk.queuing.Queue
import com.glia.androidsdk.queuing.QueueState
import com.glia.widgets.core.queue.QueueRepository
import com.glia.widgets.core.queue.QueuesState
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.functions.Predicate
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever

class SecureConversationTopBannerVisibilityUseCaseTest {

    private lateinit var queueRepository: QueueRepository
    private lateinit var pendingConversationsUseCase: HasOngoingSecureConversationUseCase
    private lateinit var shouldShowTopBannerUseCase: SecureConversationTopBannerVisibilityUseCase

    @Before
    fun setUp() {
        queueRepository = mock()
        pendingConversationsUseCase = mock()
        shouldShowTopBannerUseCase = SecureConversationTopBannerVisibilityUseCase(queueRepository, pendingConversationsUseCase)
    }

    @Test
    fun `returns error when failed to get pending conversation state`() {
        val error = Throwable("Something went wrong")
        whenever(pendingConversationsUseCase()).thenReturn(Flowable.error(error))

        shouldShowTopBannerUseCase.invoke().test()
            .assertNoErrors()
            .assertValueCount(1)
            .assertCurrentValue(Predicate { it.isFailure && it.exceptionOrNull() == error })
    }

    @Test
    fun `returns error when failed to get queue state`() {
        val error = Throwable("Something went wrong")
        whenever(pendingConversationsUseCase.invoke()).thenReturn(Flowable.just(true))
        whenever(queueRepository.queuesState).thenReturn(Flowable.error(error))

        shouldShowTopBannerUseCase.invoke().test()
            .assertNoErrors()
            .assertValueCount(1)
            .assertCurrentValue(Predicate { it.isFailure && it.exceptionOrNull() == error })
    }

    @Test
    fun `returns false when state is 'empty'`() {
        whenever(pendingConversationsUseCase.invoke()).thenReturn(Flowable.just(true))
        whenever(queueRepository.queuesState).thenReturn(Flowable.just(QueuesState.Empty))

        shouldShowTopBannerUseCase.invoke().test()
            .assertNoErrors()
            .assertValueCount(1)
            .assertCurrentValue(Predicate { it.isSuccess && it.getOrNull() == false })
    }

    @Test
    fun `returns false when state is 'queues' but list is empty`() {
        whenever(pendingConversationsUseCase.invoke()).thenReturn(Flowable.just(true))
        whenever(queueRepository.queuesState).thenReturn(Flowable.just(QueuesState.Queues(emptyList())))

        shouldShowTopBannerUseCase.invoke().test()
            .assertNoErrors()
            .assertValueCount(1)
            .assertCurrentValue(Predicate { it.isSuccess && it.getOrNull() == false })
    }

    @Test
    fun `returns false when queue list has no live media in open queue`() {
        scheduleIncomingQueues(
            newQueue(
                status = QueueState.Status.OPEN,
                media = arrayOf(Engagement.MediaType.MESSAGING)
            ),
            newQueue(
                status = QueueState.Status.OPEN,
                media = arrayOf(Engagement.MediaType.UNKNOWN)
            )
        )

        shouldShowTopBannerUseCase.invoke().test()
            .assertNoErrors()
            .assertValueCount(1)
            .assertCurrentValue(Predicate { it.isSuccess && it.getOrNull() == false })
    }

    @Test
    fun `returns false when queue list has no open queue`() {
        scheduleIncomingQueues(
            newQueue(
                status = QueueState.Status.UNSTAFFED,
                media = arrayOf(Engagement.MediaType.TEXT)
            ),
            newQueue(
                status = QueueState.Status.CLOSED,
                media = arrayOf(Engagement.MediaType.TEXT)
            ),
            newQueue(
                status = QueueState.Status.FULL,
                media = arrayOf(Engagement.MediaType.TEXT)
            )
        )

        shouldShowTopBannerUseCase.invoke().test()
            .assertNoErrors()
            .assertValueCount(1)
            .assertCurrentValue(Predicate { it.isSuccess && it.getOrNull() == false })
    }

    @Test
    fun `returns true when queue has open queue with text media`() {
        scheduleIncomingQueues(
            newQueue(
                status = QueueState.Status.OPEN,
                media = arrayOf(Engagement.MediaType.TEXT)
            )
        )

        shouldShowTopBannerUseCase.invoke().test()
            .assertNoErrors()
            .assertValueCount(1)
            .assertCurrentValue(Predicate { it.isSuccess && it.getOrNull() == true })
    }

    @Test
    fun `returns true when queue has open queue with audio media`() {
        scheduleIncomingQueues(
            newQueue(
                status = QueueState.Status.OPEN,
                media = arrayOf(Engagement.MediaType.AUDIO)
            )
        )

        shouldShowTopBannerUseCase.invoke().test()
            .assertNoErrors()
            .assertValueCount(1)
            .assertCurrentValue(Predicate { it.isSuccess && it.getOrNull() == true })
    }

    @Test
    fun `returns true when queue has open queue with video media`() {
        scheduleIncomingQueues(
            newQueue(
                status = QueueState.Status.OPEN,
                media = arrayOf(Engagement.MediaType.VIDEO)
            )
        )

        shouldShowTopBannerUseCase.invoke().test()
            .assertNoErrors()
            .assertValueCount(1)
            .assertCurrentValue(Predicate { it.isSuccess && it.getOrNull() == true })
    }

    private fun scheduleIncomingQueues(vararg queueList: Queue) {
        whenever(pendingConversationsUseCase.invoke()).thenReturn(Flowable.just(true))
        whenever(queueRepository.queuesState).thenReturn(Flowable.just(QueuesState.Queues(queueList.toList())))
    }

    private fun newQueue(status: QueueState.Status, media: Array<Engagement.MediaType>): Queue {
        return Queue("id", "name", status, media, false)
    }
}
