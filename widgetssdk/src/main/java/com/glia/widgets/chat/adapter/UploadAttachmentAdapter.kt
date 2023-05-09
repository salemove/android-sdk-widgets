package com.glia.widgets.chat.adapter

import android.content.Context
import android.text.format.Formatter
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.glia.widgets.R
import com.glia.widgets.core.fileupload.model.FileAttachment
import com.glia.widgets.core.fileupload.model.FileAttachment.Status
import com.glia.widgets.databinding.ChatAttachmentUploadedItemBinding
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.getColorCompat
import com.glia.widgets.helper.getColorStateListCompat
import com.glia.widgets.helper.layoutInflater
import com.glia.widgets.helper.toFileExtensionOrEmpty
import com.glia.widgets.view.unifiedui.applyCardLayerTheme
import com.glia.widgets.view.unifiedui.applyColorTheme
import com.glia.widgets.view.unifiedui.applyImageColorTheme
import com.glia.widgets.view.unifiedui.applyTextTheme
import com.glia.widgets.view.unifiedui.theme.chat.FilePreviewTheme
import com.glia.widgets.view.unifiedui.theme.chat.FileUploadBarTheme
import com.glia.widgets.view.unifiedui.theme.chat.UploadFileTheme
import com.google.android.material.card.MaterialCardView
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.squareup.picasso.Picasso
import com.google.android.material.R as Material_R

/**
 * [DiffUtil.ItemCallback] for [FileAttachment] type
 */
class UploadAttachmentItemCallback : DiffUtil.ItemCallback<FileAttachment>() {
    override fun areItemsTheSame(oldItem: FileAttachment, newItem: FileAttachment): Boolean =
        oldItem.uri == newItem.uri

    override fun areContentsTheSame(oldItem: FileAttachment, newItem: FileAttachment): Boolean =
        oldItem.isReadyToSend == newItem.isReadyToSend && oldItem.attachmentStatus == newItem.attachmentStatus
}

/**
 * Upload File Attachment Adapter
 */
class UploadAttachmentAdapter(private val isMessageCenter: Boolean = false) :
    ListAdapter<FileAttachment, ViewHolder>(UploadAttachmentItemCallback()) {
    private var callback: ItemCallback? = null

    fun setItemCallback(callback: ItemCallback?) {
        this.callback = callback
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(
            ChatAttachmentUploadedItemBinding.inflate(parent.layoutInflater, parent, false),
            isMessageCenter
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.onBind(getItem(position), callback)

    fun interface ItemCallback {
        fun onRemoveItemClicked(attachment: FileAttachment)
    }
}

/**
 * ViewHolder
 */
class ViewHolder(
    private val binding: ChatAttachmentUploadedItemBinding,
    isMessageCenter: Boolean
) : RecyclerView.ViewHolder(binding.root) {

    private val extensionContainerView: MaterialCardView get() = binding.typeIndicatorView
    private val extensionTypeText: TextView get() = binding.typeIndicatorText
    private val extensionTypeImage: ImageView get() = binding.typeIndicatorImage
    private val titleText: TextView get() = binding.itemTitle
    private val statusText: TextView get() = binding.statusIndicator
    private val progressIndicator: LinearProgressIndicator get() = binding.progressIndicator
    private val removeItemButton: ImageButton get() = binding.removeItemButton

    private val context: Context get() = itemView.context

    private val theme: FileUploadBarTheme? by lazy {
        Dependencies.getGliaThemeManager().theme?.run {
            if (isMessageCenter)
                secureConversationsWelcomeScreenTheme?.attachmentListTheme
            else
                chatTheme?.input?.fileUploadBar
        }
    }

    private val filePreviewTheme: FilePreviewTheme? get() = theme?.filePreview

    init {
        if (isMessageCenter) {
            itemView.setBackgroundResource(R.drawable.bg_attachment)
        }
        progressIndicator.applyColorTheme(theme?.progressBackground)
        removeItemButton.applyImageColorTheme(theme?.removeButton)
        extensionTypeText.applyTextTheme(filePreviewTheme?.text, withAlignment = false)
    }

    fun onBind(attachment: FileAttachment?, callback: UploadAttachmentAdapter.ItemCallback?) {
        val displayName = attachment?.displayName ?: return

        val size = Formatter.formatFileSize(context, attachment.size)
        val mimeType = attachment.mimeType

        removeItemButton.setOnClickListener { callback?.onRemoveItemClicked(attachment) }

        setProgressIndicatorState(attachment.attachmentStatus)

        updateTitleAndStatusText(displayName, size, attachment.attachmentStatus)

        updateExtensionType(attachment, mimeType, displayName)

        updateContentDescription(displayName, size, attachment)
    }

    private fun updateExtensionType(
        attachment: FileAttachment, mimeType: String, displayName: String
    ) {

        if (attachment.attachmentStatus.isError) {
            extensionContainerView.setCardBackgroundColor(
                itemView.getColorStateListCompat(R.color.glia_system_agent_bubble_color)
            )
            showExtensionTypeImage()
            extensionTypeImage.setImageResource(R.drawable.ic_info)
            extensionTypeImage.scaleType = ImageView.ScaleType.CENTER_INSIDE

            extensionContainerView.applyCardLayerTheme(filePreviewTheme?.errorBackground)
            extensionTypeImage.applyImageColorTheme(filePreviewTheme?.errorIcon)
        } else {
            extensionContainerView.setCardBackgroundColor(
                itemView.getColorCompat(R.color.glia_brand_primary_color)
            )
            extensionTypeImage.scaleType = ImageView.ScaleType.FIT_XY
            extensionTypeImage.imageTintList = null

            if (mimeType.startsWith("image")) {
                showExtensionTypeImage()
                Picasso.get()
                    .load(attachment.uri)
                    .resize(1024, 1024)
                    .onlyScaleDown()
                    .into(extensionTypeImage)
            } else {
                showExtensionTypeText()
                extensionTypeText.text = displayName.toFileExtensionOrEmpty().uppercase()
            }

            extensionContainerView.applyCardLayerTheme(filePreviewTheme?.background)
        }
    }

    private fun showExtensionTypeText() {
        extensionTypeImage.isVisible = false
        extensionTypeText.isVisible = true
    }

    private fun showExtensionTypeImage() {
        extensionTypeImage.isVisible = true
        extensionTypeText.isVisible = false
    }

    private fun updateContentDescription(
        displayName: String, size: String?, attachment: FileAttachment
    ) {
        removeItemButton.contentDescription = context.getString(
            R.string.glia_chat_attachment_remove_item_content_description, displayName
        )
        itemView.contentDescription = context.getString(
            R.string.glia_chat_attachment_item_content_description,
            displayName,
            size,
            getStatusIndicatorText(attachment.attachmentStatus)
        )
    }

    private fun updateTitleAndStatusText(fileName: String, byteSize: String, status: Status) {
        val textColorRes =
            if (status.isError) Material_R.color.design_default_color_error else R.color.glia_base_normal_color
        titleText.setTextColor(itemView.getColorCompat(textColorRes))

        titleText.text =
            if (status.isError) context.getString(getTitleErrorStringRes(status)) else "$fileName • $byteSize"

        statusText.text = getStatusIndicatorText(status)

        val uploadFileTheme = getUploadFileTheme(status)

        titleText.applyTextTheme(
            uploadFileTheme?.text,
            withBackground = true,
            withAlignment = false
        )

        statusText.applyTextTheme(
            uploadFileTheme?.info,
            withBackground = true,
            withAlignment = false
        )
    }

    private fun getUploadFileTheme(status: Status): UploadFileTheme? = theme?.run {
        when (status) {
            Status.UPLOADING -> uploading
            Status.SECURITY_SCAN -> uploaded
            Status.READY_TO_SEND -> uploaded
            else -> error
        }
    }

    @StringRes
    private fun getTitleErrorStringRes(status: Status): Int = when (status) {
        Status.ERROR_NETWORK_TIMEOUT -> R.string.glia_chat_attachment_upload_error_network_time_out
        Status.ERROR_INVALID_INPUT -> R.string.glia_chat_attachment_upload_error_invalid_input
        Status.ERROR_PERMISSIONS_DENIED -> R.string.glia_chat_attachment_upload_error_read_access_permissions_denied
        Status.ERROR_FORMAT_UNSUPPORTED -> R.string.glia_chat_attachment_upload_error_file_type_invalid
        Status.ERROR_FILE_TOO_LARGE -> R.string.glia_chat_attachment_upload_error_file_size_over_limit
        Status.ERROR_ENGAGEMENT_MISSING -> R.string.glia_chat_attachment_upload_error_engagement_missing
        Status.ERROR_SECURITY_SCAN_FAILED -> R.string.glia_chat_attachment_upload_error_failed_to_check_safety
        Status.ERROR_FILE_UPLOAD_FORBIDDEN -> R.string.glia_chat_attachment_upload_error_file_upload_forbidden
        Status.ERROR_SUPPORTED_FILE_ATTACHMENT_COUNT_EXCEEDED -> R.string.glia_chat_attachment_upload_error_file_count_limit_reached
        else -> R.string.glia_chat_attachment_upload_error_internal_error
    }

    private fun setProgressIndicatorState(status: Status) {

        val normalColor = theme?.progress?.primaryColor
            ?: itemView.getColorCompat(R.color.glia_brand_primary_color)
        val errorColor = theme?.errorProgress?.primaryColor
            ?: itemView.getColorCompat(com.google.android.material.R.color.design_default_color_error)

        when {
            status == Status.UPLOADING -> updateProgress(
                indicatorColor = normalColor,
                progressValue = 0,
                inDeterminate = true
            )
            status == Status.SECURITY_SCAN -> updateProgress(
                indicatorColor = normalColor,
                progressValue = 50,
                inDeterminate = true
            )
            status == Status.READY_TO_SEND -> updateProgress(
                indicatorColor = normalColor,
                progressValue = 100,
                inDeterminate = false
            )
            status.isError ->
                updateProgress(
                    indicatorColor = errorColor,
                    progressValue = 100,
                    inDeterminate = false
                )
        }
    }

    private fun updateProgress(
        @ColorInt indicatorColor: Int,
        progressValue: Int = 0,
        inDeterminate: Boolean = true
    ) {
        progressIndicator.apply {
            setIndicatorColor(indicatorColor)
            progress = progressValue
            isIndeterminate = inDeterminate
        }
    }

    private fun getStatusIndicatorText(status: Status): String =
        when {
            status == Status.SECURITY_SCAN -> context.getString(R.string.glia_chat_attachment_upload_checking_file)
            status == Status.READY_TO_SEND -> context.getString(R.string.glia_chat_attachment_upload_ready_to_send)
            status.isError -> context.getString(
                R.string.glia_chat_attachment_upload_failed_upload
            )
            status == Status.UPLOADING -> context.getString(R.string.glia_chat_attachment_upload_uploading)
            else -> context.getString(R.string.glia_chat_attachment_upload_uploading)
        }
}