package com.glia.widgets.chat.adapter.holder.imageattachment

import android.view.View
import androidx.core.view.AccessibilityDelegateCompat
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityActionCompat
import androidx.core.view.isVisible
import com.glia.widgets.R
import com.glia.widgets.StringProvider
import com.glia.widgets.UiTheme
import com.glia.widgets.chat.adapter.ChatAdapter.OnImageItemClickListener
import com.glia.widgets.chat.adapter.ChatAdapter.OnMessageClickListener
import com.glia.widgets.chat.model.VisitorAttachmentItem
import com.glia.widgets.databinding.ChatAttachmentVisitorImageLayoutBinding
import com.glia.widgets.di.Dependencies
import com.glia.widgets.filepreview.domain.usecase.GetImageFileFromCacheUseCase
import com.glia.widgets.filepreview.domain.usecase.GetImageFileFromDownloadsUseCase
import com.glia.widgets.filepreview.domain.usecase.GetImageFileFromNetworkUseCase
import com.glia.widgets.helper.getColorCompat
import com.glia.widgets.helper.getFontCompat
import com.glia.widgets.helper.rx.Schedulers
import com.glia.widgets.view.unifiedui.applyTextTheme
import com.glia.widgets.view.unifiedui.theme.UnifiedTheme
import com.glia.widgets.view.unifiedui.theme.chat.MessageBalloonTheme

internal class VisitorImageAttachmentViewHolder(
    private val binding: ChatAttachmentVisitorImageLayoutBinding,
    getImageFileFromCacheUseCase: GetImageFileFromCacheUseCase,
    getImageFileFromDownloadsUseCase: GetImageFileFromDownloadsUseCase,
    getImageFileFromNetworkUseCase: GetImageFileFromNetworkUseCase,
    schedulers: Schedulers,
    uiTheme: UiTheme,
    unifiedTheme: UnifiedTheme? = Dependencies.getGliaThemeManager().theme,
    private val stringProvider: StringProvider = Dependencies.getStringProvider()
) : ImageAttachmentViewHolder(
    binding.root,
    getImageFileFromCacheUseCase,
    getImageFileFromDownloadsUseCase,
    getImageFileFromNetworkUseCase,
    schedulers
) {
    private val visitorTheme: MessageBalloonTheme? by lazy {
        unifiedTheme?.chatTheme?.visitorMessage
    }
    private lateinit var item: VisitorAttachmentItem.Image

    init {
        binding.deliveredView.text = stringProvider.getRemoteString(R.string.chat_message_delivered)
        binding.errorView.text = stringProvider.getRemoteString(R.string.chat_message_failed_to_deliver_retry)
        setupTheme(uiTheme)
    }

    fun bind(
        item: VisitorAttachmentItem.Image,
        onImageItemClickListener: OnImageItemClickListener,
        onMessageClickListener: OnMessageClickListener
    ) {
        this.item = item
        super.bind(item.attachment)
        itemView.setOnClickListener { view: View ->
            val attachmentFile = item.attachment.remoteAttachment
            if (attachmentFile != null) {
                onImageItemClickListener.onImageItemClick(attachmentFile, view)
            } else {
                onMessageClickListener.onMessageClick(item.id)
            }
        }
        setShowError(item.showError)
        setShowDelivered(item.showDelivered)
        setAccessibilityLabels(item.showError, item.showDelivered)
    }

    private fun setAccessibilityLabels(showError: Boolean, showDelivered: Boolean) {
        if (showError) {
            itemView.contentDescription = stringProvider.getRemoteString(
                R.string.android_chat_visitor_image_attachment_not_delivered_accessibility
            )
        } else if (showDelivered) {
            itemView.contentDescription = stringProvider.getRemoteString(
                R.string.android_chat_visitor_image_attachment_delivered_accessibility
            )
        } else {
            itemView.contentDescription = stringProvider.getRemoteString(
                R.string.android_chat_visitor_image_attachment_accessibility
            )
        }
        ViewCompat.setAccessibilityDelegate(itemView, object : AccessibilityDelegateCompat() {
            override fun onInitializeAccessibilityNodeInfo(
                host: View,
                info: AccessibilityNodeInfoCompat
            ) {
                super.onInitializeAccessibilityNodeInfo(host, info)
                val actionLabel = stringProvider.getRemoteString(
                    if (showError) R.string.general_retry else R.string.general_open
                )
                val actionClick = AccessibilityActionCompat(
                    AccessibilityNodeInfoCompat.ACTION_CLICK, actionLabel
                )
                info.addAction(actionClick)
            }
        })
    }

    private fun setShowDelivered(showDelivered: Boolean) {
        binding.deliveredView.isVisible = showDelivered
    }

    private fun setShowError(showError: Boolean) {
        binding.errorView.isVisible = showError
    }

    private fun setupTheme(uiTheme: UiTheme) {
        if (uiTheme.fontRes != null) {
            val fontFamily = itemView.getFontCompat(uiTheme.fontRes)
            binding.deliveredView.typeface = fontFamily
            binding.errorView.typeface = fontFamily
        }
        uiTheme.baseNormalColor?.let(itemView::getColorCompat)
            ?.also(binding.deliveredView::setTextColor)

        uiTheme.systemNegativeColor?.let(itemView::getColorCompat)
            ?.also(binding.errorView::setTextColor)

        // Unified Ui
        binding.deliveredView.applyTextTheme(visitorTheme?.status)
        binding.errorView.applyTextTheme(visitorTheme?.error)
    }

    fun updateDelivered(delivered: Boolean) {
        setShowDelivered(delivered)
        setAccessibilityLabels(item.showError, delivered)
    }
}
