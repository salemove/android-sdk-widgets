package com.glia.widgets.chat.domain

import androidx.annotation.VisibleForTesting
import com.glia.androidsdk.chat.FilesAttachment
import com.glia.androidsdk.chat.OperatorMessage
import com.glia.androidsdk.chat.SingleChoiceAttachment
import com.glia.androidsdk.chat.SystemMessage
import com.glia.androidsdk.chat.VisitorMessage
import com.glia.widgets.chat.domain.gva.IsGvaUseCase
import com.glia.widgets.chat.domain.gva.MapGvaUseCase
import com.glia.widgets.chat.model.ChatItem
import com.glia.widgets.chat.model.CustomCardChatItem
import com.glia.widgets.chat.model.OperatorStatusItem
import com.glia.widgets.chat.model.SystemChatItem
import com.glia.widgets.chat.model.VisitorMessageItem
import com.glia.widgets.core.engagement.domain.model.ChatMessageInternal
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.TAG
import com.glia.widgets.helper.asSingleChoice

internal class AppendHistoryChatMessageUseCase(
    private val appendHistoryVisitorChatItemUseCase: AppendHistoryVisitorChatItemUseCase,
    private val appendHistoryOperatorChatItemUseCase: AppendHistoryOperatorChatItemUseCase,
    private val appendSystemMessageItemUseCase: AppendSystemMessageItemUseCase
) {
    @VisibleForTesting
    var operatorId: String? = null

    @VisibleForTesting
    fun resetOperatorId() {
        operatorId = null
    }

    @VisibleForTesting
    fun shouldShowChatHead(chatMessageInternal: ChatMessageInternal): Boolean {
        if (operatorId != chatMessageInternal.operatorId) {
            operatorId = chatMessageInternal.operatorId
            return true
        }

        return false
    }

    operator fun invoke(chatItems: MutableList<ChatItem>, chatMessageInternal: ChatMessageInternal, isLatest: Boolean) {
        when (val message = chatMessageInternal.chatMessage) {
            is VisitorMessage -> {
                resetOperatorId()
                appendHistoryVisitorChatItemUseCase(chatItems, message)
            }

            is OperatorMessage -> appendHistoryOperatorChatItemUseCase(
                chatItems,
                chatMessageInternal,
                isLatest,
                shouldShowChatHead(chatMessageInternal)
            )

            is SystemMessage -> {
                resetOperatorId()
                appendSystemMessageItemUseCase(chatItems, message)
            }

            else -> Logger.d(TAG, "Unexpected type of message received -> $message")
        }
    }
}

internal class AppendHistoryOperatorChatItemUseCase(
    private val isGvaUseCase: IsGvaUseCase,
    private val customCardAdapterTypeUseCase: CustomCardAdapterTypeUseCase,
    private val appendGvaMessageItemUseCase: AppendGvaMessageItemUseCase,
    private val appendHistoryCustomCardItemUseCase: AppendHistoryCustomCardItemUseCase,
    private val appendHistoryResponseCardOrTextItemUseCase: AppendHistoryResponseCardOrTextItemUseCase
) {
    operator fun invoke(chatItems: MutableList<ChatItem>, chatMessageInternal: ChatMessageInternal, isLatest: Boolean, showChatHead: Boolean) {
        val message: OperatorMessage = chatMessageInternal.chatMessage as OperatorMessage
        when {
            isGvaUseCase(message) -> appendGvaMessageItemUseCase(chatItems, chatMessageInternal, showChatHead)
            customCardAdapterTypeUseCase(message) != null -> appendHistoryCustomCardItemUseCase(
                chatItems,
                message,
                customCardAdapterTypeUseCase(message)!!
            )

            else -> appendHistoryResponseCardOrTextItemUseCase(chatItems, chatMessageInternal, isLatest, showChatHead)
        }
    }
}

internal class AppendHistoryVisitorChatItemUseCase(
    private val mapVisitorAttachmentUseCase: MapVisitorAttachmentUseCase
) {
    operator fun invoke(chatItems: MutableList<ChatItem>, message: VisitorMessage) {
        message.apply {
            (attachment as? FilesAttachment)?.files?.reversed()?.forEach {
                chatItems += mapVisitorAttachmentUseCase(it, message)
            }

            if (content.isNotBlank()) {
                chatItems += VisitorMessageItem(content, id, timestamp)
            }
        }
    }
}

internal class AppendSystemMessageItemUseCase {
    operator fun invoke(chatItems: MutableList<ChatItem>, message: SystemMessage) {
        val index = if (chatItems.lastOrNull() is OperatorStatusItem.InQueue) chatItems.lastIndex else chatItems.lastIndex + 1
        chatItems.add(index, message.run { SystemChatItem(content, id, timestamp) })
    }
}

internal class AppendGvaMessageItemUseCase(private val mapGvaUseCase: MapGvaUseCase) {
    operator fun invoke(chatItems: MutableList<ChatItem>, message: ChatMessageInternal, showChatHead: Boolean = true) {
        chatItems += mapGvaUseCase(message, showChatHead)
    }
}

internal class AppendHistoryCustomCardItemUseCase(
    private val customCardTypeUseCase: CustomCardTypeUseCase,
    private val customCardShouldShowUseCase: CustomCardShouldShowUseCase
) {
    operator fun invoke(chatItems: MutableList<ChatItem>, message: OperatorMessage, viewType: Int) {
        val customCardType = customCardTypeUseCase(viewType) ?: return
        if (customCardShouldShowUseCase.execute(message, customCardType, true)) {
            chatItems.add(message.run { CustomCardChatItem(message, viewType) })
        }

        message.attachment?.asSingleChoice()?.selectedOptionText?.takeIf {
            it.isNotBlank()
        }?.let {
            VisitorMessageItem(it, message.id, message.timestamp)
        }?.also {
            chatItems.add(it)
        }
    }
}

internal class AppendHistoryResponseCardOrTextItemUseCase(
    private val mapOperatorAttachmentUseCase: MapOperatorAttachmentUseCase,
    private val mapOperatorPlainTextUseCase: MapOperatorPlainTextUseCase,
    private val mapResponseCardUseCase: MapResponseCardUseCase
) {
    operator fun invoke(chatItems: MutableList<ChatItem>, message: ChatMessageInternal, isLatest: Boolean, showChatHead: Boolean) {
        val chatMessage = message.chatMessage
        chatMessage.attachment?.asSingleChoice()?.takeIf {
            it.options.isNotEmpty() && isLatest
        }?.let { addResponseCard(chatItems, it, message, showChatHead) } ?: addPlainTextAndAttachments(chatItems, message, showChatHead)
    }

    @VisibleForTesting
    fun addPlainTextAndAttachments(chatItems: MutableList<ChatItem>, message: ChatMessageInternal, showChatHead: Boolean) {
        val filesAttachment = message.chatMessage.attachment as? FilesAttachment

        filesAttachment?.files?.apply {
            for (index in indices.reversed()) {
                chatItems += mapOperatorAttachmentUseCase(get(index), message, showChatHead && index == lastIndex)
            }
        }

        if (message.chatMessage.content.isNotBlank()) {
            chatItems += mapOperatorPlainTextUseCase(message, showChatHead && filesAttachment == null)
        }
    }

    @VisibleForTesting
    fun addResponseCard(
        chatItems: MutableList<ChatItem>,
        attachment: SingleChoiceAttachment,
        message: ChatMessageInternal,
        showChatHead: Boolean
    ) {
        chatItems += mapResponseCardUseCase(attachment, message, showChatHead)
    }
}
