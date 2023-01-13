package com.glia.widgets.core.secureconversations

import com.glia.androidsdk.RequestCallback
import com.glia.androidsdk.chat.ChatMessage
import com.glia.androidsdk.chat.MessageAttachment
import com.glia.androidsdk.chat.VisitorMessage

class SecureConversationsRepository(private val secureConversations: SecureConversations) {
    fun fetchChatTranscript(callback: RequestCallback<Array<ChatMessage>>?) {
        secureConversations.fetchChatTranscript(callback)
    }

    fun send(message: String, queueIds: Array<String>, attachment: MessageAttachment, callback: RequestCallback<VisitorMessage?>) {
        secureConversations.send(message, queueIds, attachment, callback)
    }

    fun send(message: String, queueIds: Array<String>, callback: RequestCallback<VisitorMessage?>) {
        secureConversations.send(message, queueIds, callback)
    }

    fun markMessagesRead(callback: RequestCallback<Void>?) {
        secureConversations.markMessagesRead(callback)
    }
}
