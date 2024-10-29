package com.glia.widgets.chat.adapter

import androidx.recyclerview.widget.DiffUtil
import com.glia.widgets.chat.model.ChatItem
import com.glia.widgets.chat.model.MediaUpgradeStartedTimerItem
import com.glia.widgets.chat.model.VisitorAttachmentItem
import com.glia.widgets.chat.model.VisitorItemStatus
import com.glia.widgets.chat.model.VisitorMessageItem

internal class ChatAdapterDiffCallback : DiffUtil.ItemCallback<ChatItem>() {
    override fun areItemsTheSame(oldItem: ChatItem, newItem: ChatItem): Boolean = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: ChatItem, newItem: ChatItem): Boolean = oldItem.areContentsTheSame(newItem)

    override fun getChangePayload(oldItem: ChatItem, newItem: ChatItem): Any? = when {
        oldItem is MediaUpgradeStartedTimerItem.Audio && newItem is MediaUpgradeStartedTimerItem.Audio -> ChatAdapterPayload.Time(newItem.time)
        oldItem is MediaUpgradeStartedTimerItem.Video && newItem is MediaUpgradeStartedTimerItem.Video -> ChatAdapterPayload.Time(newItem.time)
        oldItem is VisitorMessageItem && newItem is VisitorMessageItem -> ChatAdapterPayload.MessageUpdated(newItem.status, newItem.message)
        oldItem is VisitorAttachmentItem.LocalImage && newItem is VisitorAttachmentItem.LocalImage -> ChatAdapterPayload.StatusChanged(newItem.status)
        oldItem is VisitorAttachmentItem.LocalFile && newItem is VisitorAttachmentItem.LocalFile -> ChatAdapterPayload.StatusChanged(newItem.status)

        else -> null
    }
}

internal sealed class ChatAdapterPayload {
    internal data class Time(val time: String) : ChatAdapterPayload()
    internal data class StatusChanged(val status: VisitorItemStatus) : ChatAdapterPayload()
    internal data class MessageUpdated(val status: VisitorItemStatus, val message: String) : ChatAdapterPayload()
}
