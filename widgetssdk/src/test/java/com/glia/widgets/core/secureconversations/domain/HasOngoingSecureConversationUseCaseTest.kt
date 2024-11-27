package com.glia.widgets.core.secureconversations.domain

import com.glia.widgets.chat.domain.IsAuthenticatedUseCase
import com.glia.widgets.core.secureconversations.SecureConversationsRepository
import com.glia.widgets.di.GliaCore
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.rxjava3.android.plugins.RxAndroidPlugins
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.plugins.RxJavaPlugins
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.BehaviorSubject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit

class HasOngoingSecureConversationUseCaseTest {
    private lateinit var repository: SecureConversationsRepository
    private lateinit var useCase: HasOngoingSecureConversationUseCase
    private lateinit var isAuthenticatedUseCase: IsAuthenticatedUseCase
    private lateinit var core: GliaCore

    @Before
    fun setUp() {
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }
        repository = mockk()
        isAuthenticatedUseCase = mockk()
        core = mockk()
        useCase = HasOngoingSecureConversationUseCase(repository, isAuthenticatedUseCase, core)
    }

    @After
    fun tearDown() {
        RxJavaPlugins.reset()
        RxAndroidPlugins.reset()
    }

    @Test
    fun `invoke returns false when GliaCore is not initialized`() {
        every { core.isInitialized } returns false

        val result = useCase().blockingLast()
        assertEquals(false, result)
        val callback = mockk<(Boolean) -> Unit>()
        useCase(callback)
        verify { callback.invoke(false) }
    }

    @Test
    fun `invoke returns false when user is not authenticated`() {
        every { core.isInitialized } returns true
        every { isAuthenticatedUseCase() } returns false

        val result = useCase().blockingLast()
        assertEquals(false, result)
        val callback = mockk<(Boolean) -> Unit>()
        useCase(callback)
        verify { callback.invoke(false) }
    }

    @Test
    fun `invoke returns false when there is no answer during timeout for both attributes`() {
        mockInitializedAndAuthenticated()

        every { repository.pendingSecureConversationsStatusObservable } returns Observable.never()
        every { repository.unreadMessagesCountObservable } returns Observable.never()

        useCase().test().awaitCount(1).assertValue(false)
        val callback = mockk<(Boolean) -> Unit>()
        useCase(callback)
        verify(timeout = 1100) { callback.invoke(false) }
    }

    @Test
    fun `invoke returns false when error is returned for both attributes`() {
        mockInitializedAndAuthenticated()

        every { repository.pendingSecureConversationsStatusObservable } returns Observable.error(Exception("Error"))
        every { repository.unreadMessagesCountObservable } returns Observable.error(Exception("Error"))

        useCase().test().assertValue(false)
        val callback = mockk<(Boolean) -> Unit>()
        useCase(callback)
        verify(timeout = 1100) { callback.invoke(false) }
    }

    @Test
    fun `invoke returns true when there are pending secure conversations`() {
        mockInitializedAndAuthenticated()

        every { repository.pendingSecureConversationsStatusObservable } returns Observable.just(true)
        every { repository.unreadMessagesCountObservable } returns Observable.just(0)

        val result = useCase().blockingLast()
        assertEquals(true, result)
        val callback = mockk<(Boolean) -> Unit>()
        useCase(callback)
        verify(timeout = 1100) { callback.invoke(true) }
    }

    @Test
    fun `invoke returns false due to timeout and then transmits value based on real state of secure conversations`() {
        mockInitializedAndAuthenticated()

        every { repository.pendingSecureConversationsStatusObservable } returns Observable.just(true).delay(2, TimeUnit.SECONDS)
        every { repository.unreadMessagesCountObservable } returns Observable.never()

        useCase().test().awaitCount(2).assertValues(false, true)
    }

    @Test
    fun `invoke returns false due to timeout and then transmits value based on real state of unread messages count`() {
        mockInitializedAndAuthenticated()

        every { repository.pendingSecureConversationsStatusObservable } returns Observable.never()
        every { repository.unreadMessagesCountObservable } returns Observable.just(2).delay(2, TimeUnit.SECONDS)

        useCase().test().awaitCount(2).assertValues(false, true)
    }

    @Test
    fun `invoke returns true when there are positive unread messages count`() {
        mockInitializedAndAuthenticated()

        every { repository.pendingSecureConversationsStatusObservable } returns Observable.just(false)
        every { repository.unreadMessagesCountObservable } returns Observable.just(10)

        val result = useCase().blockingLast()
        assertEquals(true, result)
        val callback = mockk<(Boolean) -> Unit>()
        useCase(callback)
        verify(timeout = 1100) { callback.invoke(true) }
    }

    @Test
    fun `invoke returns false when both conditions are false`() {
        mockInitializedAndAuthenticated()

        every { repository.pendingSecureConversationsStatusObservable } returns Observable.just(false)
        every { repository.unreadMessagesCountObservable } returns Observable.just(0)

        val result = useCase().blockingLast()
        assertEquals(false, result)
        val callback = mockk<(Boolean) -> Unit>()
        useCase(callback)
        verify(timeout = 1100) { callback.invoke(false) }
    }

    @Test
    fun `callback emits only once`() {
        mockInitializedAndAuthenticated()

        val pendingSC = BehaviorSubject.createDefault(false)

        every { repository.pendingSecureConversationsStatusObservable } returns pendingSC
        every { repository.unreadMessagesCountObservable } returns Observable.just(0)

        useCase().test().assertValue(false)
        val callback = mockk<(Boolean) -> Unit>()
        useCase(callback)
        verify(timeout = 1100) { callback.invoke(false) }

        pendingSC.onNext(true)
        useCase().test().assertValue(true)

        verify(timeout = 1100, exactly = 1) { callback.invoke(false) }
        verify(timeout = 1100, inverse = true) { callback.invoke(true) }
    }

    private fun mockInitializedAndAuthenticated() {
        every { core.isInitialized } returns true
        every { isAuthenticatedUseCase() } returns true
    }
}
