package com.glia.widgets.chat.adapter

import androidx.recyclerview.widget.DiffUtil
import com.glia.widgets.chat.model.ChatItem
import com.glia.widgets.chat.model.MediaUpgradeStartedTimerItem
import com.glia.widgets.chat.model.RemoteAttachmentItem
import com.glia.widgets.chat.model.VisitorAttachmentItem
import com.glia.widgets.chat.model.VisitorMessageItem

internal class ChatAdapterDiffCallback : DiffUtil.ItemCallback<ChatItem>() {
    override fun areItemsTheSame(oldItem: ChatItem, newItem: ChatItem): Boolean = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: ChatItem, newItem: ChatItem): Boolean = oldItem.areContentsTheSame(newItem)

    override fun getChangePayload(oldItem: ChatItem, newItem: ChatItem): Any? = when {
        oldItem is MediaUpgradeStartedTimerItem.Audio && newItem is MediaUpgradeStartedTimerItem.Audio -> ChatAdapterPayload.Time(newItem.time)
        oldItem is MediaUpgradeStartedTimerItem.Video && newItem is MediaUpgradeStartedTimerItem.Video -> ChatAdapterPayload.Time(newItem.time)
        oldItem is VisitorMessageItem && newItem is VisitorMessageItem -> ChatAdapterPayload.MessageUpdated(newItem.isError, newItem.message)
        oldItem is VisitorAttachmentItem.LocalImage && newItem is VisitorAttachmentItem.LocalImage -> ChatAdapterPayload.ErrorStateChanged(newItem.isError)
        oldItem is VisitorAttachmentItem.LocalFile && newItem is VisitorAttachmentItem.LocalFile -> ChatAdapterPayload.ErrorStateChanged(newItem.isError)
        oldItem is RemoteAttachmentItem && newItem is RemoteAttachmentItem -> ChatAdapterPayload.RemoteAttachmentStatusChanged
        else -> null
    }
}

internal sealed interface ChatAdapterPayload {
    data class Time(val time: String) : ChatAdapterPayload
    data class ErrorStateChanged(val isError: Boolean) : ChatAdapterPayload
    data class MessageUpdated(val isError: Boolean, val message: String) : ChatAdapterPayload
    data object RemoteAttachmentStatusChanged : ChatAdapterPayload
}
