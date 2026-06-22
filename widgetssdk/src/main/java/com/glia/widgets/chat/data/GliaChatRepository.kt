package com.glia.widgets.chat.data

import com.glia.androidsdk.Glia
import com.glia.androidsdk.GliaException
import com.glia.androidsdk.RequestCallback
import com.glia.androidsdk.chat.ChatMessage
import com.glia.androidsdk.chat.SendMessagePayload
import com.glia.androidsdk.chat.VisitorMessage
import com.glia.widgets.chat.domain.GliaSendMessageUseCase.Listener
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
        gliaCore.currentEngagement.ifPresent { it.chat.sendMessagePreview(message) }
    }

    fun sendMessage(payload: SendMessagePayload, callback: RequestCallback<VisitorMessage?>) {
        gliaCore.currentEngagement.ifPresent { engagement ->
            engagement.chat.sendMessage(payload) { visitorMessage, ex -> callback.onResult(visitorMessage, ex) }
        }
    }

    fun sendMessage(payload: SendMessagePayload, listener: Listener) {
        sendMessage(payload) { visitorMessage, ex -> onMessageReceived(visitorMessage, ex, listener, payload.messageId) }
    }

    private fun onMessageReceived(
        visitorMessage: VisitorMessage?,
        ex: GliaException?,
        listener: Listener?,
        messageId: String
    ) {
        if (listener != null) {
            if (ex != null) {
                listener.error(ex, messageId)
            } else {
                listener.messageSent(visitorMessage)
            }
        }
    }
}
