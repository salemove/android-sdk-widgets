package com.glia.widgets.core.secureconversations.domain

import com.glia.androidsdk.RequestCallback
import com.glia.widgets.core.secureconversations.SecureConversationsRepository

class MarkMessagesReadUseCase(
    private val secureConversationsRepository: SecureConversationsRepository
) {

    fun execute(
        callback: RequestCallback<Void>
    ) {
        secureConversationsRepository.markMessagesRead(callback)
    }
}
