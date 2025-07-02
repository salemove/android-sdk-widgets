package com.glia.widgets.chat.model

import android.content.Context
import android.text.format.DateUtils
import com.glia.androidsdk.chat.AttachmentFile
import com.glia.androidsdk.chat.ChatMessage
import com.glia.androidsdk.chat.SingleChoiceOption
import com.glia.widgets.chat.adapter.ChatAdapter
import com.glia.widgets.helper.isDownloaded
import com.glia.widgets.internal.fileupload.model.LocalAttachment
import java.util.UUID

internal abstract class ChatItem(@ChatAdapter.Type val viewType: Int) {
    abstract val id: String
    abstract val timestamp: Long

    open fun areContentsTheSame(newItem: ChatItem): Boolean = this == newItem
}

/**
 * This is the same as [ChatItem], but should be used for non-local chat items
 */
internal abstract class ServerChatItem(@ChatAdapter.Type viewType: Int) : ChatItem(viewType)

internal abstract class OperatorChatItem(@ChatAdapter.Type viewType: Int) : ServerChatItem(viewType) {
    abstract val showChatHead: Boolean
    abstract val operatorProfileImgUrl: String?
    abstract val operatorId: String?

    abstract fun withShowChatHead(showChatHead: Boolean): OperatorChatItem
}

internal interface LocalAttachmentItem {
    val attachment: LocalAttachment
    val messageId: String

    val id: String get() = attachment.id
}

internal interface RemoteAttachmentItem {
    val attachment: AttachmentFile
    val isFileExists: Boolean
    val isDownloading: Boolean

    fun updateWith(isFileExists: Boolean, isDownloading: Boolean): ChatItem

    val id: String get() = attachment.id

    fun isDownloaded(context: Context): Boolean = attachment.isDownloaded(context)
}

internal data class CustomCardChatItem(
    val message: ChatMessage,
    private val customCardViewType: Int
) : ServerChatItem(customCardViewType) {
    override val id: String = message.id
    override val timestamp: Long = message.timestamp
}

internal class SystemChatItem(
    val message: String,
    override val id: String,
    override val timestamp: Long
) : ServerChatItem(ChatAdapter.SYSTEM_MESSAGE_TYPE)

internal sealed class OperatorAttachmentItem(@ChatAdapter.Type viewType: Int) : OperatorChatItem(viewType) {

    data class Image(
        val attachment: AttachmentFile,
        override val id: String,
        override val timestamp: Long,
        override val showChatHead: Boolean = false,
        override val operatorProfileImgUrl: String? = null,
        override val operatorId: String? = null
    ) : OperatorAttachmentItem(ChatAdapter.OPERATOR_IMAGE_VIEW_TYPE) {
        override fun withShowChatHead(showChatHead: Boolean): OperatorChatItem = copy(showChatHead = showChatHead)
    }

    data class File(
        override val isFileExists: Boolean = false,
        override val isDownloading: Boolean = false,
        override val attachment: AttachmentFile,
        override val id: String,
        override val timestamp: Long,
        override val showChatHead: Boolean = false,
        override val operatorProfileImgUrl: String? = null,
        override val operatorId: String? = null
    ) : OperatorAttachmentItem(ChatAdapter.OPERATOR_FILE_VIEW_TYPE), RemoteAttachmentItem {
        override fun withShowChatHead(showChatHead: Boolean): OperatorChatItem = copy(showChatHead = showChatHead)
        override fun updateWith(isFileExists: Boolean, isDownloading: Boolean): ChatItem =
            copy(isFileExists = isFileExists, isDownloading = isDownloading)
    }
}

internal sealed class OperatorMessageItem : OperatorChatItem(ChatAdapter.OPERATOR_MESSAGE_VIEW_TYPE) {
    abstract val operatorName: String?
    abstract val content: String?

    data class PlainText(
        override val id: String,
        override val timestamp: Long,
        override val showChatHead: Boolean,
        override val operatorProfileImgUrl: String?,
        override val operatorId: String?,
        override val operatorName: String?,
        override val content: String?
    ) : OperatorMessageItem() {
        override fun withShowChatHead(showChatHead: Boolean): OperatorChatItem = copy(showChatHead = showChatHead)
    }

    data class ResponseCard(
        override val id: String,
        override val timestamp: Long,
        override val showChatHead: Boolean,
        override val operatorProfileImgUrl: String?,
        override val operatorId: String?,
        override val operatorName: String?,
        override val content: String?,
        val singleChoiceOptions: List<SingleChoiceOption>,
        val choiceCardImageUrl: String?
    ) : OperatorMessageItem() {

        init {
            require(singleChoiceOptions.isNotEmpty()) { "Response card should have at least one `SingleChoiceOption`" }
        }

        override fun withShowChatHead(showChatHead: Boolean): OperatorChatItem = copy(showChatHead = showChatHead)

        fun asPlainText() = PlainText(
            id = id,
            timestamp = timestamp,
            showChatHead = showChatHead,
            operatorProfileImgUrl = operatorProfileImgUrl,
            operatorId = operatorId,
            operatorName = operatorName,
            content = content
        )
    }
}

// Local

internal sealed class MediaUpgradeStartedTimerItem : ChatItem(ChatAdapter.MEDIA_UPGRADE_ITEM_TYPE) {
    override val id: String = "media_upgrade_item"
    override val timestamp: Long = -1
    abstract val time: String

    abstract fun updateTime(time: String): MediaUpgradeStartedTimerItem

    data class Audio(override val time: String = DateUtils.formatElapsedTime(0)) : MediaUpgradeStartedTimerItem() {
        override fun updateTime(time: String) = copy(time = time)
    }

    data class Video(override val time: String = DateUtils.formatElapsedTime(0)) : MediaUpgradeStartedTimerItem() {
        override fun updateTime(time: String) = copy(time = time)
    }
}

internal object NewMessagesDividerItem : ChatItem(ChatAdapter.NEW_MESSAGES_DIVIDER_TYPE) {
    override val id: String = "new_messages_item"
    override val timestamp: Long = -1
}

internal sealed class OperatorStatusItem : ChatItem(ChatAdapter.OPERATOR_STATUS_VIEW_TYPE) {
    override val id: String = "operator_status_item"
    override val timestamp: Long = -1

    data object InQueue : OperatorStatusItem()

    data class Connected(
        val operatorName: String,
        val profileImgUrl: String?
    ) : OperatorStatusItem()

    data class Joined(
        val operatorName: String,
        val profileImgUrl: String?
    ) : OperatorStatusItem()

    data object Transferring : OperatorStatusItem()
}

// Visitor

internal abstract class VisitorChatItem(@ChatAdapter.Type viewType: Int) : ChatItem(viewType) {
    abstract val isError: Boolean
    abstract fun copyWithError(isError: Boolean): VisitorChatItem
}

internal sealed class VisitorAttachmentItem(@ChatAdapter.Type viewType: Int) : VisitorChatItem(viewType) {

    data class LocalImage(
        override val id: String,
        override val messageId: String,
        override val attachment: LocalAttachment,
        override val isError: Boolean = false,
        override val timestamp: Long = System.currentTimeMillis(),
    ) : VisitorAttachmentItem(ChatAdapter.VISITOR_IMAGE_VIEW_TYPE), LocalAttachmentItem {
        override fun copyWithError(isError: Boolean): VisitorChatItem = copy(isError = isError)
    }

    data class LocalFile(
        override val id: String,
        override val messageId: String,
        override val attachment: LocalAttachment,
        override val isError: Boolean = false,
        override val timestamp: Long = System.currentTimeMillis()
    ) : VisitorAttachmentItem(ChatAdapter.VISITOR_FILE_VIEW_TYPE), LocalAttachmentItem {
        override fun copyWithError(isError: Boolean): VisitorChatItem = copy(isError = isError)
    }

    data class RemoteImage(
        override val id: String,
        val attachment: AttachmentFile,
        override val isError: Boolean = false,
        override val timestamp: Long = System.currentTimeMillis(),
    ) : VisitorAttachmentItem(ChatAdapter.VISITOR_IMAGE_VIEW_TYPE) {

        override fun copyWithError(isError: Boolean): VisitorChatItem = copy(isError = isError)
    }

    data class RemoteFile(
        override val id: String,
        override val attachment: AttachmentFile,
        override val isFileExists: Boolean,
        override val isDownloading: Boolean,
        override val isError: Boolean = false,
        override val timestamp: Long = System.currentTimeMillis()
    ) : VisitorAttachmentItem(ChatAdapter.VISITOR_FILE_VIEW_TYPE), RemoteAttachmentItem {
        override fun updateWith(isFileExists: Boolean, isDownloading: Boolean): ChatItem =
            copy(isFileExists = isFileExists, isDownloading = isDownloading)

        override fun copyWithError(isError: Boolean): VisitorChatItem = copy(isError = isError)
    }
}

internal data class VisitorMessageItem(
    val message: String,
    override val id: String,
    override val isError: Boolean = false,
    override val timestamp: Long = System.currentTimeMillis()
) : VisitorChatItem(ChatAdapter.VISITOR_MESSAGE_TYPE) {
    override fun copyWithError(isError: Boolean): VisitorChatItem = copy(isError = isError)
}

internal data class DeliveredItem(
    val messageId: String,
    override val id: String = UUID.randomUUID().toString(),
    override val timestamp: Long = System.currentTimeMillis()
) : ChatItem(ChatAdapter.DELIVERED_ITEM_TYPE)

internal data class TapToRetryItem(
    val messageId: String,
    override val id: String = UUID.randomUUID().toString(),
    override val timestamp: Long = System.currentTimeMillis()
) : ChatItem(ChatAdapter.TAP_TO_RETRY_ITEM_TYPE)
