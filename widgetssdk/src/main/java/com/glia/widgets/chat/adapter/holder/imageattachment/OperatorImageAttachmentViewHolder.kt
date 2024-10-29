package com.glia.widgets.chat.adapter.holder.imageattachment

import android.view.View
import com.glia.widgets.R
import com.glia.widgets.UiTheme
import com.glia.widgets.chat.adapter.ChatAdapter.OnImageItemClickListener
import com.glia.widgets.chat.model.OperatorAttachmentItem
import com.glia.widgets.databinding.ChatAttachmentOperatorImageLayoutBinding
import com.glia.widgets.di.Dependencies
import com.glia.widgets.filepreview.domain.usecase.GetImageFileFromCacheUseCase
import com.glia.widgets.filepreview.domain.usecase.GetImageFileFromDownloadsUseCase
import com.glia.widgets.filepreview.domain.usecase.GetImageFileFromNetworkUseCase
import com.glia.widgets.helper.addClickActionAccessibilityLabel
import com.glia.widgets.helper.rx.Schedulers
import com.glia.widgets.helper.setLocaleContentDescription
import com.glia.widgets.locale.LocaleProvider
import com.glia.widgets.view.unifiedui.theme.chat.MessageBalloonTheme

internal class OperatorImageAttachmentViewHolder(
    private val binding: ChatAttachmentOperatorImageLayoutBinding,
    getImageFileFromCacheUseCase: GetImageFileFromCacheUseCase,
    getImageFileFromDownloadsUseCase: GetImageFileFromDownloadsUseCase,
    getImageFileFromNetworkUseCase: GetImageFileFromNetworkUseCase,
    schedulers: Schedulers,
    uiTheme: UiTheme,
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
    private val operatorTheme: MessageBalloonTheme? by lazy {
        Dependencies.gliaThemeManager.theme?.chatTheme?.operatorMessage
    }

    init {
        setupOperatorStatus(uiTheme)
    }

    private fun setupOperatorStatus(uiTheme: UiTheme) {
        binding.chatHeadView.setTheme(uiTheme)
        binding.chatHeadView.setShowRippleAnimation(false)
        binding.chatHeadView.applyUserImageTheme(operatorTheme?.userImage)
    }

    fun bind(item: OperatorAttachmentItem.Image) {
        super.bind(item.attachment)
        updateOperatorStatus(item)
        setAccessibilityLabels()

        binding.imageLayout.incomingImageAttachment.setOnClickListener { v: View ->
            onImageItemClickListener.onImageItemClick(item.attachment, v)
        }
    }

    private fun setAccessibilityLabels() {
        binding.imageLayout.incomingImageAttachment.apply {
            setLocaleContentDescription(R.string.android_chat_operator_image_attachment_accessibility)
            addClickActionAccessibilityLabel(localeProvider.getString(R.string.general_open))
        }
    }

    private fun updateOperatorStatus(item: OperatorAttachmentItem) {
        binding.chatHeadView.visibility = if (item.showChatHead) View.VISIBLE else View.GONE
        if (item.operatorProfileImgUrl != null) {
            binding.chatHeadView.showProfileImage(item.operatorProfileImgUrl)
        } else {
            binding.chatHeadView.showPlaceholder()
        }
    }
}
