package com.glia.widgets.chat.domain

import com.glia.androidsdk.GliaException
import com.glia.androidsdk.RequestCallback
import com.glia.androidsdk.chat.SendMessagePayload
import com.glia.androidsdk.chat.VisitorMessage
import com.glia.widgets.chat.data.GliaChatRepository
import com.glia.widgets.core.secureconversations.SecureConversationsRepository
import com.glia.widgets.core.secureconversations.domain.ManageSecureMessagingStatusUseCase


internal class SendUnsentMessagesUseCase(
    private val chatRepository: GliaChatRepository,
    private val secureConversationsRepository: SecureConversationsRepository,
    private val shouldUseSecureMessagingApis: ManageSecureMessagingStatusUseCase
) {
    operator fun invoke(
        payload: SendMessagePayload,
        onSuccess: (VisitorMessage) -> Unit,
        onFailure: (ex: GliaException) -> Unit
    ) {
        sendMessage(payload) { response, exception ->
            if (exception != null) {
                onFailure(exception)
            }
            if (response != null) {
                onSuccess(response)
            }
        }
    }

    private fun sendMessage(payload: SendMessagePayload, callback: RequestCallback<VisitorMessage?>) {
        if (shouldUseSecureMessagingApis.shouldUseSecureMessagingEndpoints) {
            secureConversationsRepository.send(payload, callback)
        } else {
            chatRepository.sendMessage(payload, callback)
        }
    }
}
