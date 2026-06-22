package com.glia.widgets.chat.data

import com.glia.androidsdk.Engagement
import com.glia.androidsdk.Glia
import com.glia.androidsdk.GliaException
import com.glia.androidsdk.RequestCallback
import com.glia.androidsdk.chat.Chat
import com.glia.androidsdk.chat.ChatMessage
import com.glia.androidsdk.chat.SendMessagePayload
import com.glia.androidsdk.chat.VisitorMessage
import com.glia.widgets.chat.domain.GliaSendMessageUseCase.Listener
import com.glia.widgets.di.GliaCore
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.Optional
import java.util.function.Consumer

class GliaChatRepositoryTest {

    private val gliaCore: GliaCore = mockk(relaxUnitFun = true)
    private val engagement: Engagement = mockk()
    private val chat: Chat = mockk(relaxUnitFun = true)

    private lateinit var repository: GliaChatRepository

    @Before
    fun setUp() {
        every { engagement.chat } returns chat
        repository = GliaChatRepository(gliaCore)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `loadHistory forwards Core result to the listener`() {
        val messages: List<ChatMessage> = listOf(mockk())
        val historyCallbackSlot = slot<RequestCallback<List<ChatMessage>?>>()
        every { gliaCore.getChatHistory(capture(historyCallbackSlot)) } answers {
            historyCallbackSlot.captured.onResult(messages, null)
        }
        val listener: GliaChatRepository.HistoryLoadedListener = mockk(relaxed = true)

        repository.loadHistory(listener)

        verify { listener.loaded(messages, null) }
    }

    @Test
    fun `listenForAllMessages registers the listener for CHAT_MESSAGE events`() {
        val listener = Consumer<ChatMessage> {}

        repository.listenForAllMessages(listener)

        verify { gliaCore.on(Glia.Events.CHAT_MESSAGE, listener) }
    }

    @Test
    fun `unregisterAllMessageListener unregisters the listener from CHAT_MESSAGE events`() {
        val listener = Consumer<ChatMessage> {}

        repository.unregisterAllMessageListener(listener)

        verify { gliaCore.off(Glia.Events.CHAT_MESSAGE, listener) }
    }

    @Test
    fun `sendMessagePreview does nothing when message is null`() {
        repository.sendMessagePreview(null)

        verify(inverse = true) { gliaCore.currentEngagement }
    }

    @Test
    fun `sendMessagePreview sends preview to the current engagement chat`() {
        every { gliaCore.currentEngagement } returns Optional.of(engagement)

        repository.sendMessagePreview("typing...")

        verify { chat.sendMessagePreview("typing...") }
    }

    @Test
    fun `sendMessagePreview does nothing when there is no current engagement`() {
        every { gliaCore.currentEngagement } returns Optional.empty()

        repository.sendMessagePreview("typing...")

        verify(inverse = true) { chat.sendMessagePreview(any()) }
    }

    @Test
    fun `sendMessage with callback forwards Core result to the callback`() {
        val payload: SendMessagePayload = mockk()
        val visitorMessage: VisitorMessage = mockk()
        val callback: RequestCallback<VisitorMessage?> = mockk(relaxed = true)
        every { gliaCore.currentEngagement } returns Optional.of(engagement)
        val sdkCallbackSlot = slot<RequestCallback<VisitorMessage>>()
        every { chat.sendMessage(payload, capture(sdkCallbackSlot)) } answers {
            sdkCallbackSlot.captured.onResult(visitorMessage, null)
        }

        repository.sendMessage(payload, callback)

        verify { callback.onResult(visitorMessage, null) }
    }

    @Test
    fun `sendMessage with listener notifies messageSent on success`() {
        val payload: SendMessagePayload = mockk()
        val visitorMessage: VisitorMessage = mockk()
        val listener: Listener = mockk(relaxed = true)
        every { payload.messageId } returns MESSAGE_ID
        every { gliaCore.currentEngagement } returns Optional.of(engagement)
        val sdkCallbackSlot = slot<RequestCallback<VisitorMessage>>()
        every { chat.sendMessage(payload, capture(sdkCallbackSlot)) } answers {
            sdkCallbackSlot.captured.onResult(visitorMessage, null)
        }

        repository.sendMessage(payload, listener)

        verify { listener.messageSent(visitorMessage) }
        verify(inverse = true) { listener.error(any(), any()) }
    }

    @Test
    fun `sendMessage with listener notifies error with messageId on failure`() {
        val payload: SendMessagePayload = mockk()
        val exception = GliaException("error", GliaException.Cause.INTERNAL_ERROR)
        val listener: Listener = mockk(relaxed = true)
        every { payload.messageId } returns MESSAGE_ID
        every { gliaCore.currentEngagement } returns Optional.of(engagement)
        val sdkCallbackSlot = slot<RequestCallback<VisitorMessage>>()
        every { chat.sendMessage(payload, capture(sdkCallbackSlot)) } answers {
            sdkCallbackSlot.captured.onResult(null, exception)
        }

        repository.sendMessage(payload, listener)

        verify { listener.error(exception, MESSAGE_ID) }
        verify(inverse = true) { listener.messageSent(any()) }
    }

    private companion object {
        const val MESSAGE_ID = "message-id"
    }
}
