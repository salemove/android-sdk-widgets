package com.glia.widgets.chat.model

import com.glia.androidsdk.chat.SingleChoiceAttachment
import java.util.UUID

/**
 * Widgets-internal descriptor of a message the visitor is sending.
 *
 * Used to render the message optimistically, to key it in `ChatManager.State.messagePreviews`,
 * and to re-send it on retry. Each instance maps to exactly one outgoing message routed to the
 * unified Core send API (`Chat` / `SecureConversations`).
 *
 * The [messageId] is the client-generated id used to reconcile the optimistic item with the echoed
 * message from the incoming message stream. For a [File] the uploaded file id doubles as the
 * [messageId], because the live `Chat.sendFileAttachment` carries no message id and the file id is
 * the only correlation key available on the incoming message.
 *
 * @hide
 */
internal sealed interface OutgoingMessage {
    val messageId: String

    data class Text(val content: String, override val messageId: String = UUID.randomUUID().toString()) : OutgoingMessage

    data class SingleChoice(val attachment: SingleChoiceAttachment, override val messageId: String = UUID.randomUUID().toString()) : OutgoingMessage

    data class File(val fileId: String) : OutgoingMessage {
        override val messageId: String get() = fileId
    }
}
