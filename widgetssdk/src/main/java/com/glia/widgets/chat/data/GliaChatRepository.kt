package com.glia.widgets.chat.data

import com.glia.androidsdk.Glia
import com.glia.androidsdk.GliaException
import com.glia.androidsdk.chat.Chat
import com.glia.androidsdk.chat.ChatMessage
import com.glia.androidsdk.chat.SingleChoiceAttachment
import com.glia.widgets.di.GliaCore
import java.util.function.Consumer

/**
 * @hide
 */
internal class GliaChatRepository(private val gliaCore: GliaCore) {

    /**
     * @hide
     */
    fun interface HistoryLoadedListener {
        fun loaded(messages: List<ChatMessage>?, error: Throwable?)
    }

    fun loadHistory(historyLoadedListener: HistoryLoadedListener) {
        gliaCore.getChatHistory { messages, error -> historyLoadedListener.loaded(messages, error) }
    }

    fun listenForAllMessages(listener: Consumer<ChatMessage>) {
        gliaCore.on(Glia.Events.CHAT_MESSAGE, listener)
    }

    fun unregisterAllMessageListener(listener: Consumer<ChatMessage>) {
        gliaCore.off(Glia.Events.CHAT_MESSAGE, listener)
    }

    fun sendMessagePreview(message: String?) {
        message ?: return
        withChat { it.sendMessagePreview(message) }
    }

    fun sendMessage(content: String, messageId: String, onSuccess: () -> Unit, onFailure: (GliaException) -> Unit) {
        withChat { it.sendMessage(content, messageId, onSuccess, onFailure) }
    }

    fun sendSingleChoiceAttachment(
        singleChoiceAttachment: SingleChoiceAttachment,
        messageId: String,
        onSuccess: () -> Unit,
        onFailure: (GliaException) -> Unit
    ) {
        withChat { it.sendSingleChoiceAttachment(singleChoiceAttachment, messageId, onSuccess, onFailure) }
    }

    fun sendFileAttachment(fileId: String, onSuccess: () -> Unit, onFailure: (GliaException) -> Unit) {
        withChat { it.sendFileAttachment(fileId, onSuccess, onFailure) }
    }

    private fun withChat(block: (Chat) -> Unit) {
        gliaCore.currentEngagement.ifPresent { block(it.chat) }
    }
}
