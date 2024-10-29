package com.glia.widgets.chat.adapter.holder.fileattachment

import android.text.format.Formatter
import android.view.View
import com.glia.widgets.R
import com.glia.widgets.UiTheme
import com.glia.widgets.chat.adapter.ChatAdapter
import com.glia.widgets.chat.model.OperatorAttachmentItem
import com.glia.widgets.databinding.ChatAttachmentOperatorFileLayoutBinding
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.addClickActionAccessibilityLabel
import com.glia.widgets.helper.removeAccessibilityClickAction
import com.glia.widgets.helper.setLocaleContentDescription
import com.glia.widgets.locale.LocaleProvider
import com.glia.widgets.locale.StringKey
import com.glia.widgets.locale.StringKeyPair
import com.glia.widgets.view.unifiedui.theme.chat.MessageBalloonTheme

internal class OperatorFileAttachmentViewHolder @JvmOverloads constructor(
    private val binding: ChatAttachmentOperatorFileLayoutBinding,
    uiTheme: UiTheme,
    private val onFileItemClickListener: ChatAdapter.OnFileItemClickListener,
    private val localeProvider: LocaleProvider = Dependencies.localeProvider
) : FileAttachmentViewHolder(binding.root) {
    private val operatorTheme: MessageBalloonTheme? by lazy {
        Dependencies.gliaThemeManager.theme?.chatTheme?.operatorMessage
    }

    init {
        setupOperatorStatusView(uiTheme)
    }

    fun bind(item: OperatorAttachmentItem.File) {
        super.setData(item.isFileExists, item.isDownloading, item.attachment)
        updateOperatorStatusView(item)
    }

    private fun setupOperatorStatusView(uiTheme: UiTheme) {
        binding.chatHeadView.setTheme(uiTheme)
        binding.chatHeadView.setShowRippleAnimation(false)
        binding.chatHeadView.applyUserImageTheme(operatorTheme?.userImage)
    }

    private fun updateOperatorStatusView(item: OperatorAttachmentItem.File) {
        binding.chatHeadView.visibility = if (item.showChatHead) View.VISIBLE else View.GONE
        if (item.operatorProfileImgUrl != null) {
            binding.chatHeadView.showProfileImage(item.operatorProfileImgUrl)
        } else {
            binding.chatHeadView.showPlaceholder()
        }
        val name = item.attachment.name
        val size = item.attachment.size
        val byteSize = Formatter.formatFileSize(itemView.context, size)

        val attachmentView = binding.attachmentFileView.root

        when {
            item.isDownloading -> {
                attachmentView.setOnClickListener(null)
                attachmentView.removeAccessibilityClickAction()
            }

            item.isFileExists -> {
                attachmentView.setOnClickListener { onFileItemClickListener.onFileOpenClick(item.attachment) }
                attachmentView.addClickActionAccessibilityLabel(localeProvider.getString(R.string.general_open))
            }

            else -> {
                attachmentView.setOnClickListener { onFileItemClickListener.onFileDownloadClick(item.attachment) }
                attachmentView.addClickActionAccessibilityLabel(localeProvider.getString(R.string.general_download))
            }
        }

        attachmentView.setLocaleContentDescription(
            R.string.android_chat_operator_file_accessibility,
            StringKeyPair(StringKey.NAME, name),
            StringKeyPair(StringKey.SIZE, byteSize)
        )
    }
}
