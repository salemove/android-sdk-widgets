package com.glia.widgets.core.secureconversations.domain

import com.glia.widgets.engagement.MediaType
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

    private fun mockInitialState(pendingSC: Boolean = false, unreadMessagesCount: Int = 0, engagementState: State = State.NoEngagement) {
        every { repository.pendingSecureConversationsStatusObservable } returns Flowable.just(pendingSC)
        every { repository.unreadMessagesCountObservable } returns Flowable.just(unreadMessagesCount)
        every { engagementStateUseCase() } returns Flowable.just(engagementState)
    }

    @Test
    fun `invoke returns true when there are pending secure conversations`() {
        mockInitialState(pendingSC = true)

        val result = useCase().blockingLast()
        assertEquals(true, result)
        val onHasOngoingSecureConversation = mockk<() -> Unit>()
        val onNoOngoingSecureConversation = mockk<() -> Unit>()
        useCase(onHasOngoingSecureConversation, onNoOngoingSecureConversation)
        verify(timeout = 1100) { onHasOngoingSecureConversation() }
    }

    @Test
    fun `invoke returns true when there are positive unread messages count`() {
        mockInitialState(unreadMessagesCount = 3)

        val result = useCase().blockingLast()
        assertEquals(true, result)
        val onHasOngoingSecureConversation = mockk<() -> Unit>()
        val onNoOngoingSecureConversation = mockk<() -> Unit>()
        useCase(onHasOngoingSecureConversation, onNoOngoingSecureConversation)
        verify(timeout = 1100) { onHasOngoingSecureConversation() }
    }

    @Test
    fun `invoke returns true when there is transferred sc`() {
        mockInitialState(engagementState = State.TransferredToSecureConversation)

        val result = useCase().blockingLast()
        assertEquals(true, result)
        val onHasOngoingSecureConversation = mockk<() -> Unit>()
        val onNoOngoingSecureConversation = mockk<() -> Unit>()
        useCase(onHasOngoingSecureConversation, onNoOngoingSecureConversation)
        verify(timeout = 1100) { onHasOngoingSecureConversation() }
    }

    @Test
    fun `invoke returns false when all conditions are false`() {
        mockInitialState()

        val result = useCase().blockingLast()
        assertEquals(false, result)
        val onHasOngoingSecureConversation = mockk<() -> Unit>()
        val onNoOngoingSecureConversation = mockk<() -> Unit>()
        useCase(onHasOngoingSecureConversation, onNoOngoingSecureConversation)
        verify(timeout = 1100) { onNoOngoingSecureConversation() }
    }

    @Test
    fun `invoke returns false when ongoing engagement`() {
        mockInitialState(engagementState = State.EngagementStarted(false), pendingSC = true)

        val result = useCase().blockingLast()
        assertEquals(false, result)
        val onHasOngoingSecureConversation = mockk<() -> Unit>()
        val onNoOngoingSecureConversation = mockk<() -> Unit>()
        useCase(onHasOngoingSecureConversation, onNoOngoingSecureConversation)
        verify(timeout = 1100) { onNoOngoingSecureConversation() }
    }

    @Test
    fun `invoke returns false when enqueueing`() {
        mockInitialState(engagementState = State.PreQueuing(MediaType.AUDIO), unreadMessagesCount = 10)

        val result = useCase().blockingLast()
        assertEquals(false, result)
        val onHasOngoingSecureConversation = mockk<() -> Unit>()
        val onNoOngoingSecureConversation = mockk<() -> Unit>()
        useCase(onHasOngoingSecureConversation, onNoOngoingSecureConversation)
        verify(timeout = 1100) { onNoOngoingSecureConversation() }
    }

    @Test
    fun `callback emits only once`() {
        val pendingSC = BehaviorProcessor.createDefault(false)

        every { repository.pendingSecureConversationsStatusObservable } returns pendingSC
        every { repository.unreadMessagesCountObservable } returns Flowable.just(0)
        every { engagementStateUseCase() } returns Flowable.just(State.NoEngagement)

        useCase().test().assertValue(false)
        val onHasOngoingSecureConversation = mockk<() -> Unit>()
        val onNoOngoingSecureConversation = mockk<() -> Unit>()
        useCase(onHasOngoingSecureConversation, onNoOngoingSecureConversation)
        verify(timeout = 1100) { onNoOngoingSecureConversation() }

        pendingSC.onNext(true)
        useCase().test().assertValue(true)

        verify(timeout = 1100, exactly = 1) { onNoOngoingSecureConversation() }
        verify(timeout = 1100, inverse = true) { onHasOngoingSecureConversation() }
    }
}
