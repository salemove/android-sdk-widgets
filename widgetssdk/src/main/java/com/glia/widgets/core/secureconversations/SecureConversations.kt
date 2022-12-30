package com.glia.widgets.core.secureconversations

import android.net.Uri
import com.glia.androidsdk.RequestCallback
import com.glia.androidsdk.chat.ChatMessage
import com.glia.androidsdk.secureconversations.SecureConversations
import com.glia.androidsdk.chat.VisitorMessage
import com.glia.androidsdk.engagement.EngagementFile
import java.io.File

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
        callback: RequestCallback<VisitorMessage?>
    ) {
        secureConversations.send(message, queueIds, callback)
    }

    override fun markMessagesRead(callback: RequestCallback<Void>?) {
        secureConversations.markMessagesRead(callback)
    }

    override fun uploadFile(file: File, callback: RequestCallback<EngagementFile>) {
        secureConversations.uploadFile(file, callback)
    }

    override fun uploadFile(fileUri: Uri, callback: RequestCallback<EngagementFile>) {
        secureConversations.uploadFile(fileUri, callback)
    }
}
