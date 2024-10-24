package com.glia.widgets.chat.model

import com.glia.androidsdk.chat.FilesAttachment
import com.glia.androidsdk.chat.SingleChoiceAttachment
import com.glia.widgets.core.fileupload.model.LocalAttachment

internal class SendMessagePayload private constructor(
    val content: String,
    val localAttachments: List<LocalAttachment>?,
    val payload: com.glia.androidsdk.chat.SendMessagePayload
) {
    constructor(content: String, localAttachments: List<LocalAttachment>?) : this(
        content,
        localAttachments,
        com.glia.androidsdk.chat.SendMessagePayload(
            content,
            localAttachments
                ?.map { it.engagementFile }
                ?.toTypedArray()
                ?.let { FilesAttachment.from(it) }
        )
    )

    @JvmOverloads
    constructor(content: String, attachment: SingleChoiceAttachment? = null) : this(
        content,
        null,
        com.glia.androidsdk.chat.SendMessagePayload(content, attachment)
    )

    constructor(attachment: SingleChoiceAttachment) : this(
        attachment.selectedOptionText ?: "option",
        attachment
    )

    val messageId = payload.messageId
}
