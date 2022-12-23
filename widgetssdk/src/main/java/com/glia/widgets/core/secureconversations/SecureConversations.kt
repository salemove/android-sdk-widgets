package com.glia.widgets.core.secureconversations

import com.glia.androidsdk.RequestCallback
import com.glia.androidsdk.chat.ChatMessage
import com.glia.androidsdk.secureconversations.SecureConversations
import com.glia.androidsdk.chat.VisitorMessage

/**
 * Wrapper class for {@link com.glia.androidsdk.secureconversations.SecureConversations}
 * It's purpose is to execute possible Widgets-specific code
 */
class SecureConversations(private val secureConversations: SecureConversations) : SecureConversations {
    override fun fetchChatTranscript(callback: RequestCallback<Array<ChatMessage>>?) {
        secureConversations.fetchChatTranscript(callback)
    }

    override fun send(
        message: String,
        queueIds: Array<String>,
        callback: RequestCallback<VisitorMessage>
    ) {
        secureConversations.send(message, queueIds, callback)
    }

    override fun markMessagesRead(callback: RequestCallback<Void>?) {
        secureConversations.markMessagesRead(callback)
    }
}
