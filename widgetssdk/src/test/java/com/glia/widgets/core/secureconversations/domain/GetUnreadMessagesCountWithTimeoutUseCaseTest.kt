package com.glia.widgets.core.secureconversations.domain

import com.glia.androidsdk.GliaException
import com.glia.androidsdk.RequestCallback
import com.glia.widgets.core.secureconversations.SecureConversationsRepository
import org.junit.Before
import org.junit.Test
import org.mockito.internal.stubbing.answers.AnswersWithDelay
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.mockito.stubbing.Answer
import kotlin.properties.Delegates

class GetUnreadMessagesCountWithTimeoutUseCaseTest {
    private var repository: SecureConversationsRepository by Delegates.notNull()
    private var useCase: GetUnreadMessagesCountWithTimeoutUseCase by Delegates.notNull()

    @Before
    fun setUp() {
        repository = mock()
        useCase = GetUnreadMessagesCountWithTimeoutUseCase(repository)
    }

    @Test
    fun `invoke returns 0 when there is no answer during timeout`() {
        val answer: Answer<*> = AnswersWithDelay(
            TIMEOUT_SEC * 2_000L
        ) {
            val callback: RequestCallback<Int?> = it.getArgument(0)
            callback.onResult(5, null)
        }
        whenever(repository.getUnreadMessagesCount(any())).doAnswer(answer)

        useCase().test().assertComplete().assertValue(0)
    }

    @Test
    fun `invoke completes with 0 when error is returned`() {
        doAnswer {
            val callback: RequestCallback<Int?> = it.getArgument(0)
            callback.onResult(null, GliaException("", GliaException.Cause.INTERNAL_ERROR))
        }.whenever(repository).getUnreadMessagesCount(any())

        useCase().test().assertComplete().assertValue(0)
    }

    @Test
    fun `invoke completes with 0 when count is null`() {
        doAnswer {
            val callback: RequestCallback<Int?> = it.getArgument(0)
            callback.onResult(null, null)
        }.whenever(repository).getUnreadMessagesCount(any())

        useCase().test().assertComplete().assertValue(0)
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