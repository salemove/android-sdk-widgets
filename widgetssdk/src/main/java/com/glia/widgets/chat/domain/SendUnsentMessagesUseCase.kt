package com.glia.widgets.chat.domain

import com.glia.androidsdk.GliaException
import com.glia.widgets.chat.data.GliaChatRepository
import com.glia.widgets.chat.model.OutgoingMessage
import com.glia.widgets.internal.secureconversations.SecureConversationsRepository
import com.glia.widgets.internal.secureconversations.domain.ManageSecureMessagingStatusUseCase

/**
 * Use case for re-sending an [OutgoingMessage] that could not be delivered earlier
 * (operator was offline or a previous attempt failed).
 *
 * Routes the message to the secure messaging or live chat endpoint matching the current
 * engagement state and reports completion through the success/failure callbacks.
 */
internal class SendUnsentMessagesUseCase(
    private val chatRepository: GliaChatRepository,
    private val secureConversationsRepository: SecureConversationsRepository,
    private val shouldUseSecureMessagingApis: ManageSecureMessagingStatusUseCase
) {
    operator fun invoke(payload: OutgoingMessage, onSuccess: () -> Unit, onFailure: (ex: GliaException) -> Unit) {
        if (shouldUseSecureMessagingApis.shouldUseSecureMessagingEndpoints) {
            sendScMessage(payload, onSuccess, onFailure)
        } else {
            sendMessage(payload, onSuccess, onFailure)
        }
    }

    private fun sendMessage(payload: OutgoingMessage, onSuccess: () -> Unit, onFailure: (ex: GliaException) -> Unit) {
        when (payload) {
            is OutgoingMessage.File -> chatRepository.sendFileAttachment(payload.fileId, onSuccess, onFailure)
            is OutgoingMessage.SingleChoice -> chatRepository.sendSingleChoiceAttachment(payload.attachment, payload.messageId, onSuccess, onFailure)
            is OutgoingMessage.Text -> chatRepository.sendMessage(payload.content, payload.messageId, onSuccess, onFailure)
        }
    }

    private fun sendScMessage(payload: OutgoingMessage, onSuccess: () -> Unit, onFailure: (ex: GliaException) -> Unit) {
        when (payload) {
            is OutgoingMessage.File -> secureConversationsRepository.sendFileAttachment(payload.fileId, onSuccess, onFailure)
            is OutgoingMessage.SingleChoice -> secureConversationsRepository.sendSingleChoiceAttachment(
                payload.attachment,
                payload.messageId,
                onSuccess,
                onFailure
            )

            is OutgoingMessage.Text -> secureConversationsRepository.sendMessage(payload.content, payload.messageId, onSuccess, onFailure)
        }
    }
}
