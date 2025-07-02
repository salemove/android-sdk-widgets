package com.glia.widgets.internal.secureconversations

import com.glia.androidsdk.GliaException
import com.glia.androidsdk.RequestCallback
import com.glia.androidsdk.chat.ChatMessage
import com.glia.androidsdk.chat.SendMessagePayload
import com.glia.androidsdk.chat.VisitorMessage
import com.glia.androidsdk.secureconversations.SecureConversations
import com.glia.widgets.chat.data.GliaChatRepository
import com.glia.widgets.di.GliaCore
import com.glia.widgets.internal.queue.QueueRepository
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.reactivex.rxjava3.android.plugins.RxAndroidPlugins
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock

class SecureConversationsRepositoryTest {

    private lateinit var repository: SecureConversationsRepository

    private val core: GliaCore = mockk()
    private val queueRepository: QueueRepository = mockk(relaxUnitFun = true)
    private val secureConversations: SecureConversations = mockk(relaxUnitFun = true)
    private val unreadMessagesSlot = slot<RequestCallback<Int>>()
    private val pendingSCSlot = slot<RequestCallback<Boolean>>()


    @Before
    fun setUp() {
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }
        every { core.secureConversations } returns secureConversations
        repository = SecureConversationsRepository(core, queueRepository)
        verify(inverse = true) { secureConversations.subscribeToUnreadMessageCount(any()) }
        verify(inverse = true) { secureConversations.subscribeToPendingSecureConversationStatus(any()) }
        repository.unreadMessagesCountObservable.test()
            .assertNotComplete()
            .assertValue(0)
        repository.pendingSecureConversationsStatusObservable.test()
            .assertNotComplete()
            .assertValue(false)

        repository.subscribe()
        verify { core.secureConversations }
        verify { secureConversations.subscribeToUnreadMessageCount(capture(unreadMessagesSlot)) }
        verify { secureConversations.subscribeToPendingSecureConversationStatus(capture(pendingSCSlot)) }
        repository.unreadMessagesCountObservable.test()
            .assertNotComplete()
            .assertValue(0)
        repository.pendingSecureConversationsStatusObservable.test()
            .assertNotComplete()
            .assertValue(false)
    }

    @After
    fun tearDown() {
        RxAndroidPlugins.reset()
        clearAllMocks()
    }

    @Test
    fun `unreadMessagesCountObservable emits value when new value is received`() {
        unreadMessagesSlot.captured.onResult(3, null)
        repository.unreadMessagesCountObservable.test()
            .assertNotComplete()
            .assertValue(3)
    }

    @Test
    fun `unreadMessagesCountObservable emits default value when null received`() {
        unreadMessagesSlot.captured.onResult(null, null)
        repository.unreadMessagesCountObservable.test()
            .assertNotComplete()
            .assertValue(0)
    }

    @Test
    fun `unreadMessagesCountObservable emits default value when error received`() {
        unreadMessagesSlot.captured.onResult(null, mock())
        repository.unreadMessagesCountObservable.test()
            .assertNotComplete()
            .assertValue(0)
    }

    @Test
    fun `pendingSecureConversationsStatusObservable emits value when new value is received`() {
        pendingSCSlot.captured.onResult(true, null)
        repository.pendingSecureConversationsStatusObservable.test()
            .assertNotComplete()
            .assertValue(true)
    }

    @Test
    fun `pendingSecureConversationsStatusObservable emits default value when null received`() {
        pendingSCSlot.captured.onResult(null, null)
        repository.pendingSecureConversationsStatusObservable.test()
            .assertNotComplete()
            .assertValue(false)
    }

    @Test
    fun `pendingSecureConversationsStatusObservable emits default value when error received`() {
        pendingSCSlot.captured.onResult(null, mock())
        repository.pendingSecureConversationsStatusObservable.test()
            .assertNotComplete()
            .assertValue(false)
    }

    @Test
    fun `unsubscribeAndResetData() unsubscribes and emits default values`() {
        `pendingSecureConversationsStatusObservable emits value when new value is received`() // to make sure that the values are not default
        `unreadMessagesCountObservable emits value when new value is received`() // to make sure that the values are not default

        repository.unsubscribeAndResetData()
        verify { secureConversations.unSubscribeFromUnreadMessageCount(any()) }
        verify { secureConversations.unSubscribeFromPendingSecureConversationStatus(any()) }

        repository.pendingSecureConversationsStatusObservable.test()
            .assertNotComplete()
            .assertValue(false)

        repository.unreadMessagesCountObservable.test()
            .assertNotComplete()
            .assertValue(0)
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
}
