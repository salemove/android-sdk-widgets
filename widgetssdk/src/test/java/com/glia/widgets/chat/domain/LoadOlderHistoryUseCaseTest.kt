package com.glia.widgets.chat.domain

import com.glia.androidsdk.GliaException
import com.glia.androidsdk.chat.ChatMessage
import com.glia.widgets.chat.data.GliaChatRepository
import com.glia.widgets.internal.engagement.domain.MapOperatorUseCase
import com.glia.widgets.internal.engagement.domain.model.ChatMessageInternal
import io.reactivex.rxjava3.core.Single
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class LoadOlderHistoryUseCaseTest {

    private lateinit var repository: GliaChatRepository
    private lateinit var mapOperatorUseCase: MapOperatorUseCase
    private lateinit var loadOlderHistoryUseCase: LoadOlderHistoryUseCase
    private lateinit var hasOlderHistoryUseCase: HasOlderHistoryUseCase

    @Before
    fun setUp() {
        repository = mock()
        mapOperatorUseCase = mock()
        loadOlderHistoryUseCase = LoadOlderHistoryUseCase(repository, mapOperatorUseCase)
        hasOlderHistoryUseCase = HasOlderHistoryUseCase(repository)
    }

    @Test
    fun `invoke maps operators and sorts messages oldest first`() {
        val newer = chatMessage(timestamp = 200L)
        val older = chatMessage(timestamp = 100L)
        val newerInternal = ChatMessageInternal(newer)
        val olderInternal = ChatMessageInternal(older)
        stubRepository(listOf(newer, older), null)
        whenever(mapOperatorUseCase(newer)) doReturn Single.just(newerInternal)
        whenever(mapOperatorUseCase(older)) doReturn Single.just(olderInternal)

        val result = loadOlderHistoryUseCase().test()

        result.assertValue(listOf(olderInternal, newerInternal))
    }

    @Test
    fun `invoke emits empty list when there is no older page`() {
        stubRepository(emptyList(), null)

        loadOlderHistoryUseCase().test().assertValue(emptyList())
    }

    @Test
    fun `invoke propagates repository error`() {
        val error = GliaException("expired", GliaException.Cause.INTERNAL_ERROR)
        stubRepository(null, error)

        loadOlderHistoryUseCase().test().assertError(error)
    }

    @Test
    fun `hasOlderHistory delegates to repository`() {
        whenever(repository.hasOlderHistory()) doReturn true
        assertTrue(hasOlderHistoryUseCase())

        whenever(repository.hasOlderHistory()) doReturn false
        assertFalse(hasOlderHistoryUseCase())
    }

    private fun stubRepository(messages: List<ChatMessage>?, error: Throwable?) {
        whenever(repository.loadOlderHistory(any())) doAnswer {
            it.getArgument<GliaChatRepository.HistoryLoadedListener>(0).loaded(messages, error)
        }
    }

    private fun chatMessage(timestamp: Long): ChatMessage = mock<ChatMessage>().also {
        whenever(it.timestamp) doReturn timestamp
        whenever(it.id) doReturn "id-$timestamp"
    }
}
