package com.glia.widgets.core.secureconversations.domain

import com.glia.androidsdk.RequestCallback
import com.glia.androidsdk.chat.VisitorMessage
import com.glia.widgets.core.secureconversations.SecureConversationsRepository

class SendSecureMessageUseCase(
    private val queueId: String,
    private val secureConversationsRepository: SecureConversationsRepository
) {

    fun execute(
        message: String,
        callback: RequestCallback<VisitorMessage?>
    ) {
        val queueIds = arrayOf(queueId)
        secureConversationsRepository.send(message, queueIds, callback)
    }
}
