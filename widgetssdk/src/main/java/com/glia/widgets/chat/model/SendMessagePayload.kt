package com.glia.widgets.chat.model

import com.glia.androidsdk.chat.FilesAttachment
import com.glia.androidsdk.chat.SingleChoiceAttachment
import com.glia.widgets.core.fileupload.model.FileAttachment

internal class SendMessagePayload private constructor(
    val content: String,
    val fileAttachments: List<FileAttachment>?,
    val payload: com.glia.androidsdk.chat.SendMessagePayload
) {
    constructor(content: String, fileAttachments: List<FileAttachment>?) : this(
        content,
        fileAttachments,
        com.glia.androidsdk.chat.SendMessagePayload(
            content,
            fileAttachments
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
