package com.glia.widgets.core.secureconversations.domain

import com.glia.androidsdk.GliaException
import com.glia.widgets.chat.domain.IsAuthenticatedUseCase
import com.glia.widgets.core.secureconversations.SecureConversationsRepository
import com.glia.widgets.di.GliaCore
import com.glia.widgets.helper.rx.Schedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.internal.schedulers.TrampolineScheduler
import io.reactivex.rxjava3.plugins.RxJavaPlugins
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates

class ObserveUnreadMessagesCountUseCaseTest {
    private var repository: SecureConversationsRepository by Delegates.notNull()
    private var useCase: ObserveUnreadMessagesCountUseCase by Delegates.notNull()
    private var isAuthenticatedUseCase: IsAuthenticatedUseCase by Delegates.notNull()
    private var core: GliaCore by Delegates.notNull()

    @Before
    fun setUp() {
        RxJavaPlugins.reset()
        repository = mock()
        isAuthenticatedUseCase = mock()
        core = mock()

        val schedulers = mock<Schedulers>()
        whenever(schedulers.computationScheduler).thenReturn(TrampolineScheduler.instance())
        whenever(schedulers.mainScheduler).thenReturn(TrampolineScheduler.instance())

        useCase = ObserveUnreadMessagesCountUseCase(repository, isAuthenticatedUseCase, core, schedulers)
    }

    @Test
    fun `invoke returns NO_UNREAD_MESSAGES when GliaCore is not initialized`() {
        whenever(core.isInitialized).thenReturn(false)

        useCase().test().assertValue(NO_UNREAD_MESSAGES).assertNoErrors()
    }

    @Test
    fun `invoke returns NO_UNREAD_MESSAGES when user is not authenticated`() {
        whenever(core.isInitialized).thenReturn(true)
        whenever(isAuthenticatedUseCase()).thenReturn(false)

        useCase().test().assertValue(NO_UNREAD_MESSAGES).assertNoErrors()
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

        useCase().test().assertValue(NO_UNREAD_MESSAGES).assertNoErrors()
    }

    @Test
    fun `invoke completes with correct messages count when success is called`() {
        mockInitializedAndAuthenticated()

        whenever(repository.unreadMessagesCountObservable) doReturn Observable.just(10)

        useCase().test().assertValue(10).assertNoErrors()
    }

    private fun mockInitializedAndAuthenticated() {
        whenever(core.isInitialized).thenReturn(true)
        whenever(isAuthenticatedUseCase()).thenReturn(true)
    }
}
