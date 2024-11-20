package com.glia.widgets.core.secureconversations

import com.glia.androidsdk.GliaException
import com.glia.androidsdk.RequestCallback
import com.glia.androidsdk.chat.ChatMessage
import com.glia.androidsdk.chat.SendMessagePayload
import com.glia.androidsdk.chat.VisitorMessage
import com.glia.androidsdk.secureconversations.SecureConversations
import com.glia.widgets.chat.data.GliaChatRepository
import com.glia.widgets.core.queue.QueueRepository
import com.glia.widgets.core.secureconversations.domain.NO_UNREAD_MESSAGES
import com.glia.widgets.di.GliaCore
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.reactivex.rxjava3.core.Single
import org.junit.After
import org.junit.Before
import org.junit.Test

class SecureConversationsRepositoryTest {

    private lateinit var repository: SecureConversationsRepository
    private val core: GliaCore = mockk()
    private val queueRepository: QueueRepository = mockk(relaxUnitFun = true)
    private val secureConversations: SecureConversations = mockk(relaxUnitFun = true)

    @Before
    fun setUp() {
        every { core.secureConversations } returns secureConversations
        repository = SecureConversationsRepository(core, queueRepository)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `fetchChatTranscript should call secureConversations fetchChatTranscript`() {
        val listener: GliaChatRepository.HistoryLoadedListener = mockk(relaxed = true)
        val messages: Array<ChatMessage> = arrayOf(mockk())
        val exception: GliaException? = null

        repository.fetchChatTranscript(listener)

        val callbackCapturingSlot = slot<RequestCallback<Array<ChatMessage>>>()
        verify { secureConversations.fetchChatTranscript(capture(callbackCapturingSlot)) }
        callbackCapturingSlot.captured.onResult(messages, exception)
        verify { listener.loaded(messages.toList(), exception) }
    }

    @Test
    fun `send should call secureConversations send with queueIds`() {
        val payload: SendMessagePayload = mockk()
        val callback: RequestCallback<VisitorMessage?> = mockk(relaxed = true)
        val queueIds = listOf("queue1", "queue2")

        every { queueRepository.relevantQueueIds } returns Single.just(queueIds)

        repository.send(payload, callback)

        verify { secureConversations.send(payload, queueIds.toTypedArray(), any()) }
    }

    @Test
    fun `send should call callback with exception when queueIds are empty`() {
        val payload: SendMessagePayload = mockk()
        val callback: RequestCallback<VisitorMessage?> = mockk(relaxed = true)
        val queueIds = emptyList<String>()

        every { queueRepository.relevantQueueIds } returns Single.just(queueIds)

        repository.send(payload, callback)

        verify { callback.onResult(null, any<GliaException>()) }
    }

    @Test
    fun `markMessagesRead should call secureConversations markMessagesRead`() {
        val callback: RequestCallback<Void> = mockk(relaxed = true)

        repository.markMessagesRead(callback)

        verify { secureConversations.markMessagesRead(callback) }
    }

    @Test
    fun `getUnreadMessagesCount should call secureConversations getUnreadMessageCount`() {
        val callback: RequestCallback<Int> = mockk(relaxed = true)

        repository.getUnreadMessagesCount(callback)

        verify { secureConversations.getUnreadMessageCount(callback) }
    }

    @Test
    fun `subscribeToUnreadMessagesCount should call secureConversations subscribeToUnreadMessageCount`() {
        val callback: RequestCallback<Int> = mockk(relaxed = true)

        repository.subscribeToUnreadMessagesCount(callback)

        verify { secureConversations.subscribeToUnreadMessageCount(callback) }
    }

    @Test
    fun `unsubscribeFromUnreadMessagesCount should call secureConversations unSubscribeFromUnreadMessageCount`() {
        val callback: RequestCallback<Int> = mockk(relaxed = true)

        repository.unsubscribeFromUnreadMessagesCount(callback)

        verify { secureConversations.unSubscribeFromUnreadMessageCount(callback) }
    }

    @Test
    fun `unreadMessagesCountObservable should emit values`() {
        val count = 5
        val callbackSlot = slot<RequestCallback<Int>>()

        val testObserver = repository.unreadMessagesCountObservable.test()

        verify { secureConversations.subscribeToUnreadMessageCount(capture(callbackSlot)) }

        callbackSlot.captured.onResult(count, null)

        testObserver.assertValue(count)
    }

    @Test
    fun `unreadMessagesCountObservable should emit error when error is returned`() {
        val callbackSlot = slot<RequestCallback<Int>>()

        val testObserver = repository.unreadMessagesCountObservable.test()

        verify { secureConversations.subscribeToUnreadMessageCount(capture(callbackSlot)) }

        val exception = GliaException("error", GliaException.Cause.INTERNAL_ERROR)
        callbackSlot.captured.onResult(null, exception)

        testObserver.assertError(exception)
    }

    @Test
    fun `unreadMessagesCountObservable should unsubscribe from callback when disposed`() {
        val callbackSlot = slot<RequestCallback<Int>>()

        val testObserver = repository.unreadMessagesCountObservable.test()

        verify { secureConversations.subscribeToUnreadMessageCount(capture(callbackSlot)) }

        callbackSlot.captured.onResult(null, null)

        testObserver.assertValue(NO_UNREAD_MESSAGES).dispose()

        verify { secureConversations.unSubscribeFromUnreadMessageCount(eq(callbackSlot.captured)) }
    }

    @Test
    fun `pendingSecureConversationsStatusObservable should emit values`() {
        val status = true
        val callbackSlot = slot<RequestCallback<Boolean>>()

        val testObserver = repository.pendingSecureConversationsStatusObservable.test()

        verify { secureConversations.subscribeToPendingSecureConversationStatus(capture(callbackSlot)) }

        callbackSlot.captured.onResult(status, null)

        testObserver.assertValue(status)
    }

    @Test
    fun `pendingSecureConversationsStatusObservable should emit error when error is returned`() {
        val callbackSlot = slot<RequestCallback<Boolean>>()

        val testObserver = repository.pendingSecureConversationsStatusObservable.test()

        verify { secureConversations.subscribeToPendingSecureConversationStatus(capture(callbackSlot)) }
        val exception = GliaException("error", GliaException.Cause.INTERNAL_ERROR)
        callbackSlot.captured.onResult(null, exception)

        testObserver.assertError(exception)
    }

    @Test
    fun `pendingSecureConversationsStatusObservable should unsubscribe when disposed`() {
        val callbackSlot = slot<RequestCallback<Boolean>>()

        val testObserver = repository.pendingSecureConversationsStatusObservable.test()

        verify { secureConversations.subscribeToPendingSecureConversationStatus(capture(callbackSlot)) }

        callbackSlot.captured.onResult(null, null)

        testObserver.assertValue(false).dispose()

        verify { secureConversations.unSubscribeFromPendingSecureConversationStatus(eq(callbackSlot.captured)) }
    }
}
