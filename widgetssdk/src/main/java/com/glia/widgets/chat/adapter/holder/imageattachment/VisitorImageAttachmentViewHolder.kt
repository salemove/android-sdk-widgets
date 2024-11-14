package com.glia.widgets.chat.adapter.holder.imageattachment

import android.view.View
import com.glia.widgets.R
import com.glia.widgets.chat.adapter.ChatAdapter.OnImageItemClickListener
import com.glia.widgets.chat.adapter.ChatAdapter.OnRetryClickListener
import com.glia.widgets.chat.model.VisitorAttachmentItem
import com.glia.widgets.databinding.ChatAttachmentVisitorImageLayoutBinding
import com.glia.widgets.di.Dependencies
import com.glia.widgets.filepreview.domain.usecase.GetImageFileFromCacheUseCase
import com.glia.widgets.filepreview.domain.usecase.GetImageFileFromDownloadsUseCase
import com.glia.widgets.filepreview.domain.usecase.GetImageFileFromNetworkUseCase
import com.glia.widgets.helper.addClickActionAccessibilityLabel
import com.glia.widgets.helper.removeAccessibilityClickAction
import com.glia.widgets.helper.rx.Schedulers
import com.glia.widgets.helper.setLocaleContentDescription
import com.glia.widgets.locale.LocaleProvider

internal class VisitorImageAttachmentViewHolder(
    private val binding: ChatAttachmentVisitorImageLayoutBinding,
    getImageFileFromCacheUseCase: GetImageFileFromCacheUseCase,
    getImageFileFromDownloadsUseCase: GetImageFileFromDownloadsUseCase,
    getImageFileFromNetworkUseCase: GetImageFileFromNetworkUseCase,
    schedulers: Schedulers,
    private val onRetryClickListener: OnRetryClickListener,
    private val onImageItemClickListener: OnImageItemClickListener,
    private val localeProvider: LocaleProvider = Dependencies.localeProvider
) : ImageAttachmentViewHolder(
    binding.root,
    binding.imageLayout.incomingImageAttachment,
    getImageFileFromCacheUseCase,
    getImageFileFromDownloadsUseCase,
    getImageFileFromNetworkUseCase,
    schedulers
) {
    private var messageId: String? = null

    fun bind(item: VisitorAttachmentItem.LocalImage) {
        messageId = item.messageId
        super.bind(item.attachment)

        updateStatus(item.isError, onRetryClickListener)

        binding.imageLayout.incomingImageAttachment.setOnClickListener {
            onImageItemClickListener.onLocalImageItemClick(item.attachment, it)
        }
    }

    fun bind(item: VisitorAttachmentItem.RemoteImage) {
        super.bind(item.attachment)

        setUpState(item.isError)

        binding.imageLayout.incomingImageAttachment.setOnClickListener {
            onImageItemClickListener.onImageItemClick(item.attachment, it)
        }
    }

    private fun setUpState(isError: Boolean) {
        setAccessibilityLabels(isError)
    }

    private fun setAccessibilityLabels(isError: Boolean) {
        val imageView = binding.imageLayout.incomingImageAttachment
        imageView.addClickActionAccessibilityLabel(localeProvider.getString(R.string.general_open))

        when {
            isError -> {
                imageView.setLocaleContentDescription(R.string.android_chat_visitor_image_attachment_not_delivered_accessibility)
                itemView.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
                itemView.setLocaleContentDescription(R.string.android_chat_visitor_image_attachment_not_delivered_accessibility)
                itemView.addClickActionAccessibilityLabel(localeProvider.getString(R.string.general_retry))
            }

            else -> {
                imageView.setLocaleContentDescription(R.string.android_chat_visitor_image_attachment_accessibility)
                itemView.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
                itemView.removeAccessibilityClickAction()
            }
        }
    }

    fun updateStatus(isError: Boolean, onRetryClickListener: OnRetryClickListener) {
        if (isError) {
            itemView.setOnClickListener { onRetryClickListener.onRetryClicked(messageId ?: return@setOnClickListener) }
        } else {
            itemView.setOnClickListener(null)
        }

        setUpState(isError)
    }
}
