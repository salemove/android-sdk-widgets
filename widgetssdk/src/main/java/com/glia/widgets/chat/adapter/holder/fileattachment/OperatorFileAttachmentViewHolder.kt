package com.glia.widgets.chat.adapter.holder.fileattachment

import android.text.format.Formatter
import android.view.View
import androidx.core.view.AccessibilityDelegateCompat
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityActionCompat
import com.glia.widgets.R
import com.glia.widgets.UiTheme
import com.glia.widgets.chat.adapter.ChatAdapter.OnFileItemClickListener
import com.glia.widgets.chat.model.OperatorAttachmentItem
import com.glia.widgets.databinding.ChatAttachmentOperatorFileLayoutBinding
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.setLocaleContentDescription
import com.glia.widgets.locale.LocaleProvider
import com.glia.widgets.locale.StringKey
import com.glia.widgets.locale.StringKeyPair
import com.glia.widgets.view.unifiedui.theme.chat.MessageBalloonTheme

internal class OperatorFileAttachmentViewHolder @JvmOverloads constructor(
    private val binding: ChatAttachmentOperatorFileLayoutBinding,
    uiTheme: UiTheme,
    private val localeProvider: LocaleProvider = Dependencies.localeProvider
) : FileAttachmentViewHolder(binding.root, localeProvider) {
    private val operatorTheme: MessageBalloonTheme? by lazy {
        Dependencies.gliaThemeManager.theme?.chatTheme?.operatorMessage
    }

    init {
        setupOperatorStatusView(uiTheme)
    }

    fun bind(item: OperatorAttachmentItem.File, listener: OnFileItemClickListener?) {
        super.setData(item.isFileExists, item.isDownloading, item.attachment, listener)
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
        val name = getAttachmentName(item.attachment)
        val size = getAttachmentSize(item.attachment)
        val byteSize = Formatter.formatFileSize(itemView.context, size)
        itemView.setLocaleContentDescription(
            R.string.android_chat_operator_file_accessibility,
            StringKeyPair(StringKey.NAME, name),
            StringKeyPair(StringKey.SIZE, byteSize)
        )
        ViewCompat.setAccessibilityDelegate(itemView, object : AccessibilityDelegateCompat() {
            override fun onInitializeAccessibilityNodeInfo(
                host: View,
                info: AccessibilityNodeInfoCompat
            ) {
                super.onInitializeAccessibilityNodeInfo(host, info)
                val actionLabel =
                    localeProvider.getString(if (item.isFileExists) R.string.general_open else R.string.general_download)
                val actionClick = AccessibilityActionCompat(
                    AccessibilityNodeInfoCompat.ACTION_CLICK, actionLabel
                )
                info.addAction(actionClick)
            }
        })
    }
}
