package com.glia.widgets.chat.domain

import com.glia.androidsdk.GliaException
import com.glia.androidsdk.RequestCallback
import com.glia.androidsdk.chat.Chat
import com.glia.androidsdk.chat.ChatMessage
import com.glia.androidsdk.secureconversations.SecureConversations
import com.glia.widgets.chat.data.GliaChatRepository
import com.glia.widgets.di.GliaCore
import com.glia.widgets.engagement.EngagementRepository
import com.glia.widgets.internal.engagement.domain.GetOperatorUseCase
import com.glia.widgets.internal.engagement.domain.MapOperatorUseCase
import com.glia.widgets.internal.engagement.domain.model.ChatHistoryResponse
import com.glia.widgets.internal.engagement.domain.model.ChatMessageInternal
import com.glia.widgets.internal.secureconversations.SecureConversationsRepository
import com.glia.widgets.internal.secureconversations.domain.ManageSecureMessagingStatusUseCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.reactivex.rxjava3.android.plugins.RxAndroidPlugins
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.schedulers.Schedulers
import org.junit.After
import org.junit.Before
import org.junit.Test
import com.glia.widgets.helper.rx.Schedulers as GliaSchedulers

class GliaLoadHistoryUseCaseTest {

    private val gliaCore: GliaCore = mockk()
    private val secureConversations: SecureConversations = mockk(relaxUnitFun = true)
    private val engagementRepository: EngagementRepository = mockk()

    // Strict on purpose: the transcripts hold visitor messages only, so an operator lookup must never run.
    private val getOperatorUseCase: GetOperatorUseCase = mockk()

    private val testSchedulers: GliaSchedulers = object : GliaSchedulers {
        override val computationScheduler: Scheduler = Schedulers.trampoline()
        override val mainScheduler: Scheduler = Schedulers.trampoline()
    }

    private val earlierMessage: ChatMessage = visitorMessage(timestamp = 1)
    private val laterMessage: ChatMessage = visitorMessage(timestamp = 2)

    private lateinit var secureConversationsRepository: SecureConversationsRepository
    private lateinit var useCase: GliaLoadHistoryUseCase

    @Before
    fun setUp() {
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }
        // The init handler is a no-op once another test has already initialized AndroidSchedulers.mainThread().
        RxAndroidPlugins.setMainThreadSchedulerHandler { Schedulers.trampoline() }
        every { gliaCore.secureConversations } returns secureConversations
        secureConversationsRepository = SecureConversationsRepository(gliaCore, mockk(), testSchedulers)
        useCase = GliaLoadHistoryUseCase(
            GliaChatRepository(gliaCore),
            secureConversationsRepository,
            ManageSecureMessagingStatusUseCase(engagementRepository),
            MapOperatorUseCase(getOperatorUseCase)
        )
    }

    @After
    fun tearDown() {
        RxAndroidPlugins.reset()
    }

    @Test
    fun `invoke returns the transcript sorted by timestamp without an unread count for a live engagement`() {
        givenSecureConversation(false)
        givenCoreTranscript(laterMessage, earlierMessage)

        useCase().test().assertResult(
            ChatHistoryResponse(listOf(ChatMessageInternal(earlierMessage), ChatMessageInternal(laterMessage)))
        )
    }

    @Test
    fun `invoke loads a secure conversation transcript from Core and adds the unread messages count`() {
        givenSecureConversation(true)
        givenUnreadMessagesCount(3)
        givenCoreTranscript(laterMessage, earlierMessage)

        useCase().test().assertResult(
            ChatHistoryResponse(listOf(ChatMessageInternal(earlierMessage), ChatMessageInternal(laterMessage)), 3)
        )
        verify(exactly = 1) { gliaCore.getChatHistory(any(), any()) }
    }

    @Test
    fun `invoke fails with the Core error and emits no history`() {
        val exception = GliaException("forbidden", GliaException.Cause.FORBIDDEN)
        givenSecureConversation(false)
        every { gliaCore.getChatHistory(any(), any()) } answers { secondArg<(GliaException) -> Unit>()(exception) }

        useCase().test()
            .assertNoValues()
            .assertError(exception)
    }

    private fun givenSecureConversation(isSecure: Boolean) {
        every { engagementRepository.isSecureMessagingRequested } returns isSecure
        every { engagementRepository.isQueueingOrLiveEngagement } returns !isSecure
        every { engagementRepository.isTransferredSecureConversation } returns false
    }

    private fun givenCoreTranscript(vararg messages: ChatMessage) {
        every { gliaCore.getChatHistory(any(), any()) } answers { firstArg<(List<ChatMessage>) -> Unit>()(messages.toList()) }
    }

    private fun givenUnreadMessagesCount(count: Int) {
        val unreadCountCallback = slot<RequestCallback<Int>>()
        secureConversationsRepository.subscribe()
        verify { secureConversations.subscribeToUnreadMessageCount(capture(unreadCountCallback)) }
        unreadCountCallback.captured.onResult(count, null)
    }

    private fun visitorMessage(timestamp: Long): ChatMessage = mockk {
        every { senderType } returns Chat.Participant.VISITOR
        every { this@mockk.timestamp } returns timestamp
    }
}
