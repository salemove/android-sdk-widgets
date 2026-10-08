package com.glia.widgets.chat.domain

import com.glia.androidsdk.GliaException
import com.glia.androidsdk.chat.ChatHistory
import com.glia.androidsdk.chat.ChatMessage
import com.glia.widgets.chat.data.GliaChatRepository
import com.glia.widgets.internal.engagement.domain.MapOperatorUseCase
import com.glia.widgets.internal.engagement.domain.model.ChatHistoryResponse
import com.glia.widgets.internal.engagement.domain.model.ChatMessageInternal
import io.reactivex.rxjava3.core.Single
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class LoadOlderHistoryUseCaseTest {

    private lateinit var repository: GliaChatRepository
    private lateinit var mapOperatorUseCase: MapOperatorUseCase
    private lateinit var loadOlderHistoryUseCase: LoadOlderHistoryUseCase

    private val requestedPage: ChatHistory.OlderPage = mock()
    private val nextOlderPage: ChatHistory.OlderPage = mock()

    @Before
    fun setUp() {
        repository = mock()
        mapOperatorUseCase = mock()
        loadOlderHistoryUseCase = LoadOlderHistoryUseCase(repository, mapOperatorUseCase)
    }

    @Test
    fun `invoke maps operators, sorts messages oldest first and carries the next key`() {
        val newer = chatMessage(timestamp = 200L)
        val older = chatMessage(timestamp = 100L)
        val newerInternal = ChatMessageInternal(newer)
        val olderInternal = ChatMessageInternal(older)
        stubRepository(chatHistory(listOf(newer, older), nextOlderPage))
        whenever(mapOperatorUseCase(newer)) doReturn Single.just(newerInternal)
        whenever(mapOperatorUseCase(older)) doReturn Single.just(olderInternal)

        loadOlderHistoryUseCase(requestedPage).test()
            .assertValue(ChatHistoryResponse(listOf(olderInternal, newerInternal), olderPage = nextOlderPage))
    }

    @Test
    fun `invoke emits an empty page without a key when Core returns one`() {
        stubRepository(chatHistory(emptyList(), null))

        loadOlderHistoryUseCase(requestedPage).test().assertValue(ChatHistoryResponse(emptyList()))
    }

    @Test
    fun `invoke propagates repository error`() {
        val error = GliaException("expired", GliaException.Cause.INTERNAL_ERROR)
        stubRepository(error = error)

        loadOlderHistoryUseCase(requestedPage).test().assertError(error)
    }

    /** Answers only for [requestedPage], so a wrong key fails the test by never completing. */
    private fun stubRepository(history: ChatHistory? = null, error: GliaException? = null) {
        whenever(repository.loadOlderHistory(eq(requestedPage), any(), any())) doAnswer {
            if (error != null) {
                it.getArgument<(GliaException) -> Unit>(2)(error)
            } else {
                it.getArgument<(ChatHistory) -> Unit>(1)(requireNotNull(history))
            }
        }
    }

    private fun chatHistory(messages: List<ChatMessage>, olderPage: ChatHistory.OlderPage?): ChatHistory = mock {
        on { this.messages } doReturn messages
        on { this.olderPage } doReturn olderPage
    }

    private fun chatMessage(timestamp: Long): ChatMessage = mock<ChatMessage>().also {
        whenever(it.timestamp) doReturn timestamp
        whenever(it.id) doReturn "id-$timestamp"
    }
}
