package com.glia.widgets.chat.adapter.holder.fileattachment

import android.text.format.Formatter
import android.view.View
import androidx.core.view.AccessibilityDelegateCompat
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityActionCompat
import androidx.core.view.isVisible
import com.glia.widgets.R
import com.glia.widgets.StringKey
import com.glia.widgets.StringKeyPair
import com.glia.widgets.StringProvider
import com.glia.widgets.UiTheme
import com.glia.widgets.chat.adapter.ChatAdapter.OnFileItemClickListener
import com.glia.widgets.chat.adapter.ChatAdapter.OnMessageClickListener
import com.glia.widgets.chat.model.VisitorAttachmentItem
import com.glia.widgets.databinding.ChatAttachmentVisitorFileLayoutBinding
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.getColorCompat
import com.glia.widgets.helper.getFontCompat
import com.glia.widgets.view.unifiedui.applyTextTheme
import com.glia.widgets.view.unifiedui.theme.UnifiedTheme
import com.glia.widgets.view.unifiedui.theme.chat.MessageBalloonTheme

internal class VisitorFileAttachmentViewHolder(
    private val binding: ChatAttachmentVisitorFileLayoutBinding,
    uiTheme: UiTheme,
    unifiedTheme: UnifiedTheme? = Dependencies.getGliaThemeManager().theme,
    private val stringProvider: StringProvider = Dependencies.getStringProvider()
) : FileAttachmentViewHolder(binding.root, stringProvider) {
    private val visitorTheme: MessageBalloonTheme? by lazy {
        unifiedTheme?.chatTheme?.visitorMessage
    }

    private lateinit var item: VisitorAttachmentItem.File

    init {
        setupDeliveredView(uiTheme)
        setupErrorView(uiTheme)
    }

    fun bind(
        item: VisitorAttachmentItem.File,
        onFileItemClickListener: OnFileItemClickListener,
        onMessageClickListener: OnMessageClickListener
    ) {
        this.item = item
        val isLocalFile = item.attachment.localAttachment != null
        super.setData(
            isLocalFile,
            item.isFileExists,
            item.isDownloading,
            item.attachment,
            onFileItemClickListener
        )
        if (isLocalFile) {
            itemView.setOnClickListener { _ -> onMessageClickListener.onMessageClick(item.id) }
        }
        setShowDelivered(item.showDelivered)
        setShowError(item.showError)
        setAccessibilityLabels(item.showDelivered)
    }

    private fun setupDeliveredView(uiTheme: UiTheme) {
        if (uiTheme.fontRes != null) {
            val fontFamily = itemView.getFontCompat(uiTheme.fontRes)
            binding.deliveredView.typeface = fontFamily
        }
        uiTheme.baseNormalColor?.let(itemView::getColorCompat)
            ?.also(binding.deliveredView::setTextColor)
        binding.deliveredView.applyTextTheme(visitorTheme?.status)

        binding.deliveredView.text = stringProvider.getRemoteString(R.string.chat_message_delivered)
    }

    private fun setupErrorView(uiTheme: UiTheme) {
        if (uiTheme.fontRes != null) {
            val fontFamily = itemView.getFontCompat(uiTheme.fontRes)
            binding.errorView.typeface = fontFamily
        }
        uiTheme.systemNegativeColor?.let(itemView::getColorCompat)
            ?.also(binding.errorView::setTextColor)
        binding.errorView.applyTextTheme(visitorTheme?.error)

        binding.errorView.text = stringProvider.getRemoteString(R.string.chat_message_failed_to_deliver_retry)
    }

    private fun setShowDelivered(showDelivered: Boolean) {
        binding.deliveredView.isVisible = showDelivered
    }

    private fun setShowError(showError: Boolean) {
        binding.errorView.isVisible = showError
    }

    private fun setAccessibilityLabels(showDelivered: Boolean) {
        val name = getAttachmentName(item.attachment)
        val size = getAttachmentSize(item.attachment)
        val byteSize = Formatter.formatFileSize(itemView.context, size)
        val stringKey = if (item.showError) {
            R.string.android_chat_visitor_file_not_delivered_accessibility
        } else if (showDelivered) {
            R.string.android_chat_visitor_file_delivered_accessibility
        } else {
            R.string.android_chat_visitor_file_accessibility
        }
        itemView.contentDescription = stringProvider.getRemoteString(
            stringKey,
            StringKeyPair(StringKey.NAME, name),
            StringKeyPair(StringKey.SIZE, byteSize)
        )
        ViewCompat.setAccessibilityDelegate(itemView, object : AccessibilityDelegateCompat() {
            override fun onInitializeAccessibilityNodeInfo(
                host: View,
                info: AccessibilityNodeInfoCompat
            ) {
                super.onInitializeAccessibilityNodeInfo(host, info)
                val actionLabel = if (item.showError) {
                    stringProvider.getRemoteString(R.string.general_retry)
                } else if (item.isFileExists) {
                    stringProvider.getRemoteString(R.string.general_open)
                } else {
                    stringProvider.getRemoteString(R.string.general_download)
                }
                val actionClick = AccessibilityActionCompat(
                    AccessibilityNodeInfoCompat.ACTION_CLICK, actionLabel
                )
                info.addAction(actionClick)
            }
        })
    }

    fun updateDelivered(delivered: Boolean) {
        setShowDelivered(delivered)
        setAccessibilityLabels(delivered)
    }
}
