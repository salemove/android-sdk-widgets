package com.glia.widgets.chat.adapter

import androidx.recyclerview.widget.DiffUtil
import com.glia.widgets.chat.model.ChatItem
import com.glia.widgets.chat.model.MediaUpgradeStartedTimerItem
import com.glia.widgets.chat.model.VisitorChatItem

internal class ChatAdapterDiffCallback : DiffUtil.ItemCallback<ChatItem>() {
    override fun areItemsTheSame(oldItem: ChatItem, newItem: ChatItem): Boolean = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: ChatItem, newItem: ChatItem): Boolean = oldItem.areContentsTheSame(newItem)

    override fun getChangePayload(oldItem: ChatItem, newItem: ChatItem): Any? = when {
        oldItem is MediaUpgradeStartedTimerItem.Audio && newItem is MediaUpgradeStartedTimerItem.Audio -> ChatAdapterPayload.Time(newItem.time)
        oldItem is MediaUpgradeStartedTimerItem.Video && newItem is MediaUpgradeStartedTimerItem.Video -> ChatAdapterPayload.Time(newItem.time)
        oldItem is VisitorChatItem && newItem is VisitorChatItem ->
            if (oldItem.showDelivered != newItem.showDelivered)
                ChatAdapterPayload.ShowDelivered(newItem.showDelivered)
            else if (oldItem.showError != newItem.showError)
                ChatAdapterPayload.ShowError(newItem.showError)
            else null
        else -> null
    }
}

internal sealed class ChatAdapterPayload {
    internal data class Time(val time: String) : ChatAdapterPayload()
    internal data class ShowDelivered(val showDelivered: Boolean) : ChatAdapterPayload()
    internal data class ShowError(val showError: Boolean) : ChatAdapterPayload()
}
