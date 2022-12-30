package com.glia.widgets.core.secureconversations.domain

import com.glia.androidsdk.RequestCallback
import com.glia.androidsdk.chat.ChatMessage
import com.glia.widgets.core.secureconversations.SecureConversationsRepository

class FetchChatTranscriptUseCase(
    private val secureConversationsRepository: SecureConversationsRepository
) {

    fun execute(
        callback: RequestCallback<Array<ChatMessage>>
    ) {
        secureConversationsRepository.fetchChatTranscript(callback)
    }
}
