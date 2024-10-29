package com.glia.widgets.chat.domain

import androidx.annotation.VisibleForTesting
import com.glia.androidsdk.chat.FilesAttachment
import com.glia.androidsdk.chat.OperatorMessage
import com.glia.androidsdk.chat.SingleChoiceAttachment
import com.glia.androidsdk.chat.SystemMessage
import com.glia.androidsdk.chat.VisitorMessage
import com.glia.widgets.chat.ChatManager
import com.glia.widgets.chat.domain.gva.IsGvaUseCase
import com.glia.widgets.chat.model.ChatItem
import com.glia.widgets.chat.model.LocalAttachmentItem
import com.glia.widgets.chat.model.OperatorChatItem
import com.glia.widgets.chat.model.ServerChatItem
import com.glia.widgets.chat.model.VisitorChatItem
import com.glia.widgets.chat.model.VisitorItemStatus
import com.glia.widgets.chat.model.VisitorMessageItem
import com.glia.widgets.core.engagement.domain.model.ChatMessageInternal
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.TAG
import com.glia.widgets.helper.asSingleChoice

internal class AppendNewChatMessageUseCase(
    private val appendNewOperatorMessageUseCase: AppendNewOperatorMessageUseCase,
    private val appendNewVisitorMessageUseCase: AppendNewVisitorMessageUseCase,
    private val appendSystemMessageItemUseCase: AppendSystemMessageItemUseCase
) {
    operator fun invoke(state: ChatManager.State, chatMessageInternal: ChatMessageInternal) {
        when (val message = chatMessageInternal.chatMessage) {
            is VisitorMessage -> {
                appendNewVisitorMessageUseCase(state, chatMessageInternal)
                state.resetOperator()
            }

            is OperatorMessage -> appendNewOperatorMessageUseCase(state, chatMessageInternal)

            is SystemMessage -> {
                appendSystemMessageItemUseCase(state.chatItems, message)
                state.resetOperator()
            }

            else -> Logger.d(TAG, "Unexpected type of message received -> $message")
        }
    }
}

internal class AppendNewOperatorMessageUseCase(
    private val isGvaUseCase: IsGvaUseCase,
    private val customCardAdapterTypeUseCase: CustomCardAdapterTypeUseCase,
    private val appendGvaMessageItemUseCase: AppendGvaMessageItemUseCase,
    private val appendHistoryCustomCardItemUseCase: AppendHistoryCustomCardItemUseCase,
    private val appendNewResponseCardOrTextItemUseCase: AppendNewResponseCardOrTextItemUseCase
) {
    operator fun invoke(state: ChatManager.State, chatMessageInternal: ChatMessageInternal) {
        val itemsCount = state.chatItems.count()
        val message: OperatorMessage = chatMessageInternal.chatMessage as OperatorMessage
        when {
            isGvaUseCase(message) -> appendGvaMessageItemUseCase(
                state.chatItems,
                chatMessageInternal
            )

            customCardAdapterTypeUseCase(message) != null -> appendHistoryCustomCardItemUseCase(
                state.chatItems,
                message,
                customCardAdapterTypeUseCase(message)!!
            )

            else -> appendNewResponseCardOrTextItemUseCase(state.chatItems, chatMessageInternal)
        }

        state.apply { addedMessagesCount = chatItems.count { it is ServerChatItem || it is VisitorChatItem } - itemsCount }

        val lastMessageWithVisibleOperatorImage = state.lastMessageWithVisibleOperatorImage
        val lastItem = state.chatItems.lastOrNull()

        if (lastItem !is OperatorChatItem) {
            state.resetOperator()
            return
        }

        if (state.isOperatorChanged(lastItem) || lastMessageWithVisibleOperatorImage == null) {
            return
        }

        val index = state.chatItems.indexOf(lastMessageWithVisibleOperatorImage)
        if (index == -1) return
        state.chatItems[index] = lastMessageWithVisibleOperatorImage.withShowChatHead(false)
    }
}

internal class AppendNewResponseCardOrTextItemUseCase(
    private val mapOperatorAttachmentUseCase: MapOperatorAttachmentUseCase,
    private val mapOperatorPlainTextUseCase: MapOperatorPlainTextUseCase,
    private val mapResponseCardUseCase: MapResponseCardUseCase
) {
    operator fun invoke(chatItems: MutableList<ChatItem>, message: ChatMessageInternal) {
        val chatMessage = message.chatMessage
        chatMessage.attachment?.asSingleChoice()?.takeIf {
            it.options.isNotEmpty()
        }?.let { addResponseCard(chatItems, it, message) } ?: addPlainTextAndAttachments(chatItems, message)
    }

    @VisibleForTesting
    fun addPlainTextAndAttachments(chatItems: MutableList<ChatItem>, message: ChatMessageInternal) {
        val filesAttachment = message.chatMessage.attachment as? FilesAttachment

        if (message.chatMessage.content.isNotBlank()) {
            chatItems += mapOperatorPlainTextUseCase(message, filesAttachment?.files.isNullOrEmpty())
        }

        filesAttachment?.files?.apply {
            for (index in indices) {
                chatItems += mapOperatorAttachmentUseCase(get(index), message, index == lastIndex)
            }
        }
    }

    @VisibleForTesting
    fun addResponseCard(
        chatItems: MutableList<ChatItem>,
        attachment: SingleChoiceAttachment,
        message: ChatMessageInternal
    ) {
        chatItems += mapResponseCardUseCase(attachment, message, true)
    }
}

internal class AppendNewVisitorMessageUseCase(private val mapVisitorAttachmentUseCase: MapVisitorAttachmentUseCase) {
    private var lastDeliveredItem: VisitorChatItem? = null

    operator fun invoke(state: ChatManager.State, chatMessageInternal: ChatMessageInternal) {
        val message = chatMessageInternal.chatMessage as VisitorMessage

        if (state.messagePreviews.remove(message.id) != null) {
            markMessageDelivered(state, message)
            return
        }

        addNewMessage(state, message)
    }

    private fun markLastDeliveredItemAsDelivered(state: ChatManager.State) {
        lastDeliveredItem?.also {
            val index = state.chatItems.indexOf(it)
            state.chatItems[index] = it.withStatus(VisitorItemStatus.HISTORY)
            lastDeliveredItem = null
        }
    }

    private fun addNewMessage(state: ChatManager.State, message: VisitorMessage) {
        markLastDeliveredItemAsDelivered(state)

        message.apply {
            val files = (attachment as? FilesAttachment)?.files

            if (content.isNotBlank()) {
                val messageStatus = if (files != null) VisitorItemStatus.HISTORY else VisitorItemStatus.DELIVERED
                state.chatItems += VisitorMessageItem(content, id, messageStatus, timestamp).also {
                    lastDeliveredItem = it
                }
            }

            files?.forEachIndexed { index, attachmentFile ->
                val showDelivered = index == files.lastIndex
                state.chatItems += mapVisitorAttachmentUseCase(attachmentFile, message, showDelivered).also {
                    if (showDelivered) {
                        lastDeliveredItem = it
                    }
                }
            }

        }
    }

    private fun markMessageDelivered(state: ChatManager.State, message: VisitorMessage) {
        markLastDeliveredItemAsDelivered(state)

        val chatItems = state.chatItems

        val files = (message.attachment as? FilesAttachment)?.files?.takeIf { it.isNotEmpty() }

        val messageIndex = chatItems.indexOfLast { it.id == message.id }

        if (messageIndex != -1) {
            val messageStatus = if (files != null) {
                VisitorItemStatus.HISTORY
            } else {
                VisitorItemStatus.DELIVERED
            }
            chatItems[messageIndex] = VisitorMessageItem(message.content, message.id, messageStatus, message.timestamp).also {
                lastDeliveredItem = it
            }
        }

        if (files == null) return

        val lastDeliveredIndex = chatItems.indexOfLast { (it as? LocalAttachmentItem)?.messageId == message.id }

        files.forEach { attachment ->
            val index = chatItems.indexOfLast { it.id == attachment.id }
            val visitorChatItem = chatItems[index] as VisitorChatItem

            chatItems[index] = if (index == lastDeliveredIndex) {
                visitorChatItem.withStatus(VisitorItemStatus.DELIVERED).also {
                    lastDeliveredItem = it
                }
            } else {
                visitorChatItem.withStatus(VisitorItemStatus.HISTORY)
            }
        }

    }

}
