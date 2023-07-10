package com.glia.widgets.chat.model.history

import com.glia.androidsdk.chat.AttachmentFile

data class OperatorAttachmentItem(
    private val chatItemId: String?,
    private val viewType: Int,
    private val showChatHead: Boolean,
    val attachmentFile: AttachmentFile,
    private val operatorProfileImgUrl: String?,
    val isFileExists: Boolean,
    val isDownloading: Boolean,
    private val operatorId: String?,
    private val messageId: String?,
    private val timestamp: Long
) : OperatorChatItem(chatItemId, viewType, showChatHead, operatorProfileImgUrl, operatorId, messageId, timestamp)
