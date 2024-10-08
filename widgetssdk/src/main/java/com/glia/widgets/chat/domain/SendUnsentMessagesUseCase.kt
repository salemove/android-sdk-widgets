package com.glia.widgets.chat.domain

import com.glia.androidsdk.GliaException
import com.glia.androidsdk.RequestCallback
import com.glia.androidsdk.chat.VisitorMessage
import com.glia.widgets.chat.data.GliaChatRepository
import com.glia.widgets.chat.model.SendMessagePayload
import com.glia.widgets.chat.model.Unsent
import com.glia.widgets.core.secureconversations.SecureConversationsRepository
import com.glia.widgets.core.secureconversations.domain.IsSecureEngagementUseCase


internal class SendUnsentMessagesUseCase(
    private val chatRepository: GliaChatRepository,
    private val secureConversationsRepository: SecureConversationsRepository,
    private val isSecureEngagementUseCase: IsSecureEngagementUseCase
) {
    operator fun invoke(
        message: Unsent,
        onSuccess: (VisitorMessage) -> Unit,
        onFailure: (ex: GliaException) -> Unit
    ) {
        sendMessage(message.payload) { response, exception ->
            if (exception != null) {
                onFailure(exception)
            }
            if (response != null) {
                onSuccess(response)
            }
        }
    }

    private fun sendMessage(payload: SendMessagePayload, callback: RequestCallback<VisitorMessage?>) {
        if (isSecureEngagementUseCase()) {
            secureConversationsRepository.send(payload, callback)
        } else {
            chatRepository.sendMessage(payload, callback)
        }
    }
}
