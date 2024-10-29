package com.glia.widgets.chat.adapter.holder.imageattachment

import androidx.core.view.isVisible
import com.glia.widgets.R
import com.glia.widgets.UiTheme
import com.glia.widgets.chat.adapter.ChatAdapter.OnImageItemClickListener
import com.glia.widgets.chat.adapter.ChatAdapter.OnTapToRetryClickListener
import com.glia.widgets.chat.model.VisitorAttachmentItem
import com.glia.widgets.chat.model.VisitorItemStatus
import com.glia.widgets.databinding.ChatAttachmentVisitorImageLayoutBinding
import com.glia.widgets.di.Dependencies
import com.glia.widgets.filepreview.domain.usecase.GetImageFileFromCacheUseCase
import com.glia.widgets.filepreview.domain.usecase.GetImageFileFromDownloadsUseCase
import com.glia.widgets.filepreview.domain.usecase.GetImageFileFromNetworkUseCase
import com.glia.widgets.helper.addClickActionAccessibilityLabel
import com.glia.widgets.helper.getColorCompat
import com.glia.widgets.helper.getFontCompat
import com.glia.widgets.helper.removeAccessibilityClickAction
import com.glia.widgets.helper.rx.Schedulers
import com.glia.widgets.helper.setLocaleContentDescription
import com.glia.widgets.helper.setLocaleText
import com.glia.widgets.locale.LocaleProvider
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
    private val onTapToRetryClickListener: OnTapToRetryClickListener,
    private val onImageItemClickListener: OnImageItemClickListener,
    unifiedTheme: UnifiedTheme? = Dependencies.gliaThemeManager.theme,
    private val localeProvider: LocaleProvider = Dependencies.localeProvider
) : ImageAttachmentViewHolder(
    binding.root,
    binding.imageLayout.incomingImageAttachment,
    getImageFileFromCacheUseCase,
    getImageFileFromDownloadsUseCase,
    getImageFileFromNetworkUseCase,
    schedulers
) {
    private val visitorTheme: MessageBalloonTheme? by lazy { unifiedTheme?.chatTheme?.visitorMessage }
    private var messageId: String? = null

    init {
        binding.deliveredView.setLocaleText(R.string.chat_message_delivered)
        binding.errorView.setLocaleText(R.string.chat_message_failed_to_deliver_retry)
        setupTheme(uiTheme)
    }

    fun bind(item: VisitorAttachmentItem.LocalImage) {
        messageId = item.messageId
        super.bind(item.attachment)

        updateStatus(item.status, onTapToRetryClickListener)

        binding.imageLayout.incomingImageAttachment.setOnClickListener {
            onImageItemClickListener.onLocalImageItemClick(item.attachment, it)
        }
    }

    fun bind(item: VisitorAttachmentItem.RemoteImage) {
        super.bind(item.attachment)

        setUpState(item.status)

        binding.imageLayout.incomingImageAttachment.setOnClickListener {
            onImageItemClickListener.onImageItemClick(item.attachment, it)
        }
    }

    private fun setUpState(status: VisitorItemStatus) {
        binding.deliveredView.isVisible = status == VisitorItemStatus.DELIVERED
        binding.errorView.isVisible = status == VisitorItemStatus.ERROR_INDICATOR

        setAccessibilityLabels(status)
    }

    private fun setAccessibilityLabels(status: VisitorItemStatus) {
        val imageView = binding.imageLayout.incomingImageAttachment
        imageView.addClickActionAccessibilityLabel(localeProvider.getString(R.string.general_open))

        when (status) {
            VisitorItemStatus.ERROR_INDICATOR, VisitorItemStatus.ERROR -> {
                imageView.setLocaleContentDescription(R.string.android_chat_visitor_image_attachment_not_delivered_accessibility)
                itemView.addClickActionAccessibilityLabel(localeProvider.getString(R.string.general_retry))
            }

            VisitorItemStatus.DELIVERED -> {
                imageView.setLocaleContentDescription(R.string.android_chat_visitor_image_attachment_delivered_accessibility)
                itemView.removeAccessibilityClickAction()
            }

            else -> {
                imageView.setLocaleContentDescription(R.string.android_chat_visitor_image_attachment_accessibility)
                itemView.removeAccessibilityClickAction()
            }
        }
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

    fun updateStatus(status: VisitorItemStatus, onTapToRetryClickListener: OnTapToRetryClickListener) {
        if (status.isError) {
            itemView.setOnClickListener { onTapToRetryClickListener.onTapToRetryClicked(messageId ?: return@setOnClickListener) }
        } else {
            itemView.setOnClickListener(null)
        }

        setUpState(status)
    }
}
