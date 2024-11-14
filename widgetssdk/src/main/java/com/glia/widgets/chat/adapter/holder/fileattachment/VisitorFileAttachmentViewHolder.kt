package com.glia.widgets.chat.adapter.holder.fileattachment

import android.text.format.Formatter
import android.view.View
import com.glia.widgets.R
import com.glia.widgets.chat.adapter.ChatAdapter.OnFileItemClickListener
import com.glia.widgets.chat.adapter.ChatAdapter.OnRetryClickListener
import com.glia.widgets.chat.model.VisitorAttachmentItem
import com.glia.widgets.databinding.ChatAttachmentVisitorFileLayoutBinding
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.addClickActionAccessibilityLabel
import com.glia.widgets.helper.removeAccessibilityClickAction
import com.glia.widgets.helper.setLocaleContentDescription
import com.glia.widgets.locale.LocaleProvider
import com.glia.widgets.locale.StringKey
import com.glia.widgets.locale.StringKeyPair

internal class VisitorFileAttachmentViewHolder(
    private val binding: ChatAttachmentVisitorFileLayoutBinding,
    private val onFileItemClickListener: OnFileItemClickListener,
    private val onRetryClickListener: OnRetryClickListener,
    private val localeProvider: LocaleProvider = Dependencies.localeProvider
) : FileAttachmentViewHolder(binding.root) {
    private var messageId: String? = null
    private var name: String? = null
    private var size: Long? = null

    fun bind(item: VisitorAttachmentItem.LocalFile) {
        messageId = item.messageId
        name = item.attachment.displayName
        size = item.attachment.size

        super.setData(item.attachment)
        setUpState(isError = item.isError, fileExists = true, name = item.attachment.displayName, size = item.attachment.size)

        binding.attachmentFileView.root.setOnClickListener {
            onFileItemClickListener.onLocalFileOpenClick(item.attachment)
        }
    }

    fun bind(item: VisitorAttachmentItem.RemoteFile) {
        name = item.attachment.name
        size = item.attachment.size

        val isFileExists = isFileExists(item)

        super.setData(isFileExists, item.isDownloading, item.attachment)
        setUpState(isError = item.isError, fileExists = isFileExists, name = item.attachment.name, size = item.attachment.size)

        val attachmentView = binding.attachmentFileView.root

        when {
            item.isDownloading -> attachmentView.setOnClickListener(null)
            isFileExists -> attachmentView.setOnClickListener { onFileItemClickListener.onFileOpenClick(item.attachment) }
            else -> attachmentView.setOnClickListener { onFileItemClickListener.onFileDownloadClick(item.attachment) }
        }
    }

    private fun isFileExists(item: VisitorAttachmentItem.RemoteFile): Boolean = item.isDownloaded(itemView.context)

    private fun setUpState(isError: Boolean, fileExists: Boolean, name: String, size: Long) {
        setAccessibilityLabels(isError, fileExists, name, size)
        if (isError) {
            itemView.setOnClickListener { onRetryClickListener.onRetryClicked(messageId ?: return@setOnClickListener) }
        } else {
            itemView.setOnClickListener(null)
        }
    }

    private fun setAccessibilityLabels(isError: Boolean, fileExists: Boolean, name: String, size: Long) {
        val byteSize = Formatter.formatFileSize(itemView.context, size)
        val stringKey = when {
            isError -> R.string.android_chat_visitor_file_not_delivered_accessibility
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

        if (isError) {
            itemView.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
            itemView.setLocaleContentDescription(stringKey, StringKeyPair(StringKey.NAME, name), StringKeyPair(StringKey.SIZE, byteSize))
            itemView.addClickActionAccessibilityLabel(localeProvider.getString(R.string.general_retry))
        } else {
            itemView.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
            itemView.removeAccessibilityClickAction()
        }
    }

    fun updateStatus(isError: Boolean) {
        setUpState(isError, true, name.orEmpty(), size ?: 0)
    }
}
