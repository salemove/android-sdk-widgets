package com.glia.widgets.chat.model

import android.content.Context
import android.text.format.DateUtils
import com.glia.androidsdk.GliaException
import com.glia.androidsdk.chat.AttachmentFile
import com.glia.androidsdk.chat.ChatMessage
import com.glia.androidsdk.chat.SingleChoiceOption
import com.glia.widgets.chat.adapter.ChatAdapter
import com.glia.widgets.core.fileupload.model.FileAttachment
import com.glia.widgets.helper.isDownloaded
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

internal sealed class Attachment(val id: String) {
    val remoteAttachment: AttachmentFile? get() = (this as? Remote)?.attachmentFile
    val localAttachment: FileAttachment? get() = (this as? Local)?.fileAttachment

    data class Remote(val attachmentFile: AttachmentFile) : Attachment(attachmentFile.id)
    data class Local(val fileAttachment: FileAttachment) : Attachment(UUID.randomUUID().toString())
}

internal interface AttachmentItem {
    val attachment: Attachment
    val isFileExists: Boolean
    val isDownloading: Boolean

    val attachmentId: String get() = attachment.id

    fun isDownloaded(context: Context): Boolean = when (val attachment = attachment) {
        is Attachment.Remote -> attachment.attachmentFile.isDownloaded(context)
        is Attachment.Local -> false
    }

    fun updateWith(isFileExists: Boolean, isDownloading: Boolean): ChatItem
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

internal sealed class OperatorAttachmentItem(@ChatAdapter.Type viewType: Int) : OperatorChatItem(viewType), AttachmentItem {

    data class Image(
        override val isFileExists: Boolean = false,
        override val isDownloading: Boolean = false,
        override val attachment: Attachment,
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
        override val attachment: Attachment,
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

    object InQueue : OperatorStatusItem()

    data class Connected(
        val operatorName: String,
        val profileImgUrl: String?
    ) : OperatorStatusItem()

    data class Joined(
        val operatorName: String,
        val profileImgUrl: String?
    ) : OperatorStatusItem()

    object Transferring : OperatorStatusItem()
}

// Visitor

internal abstract class VisitorChatItem(@ChatAdapter.Type viewType: Int) : ChatItem(viewType) {
    abstract val showDelivered: Boolean
    abstract val showError: Boolean
    abstract fun withDeliveredStatus(delivered: Boolean): VisitorChatItem
}

internal sealed class VisitorAttachmentItem(@ChatAdapter.Type viewType: Int) : VisitorChatItem(viewType), AttachmentItem {

    data class Image(
        override val id: String,
        override val timestamp: Long = System.currentTimeMillis(),
        override val attachment: Attachment,
        override val isFileExists: Boolean = false,
        override val isDownloading: Boolean = false,
        override val showDelivered: Boolean = false,
        override val showError: Boolean = false
    ) : VisitorAttachmentItem(ChatAdapter.VISITOR_IMAGE_VIEW_TYPE) {
        override fun withDeliveredStatus(delivered: Boolean): VisitorChatItem = copy(showDelivered = delivered)

        override fun updateWith(isFileExists: Boolean, isDownloading: Boolean): ChatItem =
            copy(isFileExists = isFileExists, isDownloading = isDownloading)
    }

    data class File(
        override val id: String,
        override val timestamp: Long = System.currentTimeMillis(),
        override val attachment: Attachment,
        override val isFileExists: Boolean = false,
        override val isDownloading: Boolean = false,
        override val showDelivered: Boolean = false,
        override val showError: Boolean = false
    ) : VisitorAttachmentItem(ChatAdapter.VISITOR_FILE_VIEW_TYPE) {
        override fun withDeliveredStatus(delivered: Boolean): VisitorChatItem = copy(showDelivered = delivered)

        override fun updateWith(isFileExists: Boolean, isDownloading: Boolean): ChatItem =
            copy(isFileExists = isFileExists, isDownloading = isDownloading)
    }
}

internal data class VisitorMessageItem(
    val message: String,
    override val id: String = UUID.randomUUID().toString(),
    override val timestamp: Long = System.currentTimeMillis(),
    override val showDelivered: Boolean = false,
    override val showError: Boolean = false
) : VisitorChatItem(ChatAdapter.VISITOR_MESSAGE_TYPE) {

    override fun withDeliveredStatus(delivered: Boolean): VisitorChatItem {
        check(!delivered) { "The method should be called only with false value, to hide delivered status" }
        return copy(showDelivered = delivered)
    }
}

internal data class Unsent(
    val payload: SendMessagePayload,
    val error: GliaException? = null
) {
    val messageId: String = payload.messageId
    val content: String = payload.content

    private val fileAttachments: List<FileAttachment>? = payload.fileAttachments
    private val hasFileAttachments: Boolean = !fileAttachments.isNullOrEmpty()

    val chatMessage: VisitorMessageItem? = if (content.isNotEmpty()) VisitorMessageItem(
        id = messageId,
        message = content,
        showError = error != null && !hasFileAttachments
    ) else null

    val attachmentItems: List<VisitorAttachmentItem>? = fileAttachments?.map {
        val showError = error != null && fileAttachments.indexOf(it) == fileAttachments.lastIndex
        val attachment = Attachment.Local(it)
        if (it.isImage) {
            VisitorAttachmentItem.Image(
                id = messageId,
                attachment = attachment,
                showError = showError
            )
        } else {
            VisitorAttachmentItem.File(
                id = messageId,
                attachment = attachment,
                showError = showError
            )
        }
    }
}
