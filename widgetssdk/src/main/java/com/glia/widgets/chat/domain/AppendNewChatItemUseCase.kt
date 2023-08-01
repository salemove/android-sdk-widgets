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
import com.glia.widgets.chat.model.OperatorChatItem
import com.glia.widgets.chat.model.VisitorChatItem
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

        state.apply { addedMessagesCount = chatItems.count() - itemsCount }


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

internal class AppendNewVisitorMessageUseCase(
    private val mapVisitorAttachmentUseCase: MapVisitorAttachmentUseCase
) {

    @VisibleForTesting
    var lastDeliveredItem: VisitorChatItem? = null

    @VisibleForTesting
    fun addUnsentItem(state: ChatManager.State, message: VisitorMessage): Boolean {
        if (state.unsentItems.isEmpty()) return false

        val unsentMessage =
            state.unsentItems.firstOrNull { it.message == message.content } ?: return false
        state.unsentItems.remove(unsentMessage)

        val index = state.chatItems.indexOf(unsentMessage)
        if (index != -1) {
            if (lastDeliveredItem != null) {
                val lastDeliveredIndex = state.chatItems.indexOf(lastDeliveredItem!!)
                state.chatItems[lastDeliveredIndex] = lastDeliveredItem!!.withDeliveredStatus(false)
            }

            state.chatItems[index] = unsentMessage.run {
                lastDeliveredItem = VisitorMessageItem.Delivered(message.id, timestamp, this.message)
                lastDeliveredItem!!
            }

            return true
        }

        return false
    }

    operator fun invoke(state: ChatManager.State, chatMessageInternal: ChatMessageInternal) {
        val message = chatMessageInternal.chatMessage as VisitorMessage

        if (!addUnsentItem(state, message)) {
            message.apply {
                val files = (attachment as? FilesAttachment)?.files
                val hasFiles = !files.isNullOrEmpty()

                if (content.isNotBlank()) {
                    if (hasFiles) {
                        state.chatItems += VisitorMessageItem.New(id, timestamp, content)
                    } else {
                        state.chatItems += VisitorMessageItem.Delivered(id, timestamp, content)
                    }
                }

                files?.forEachIndexed { index, attachmentFile ->
                    state.chatItems += mapVisitorAttachmentUseCase(attachmentFile, message, index == files.lastIndex)
                }

                if (lastDeliveredItem != null) {
                    val index = state.chatItems.indexOf(lastDeliveredItem!!)
                    state.chatItems[index] = lastDeliveredItem!!.withDeliveredStatus(false)
                }

                lastDeliveredItem = state.chatItems.last() as? VisitorChatItem
            }
        }
    }
}
