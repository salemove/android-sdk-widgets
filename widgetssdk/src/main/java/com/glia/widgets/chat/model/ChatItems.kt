package com.glia.widgets.chat.model

import android.content.Context
import com.glia.androidsdk.chat.AttachmentFile
import com.glia.androidsdk.chat.ChatMessage
import com.glia.androidsdk.chat.SingleChoiceOption
import com.glia.widgets.chat.adapter.ChatAdapter
import com.glia.widgets.helper.isDownloaded

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

internal interface AttachmentItem {
    val attachmentFile: AttachmentFile
    val isFileExists: Boolean
    val isDownloading: Boolean

    val attachmentId: String get() = attachmentFile.id

    fun isDownloaded(context: Context): Boolean = attachmentFile.isDownloaded(context)

    fun updateWith(isFileExists: Boolean, isDownloading: Boolean): ChatItem
}

internal data class CustomCardChatItem(
    val message: ChatMessage, private val customCardViewType: Int
) : ServerChatItem(customCardViewType) {
    override val id: String = message.id
    override val timestamp: Long = message.timestamp
}

internal class SystemChatItem(
    val message: String,
    override val id: String,
    override val timestamp: Long
) : ServerChatItem(ChatAdapter.SYSTEM_MESSAGE_TYPE)

internal sealed class OperatorAttachmentItem(@ChatAdapter.Type viewType: Int) : OperatorChatItem(viewType), AttachmentItem {

    data class Image(
        override val isFileExists: Boolean = false,
        override val isDownloading: Boolean = false,
        override val attachmentFile: AttachmentFile,
        override val id: String,
        override val timestamp: Long,
        override val showChatHead: Boolean = false,
        override val operatorProfileImgUrl: String? = null,
        override val operatorId: String? = null
    ) : OperatorAttachmentItem(ChatAdapter.OPERATOR_IMAGE_VIEW_TYPE) {
        override fun withShowChatHead(showChatHead: Boolean): OperatorChatItem = copy(showChatHead = showChatHead)

        override fun updateWith(isFileExists: Boolean, isDownloading: Boolean): ChatItem =
            copy(isFileExists = isFileExists, isDownloading = isDownloading)
    }

    data class File(
        override val isFileExists: Boolean = false,
        override val isDownloading: Boolean = false,
        override val attachmentFile: AttachmentFile,
        override val id: String,
        override val timestamp: Long,
        override val showChatHead: Boolean = false,
        override val operatorProfileImgUrl: String? = null,
        override val operatorId: String? = null
    ) : OperatorAttachmentItem(ChatAdapter.OPERATOR_FILE_VIEW_TYPE) {
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

        fun toPlainText() = PlainText(
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

    data class Audio(override val time: String) : MediaUpgradeStartedTimerItem() {
        override fun updateTime(time: String) = copy(time = time)
    }

    data class Video(override val time: String) : MediaUpgradeStartedTimerItem() {
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

    abstract val companyName: String?

    data class InQueue(override val companyName: String?) : OperatorStatusItem()

    data class Connected(
        override val companyName: String?,
        val operatorName: String,
        val profileImgUrl: String?
    ) : OperatorStatusItem()

    data class Joined(
        override val companyName: String?,
        val operatorName: String,
        val profileImgUrl: String?
    ) : OperatorStatusItem()

    object Transferring : OperatorStatusItem() {
        override val companyName: String? = null
    }

}

// Visitor

internal sealed class VisitorAttachmentItem(@ChatAdapter.Type viewType: Int) : ChatItem(viewType), AttachmentItem {
    abstract val showDelivered: Boolean
    abstract fun withDeliveredStatus(delivered: Boolean): VisitorAttachmentItem

    data class Image(
        override val id: String,
        override val timestamp: Long,
        override val attachmentFile: AttachmentFile,
        override val isFileExists: Boolean = false,
        override val isDownloading: Boolean = false,
        override val showDelivered: Boolean = false
    ) : VisitorAttachmentItem(ChatAdapter.VISITOR_IMAGE_VIEW_TYPE) {
        override fun withDeliveredStatus(delivered: Boolean): VisitorAttachmentItem = copy(showDelivered = delivered)

        override fun updateWith(isFileExists: Boolean, isDownloading: Boolean): ChatItem =
            copy(isFileExists = isFileExists, isDownloading = isDownloading)
    }

    data class File(
        override val id: String,
        override val timestamp: Long,
        override val attachmentFile: AttachmentFile,
        override val isFileExists: Boolean = false,
        override val isDownloading: Boolean = false,
        override val showDelivered: Boolean = false
    ) : VisitorAttachmentItem(ChatAdapter.VISITOR_FILE_VIEW_TYPE) {
        override fun withDeliveredStatus(delivered: Boolean): VisitorAttachmentItem = copy(showDelivered = delivered)

        override fun updateWith(isFileExists: Boolean, isDownloading: Boolean): ChatItem =
            copy(isFileExists = isFileExists, isDownloading = isDownloading)
    }
}

internal sealed class VisitorMessageItem : ChatItem(ChatAdapter.VISITOR_MESSAGE_TYPE) {
    val showDelivered: Boolean
        get() = this is Delivered

    abstract val message: String

    data class New(
        override val id: String,
        override val timestamp: Long,
        override val message: String
    ) : VisitorMessageItem()

    data class History(
        override val id: String,
        override val timestamp: Long,
        override val message: String
    ) : VisitorMessageItem()

    data class Unsent(
        override val id: String,
        override val timestamp: Long,
        override val message: String
    ) : VisitorMessageItem()

    data class Delivered(
        override val id: String,
        override val timestamp: Long,
        override val message: String
    ) : VisitorMessageItem()
}
