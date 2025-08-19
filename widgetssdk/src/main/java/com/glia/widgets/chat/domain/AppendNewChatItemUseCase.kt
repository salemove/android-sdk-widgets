package com.glia.widgets.chat.domain

import androidx.annotation.VisibleForTesting
import com.glia.androidsdk.chat.FilesAttachment
import com.glia.androidsdk.chat.OperatorMessage
import com.glia.androidsdk.chat.SingleChoiceAttachment
import com.glia.androidsdk.chat.SystemMessage
import com.glia.androidsdk.chat.VisitorMessage
import com.glia.telemetry_lib.Attributes
import com.glia.telemetry_lib.GliaLogger
import com.glia.telemetry_lib.LogEvents
import com.glia.widgets.chat.ChatManager
import com.glia.widgets.chat.domain.gva.IsGvaUseCase
import com.glia.widgets.chat.model.ChatItem
import com.glia.widgets.chat.model.DeliveredItem
import com.glia.widgets.chat.model.LocalAttachmentItem
import com.glia.widgets.chat.model.OperatorChatItem
import com.glia.widgets.chat.model.VisitorChatItem
import com.glia.widgets.chat.model.VisitorMessageItem
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.TAG
import com.glia.widgets.helper.asSingleChoice
import com.glia.widgets.internal.engagement.domain.model.ChatMessageInternal

/**
 * Use case for appending a new chat message to the chat state.
 * Handles different types of messages including visitor messages, operator messages, and system messages.
 * Updates the state with the new message and manages the visibility of the operator's chat head.
 */
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
                state.addedMessagesCount = 1
            }

            else -> Logger.d(TAG, "Unexpected type of message received -> $message")
        }
    }
}

/**
 * Use case for appending a new operator message to the chat state.
 * Handles different types of operator messages including GVA messages, custom card messages, and response card or text messages.
 * Updates the state with the new message and manages the visibility of the operator's chat head.
 */
internal class AppendNewOperatorMessageUseCase(
    private val isGvaUseCase: IsGvaUseCase,
    private val customCardAdapterTypeUseCase: CustomCardAdapterTypeUseCase,
    private val appendGvaMessageItemUseCase: AppendGvaMessageItemUseCase,
    private val appendHistoryCustomCardItemUseCase: AppendHistoryCustomCardItemUseCase,
    private val appendNewResponseCardOrTextItemUseCase: AppendNewResponseCardOrTextItemUseCase
) {
    operator fun invoke(state: ChatManager.State, chatMessageInternal: ChatMessageInternal) {
        val initialItemCount = state.chatItems.count()
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

        state.apply { addedMessagesCount = chatItems.count() - initialItemCount }

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

/**
 * Use case for appending a new response card or plain text item to the chat state.
 * Handles the addition of response cards, plain text messages, and attachments.
 * Updates the state with the new message.
 */
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
        GliaLogger.i(LogEvents.CHAT_SCREEN_SINGLE_CHOICE_SHOWN, null) {
            put(Attributes.MESSAGE_ID, message.chatMessage.id)
        }
    }
}

/**
 * Use case for appending a new visitor message to the chat state.
 * Handles the addition of visitor messages and attachments.
 * Updates the state with the new message.
 */
internal class AppendNewVisitorMessageUseCase(private val mapVisitorAttachmentUseCase: MapVisitorAttachmentUseCase) {
    private var lastDeliveredItem: DeliveredItem? = null

    operator fun invoke(state: ChatManager.State, chatMessageInternal: ChatMessageInternal) {
        val message = chatMessageInternal.chatMessage as VisitorMessage

        if (state.messagePreviews.remove(message.id) != null) {
            state.preEngagementChatItemIds.remove(message.id)
            markMessageDelivered(state, message)
            return
        }

        addNewMessage(state, message)
    }

    private fun markLastDeliveredItemAsDelivered(state: ChatManager.State) {
        lastDeliveredItem?.also(state.chatItems::remove)
        lastDeliveredItem = null
    }

    private fun addNewMessage(state: ChatManager.State, message: VisitorMessage) {
        markLastDeliveredItemAsDelivered(state)

        message.apply {
            val files = (attachment as? FilesAttachment)?.files
            val text = content.takeIf { it.isNotBlank() }

            if (text == null && files.isNullOrEmpty()) return

            text?.let {
                state.chatItems += VisitorMessageItem(it, id, false, timestamp)
            }

            files?.forEach {
                state.chatItems += mapVisitorAttachmentUseCase(it, this)
            }

            state.chatItems += DeliveredItem(messageId = id, timestamp = timestamp).also {
                lastDeliveredItem = it
            }
        }
    }

    private fun markMessageDelivered(state: ChatManager.State, message: VisitorMessage) {
        markLastDeliveredItemAsDelivered(state)

        val chatItems = state.chatItems

        val files = (message.attachment as? FilesAttachment)?.files.orEmpty()

        val messageIndex = chatItems.indexOfLast { it.id == message.id }

        if (messageIndex != -1) {
            chatItems[messageIndex] = VisitorMessageItem(message.content, message.id, false, message.timestamp)
        }

        files.forEach { attachment ->
            val index = chatItems.indexOfLast { it.id == attachment.id }
            val visitorChatItem = chatItems[index] as VisitorChatItem

            chatItems[index] = visitorChatItem.copyWithError(false)
        }

        val lastDeliveredIndex = if (files.isNotEmpty()) {
            chatItems.indexOfLast { (it as? LocalAttachmentItem)?.messageId == message.id }
        } else {
            messageIndex
        }

        val deliveredItem = DeliveredItem(messageId = message.id, timestamp = message.timestamp).also {
            lastDeliveredItem = it
        }

        chatItems.add(lastDeliveredIndex + 1, deliveredItem)

    }

}
