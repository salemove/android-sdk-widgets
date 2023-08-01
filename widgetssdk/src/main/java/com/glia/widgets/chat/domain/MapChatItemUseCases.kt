package com.glia.widgets.chat.domain

import com.glia.androidsdk.chat.AttachmentFile
import com.glia.androidsdk.chat.SingleChoiceAttachment
import com.glia.androidsdk.chat.VisitorMessage
import com.glia.widgets.chat.model.OperatorAttachmentItem
import com.glia.widgets.chat.model.OperatorMessageItem
import com.glia.widgets.chat.model.VisitorAttachmentItem
import com.glia.widgets.chat.model.VisitorChatItem
import com.glia.widgets.core.engagement.domain.model.ChatMessageInternal
import com.glia.widgets.helper.isImage
import kotlin.jvm.optionals.getOrNull

internal class MapOperatorAttachmentUseCase {
    operator fun invoke(attachment: AttachmentFile, chatMessageInternal: ChatMessageInternal, showChatHead: Boolean) = chatMessageInternal.run {
        if (attachment.isImage) {
            OperatorAttachmentItem.Image(
                attachmentFile = attachment,
                id = chatMessage.id,
                timestamp = chatMessage.timestamp,
                showChatHead = showChatHead,
                operatorProfileImgUrl = operatorImageUrl,
                operatorId = operatorId
            )
        } else {
            OperatorAttachmentItem.File(
                attachmentFile = attachment,
                id = chatMessage.id,
                timestamp = chatMessage.timestamp,
                showChatHead = showChatHead,
                operatorProfileImgUrl = operatorImageUrl,
                operatorId = operatorId
            )
        }
    }
}

internal class MapVisitorAttachmentUseCase {
    operator fun invoke(attachmentFile: AttachmentFile, message: VisitorMessage, showDelivered: Boolean = false): VisitorChatItem = message.run {
        if (attachmentFile.isImage) {
            VisitorAttachmentItem.Image(id, timestamp, attachmentFile, showDelivered = showDelivered)
        } else {
            VisitorAttachmentItem.File(id, timestamp, attachmentFile, showDelivered = showDelivered)
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
