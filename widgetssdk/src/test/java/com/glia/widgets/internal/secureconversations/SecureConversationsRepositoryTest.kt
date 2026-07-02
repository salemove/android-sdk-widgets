package com.glia.widgets.internal.secureconversations

import com.glia.androidsdk.GliaException
import com.glia.androidsdk.RequestCallback
import com.glia.androidsdk.chat.ChatMessage
import com.glia.androidsdk.chat.SingleChoiceAttachment
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
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import com.glia.widgets.helper.rx.Schedulers as GliaSchedulers

class SecureConversationsRepositoryTest {

    private lateinit var repository: SecureConversationsRepository

    private val core: GliaCore = mockk()
    private val queueRepository: QueueRepository = mockk(relaxUnitFun = true)
    private val secureConversations: SecureConversations = mockk(relaxUnitFun = true)
    private val unreadMessagesSlot = slot<RequestCallback<Int>>()
    private val pendingSCSlot = slot<RequestCallback<Boolean>>()


    private val testSchedulers: GliaSchedulers = object : GliaSchedulers {
        override val computationScheduler: Scheduler = Schedulers.trampoline()
        override val mainScheduler: Scheduler = Schedulers.trampoline()
    }

    @Before
    fun setUp() {
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }
        every { core.secureConversations } returns secureConversations
        repository = SecureConversationsRepository(core, queueRepository, testSchedulers)
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
    fun `sendMessage sends through secureConversations with relevant queue ids`() {
        val queueIdsSlot = slot<Array<String>>()
        every { queueRepository.relevantQueueIds } returns Single.just(listOf("queue1", "queue2"))
        every { secureConversations.sendMessage(any(), capture(queueIdsSlot), any(), any(), any()) } returns Unit

        repository.sendMessage("content", MESSAGE_ID, {}, {})

        verify { secureConversations.sendMessage(eq("content"), any(), eq(MESSAGE_ID), any(), any()) }
        assertArrayEquals(arrayOf("queue1", "queue2"), queueIdsSlot.captured)
    }

    @Test
    fun `sendMessage emits sending state and invokes onSuccess when Core reports success`() {
        val onSuccess: () -> Unit = mockk(relaxed = true)
        val sdkOnSuccessSlot = slot<() -> Unit>()
        every { queueRepository.relevantQueueIds } returns Single.just(listOf("queue1"))
        every { secureConversations.sendMessage(any(), any(), any(), capture(sdkOnSuccessSlot), any()) } returns Unit

        val sendingObserver = repository.messageSendingObservable.test()

        repository.sendMessage("content", MESSAGE_ID, onSuccess, {})
        sdkOnSuccessSlot.captured.invoke()

        verify { onSuccess.invoke() }
        sendingObserver.assertValues(false, true, false)
    }

    @Test
    fun `sendMessage invokes onFailure and stops sending state when Core reports failure`() {
        val onFailure: (GliaException) -> Unit = mockk(relaxed = true)
        val exception = GliaException("error", GliaException.Cause.INTERNAL_ERROR)
        val sdkOnFailureSlot = slot<(GliaException) -> Unit>()
        every { queueRepository.relevantQueueIds } returns Single.just(listOf("queue1"))
        every { secureConversations.sendMessage(any(), any(), any(), any(), capture(sdkOnFailureSlot)) } returns Unit

        val sendingObserver = repository.messageSendingObservable.test()

        repository.sendMessage("content", MESSAGE_ID, {}, onFailure)
        sdkOnFailureSlot.captured.invoke(exception)

        verify { onFailure.invoke(exception) }
        sendingObserver.assertValues(false, true, false)
    }

    @Test
    fun `sendMessage invokes onFailure when relevant queue ids are empty`() {
        val onFailure: (GliaException) -> Unit = mockk(relaxed = true)
        every { queueRepository.relevantQueueIds } returns Single.just(emptyList())

        repository.sendMessage("content", MESSAGE_ID, {}, onFailure)

        verify { onFailure.invoke(any()) }
        verify(inverse = true) { secureConversations.sendMessage(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `sendMessageWithAttachments sends content and file ids through secureConversations`() {
        val fileAttachments = listOf("file1", "file2")
        every { queueRepository.relevantQueueIds } returns Single.just(listOf("queue1"))
        every { secureConversations.sendMessageWithAttachments(any(), any(), any(), any(), any(), any()) } returns Unit

        repository.sendMessageWithAttachments("content", fileAttachments, MESSAGE_ID, {}, {})

        verify {
            secureConversations.sendMessageWithAttachments(eq("content"), eq(fileAttachments), any(), eq(MESSAGE_ID), any(), any())
        }
    }

    @Test
    fun `sendSingleChoiceAttachment sends attachment through secureConversations`() {
        val attachment: SingleChoiceAttachment = mockk()
        every { queueRepository.relevantQueueIds } returns Single.just(listOf("queue1"))
        every { secureConversations.sendSingleChoiceAttachment(any(), any(), any(), any(), any()) } returns Unit

        repository.sendSingleChoiceAttachment(attachment, MESSAGE_ID, {}, {})

        verify { secureConversations.sendSingleChoiceAttachment(eq(attachment), any(), eq(MESSAGE_ID), any(), any()) }
    }

    @Test
    fun `sendFileAttachment sends file id through secureConversations`() {
        every { queueRepository.relevantQueueIds } returns Single.just(listOf("queue1"))
        every { secureConversations.sendFileAttachment(any(), any(), any(), any()) } returns Unit

        repository.sendFileAttachment(FILE_ID, {}, {})

        verify { secureConversations.sendFileAttachment(eq(FILE_ID), any(), any(), any()) }
    }

    @Test
    fun `markMessagesRead should call secureConversations markMessagesRead`() {
        val callback: RequestCallback<Void> = mockk(relaxed = true)

        repository.markMessagesRead(callback)

        verify { secureConversations.markMessagesRead(callback) }
    }

    private companion object {
        const val MESSAGE_ID = "message-id"
        const val FILE_ID = "file-id"
    }
}
