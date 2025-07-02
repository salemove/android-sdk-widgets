package com.glia.widgets.chat.domain

import com.glia.androidsdk.chat.AttachmentFile
import com.glia.androidsdk.chat.SingleChoiceAttachment
import com.glia.androidsdk.chat.VisitorMessage
import com.glia.widgets.chat.model.OperatorAttachmentItem
import com.glia.widgets.chat.model.OperatorMessageItem
import com.glia.widgets.chat.model.VisitorAttachmentItem
import com.glia.widgets.chat.model.VisitorChatItem
import com.glia.widgets.helper.isImage
import com.glia.widgets.internal.engagement.domain.model.ChatMessageInternal
import kotlin.jvm.optionals.getOrNull

internal class MapOperatorAttachmentUseCase {
    operator fun invoke(attachment: AttachmentFile, chatMessageInternal: ChatMessageInternal, showChatHead: Boolean) = chatMessageInternal.run {
        if (attachment.isImage) {
            OperatorAttachmentItem.Image(
                attachment = attachment,
                id = attachment.id,
                timestamp = chatMessage.timestamp,
                showChatHead = showChatHead,
                operatorProfileImgUrl = operatorImageUrl,
                operatorId = operatorId
            )
        } else {
            OperatorAttachmentItem.File(
                attachment = attachment,
                id = attachment.id,
                timestamp = chatMessage.timestamp,
                showChatHead = showChatHead,
                operatorProfileImgUrl = operatorImageUrl,
                operatorId = operatorId
            )
        }
    }
}

internal class MapVisitorAttachmentUseCase {
    operator fun invoke(attachmentFile: AttachmentFile, message: VisitorMessage): VisitorChatItem = message.run {
        if (attachmentFile.isImage) {
            VisitorAttachmentItem.RemoteImage(
                id = attachmentFile.id,
                attachment = attachmentFile,
                timestamp = timestamp
            )
        } else {
            VisitorAttachmentItem.RemoteFile(
                id = attachmentFile.id,
                attachment = attachmentFile,
                isFileExists = false,
                isDownloading = false,
                timestamp = timestamp
            )
        }
    }
}

internal class MapOperatorPlainTextUseCase {
    operator fun invoke(chatMessageInternal: ChatMessageInternal, showChatHead: Boolean): OperatorMessageItem = chatMessageInternal.run {
        OperatorMessageItem.PlainText(
            chatMessage.id,
            chatMessage.timestamp,
            showChatHead,
            operatorImageUrl,
            operatorId,
            operatorName,
            chatMessage.content
        )
    }
}

internal class MapResponseCardUseCase {
    operator fun invoke(attachment: SingleChoiceAttachment, message: ChatMessageInternal, showChatHead: Boolean): OperatorMessageItem.ResponseCard =
        message.run {
            OperatorMessageItem.ResponseCard(
                chatMessage.id,
                chatMessage.timestamp,
                showChatHead,
                operatorImageUrl,
                operatorId,
                operatorName,
                chatMessage.content,
                attachment.options.asList(),
                attachment.imageUrl.getOrNull()
            )
        }
}
