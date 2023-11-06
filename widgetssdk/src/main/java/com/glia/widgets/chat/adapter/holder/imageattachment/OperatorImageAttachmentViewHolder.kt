package com.glia.widgets.chat.adapter.holder.imageattachment

import android.view.View
import androidx.core.view.AccessibilityDelegateCompat
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityActionCompat
import com.glia.widgets.R
import com.glia.widgets.StringProvider
import com.glia.widgets.UiTheme
import com.glia.widgets.chat.adapter.ChatAdapter.OnImageItemClickListener
import com.glia.widgets.chat.model.OperatorAttachmentItem
import com.glia.widgets.databinding.ChatAttachmentOperatorImageLayoutBinding
import com.glia.widgets.di.Dependencies
import com.glia.widgets.filepreview.domain.usecase.GetImageFileFromCacheUseCase
import com.glia.widgets.filepreview.domain.usecase.GetImageFileFromDownloadsUseCase
import com.glia.widgets.filepreview.domain.usecase.GetImageFileFromNetworkUseCase
import com.glia.widgets.helper.rx.Schedulers
import com.glia.widgets.view.OperatorStatusView
import com.glia.widgets.view.unifiedui.theme.UnifiedTheme

internal class OperatorImageAttachmentViewHolder(
    private val binding: ChatAttachmentOperatorImageLayoutBinding,
    getImageFileFromCacheUseCase: GetImageFileFromCacheUseCase,
    getImageFileFromDownloadsUseCase: GetImageFileFromDownloadsUseCase,
    getImageFileFromNetworkUseCase: GetImageFileFromNetworkUseCase,
    schedulers: Schedulers,
    uiTheme: UiTheme,
    private val stringProvider: StringProvider = Dependencies.getStringProvider()
) : ImageAttachmentViewHolder(
    binding.root,
    getImageFileFromCacheUseCase,
    getImageFileFromDownloadsUseCase,
    getImageFileFromNetworkUseCase,
    schedulers
) {

    init {
        setupOperatorStatus(uiTheme)
    }

    private fun setupOperatorStatus(uiTheme: UiTheme) {
        binding.chatHeadView.setTheme(uiTheme)
        binding.chatHeadView.setShowRippleAnimation(false)
    }

    fun bind(
        item: OperatorAttachmentItem.Image,
        onImageItemClickListener: OnImageItemClickListener
    ) {
        super.bind(item.attachment)
        val attachmentFile = item.attachment.remoteAttachment
        if (attachmentFile != null) {
            itemView.setOnClickListener { v: View ->
                onImageItemClickListener.onImageItemClick(attachmentFile, v)
            }
        } else {
            itemView.setOnClickListener(null)
        }
        updateOperatorStatus(item)
        setAccessibilityLabels()
    }

    private fun setAccessibilityLabels() {
        itemView.contentDescription = stringProvider.getRemoteString(R.string.android_chat_operator_image_attachment_accessibility)
        ViewCompat.setAccessibilityDelegate(itemView, object : AccessibilityDelegateCompat() {
            override fun onInitializeAccessibilityNodeInfo(
                host: View,
                info: AccessibilityNodeInfoCompat
            ) {
                super.onInitializeAccessibilityNodeInfo(host, info)
                val actionLabel = stringProvider.getRemoteString(R.string.general_open)
                val actionClick = AccessibilityActionCompat(
                    AccessibilityNodeInfoCompat.ACTION_CLICK, actionLabel
                )
                info.addAction(actionClick)
            }
        })
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
