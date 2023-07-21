package com.glia.widgets.chat.adapter

import androidx.recyclerview.widget.DiffUtil
import com.glia.widgets.chat.model.ChatItem
import com.glia.widgets.chat.model.MediaUpgradeStartedTimerItem

internal class ChatAdapterDiffCallback : DiffUtil.ItemCallback<ChatItem>() {
    override fun areItemsTheSame(oldItem: ChatItem, newItem: ChatItem): Boolean = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: ChatItem, newItem: ChatItem): Boolean = oldItem.areContentsTheSame(newItem)

    override fun getChangePayload(oldItem: ChatItem, newItem: ChatItem): Any? = when {
        oldItem is MediaUpgradeStartedTimerItem.Audio && newItem is MediaUpgradeStartedTimerItem.Audio -> newItem.time
        oldItem is MediaUpgradeStartedTimerItem.Video && newItem is MediaUpgradeStartedTimerItem.Video -> newItem.time
        else -> null
    }
}
