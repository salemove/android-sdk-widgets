package com.glia.widgets.core.secureconversations.domain

import com.glia.widgets.core.secureconversations.SecureConversationsRepository
import com.glia.widgets.engagement.State
import com.glia.widgets.engagement.domain.EngagementStateUseCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.rxjava3.android.plugins.RxAndroidPlugins
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.plugins.RxJavaPlugins
import io.reactivex.rxjava3.processors.BehaviorProcessor
import io.reactivex.rxjava3.schedulers.Schedulers
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class HasOngoingSecureConversationUseCaseTest {
    private lateinit var repository: SecureConversationsRepository
    private lateinit var useCase: HasOngoingSecureConversationUseCase
    private lateinit var engagementStateUseCase: EngagementStateUseCase

    @Before
    fun setUp() {
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }
        repository = mockk()
        engagementStateUseCase = mockk()
        useCase = HasOngoingSecureConversationUseCase(repository, engagementStateUseCase)
    }

    @After
    fun tearDown() {
        RxJavaPlugins.reset()
        RxAndroidPlugins.reset()
    }

    private fun mockInitialState(pendingSC: Boolean = false, unreadMessagesCount: Int = 0, transferredSC: Boolean = false) {
        every { repository.pendingSecureConversationsStatusObservable } returns Flowable.just(pendingSC)
        every { repository.unreadMessagesCountObservable } returns Flowable.just(unreadMessagesCount)
        every { engagementStateUseCase() } returns Flowable.just(if (transferredSC) State.TransferredToSecureConversation else State.NoEngagement)
    }

    @Test
    fun `invoke returns true when there are pending secure conversations`() {
        mockInitialState(pendingSC = true)

        val result = useCase().blockingLast()
        assertEquals(true, result)
        val callback = mockk<(Boolean) -> Unit>()
        useCase(callback)
        verify(timeout = 1100) { callback.invoke(true) }
    }

    @Test
    fun `invoke returns true when there are positive unread messages count`() {
        mockInitialState(unreadMessagesCount = 3)

        val result = useCase().blockingLast()
        assertEquals(true, result)
        val callback = mockk<(Boolean) -> Unit>()
        useCase(callback)
        verify(timeout = 1100) { callback.invoke(true) }
    }

    @Test
    fun `invoke returns true when there is transferred sc`() {
        mockInitialState(transferredSC = true)

        val result = useCase().blockingLast()
        assertEquals(true, result)
        val callback = mockk<(Boolean) -> Unit>()
        useCase(callback)
        verify(timeout = 1100) { callback.invoke(true) }
    }

    @Test
    fun `invoke returns false when all conditions are false`() {
        mockInitialState()

        val result = useCase().blockingLast()
        assertEquals(false, result)
        val callback = mockk<(Boolean) -> Unit>()
        useCase(callback)
        verify(timeout = 1100) { callback.invoke(false) }
    }

    @Test
    fun `callback emits only once`() {
        val pendingSC = BehaviorProcessor.createDefault(false)

        every { repository.pendingSecureConversationsStatusObservable } returns pendingSC
        every { repository.unreadMessagesCountObservable } returns Flowable.just(0)
        every { engagementStateUseCase() } returns Flowable.just(State.NoEngagement)

        useCase().test().assertValue(false)
        val callback = mockk<(Boolean) -> Unit>()
        useCase(callback)
        verify(timeout = 1100) { callback.invoke(false) }

        pendingSC.onNext(true)
        useCase().test().assertValue(true)

        verify(timeout = 1100, exactly = 1) { callback.invoke(false) }
        verify(timeout = 1100, inverse = true) { callback.invoke(true) }
    }
}
