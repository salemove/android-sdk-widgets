package com.glia.widgets.chat.data

import com.glia.androidsdk.Engagement
import com.glia.androidsdk.Glia
import com.glia.androidsdk.GliaException
import com.glia.androidsdk.chat.Chat
import com.glia.androidsdk.chat.ChatMessage
import com.glia.androidsdk.chat.SingleChoiceAttachment
import com.glia.widgets.di.GliaCore
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
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
    fun `loadHistory delegates to Core with the same callbacks`() {
        val onSuccess: (List<ChatMessage>) -> Unit = mockk(relaxed = true)
        val onError: (GliaException) -> Unit = mockk(relaxed = true)

        repository.loadHistory(onSuccess, onError)

        verify { gliaCore.getChatHistory(onSuccess, onError) }
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
    fun `sendMessage delegates to the current engagement chat`() {
        val onSuccess: () -> Unit = mockk(relaxed = true)
        val onFailure: (GliaException) -> Unit = mockk(relaxed = true)
        every { gliaCore.currentEngagement } returns Optional.of(engagement)

        repository.sendMessage("content", MESSAGE_ID, onSuccess, onFailure)

        verify { chat.sendMessage("content", MESSAGE_ID, onSuccess, onFailure) }
    }

    @Test
    fun `sendMessage does nothing when there is no current engagement`() {
        every { gliaCore.currentEngagement } returns Optional.empty()

        repository.sendMessage("content", MESSAGE_ID, {}, {})

        verify(inverse = true) { chat.sendMessage(any(), any(), any(), any()) }
    }

    @Test
    fun `sendSingleChoiceAttachment delegates to the current engagement chat`() {
        val attachment: SingleChoiceAttachment = mockk()
        val onSuccess: () -> Unit = mockk(relaxed = true)
        val onFailure: (GliaException) -> Unit = mockk(relaxed = true)
        every { gliaCore.currentEngagement } returns Optional.of(engagement)

        repository.sendSingleChoiceAttachment(attachment, MESSAGE_ID, onSuccess, onFailure)

        verify { chat.sendSingleChoiceAttachment(attachment, MESSAGE_ID, onSuccess, onFailure) }
    }

    @Test
    fun `sendFileAttachment delegates to the current engagement chat`() {
        val onSuccess: () -> Unit = mockk(relaxed = true)
        val onFailure: (GliaException) -> Unit = mockk(relaxed = true)
        every { gliaCore.currentEngagement } returns Optional.of(engagement)

        repository.sendFileAttachment(FILE_ID, onSuccess, onFailure)

        verify { chat.sendFileAttachment(FILE_ID, onSuccess, onFailure) }
    }

    private companion object {
        const val MESSAGE_ID = "message-id"
        const val FILE_ID = "file-id"
    }
}
