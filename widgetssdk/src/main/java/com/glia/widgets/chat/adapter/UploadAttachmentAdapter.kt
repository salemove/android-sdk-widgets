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
import coil3.load
import com.glia.widgets.R
import com.glia.widgets.databinding.ChatAttachmentUploadedItemBinding
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.getColorCompat
import com.glia.widgets.helper.getColorStateListCompat
import com.glia.widgets.helper.layoutInflater
import com.glia.widgets.helper.setLocaleContentDescription
import com.glia.widgets.helper.toFileExtensionOrEmpty
import com.glia.widgets.internal.fileupload.model.LocalAttachment
import com.glia.widgets.internal.fileupload.model.LocalAttachment.Status
import com.glia.widgets.locale.StringKey
import com.glia.widgets.locale.StringKeyPair
import com.glia.widgets.view.unifiedui.applyCardLayerTheme
import com.glia.widgets.view.unifiedui.applyColorTheme
import com.glia.widgets.view.unifiedui.applyImageColorTheme
import com.glia.widgets.view.unifiedui.applyTextTheme
import com.glia.widgets.view.unifiedui.theme.chat.FilePreviewTheme
import com.glia.widgets.view.unifiedui.theme.chat.FileUploadBarTheme
import com.glia.widgets.view.unifiedui.theme.chat.UploadFileTheme
import com.google.android.material.card.MaterialCardView
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.R as Material_R

/**
 * [DiffUtil.ItemCallback] for [LocalAttachment] type
 */
internal class UploadAttachmentItemCallback : DiffUtil.ItemCallback<LocalAttachment>() {
    override fun areItemsTheSame(oldItem: LocalAttachment, newItem: LocalAttachment): Boolean =
        oldItem.uri == newItem.uri

    override fun areContentsTheSame(oldItem: LocalAttachment, newItem: LocalAttachment): Boolean =
        oldItem.isReadyToSend == newItem.isReadyToSend && oldItem.attachmentStatus == newItem.attachmentStatus
}

/**
 * Upload File Attachment Adapter
 */
internal class UploadAttachmentAdapter(private val isMessageCenter: Boolean = false) :
    ListAdapter<LocalAttachment, ViewHolder>(UploadAttachmentItemCallback()) {
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
        fun onRemoveItemClicked(attachment: LocalAttachment)
    }
}

/**
 * ViewHolder
 */
internal class ViewHolder(
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
    private val localeProvider = Dependencies.localeProvider

    private val theme: FileUploadBarTheme? by lazy {
        Dependencies.gliaThemeManager.theme?.run {
            if (isMessageCenter) {
                secureMessagingWelcomeScreenTheme?.attachmentListTheme
            } else {
                chatTheme?.input?.fileUploadBar
            }
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

    fun onBind(attachment: LocalAttachment?, callback: UploadAttachmentAdapter.ItemCallback?) {
        val displayName = attachment?.displayName ?: return

        val size = Formatter.formatFileSize(context, attachment.size)

        removeItemButton.setOnClickListener { callback?.onRemoveItemClicked(attachment) }

        setProgressIndicatorState(attachment.attachmentStatus)

        updateTitleAndStatusText(displayName, size, attachment.attachmentStatus)

        updateExtensionType(attachment, displayName)

        updateContentDescription(displayName, size, attachment)
    }

    private fun updateExtensionType(
        attachment: LocalAttachment,
        displayName: String
    ) {
        if (attachment.attachmentStatus.isError) {
            extensionContainerView.setCardBackgroundColor(
                itemView.getColorStateListCompat(R.color.glia_neutral_color)
            )
            showExtensionTypeImage()
            extensionTypeImage.setImageResource(R.drawable.ic_info)
            extensionTypeImage.scaleType = ImageView.ScaleType.CENTER_INSIDE

            extensionContainerView.applyCardLayerTheme(filePreviewTheme?.errorBackground)
            extensionTypeImage.applyImageColorTheme(filePreviewTheme?.errorIcon)
        } else {
            extensionContainerView.setCardBackgroundColor(
                itemView.getColorCompat(R.color.glia_primary_color)
            )
            extensionTypeImage.scaleType = ImageView.ScaleType.FIT_XY
            extensionTypeImage.imageTintList = null

            if (attachment.isImage) {
                showExtensionTypeImage()
                extensionTypeImage.load(attachment.uri)
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
        displayName: String,
        size: String?,
        attachment: LocalAttachment
    ) {
        removeItemButton.setLocaleContentDescription(
            R.string.chat_file_remove_upload_accessibility_label,
            StringKeyPair(StringKey.NAME, displayName)
        )
        itemView.setLocaleContentDescription(
            R.string.android_chat_file_accessibility,
            StringKeyPair(StringKey.NAME, displayName),
            StringKeyPair(StringKey.SIZE, size ?: localeProvider.getRemoteString(R.string.general_unknown)),
            StringKeyPair(StringKey.STATUS, getStatusIndicatorText(attachment.attachmentStatus))
        )
    }

    private fun updateTitleAndStatusText(fileName: String, byteSize: String, status: Status) {
        val textColorRes =
            if (status.isError) Material_R.color.design_default_color_error else R.color.glia_normal_color
        titleText.setTextColor(itemView.getColorCompat(textColorRes))

        titleText.text =
            if (status.isError) localeProvider.getString(getTitleErrorStringRes(status)) else "$fileName • $byteSize"

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
        Status.ERROR_NETWORK_TIMEOUT -> R.string.android_upload_error_network
        Status.ERROR_INVALID_INPUT -> R.string.android_upload_error_invalid_input
        Status.ERROR_PERMISSIONS_DENIED -> R.string.android_upload_error_permissions
        Status.ERROR_FORMAT_UNSUPPORTED -> R.string.chat_attachment_unsupported_file
        Status.ERROR_FILE_TOO_LARGE -> R.string.chat_file_size_limit_error
        Status.ERROR_ENGAGEMENT_MISSING -> R.string.android_upload_error_engagement_missing
        Status.ERROR_SECURITY_SCAN_FAILED -> R.string.chat_file_infected_file_error
        Status.ERROR_FILE_UPLOAD_FORBIDDEN -> R.string.android_upload_error_forbidden
        Status.ERROR_SUPPORTED_FILE_ATTACHMENT_COUNT_EXCEEDED -> R.string.android_upload_error_file_limit
        else -> R.string.error_internal
    }

    private fun setProgressIndicatorState(status: Status) {
        val normalColor = theme?.progress?.primaryColor
            ?: itemView.getColorCompat(R.color.glia_primary_color)
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
            isIndeterminate = inDeterminate
            setIndicatorColor(indicatorColor)
            progress = progressValue
        }
    }

    private fun getStatusIndicatorText(status: Status): String =
        localeProvider.getString(
            when {
                status == Status.SECURITY_SCAN -> R.string.chat_file_upload_scanning
                status == Status.READY_TO_SEND -> R.string.chat_file_upload_success
                status.isError -> R.string.chat_file_upload_failed
                status == Status.UPLOADING -> R.string.chat_file_upload_in_progress
                else -> R.string.chat_file_upload_in_progress
            }
        )
}
