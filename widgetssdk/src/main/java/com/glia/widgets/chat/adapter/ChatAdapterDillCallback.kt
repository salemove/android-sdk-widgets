package com.glia.widgets.chat.adapter

import androidx.recyclerview.widget.DiffUtil
import com.glia.widgets.chat.model.history.*

class ChatAdapterDillCallback : DiffUtil.ItemCallback<ChatItem>() {
    override fun areItemsTheSame(oldItem: ChatItem, newItem: ChatItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: ChatItem, newItem: ChatItem): Boolean {
        return when {
            oldItem is OperatorStatusItem && newItem is OperatorStatusItem -> oldItem == newItem
            oldItem is VisitorMessageItem && newItem is VisitorMessageItem -> oldItem == newItem
            oldItem is OperatorMessageItem && newItem is OperatorMessageItem -> oldItem == newItem
            oldItem is MediaUpgradeStartedTimerItem && newItem is MediaUpgradeStartedTimerItem -> oldItem == newItem
            oldItem is OperatorAttachmentItem && newItem is OperatorAttachmentItem -> oldItem == newItem
            oldItem is VisitorAttachmentItem && newItem is VisitorAttachmentItem -> oldItem == newItem
            oldItem is CustomCardItem && newItem is CustomCardItem -> oldItem == newItem
            else -> false
        }
    }

    override fun getChangePayload(oldItem: ChatItem, newItem: ChatItem): Any? {
        if (
            oldItem is MediaUpgradeStartedTimerItem
            && newItem is MediaUpgradeStartedTimerItem
            && oldItem.type == newItem.type
        ) {
            return newItem.time
        }

        return null
    }
}