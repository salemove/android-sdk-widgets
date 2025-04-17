package com.glia.widgets.chat.adapter

import androidx.annotation.VisibleForTesting
import com.glia.androidsdk.chat.SingleChoiceAttachment
import com.glia.androidsdk.chat.ChatMessage as CoreChatMessage
import org.json.JSONObject

/**
 * Represents a chat message in the Glia Widgets SDK.
 *
 * @property id The unique identifier of the message.
 * @property metadata Custom payloads for tailoring the visitor experience, or `null` if none exists.
 * @property selectedOption The value of the selected option. Available only when an answer
 * has been given to a single-choice attachment.
 */
data class CustomCardMessage(
    val id: String,
    val metadata: JSONObject?,
    val selectedOption: String?
) {
    internal constructor(chatMessage: CoreChatMessage) : this(
        chatMessage.id,
        chatMessage.metadata,
        (chatMessage.attachment as? SingleChoiceAttachment)?.selectedOption
    )

    /**
     * Defines possible chat participant types.
     */
    enum class Participant {
        VISITOR,
        OPERATOR,
        SYSTEM,
        UNKNOWN;

        /**
         * @hide
         */
        companion object {
            @VisibleForTesting
            fun toWidgetsType(chatMessage: String): Participant =
                kotlin.runCatching { valueOf(chatMessage) }.getOrNull() ?: UNKNOWN
        }
    }
}
