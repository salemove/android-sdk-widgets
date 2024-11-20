package com.glia.widgets.core.secureconversations.domain

import com.glia.androidsdk.GliaException
import com.glia.widgets.chat.domain.IsAuthenticatedUseCase
import com.glia.widgets.core.secureconversations.SecureConversationsRepository
import com.glia.widgets.di.GliaCore
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.plugins.RxJavaPlugins
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates

class GetUnreadMessagesCountWithTimeoutUseCaseTest {
    private var repository: SecureConversationsRepository by Delegates.notNull()
    private var useCase: GetUnreadMessagesCountWithTimeoutUseCase by Delegates.notNull()
    private var isAuthenticatedUseCase: IsAuthenticatedUseCase by Delegates.notNull()
    private var core: GliaCore by Delegates.notNull()

    @Before
    fun setUp() {
        RxJavaPlugins.reset()
        repository = mock()
        isAuthenticatedUseCase = mock()
        core = mock()
        useCase = GetUnreadMessagesCountWithTimeoutUseCase(repository, isAuthenticatedUseCase, core)
    }

    @Test
    fun `invoke returns NO_UNREAD_MESSAGES when GliaCore is not initialized`() {
        whenever(core.isInitialized).thenReturn(false)

        useCase().test().assertComplete().assertValue(NO_UNREAD_MESSAGES)
    }

    @Test
    fun `invoke returns NO_UNREAD_MESSAGES when user is not authenticated`() {
        whenever(core.isInitialized).thenReturn(true)
        whenever(isAuthenticatedUseCase()).thenReturn(false)

        useCase().test().assertComplete().assertValue(NO_UNREAD_MESSAGES)
    }

    @Test
    fun `invoke returns NO_UNREAD_MESSAGES when there is no answer during timeout`() {
        mockInitializedAndAuthenticated()

        whenever(repository.unreadMessagesCountObservable) doReturn Observable.never()

        useCase().test().awaitDone(TIMEOUT_SEC + 1, TimeUnit.SECONDS).assertComplete().assertValue(NO_UNREAD_MESSAGES)
    }

    @Test
    fun `invoke completes with NO_UNREAD_MESSAGES when error is returned`() {
        mockInitializedAndAuthenticated()
        whenever(repository.unreadMessagesCountObservable) doReturn Observable.error(GliaException("", GliaException.Cause.INTERNAL_ERROR))

        useCase().test().assertComplete().assertValue(NO_UNREAD_MESSAGES)
    }

    @Test
    fun `invoke completes with NO_UNREAD_MESSAGES when count is 0`() {
        mockInitializedAndAuthenticated()

        whenever(repository.unreadMessagesCountObservable) doReturn Observable.just(0)

        useCase().test().assertComplete().assertValue(NO_UNREAD_MESSAGES)
    }

    @Test
    fun `invoke completes with correct messages count when success is called`() {
        mockInitializedAndAuthenticated()

        whenever(repository.unreadMessagesCountObservable) doReturn Observable.just(10)

        useCase().test().assertComplete().assertValue(10)
    }

    private fun mockInitializedAndAuthenticated() {
        whenever(core.isInitialized).thenReturn(true)
        whenever(isAuthenticatedUseCase()).thenReturn(true)
    }
}
