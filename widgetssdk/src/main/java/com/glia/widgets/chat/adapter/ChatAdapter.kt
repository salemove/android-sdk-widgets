package com.glia.widgets.chat.adapter

import android.view.View
import android.view.ViewGroup
import androidx.annotation.IntDef
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.glia.androidsdk.chat.AttachmentFile
import com.glia.widgets.UiTheme
import com.glia.widgets.chat.adapter.holder.CustomCardViewHolder
import com.glia.widgets.chat.adapter.holder.DeliveredItemViewHolder
import com.glia.widgets.chat.adapter.holder.GvaGalleryViewHolder
import com.glia.widgets.chat.adapter.holder.GvaPersistentButtonsViewHolder
import com.glia.widgets.chat.adapter.holder.GvaResponseTextViewHolder
import com.glia.widgets.chat.adapter.holder.MediaUpgradeStartedViewHolder
import com.glia.widgets.chat.adapter.holder.NewMessagesDividerViewHolder
import com.glia.widgets.chat.adapter.holder.OperatorMessageViewHolder
import com.glia.widgets.chat.adapter.holder.OperatorStatusViewHolder
import com.glia.widgets.chat.adapter.holder.SystemMessageViewHolder
import com.glia.widgets.chat.adapter.holder.TapToRetryItemViewHolder
import com.glia.widgets.chat.adapter.holder.VisitorMessageViewHolder
import com.glia.widgets.chat.adapter.holder.fileattachment.OperatorFileAttachmentViewHolder
import com.glia.widgets.chat.adapter.holder.fileattachment.VisitorFileAttachmentViewHolder
import com.glia.widgets.chat.adapter.holder.imageattachment.ImageAttachmentViewHolder
import com.glia.widgets.chat.adapter.holder.imageattachment.OperatorImageAttachmentViewHolder
import com.glia.widgets.chat.adapter.holder.imageattachment.VisitorImageAttachmentViewHolder
import com.glia.widgets.chat.model.ChatItem
import com.glia.widgets.chat.model.CustomCardChatItem
import com.glia.widgets.chat.model.GvaButton
import com.glia.widgets.chat.model.GvaGalleryCards
import com.glia.widgets.chat.model.GvaPersistentButtons
import com.glia.widgets.chat.model.GvaQuickReplies
import com.glia.widgets.chat.model.GvaResponseText
import com.glia.widgets.chat.model.MediaUpgradeStartedTimerItem
import com.glia.widgets.chat.model.OperatorAttachmentItem
import com.glia.widgets.chat.model.OperatorMessageItem
import com.glia.widgets.chat.model.OperatorStatusItem
import com.glia.widgets.chat.model.SystemChatItem
import com.glia.widgets.chat.model.TapToRetryItem
import com.glia.widgets.chat.model.VisitorAttachmentItem
import com.glia.widgets.chat.model.VisitorMessageItem
import com.glia.widgets.core.fileupload.model.LocalAttachment
import com.glia.widgets.databinding.ChatAttachmentOperatorFileLayoutBinding
import com.glia.widgets.databinding.ChatAttachmentOperatorImageLayoutBinding
import com.glia.widgets.databinding.ChatAttachmentVisitorFileLayoutBinding
import com.glia.widgets.databinding.ChatAttachmentVisitorImageLayoutBinding
import com.glia.widgets.databinding.ChatDeliveredItemLayoutBinding
import com.glia.widgets.databinding.ChatGvaGalleryLayoutBinding
import com.glia.widgets.databinding.ChatGvaPersistentButtonsContentBinding
import com.glia.widgets.databinding.ChatMediaUpgradeLayoutBinding
import com.glia.widgets.databinding.ChatNewMessagesDividerLayoutBinding
import com.glia.widgets.databinding.ChatOperatorMessageLayoutBinding
import com.glia.widgets.databinding.ChatOperatorStatusLayoutBinding
import com.glia.widgets.databinding.ChatReceiveMessageContentBinding
import com.glia.widgets.databinding.ChatTapToRetryItemLayoutBinding
import com.glia.widgets.databinding.ChatVisitorMessageLayoutBinding
import com.glia.widgets.di.Dependencies
import com.glia.widgets.filepreview.domain.usecase.GetImageFileFromCacheUseCase
import com.glia.widgets.filepreview.domain.usecase.GetImageFileFromDownloadsUseCase
import com.glia.widgets.filepreview.domain.usecase.GetImageFileFromNetworkUseCase
import com.glia.widgets.helper.layoutInflater
import com.glia.widgets.helper.rx.Schedulers
import com.glia.widgets.view.SingleChoiceCardView.OnOptionClickedListener

internal class ChatAdapter(
    private val uiTheme: UiTheme,
    private val onRetryClickListener: OnRetryClickListener,
    private val onOptionClickedListener: OnOptionClickedListener,
    private val onFileItemClickListener: OnFileItemClickListener,
    private val onImageItemClickListener: OnImageItemClickListener,
    private val onCustomCardResponse: OnCustomCardResponse,
    private val onGvaButtonsClickListener: OnGvaButtonsClickListener,
    private val chatItemHeightManager: ChatItemHeightManager,
    private val customCardAdapter: CustomCardAdapter?,
    private val getImageFileFromCacheUseCase: GetImageFileFromCacheUseCase,
    private val getImageFileFromDownloadsUseCase: GetImageFileFromDownloadsUseCase,
    private val getImageFileFromNetworkUseCase: GetImageFileFromNetworkUseCase,
    private val schedulers: Schedulers = Dependencies.schedulers
) : ListAdapter<ChatItem, RecyclerView.ViewHolder>(ChatAdapterDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, @Type viewType: Int): RecyclerView.ViewHolder {
        val inflater = parent.layoutInflater
        return when (viewType) {
            OPERATOR_STATUS_VIEW_TYPE ->
                OperatorStatusViewHolder(ChatOperatorStatusLayoutBinding.inflate(inflater, parent, false), uiTheme)

            VISITOR_FILE_VIEW_TYPE ->
                VisitorFileAttachmentViewHolder(
                    ChatAttachmentVisitorFileLayoutBinding.inflate(inflater, parent, false),
                    onFileItemClickListener,
                    onRetryClickListener
                )

            VISITOR_IMAGE_VIEW_TYPE -> {
                VisitorImageAttachmentViewHolder(
                    ChatAttachmentVisitorImageLayoutBinding.inflate(inflater, parent, false),
                    getImageFileFromCacheUseCase,
                    getImageFileFromDownloadsUseCase,
                    getImageFileFromNetworkUseCase,
                    schedulers,
                    onRetryClickListener,
                    onImageItemClickListener
                )
            }

            VISITOR_MESSAGE_TYPE -> {
                VisitorMessageViewHolder(
                    ChatVisitorMessageLayoutBinding.inflate(inflater, parent, false),
                    onRetryClickListener,
                    uiTheme
                )
            }

            OPERATOR_IMAGE_VIEW_TYPE -> {
                OperatorImageAttachmentViewHolder(
                    ChatAttachmentOperatorImageLayoutBinding.inflate(inflater, parent, false),
                    getImageFileFromCacheUseCase,
                    getImageFileFromDownloadsUseCase,
                    getImageFileFromNetworkUseCase,
                    schedulers,
                    uiTheme,
                    onImageItemClickListener
                )
            }

            OPERATOR_FILE_VIEW_TYPE ->
                OperatorFileAttachmentViewHolder(
                    ChatAttachmentOperatorFileLayoutBinding.inflate(inflater, parent, false),
                    uiTheme,
                    onFileItemClickListener
                )

            OPERATOR_MESSAGE_VIEW_TYPE ->
                OperatorMessageViewHolder(ChatOperatorMessageLayoutBinding.inflate(inflater, parent, false), uiTheme)

            MEDIA_UPGRADE_ITEM_TYPE ->
                MediaUpgradeStartedViewHolder(ChatMediaUpgradeLayoutBinding.inflate(inflater, parent, false), uiTheme)

            NEW_MESSAGES_DIVIDER_TYPE ->
                NewMessagesDividerViewHolder(ChatNewMessagesDividerLayoutBinding.inflate(inflater, parent, false), uiTheme)

            SYSTEM_MESSAGE_TYPE -> SystemMessageViewHolder(ChatReceiveMessageContentBinding.inflate(inflater, parent, false), uiTheme)

            GVA_RESPONSE_TEXT_TYPE, GVA_QUICK_REPLIES_TYPE -> {
                val operatorMessageBinding = ChatOperatorMessageLayoutBinding.inflate(inflater, parent, false)
                GvaResponseTextViewHolder(
                    operatorMessageBinding,
                    ChatReceiveMessageContentBinding.inflate(
                        inflater,
                        operatorMessageBinding.contentLayout,
                        true
                    ),
                    uiTheme
                )
            }

            GVA_PERSISTENT_BUTTONS_TYPE -> {
                val operatorMessageBinding = ChatOperatorMessageLayoutBinding.inflate(inflater, parent, false)
                GvaPersistentButtonsViewHolder(
                    operatorMessageBinding,
                    ChatGvaPersistentButtonsContentBinding.inflate(
                        inflater,
                        operatorMessageBinding.contentLayout,
                        true
                    ),
                    onGvaButtonsClickListener,
                    uiTheme
                )
            }

            GVA_GALLERY_CARDS_TYPE ->
                GvaGalleryViewHolder(ChatGvaGalleryLayoutBinding.inflate(inflater, parent, false), onGvaButtonsClickListener, uiTheme)

            DELIVERED_ITEM_TYPE -> DeliveredItemViewHolder(ChatDeliveredItemLayoutBinding.inflate(inflater, parent, false), uiTheme)

            TAP_TO_RETRY_ITEM_TYPE -> TapToRetryItemViewHolder(
                ChatTapToRetryItemLayoutBinding.inflate(inflater, parent, false),
                uiTheme,
                onRetryClickListener
            )

            else -> {
                var customCardViewHolder: CustomCardViewHolder? = null
                if (customCardAdapter != null) {
                    customCardViewHolder = customCardAdapter.getCustomCardViewHolder(parent, inflater, viewType)
                }
                customCardViewHolder ?: throw IllegalArgumentException("Unknown view type: $viewType")
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>) {
        val isHandled: Boolean = when (holder) {
            is MediaUpgradeStartedViewHolder -> updateMediaUpgradeStartedViewHolder(payloads, holder)
            is VisitorMessageViewHolder -> updateVisitorMessageViewHolder(payloads, holder)
            is VisitorFileAttachmentViewHolder -> updateVisitorFileAttachmentViewHolder(payloads, holder, getItem(position))
            is VisitorImageAttachmentViewHolder -> updateVisitorImageAttachmentViewHolder(payloads, holder)
            is OperatorFileAttachmentViewHolder -> updateOperatorFileAttachmentViewHolder(payloads, holder, getItem(position))
            else -> false
        }

        if (!isHandled) {
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    private fun updateVisitorMessageViewHolder(payloads: MutableList<Any>, holder: VisitorMessageViewHolder): Boolean =
        when (val payload = payloads.lastOrNull { it is ChatAdapterPayload }) {
            is ChatAdapterPayload.MessageUpdated -> {
                holder.updateStatus(payload.isError, payload.message)
                true
            }

            else -> false
        }

    private fun updateOperatorFileAttachmentViewHolder(
        payloads: MutableList<Any>,
        holder: OperatorFileAttachmentViewHolder,
        chatItem: ChatItem
    ): Boolean {
        return when (payloads.lastOrNull { it is ChatAdapterPayload }) {
            ChatAdapterPayload.RemoteAttachmentStatusChanged -> {
                holder.bind(chatItem as? OperatorAttachmentItem.File ?: return false)
                true
            }

            else -> false
        }
    }

    private fun updateVisitorFileAttachmentViewHolder(
        payloads: MutableList<Any>,
        holder: VisitorFileAttachmentViewHolder,
        chatItem: ChatItem
    ): Boolean {
        return when (val payload = payloads.lastOrNull { it is ChatAdapterPayload }) {
            is ChatAdapterPayload.ErrorStateChanged -> {
                holder.updateStatus(payload.isError)
                true
            }

            ChatAdapterPayload.RemoteAttachmentStatusChanged -> {
                holder.bind(chatItem as? VisitorAttachmentItem.RemoteFile ?: return false)
                true
            }

            else -> false
        }
    }

    private fun updateVisitorImageAttachmentViewHolder(payloads: MutableList<Any>, holder: VisitorImageAttachmentViewHolder): Boolean =
        when (val payload = payloads.lastOrNull { it is ChatAdapterPayload }) {
            is ChatAdapterPayload.ErrorStateChanged -> {
                holder.updateStatus(payload.isError, onRetryClickListener)
                true
            }

            else -> false
        }

    private fun updateMediaUpgradeStartedViewHolder(payloads: MutableList<Any>, viewHolder: MediaUpgradeStartedViewHolder): Boolean =
        when (val payload = payloads.lastOrNull { it is ChatAdapterPayload }) {
            is ChatAdapterPayload.Time -> {
                viewHolder.updateTime(payload.time)
                true
            }

            else -> false
        }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val chatItem = getItem(position)) {
            is OperatorStatusItem -> (holder as OperatorStatusViewHolder).bind(chatItem)
            is VisitorMessageItem -> (holder as VisitorMessageViewHolder).bind(chatItem)
            is OperatorMessageItem -> (holder as OperatorMessageViewHolder).bind(chatItem, onOptionClickedListener)
            is MediaUpgradeStartedTimerItem -> (holder as MediaUpgradeStartedViewHolder).bind(chatItem)
            is OperatorAttachmentItem.Image -> (holder as OperatorImageAttachmentViewHolder).bind(chatItem)
            is OperatorAttachmentItem.File -> (holder as OperatorFileAttachmentViewHolder).bind(chatItem)
            is VisitorAttachmentItem.RemoteFile -> (holder as VisitorFileAttachmentViewHolder).bind(chatItem)
            is VisitorAttachmentItem.LocalFile -> (holder as VisitorFileAttachmentViewHolder).bind(chatItem)
            is VisitorAttachmentItem.RemoteImage -> (holder as VisitorImageAttachmentViewHolder).bind(chatItem)
            is VisitorAttachmentItem.LocalImage -> (holder as VisitorImageAttachmentViewHolder).bind(chatItem)
            is TapToRetryItem -> (holder as TapToRetryItemViewHolder).bind(chatItem)
            is SystemChatItem -> (holder as SystemMessageViewHolder).bind(chatItem.message)
            is GvaResponseText -> (holder as GvaResponseTextViewHolder).bind(chatItem)
            is GvaQuickReplies -> (holder as GvaResponseTextViewHolder).bind(chatItem.asResponseText())
            is GvaPersistentButtons -> (holder as GvaPersistentButtonsViewHolder).bind(chatItem)
            is GvaGalleryCards -> (holder as GvaGalleryViewHolder).bind(chatItem, chatItemHeightManager.getMeasuredHeight(chatItem))
            is CustomCardChatItem -> {
                (holder as CustomCardViewHolder).bind(CustomCardMessage(chatItem.message)) { text: String, value: String ->
                    onCustomCardResponse.onCustomCardResponse(chatItem, text, value)
                }
            }
        }
    }


    override fun getItemViewType(position: Int): Int = getItem(position).viewType

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)
        if (holder is ImageAttachmentViewHolder) {
            holder.onStopView()
        }
    }

    override fun submitList(items: List<ChatItem>?) {
        chatItemHeightManager.measureHeight(items)
        super.submitList(items)
    }

    interface OnFileItemClickListener {
        fun onFileOpenClick(file: AttachmentFile)
        fun onFileDownloadClick(file: AttachmentFile)
        fun onLocalFileOpenClick(attachment: LocalAttachment)
    }

    interface OnImageItemClickListener {
        fun onImageItemClick(item: AttachmentFile, view: View)
        fun onLocalImageItemClick(attachment: LocalAttachment, view: View)
    }

    fun interface OnCustomCardResponse {
        fun onCustomCardResponse(customCard: CustomCardChatItem, text: String, value: String)
    }

    fun interface OnGvaButtonsClickListener {
        fun onGvaButtonClicked(gvaButton: GvaButton)
    }

    fun interface OnRetryClickListener {
        fun onRetryClicked(messageId: String)
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

        // GVA Types
        const val GVA_RESPONSE_TEXT_TYPE = 10
        const val GVA_QUICK_REPLIES_TYPE = 11
        const val GVA_PERSISTENT_BUTTONS_TYPE = 12
        const val GVA_GALLERY_CARDS_TYPE = 13

        //Statuses
        const val DELIVERED_ITEM_TYPE = 14
        const val TAP_TO_RETRY_ITEM_TYPE = 15

        // Custom Card
        const val CUSTOM_CARD_TYPE = 16 // Should be the last type with the highest value
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
        CUSTOM_CARD_TYPE,
        GVA_RESPONSE_TEXT_TYPE,
        GVA_QUICK_REPLIES_TYPE,
        GVA_PERSISTENT_BUTTONS_TYPE,
        GVA_GALLERY_CARDS_TYPE,
        DELIVERED_ITEM_TYPE,
        TAP_TO_RETRY_ITEM_TYPE
    )
    @Retention(AnnotationRetention.SOURCE)
    annotation class Type
}
