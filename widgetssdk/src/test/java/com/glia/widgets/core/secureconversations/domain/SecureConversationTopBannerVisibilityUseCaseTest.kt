package com.glia.widgets.core.secureconversations.domain

import android.assertCurrentValue
import com.glia.androidsdk.Engagement
import com.glia.androidsdk.queuing.QueueState
import com.glia.widgets.core.queue.Queue
import com.glia.widgets.core.queue.QueueRepository
import com.glia.widgets.core.queue.QueuesState
import io.reactivex.rxjava3.android.plugins.RxAndroidPlugins
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.schedulers.Schedulers
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever

class SecureConversationTopBannerVisibilityUseCaseTest {

    private lateinit var queueRepository: QueueRepository
    private lateinit var manageSecureMessagingStatusUseCase: ManageSecureMessagingStatusUseCase
    private lateinit var shouldShowTopBannerUseCase: SecureConversationTopBannerVisibilityUseCase

    @Before
    fun setUp() {
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }
        queueRepository = mock()
        manageSecureMessagingStatusUseCase = mock()
        shouldShowTopBannerUseCase = SecureConversationTopBannerVisibilityUseCase(queueRepository, manageSecureMessagingStatusUseCase)
    }

    @After
    fun tearDown() {
        RxAndroidPlugins.reset()
    }

    @Test
    fun `returns false when state is 'empty'`() {
        whenever(manageSecureMessagingStatusUseCase.shouldBehaveAsSecureMessaging) doReturn true
        whenever(queueRepository.queuesState).thenReturn(Flowable.just(QueuesState.Empty))

        shouldShowTopBannerUseCase().test()
            .assertNoErrors()
            .assertValueCount(1)
            .assertCurrentValue(false)
    }

    @Test
    fun `returns false when state is 'Error'`() {
        whenever(manageSecureMessagingStatusUseCase.shouldBehaveAsSecureMessaging) doReturn true
        whenever(queueRepository.queuesState).thenReturn(Flowable.just(QueuesState.Error(Exception())))

        shouldShowTopBannerUseCase().test()
            .assertNoErrors()
            .assertValueCount(1)
            .assertCurrentValue(false)
    }

    @Test
    fun `returns false when state is 'queues' but list is empty`() {
        scheduleIncomingQueues()

        shouldShowTopBannerUseCase().test()
            .assertNoErrors()
            .assertValueCount(1)
            .assertCurrentValue(false)
    }

    @Test
    fun `returns false when queue list has no live media in open queue`() {
        scheduleIncomingQueues(
            newQueue(
                status = QueueState.Status.OPEN,
                media = listOf(Engagement.MediaType.MESSAGING)
            ),
            newQueue(
                status = QueueState.Status.OPEN,
                media = listOf(Engagement.MediaType.UNKNOWN)
            )
        )

        shouldShowTopBannerUseCase.invoke().test()
            .assertNoErrors()
            .assertValueCount(1)
            .assertCurrentValue(false)
    }

    @Test
    fun `returns false when queue list has no open queue`() {
        scheduleIncomingQueues(
            newQueue(
                status = QueueState.Status.UNSTAFFED,
                media = listOf(Engagement.MediaType.TEXT)
            ),
            newQueue(
                status = QueueState.Status.CLOSED,
                media = listOf(Engagement.MediaType.TEXT)
            ),
            newQueue(
                status = QueueState.Status.FULL,
                media = listOf(Engagement.MediaType.TEXT)
            )
        )

        shouldShowTopBannerUseCase.invoke().test()
            .assertNoErrors()
            .assertValueCount(1)
            .assertCurrentValue(false)
    }

    @Test
    fun `returns true when queue has open queue with text media`() {
        scheduleIncomingQueues(
            newQueue(
                status = QueueState.Status.OPEN,
                media = listOf(Engagement.MediaType.TEXT)
            )
        )

        shouldShowTopBannerUseCase.invoke().test()
            .assertNoErrors()
            .assertValueCount(1)
            .assertCurrentValue(true)
    }

    @Test
    fun `returns true when queue has open queue with audio media`() {
        scheduleIncomingQueues(
            newQueue(
                status = QueueState.Status.OPEN,
                media = listOf(Engagement.MediaType.AUDIO)
            )
        )

        shouldShowTopBannerUseCase.invoke().test()
            .assertNoErrors()
            .assertValueCount(1)
            .assertCurrentValue(true)
    }

    @Test
    fun `returns true when queue has open queue with video media`() {
        scheduleIncomingQueues(
            newQueue(
                status = QueueState.Status.OPEN,
                media = listOf(Engagement.MediaType.VIDEO)
            )
        )

        shouldShowTopBannerUseCase.invoke().test()
            .assertNoErrors()
            .assertValueCount(1)
            .assertCurrentValue(true)
    }

    private fun scheduleIncomingQueues(vararg queueList: Queue) {
        whenever(manageSecureMessagingStatusUseCase.shouldBehaveAsSecureMessaging) doReturn true
        whenever(queueRepository.queuesState) doReturn Flowable.just(QueuesState.Queues(queueList.toList()))
    }

    private fun newQueue(status: QueueState.Status, media: List<Engagement.MediaType>): Queue {
        return Queue("id", "name", false, System.currentTimeMillis(), media, status)
    }
}
