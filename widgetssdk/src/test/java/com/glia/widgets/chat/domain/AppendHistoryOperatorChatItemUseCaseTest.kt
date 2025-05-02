package com.glia.widgets.chat.domain

import com.glia.androidsdk.chat.OperatorMessage
import com.glia.widgets.chat.domain.gva.IsGvaUseCase
import com.glia.widgets.internal.engagement.domain.model.ChatMessageInternal
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class AppendHistoryOperatorChatItemUseCaseTest {
    private lateinit var isGvaUseCase: IsGvaUseCase
    private lateinit var customCardAdapterTypeUseCase: CustomCardAdapterTypeUseCase
    private lateinit var appendGvaMessageItemUseCase: AppendGvaMessageItemUseCase
    private lateinit var appendHistoryCustomCardItemUseCase: AppendHistoryCustomCardItemUseCase
    private lateinit var appendHistoryResponseCardOrTextItemUseCase: AppendHistoryResponseCardOrTextItemUseCase
    private lateinit var useCase: AppendHistoryOperatorChatItemUseCase

    private lateinit var chatMessageInternal: ChatMessageInternal

    @Before
    fun setUp() {
        isGvaUseCase = mock()
        customCardAdapterTypeUseCase = mock()
        appendGvaMessageItemUseCase = mock()
        appendHistoryCustomCardItemUseCase = mock()
        appendHistoryResponseCardOrTextItemUseCase = mock()
        chatMessageInternal = mock()

        useCase = AppendHistoryOperatorChatItemUseCase(
            isGvaUseCase,
            customCardAdapterTypeUseCase,
            appendGvaMessageItemUseCase,
            appendHistoryCustomCardItemUseCase,
            appendHistoryResponseCardOrTextItemUseCase
        )
    }

    @Test
    fun `invoke appends GVA message item when chatMessage is GVA`() {
        whenever(chatMessageInternal.chatMessage) doReturn mock<OperatorMessage>()
        whenever(isGvaUseCase.invoke(any())) doReturn true

        useCase(mock(), chatMessageInternal, isLatest = true, showChatHead = true)

        verify(appendGvaMessageItemUseCase).invoke(any(), any(), any())
    }

    @Test
    fun `invoke appends custom card message item when chatMessage is custom card`() {
        whenever(chatMessageInternal.chatMessage) doReturn mock<OperatorMessage>()
        whenever(isGvaUseCase.invoke(any())) doReturn false
        whenever(customCardAdapterTypeUseCase.invoke(any())) doReturn 1

        useCase(mock(), chatMessageInternal, isLatest = true, showChatHead = true)

        verify(appendHistoryCustomCardItemUseCase).invoke(any(), any(), any())
    }

    @Test
    fun `invoke appends response card or text message item when chatMessage is not GVA or CustomCard`() {
        whenever(chatMessageInternal.chatMessage) doReturn mock<OperatorMessage>()
        whenever(isGvaUseCase.invoke(any())) doReturn false
        whenever(customCardAdapterTypeUseCase.invoke(any())) doReturn null

        useCase(mock(), chatMessageInternal, isLatest = true, showChatHead = true)

        verify(appendHistoryResponseCardOrTextItemUseCase).invoke(any(), any(), any(), any())
    }
}
