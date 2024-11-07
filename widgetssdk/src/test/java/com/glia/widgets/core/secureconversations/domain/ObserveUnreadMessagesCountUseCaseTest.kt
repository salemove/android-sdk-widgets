package com.glia.widgets.core.secureconversations.domain

import com.glia.androidsdk.GliaException
import com.glia.androidsdk.RequestCallback
import com.glia.widgets.chat.domain.IsAuthenticatedUseCase
import com.glia.widgets.core.secureconversations.SecureConversationsRepository
import com.glia.widgets.di.GliaCore
import io.reactivex.rxjava3.plugins.RxJavaPlugins
import org.junit.Before
import org.junit.Test
import org.mockito.internal.stubbing.answers.AnswersWithDelay
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.mockito.stubbing.Answer
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
        useCase = ObserveUnreadMessagesCountUseCase(repository, isAuthenticatedUseCase, core)
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

        val answer: Answer<*> = AnswersWithDelay(
            TIMEOUT_SEC * 2_000L
        ) {
            val callback: RequestCallback<Int?> = it.getArgument(0)
            callback.onResult(5, null)
        }
        whenever(repository.getUnreadMessagesCount(any())).doAnswer(answer)

        useCase().test().assertComplete().assertValue(NO_UNREAD_MESSAGES)
    }

    @Test
    fun `invoke completes with NO_UNREAD_MESSAGES when error is returned`() {
        mockInitializedAndAuthenticated()

        doAnswer {
            val callback: RequestCallback<Int?> = it.getArgument(0)
            callback.onResult(null, GliaException("", GliaException.Cause.INTERNAL_ERROR))
        }.whenever(repository).getUnreadMessagesCount(any())

        useCase().test().assertComplete().assertValue(NO_UNREAD_MESSAGES)
    }

    @Test
    fun `invoke completes with NO_UNREAD_MESSAGES when count is null`() {
        mockInitializedAndAuthenticated()

        doAnswer {
            val callback: RequestCallback<Int?> = it.getArgument(0)
            callback.onResult(null, null)
        }.whenever(repository).getUnreadMessagesCount(any())

        useCase().test().assertValue(NO_UNREAD_MESSAGES).assertNoErrors()
    }

    @Test
    fun `invoke completes with NO_UNREAD_MESSAGES when count is 0`() {
        mockInitializedAndAuthenticated()

        doAnswer {
            val callback: RequestCallback<Int?> = it.getArgument(0)
            callback.onResult(0, null)
        }.whenever(repository).getUnreadMessagesCount(any())

        useCase().test().assertValue(NO_UNREAD_MESSAGES).assertNoErrors()
    }

    @Test
    fun `invoke completes with correct messages count when success is called`() {
        mockInitializedAndAuthenticated()

        doAnswer {
            val callback: RequestCallback<Int?> = it.getArgument(0)
            callback.onResult(10, null)
        }.whenever(repository).getUnreadMessagesCount(any())

        useCase().test().assertValue(10).assertNoErrors()
    }

    private fun mockInitializedAndAuthenticated() {
        whenever(core.isInitialized).thenReturn(true)
        whenever(isAuthenticatedUseCase()).thenReturn(true)
    }
}
