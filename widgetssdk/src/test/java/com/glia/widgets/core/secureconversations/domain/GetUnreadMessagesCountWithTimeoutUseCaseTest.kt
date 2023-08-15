package com.glia.widgets.core.secureconversations.domain

import com.glia.androidsdk.GliaException
import com.glia.androidsdk.RequestCallback
import com.glia.widgets.core.secureconversations.SecureConversationsRepository
import io.reactivex.plugins.RxJavaPlugins
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.internal.stubbing.answers.AnswersWithDelay
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.mockito.stubbing.Answer
import org.robolectric.RobolectricTestRunner
import kotlin.properties.Delegates

@RunWith(RobolectricTestRunner::class)
class GetUnreadMessagesCountWithTimeoutUseCaseTest {
    private var repository: SecureConversationsRepository by Delegates.notNull()
    private var useCase: GetUnreadMessagesCountWithTimeoutUseCase by Delegates.notNull()

    @Before
    fun setUp() {
        RxJavaPlugins.reset()
        repository = mock()
        useCase = GetUnreadMessagesCountWithTimeoutUseCase(repository)
    }

    @Test
    fun `invoke returns NO_UNREAD_MESSAGES when there is no answer during timeout`() {
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
        doAnswer {
            val callback: RequestCallback<Int?> = it.getArgument(0)
            callback.onResult(null, GliaException("", GliaException.Cause.INTERNAL_ERROR))
        }.whenever(repository).getUnreadMessagesCount(any())

        useCase().test().assertComplete().assertValue(NO_UNREAD_MESSAGES)
    }

    @Test
    fun `invoke completes with NO_UNREAD_MESSAGES when count is null`() {
        doAnswer {
            val callback: RequestCallback<Int?> = it.getArgument(0)
            callback.onResult(null, null)
        }.whenever(repository).getUnreadMessagesCount(any())

        useCase().test().assertComplete().assertValue(NO_UNREAD_MESSAGES)
    }

    @Test
    fun `invoke completes with NO_UNREAD_MESSAGES when count is 0`() {
        doAnswer {
            val callback: RequestCallback<Int?> = it.getArgument(0)
            callback.onResult(0, null)
        }.whenever(repository).getUnreadMessagesCount(any())

        useCase().test().assertComplete().assertValue(NO_UNREAD_MESSAGES)
    }

    @Test
    fun `invoke completes with correct messages count when success is called`() {
        doAnswer {
            val callback: RequestCallback<Int?> = it.getArgument(0)
            callback.onResult(10, null)
        }.whenever(repository).getUnreadMessagesCount(any())

        useCase().test().assertComplete().assertValue(10)
    }
}
