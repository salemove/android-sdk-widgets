package com.glia.widgets.chat.adapter

import android.view.View
import android.view.ViewGroup
import androidx.annotation.IntDef
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.RecyclerView
import com.glia.androidsdk.chat.AttachmentFile
import com.glia.widgets.R
import com.glia.widgets.UiTheme
import com.glia.widgets.chat.adapter.holder.CustomCardViewHolder
import com.glia.widgets.chat.adapter.holder.MediaUpgradeStartedViewHolder
import com.glia.widgets.chat.adapter.holder.NewMessagesDividerViewHolder
import com.glia.widgets.chat.adapter.holder.OperatorMessageViewHolder
import com.glia.widgets.chat.adapter.holder.OperatorStatusViewHolder
import com.glia.widgets.chat.adapter.holder.SystemMessageViewHolder
import com.glia.widgets.chat.adapter.holder.VisitorMessageViewHolder
import com.glia.widgets.chat.adapter.holder.fileattachment.OperatorFileAttachmentViewHolder
import com.glia.widgets.chat.adapter.holder.fileattachment.VisitorFileAttachmentViewHolder
import com.glia.widgets.chat.adapter.holder.imageattachment.ImageAttachmentViewHolder
import com.glia.widgets.chat.adapter.holder.imageattachment.OperatorImageAttachmentViewHolder
import com.glia.widgets.chat.adapter.holder.imageattachment.VisitorImageAttachmentViewHolder
import com.glia.widgets.chat.model.history.ChatItem
import com.glia.widgets.chat.model.history.CustomCardItem
import com.glia.widgets.chat.model.history.MediaUpgradeStartedTimerItem
import com.glia.widgets.chat.model.history.OperatorAttachmentItem
import com.glia.widgets.chat.model.history.OperatorMessageItem
import com.glia.widgets.chat.model.history.OperatorStatusItem
import com.glia.widgets.chat.model.history.SystemChatItem
import com.glia.widgets.chat.model.history.VisitorAttachmentItem
import com.glia.widgets.chat.model.history.VisitorMessageItem
import com.glia.widgets.databinding.ChatMediaUpgradeLayoutBinding
import com.glia.widgets.databinding.ChatNewMessagesDividerLayoutBinding
import com.glia.widgets.databinding.ChatOperatorMessageLayoutBinding
import com.glia.widgets.databinding.ChatOperatorStatusLayoutBinding
import com.glia.widgets.databinding.ChatReceiveMessageContentBinding
import com.glia.widgets.databinding.ChatVisitorMessageLayoutBinding
import com.glia.widgets.filepreview.domain.usecase.GetImageFileFromCacheUseCase
import com.glia.widgets.filepreview.domain.usecase.GetImageFileFromDownloadsUseCase
import com.glia.widgets.filepreview.domain.usecase.GetImageFileFromNetworkUseCase
import com.glia.widgets.helper.layoutInflater
import com.glia.widgets.view.SingleChoiceCardView.OnOptionClickedListener

internal class ChatAdapter(
    private val uiTheme: UiTheme,
    private val onOptionClickedListener: OnOptionClickedListener,
    private val onFileItemClickListener: OnFileItemClickListener,
    private val onImageItemClickListener: OnImageItemClickListener,
    private val onCustomCardResponse: OnCustomCardResponse,
    private val customCardAdapter: CustomCardAdapter?,
    private val getImageFileFromCacheUseCase: GetImageFileFromCacheUseCase,
    private val getImageFileFromDownloadsUseCase: GetImageFileFromDownloadsUseCase,
    private val getImageFileFromNetworkUseCase: GetImageFileFromNetworkUseCase
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val differ = AsyncListDiffer(this, ChatAdapterDillCallback())

    override fun onCreateViewHolder(
        parent: ViewGroup,
        @Type viewType: Int
    ): RecyclerView.ViewHolder {
        val inflater = parent.layoutInflater
        return when (viewType) {
            OPERATOR_STATUS_VIEW_TYPE -> {
                OperatorStatusViewHolder(
                    ChatOperatorStatusLayoutBinding.inflate(inflater, parent, false),
                    uiTheme
                )
            }
            VISITOR_FILE_VIEW_TYPE -> {
                val view =
                    inflater.inflate(R.layout.chat_attachment_visitor_file_layout, parent, false)
                VisitorFileAttachmentViewHolder(view, uiTheme)
            }
            VISITOR_IMAGE_VIEW_TYPE -> {
                VisitorImageAttachmentViewHolder(
                    inflater.inflate(R.layout.chat_attachment_visitor_image_layout, parent, false),
                    uiTheme,
                    getImageFileFromCacheUseCase,
                    getImageFileFromDownloadsUseCase,
                    getImageFileFromNetworkUseCase
                )
            }
            VISITOR_MESSAGE_TYPE -> {
                VisitorMessageViewHolder(
                    ChatVisitorMessageLayoutBinding.inflate(inflater, parent, false),
                    uiTheme
                )
            }
            OPERATOR_IMAGE_VIEW_TYPE -> {
                OperatorImageAttachmentViewHolder(
                    inflater.inflate(R.layout.chat_attachment_operator_image_layout, parent, false),
                    uiTheme,
                    getImageFileFromCacheUseCase,
                    getImageFileFromDownloadsUseCase,
                    getImageFileFromNetworkUseCase
                )
            }
            OPERATOR_FILE_VIEW_TYPE -> {
                OperatorFileAttachmentViewHolder(
                    inflater.inflate(
                        R.layout.chat_attachment_operator_file_layout,
                        parent,
                        false
                    ),
                    uiTheme
                )
            }
            OPERATOR_MESSAGE_VIEW_TYPE -> {
                OperatorMessageViewHolder(
                    ChatOperatorMessageLayoutBinding.inflate(inflater, parent, false),
                    uiTheme
                )
            }
            MEDIA_UPGRADE_ITEM_TYPE -> {
                MediaUpgradeStartedViewHolder(
                    ChatMediaUpgradeLayoutBinding.inflate(inflater, parent, false),
                    uiTheme
                )
            }
            NEW_MESSAGES_DIVIDER_TYPE -> {
                NewMessagesDividerViewHolder(
                    ChatNewMessagesDividerLayoutBinding.inflate(
                        inflater,
                        parent,
                        false
                    ),
                    uiTheme
                )
            }
            SYSTEM_MESSAGE_TYPE -> SystemMessageViewHolder(
                ChatReceiveMessageContentBinding.inflate(
                    inflater,
                    parent,
                    false
                ),
                uiTheme
            )
            else -> {
                var customCardViewHolder: CustomCardViewHolder? = null
                if (customCardAdapter != null) {
                    customCardViewHolder =
                        customCardAdapter.getCustomCardViewHolder(
                            parent,
                            inflater,
                            uiTheme,
                            viewType
                        )
                }
                customCardViewHolder
                    ?: throw IllegalArgumentException("Unknown view type: $viewType")
            }
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (differ.currentList[position] is MediaUpgradeStartedTimerItem) {
            val time = (payloads.firstOrNull() as? String)

            if (time != null) {
                (holder as? MediaUpgradeStartedViewHolder)?.updateTime(time)
                return
            }
        }
        super.onBindViewHolder(holder, position, payloads)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val chatItem = differ.currentList[position]) {
            is OperatorStatusItem -> (holder as OperatorStatusViewHolder).bind(chatItem)
            is VisitorMessageItem -> (holder as VisitorMessageViewHolder).bind(chatItem)
            is OperatorMessageItem -> (holder as OperatorMessageViewHolder).bind(
                chatItem,
                onOptionClickedListener
            )
            is MediaUpgradeStartedTimerItem -> (holder as MediaUpgradeStartedViewHolder).bind(
                chatItem
            )
            is OperatorAttachmentItem -> {
                if (chatItem.getViewType() == OPERATOR_FILE_VIEW_TYPE) {
                    (holder as OperatorFileAttachmentViewHolder).bind(
                        chatItem,
                        onFileItemClickListener
                    )
                } else {
                    (holder as OperatorImageAttachmentViewHolder).bind(
                        chatItem,
                        onImageItemClickListener
                    )
                }
            }
            is VisitorAttachmentItem -> {
                if (chatItem.getViewType() == VISITOR_FILE_VIEW_TYPE) {
                    (holder as VisitorFileAttachmentViewHolder).bind(
                        chatItem,
                        onFileItemClickListener
                    )
                } else {
                    val viewHolder = holder as VisitorImageAttachmentViewHolder
                    viewHolder.bind(chatItem.attachmentFile, chatItem.showDelivered)
                    viewHolder.itemView.setOnClickListener {
                        onImageItemClickListener.onImageItemClick(chatItem.attachmentFile, it)
                    }
                }
            }
            is SystemChatItem -> (holder as SystemMessageViewHolder).bind(chatItem.message)
            is CustomCardItem -> {
                (holder as CustomCardViewHolder).bind(chatItem.message) { text: String?, value: String? ->
                    onCustomCardResponse.onCustomCardResponse(chatItem.getId(), text, value)
                }
            }
        }
    }

    override fun getItemCount(): Int = differ.currentList.size

    override fun getItemViewType(position: Int): Int = differ.currentList[position].viewType

    override fun getItemId(position: Int): Long = position.toLong()

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)
        if (holder is ImageAttachmentViewHolder) {
            holder.onStopView()
        }
    }

    fun submitList(items: List<ChatItem>?) {
        differ.submitList(items)
    }

    val currentList: List<ChatItem>
        get() = differ.currentList

    interface OnFileItemClickListener {
        fun onFileOpenClick(file: AttachmentFile)
        fun onFileDownloadClick(file: AttachmentFile)
    }

    interface OnImageItemClickListener {
        fun onImageItemClick(item: AttachmentFile, view: View)
    }

    fun interface OnCustomCardResponse {
        fun onCustomCardResponse(messageId: String, text: String?, value: String?)
    }

    companion object {
        const val OPERATOR_STATUS_VIEW_TYPE = 0
        const val VISITOR_MESSAGE_TYPE = 1
        const val OPERATOR_MESSAGE_VIEW_TYPE = 2
        const val MEDIA_UPGRADE_ITEM_TYPE = 3
        const val OPERATOR_FILE_VIEW_TYPE = 4
        const val OPERATOR_IMAGE_VIEW_TYPE = 5
        const val VISITOR_FILE_VIEW_TYPE = 6
        const val VISITOR_IMAGE_VIEW_TYPE = 7
        const val NEW_MESSAGES_DIVIDER_TYPE = 8
        const val SYSTEM_MESSAGE_TYPE = 9
        const val CUSTOM_CARD_TYPE = 10 // Should be the last type with the highest value
    }

    @IntDef(
        OPERATOR_STATUS_VIEW_TYPE,
        VISITOR_MESSAGE_TYPE,
        OPERATOR_MESSAGE_VIEW_TYPE,
        MEDIA_UPGRADE_ITEM_TYPE,
        OPERATOR_FILE_VIEW_TYPE,
        OPERATOR_IMAGE_VIEW_TYPE,
        VISITOR_FILE_VIEW_TYPE,
        VISITOR_IMAGE_VIEW_TYPE,
        NEW_MESSAGES_DIVIDER_TYPE,
        SYSTEM_MESSAGE_TYPE,
        CUSTOM_CARD_TYPE
    )
    @Retention(AnnotationRetention.SOURCE)
    annotation class Type
}
