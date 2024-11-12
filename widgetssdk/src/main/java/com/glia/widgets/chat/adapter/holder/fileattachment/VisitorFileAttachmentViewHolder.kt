package com.glia.widgets.chat.adapter.holder.fileattachment

import android.text.format.Formatter
import androidx.core.view.isVisible
import com.glia.widgets.R
import com.glia.widgets.UiTheme
import com.glia.widgets.chat.adapter.ChatAdapter.OnFileItemClickListener
import com.glia.widgets.chat.adapter.ChatAdapter.OnRetryClickListener
import com.glia.widgets.chat.model.VisitorAttachmentItem
import com.glia.widgets.chat.model.VisitorItemStatus
import com.glia.widgets.databinding.ChatAttachmentVisitorFileLayoutBinding
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.addClickActionAccessibilityLabel
import com.glia.widgets.helper.getColorCompat
import com.glia.widgets.helper.getFontCompat
import com.glia.widgets.helper.removeAccessibilityClickAction
import com.glia.widgets.helper.setLocaleContentDescription
import com.glia.widgets.helper.setLocaleText
import com.glia.widgets.locale.LocaleProvider
import com.glia.widgets.locale.StringKey
import com.glia.widgets.locale.StringKeyPair
import com.glia.widgets.view.unifiedui.applyTextTheme
import com.glia.widgets.view.unifiedui.theme.UnifiedTheme
import com.glia.widgets.view.unifiedui.theme.chat.MessageBalloonTheme

internal class VisitorFileAttachmentViewHolder(
    private val binding: ChatAttachmentVisitorFileLayoutBinding,
    uiTheme: UiTheme,
    private val onFileItemClickListener: OnFileItemClickListener,
    private val onRetryClickListener: OnRetryClickListener,
    unifiedTheme: UnifiedTheme? = Dependencies.gliaThemeManager.theme,
    private val localeProvider: LocaleProvider = Dependencies.localeProvider
) : FileAttachmentViewHolder(binding.root) {
    private val visitorTheme: MessageBalloonTheme? by lazy { unifiedTheme?.chatTheme?.visitorMessage }
    private var messageId: String? = null
    private var name: String? = null
    private var size: Long? = null

    init {
        setupDeliveredView(uiTheme)
        setupErrorView(uiTheme)
    }

    fun bind(item: VisitorAttachmentItem.LocalFile) {
        messageId = item.messageId
        name = item.attachment.displayName
        size = item.attachment.size

        super.setData(item.attachment)
        setUpState(status = item.status, fileExists = true, name = item.attachment.displayName, size = item.attachment.size)

        binding.attachmentFileView.root.setOnClickListener {
            onFileItemClickListener.onLocalFileOpenClick(item.attachment)
        }
    }

    fun bind(item: VisitorAttachmentItem.RemoteFile) {
        name = item.attachment.name
        size = item.attachment.size

        val isFileExists = isFileExists(item)

        super.setData(isFileExists, item.isDownloading, item.attachment)
        setUpState(status = item.status, fileExists = isFileExists, name = item.attachment.name, size = item.attachment.size)

        val attachmentView = binding.attachmentFileView.root

        when {
            item.isDownloading -> attachmentView.setOnClickListener(null)
            isFileExists -> attachmentView.setOnClickListener { onFileItemClickListener.onFileOpenClick(item.attachment) }
            else -> attachmentView.setOnClickListener { onFileItemClickListener.onFileDownloadClick(item.attachment) }
        }
    }

    private fun isFileExists(item: VisitorAttachmentItem.RemoteFile): Boolean = item.isDownloaded(itemView.context)

    private fun setUpState(status: VisitorItemStatus, fileExists: Boolean, name: String, size: Long) {
        binding.deliveredView.isVisible = status == VisitorItemStatus.DELIVERED
        binding.errorView.isVisible = status == VisitorItemStatus.ERROR_INDICATOR
        setAccessibilityLabels(status, fileExists, name, size)
        if (status.isError) {
            itemView.setOnClickListener { onRetryClickListener.onRetryClicked(messageId ?: return@setOnClickListener) }
        } else {
            itemView.setOnClickListener(null)
        }
    }

    private fun setupDeliveredView(uiTheme: UiTheme) {
        if (uiTheme.fontRes != null) {
            val fontFamily = itemView.getFontCompat(uiTheme.fontRes)
            binding.deliveredView.typeface = fontFamily
        }
        uiTheme.baseNormalColor?.let(itemView::getColorCompat)
            ?.also(binding.deliveredView::setTextColor)
        binding.deliveredView.applyTextTheme(visitorTheme?.status)

        binding.deliveredView.setLocaleText(R.string.chat_message_delivered)
    }

    private fun setupErrorView(uiTheme: UiTheme) {
        if (uiTheme.fontRes != null) {
            val fontFamily = itemView.getFontCompat(uiTheme.fontRes)
            binding.errorView.typeface = fontFamily
        }
        uiTheme.systemNegativeColor?.let(itemView::getColorCompat)
            ?.also(binding.errorView::setTextColor)
        binding.errorView.applyTextTheme(visitorTheme?.error)

        binding.errorView.setLocaleText(R.string.chat_message_failed_to_deliver_retry)
    }

    private fun setAccessibilityLabels(status: VisitorItemStatus, fileExists: Boolean, name: String, size: Long) {
        val byteSize = Formatter.formatFileSize(itemView.context, size)
        val stringKey = when (status) {
            VisitorItemStatus.ERROR_INDICATOR, VisitorItemStatus.ERROR -> R.string.android_chat_visitor_file_not_delivered_accessibility
            VisitorItemStatus.DELIVERED -> R.string.android_chat_visitor_file_delivered_accessibility
            else -> R.string.android_chat_visitor_file_accessibility
        }

        binding.attachmentFileView.root.apply {
            setLocaleContentDescription(stringKey, StringKeyPair(StringKey.NAME, name), StringKeyPair(StringKey.SIZE, byteSize))
            val actionLabel = if (fileExists) {
                localeProvider.getString(R.string.general_open)
            } else {
                localeProvider.getString(R.string.general_download)
            }

            addClickActionAccessibilityLabel(actionLabel)
        }

        if (status.isError) {
            itemView.addClickActionAccessibilityLabel(localeProvider.getString(R.string.general_retry))
        } else {
            itemView.removeAccessibilityClickAction()
        }
    }

    fun updateStatus(status: VisitorItemStatus) {
        setUpState(status, true, name.orEmpty(), size ?: 0)
    }
}
