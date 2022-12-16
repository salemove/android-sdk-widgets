package com.glia.widgets.chat.adapter.holder

import android.graphics.Typeface
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.glia.widgets.R
import com.glia.widgets.UiTheme
import com.glia.widgets.chat.model.history.OperatorStatusItem
import com.glia.widgets.databinding.ChatOperatorStatusLayoutBinding
import com.glia.widgets.di.Dependencies
import com.glia.widgets.view.OperatorStatusView
import com.glia.widgets.view.unifiedui.exstensions.applyTextTheme
import com.glia.widgets.view.unifiedui.exstensions.getColorCompat
import com.glia.widgets.view.unifiedui.exstensions.getFontCompat
import com.glia.widgets.view.unifiedui.theme.chat.EngagementStateTheme
import com.glia.widgets.view.unifiedui.theme.chat.EngagementStatesTheme

class OperatorStatusViewHolder(
    binding: ChatOperatorStatusLayoutBinding, uiTheme: UiTheme
) : RecyclerView.ViewHolder(binding.root) {
    private val statusPictureView: OperatorStatusView by lazy { binding.statusPictureView }
    private val chatStartingHeadingView: TextView by lazy { binding.chatStartingHeadingView }
    private val chatStartingCaptionView: TextView by lazy { binding.chatStartingCaptionView }
    private val chatStartedNameView: TextView by lazy { binding.chatStartedNameView }
    private val chatStartedCaptionView: TextView by lazy { binding.chatStartedCaptionView }

    private val engagementStatesTheme: EngagementStatesTheme? by lazy {
        Dependencies.getGliaThemeManager().theme?.chatTheme?.connect
    }

    init {
        applyBaseConfig(uiTheme)
    }

    private fun applyBaseConfig(uiTheme: UiTheme) {
        statusPictureView.setTheme(uiTheme)
        setStartingHeadingTextColor(uiTheme)
        setStartingCaptionTextColor(uiTheme)
        setStartedHeadingTextColor(uiTheme)
        setStartedCaptionTextColor(uiTheme)

        uiTheme.fontRes?.let(itemView::getFontCompat)?.also {
            chatStartingHeadingView.setTypeface(it, Typeface.BOLD)
            chatStartingCaptionView.typeface = it
            chatStartedNameView.setTypeface(it, Typeface.BOLD)
            chatStartedCaptionView.typeface = it
        }
        engagementStatesTheme?.operator.also(statusPictureView::applyOperatorTheme)
    }

    private fun setStartingHeadingTextColor(uiTheme: UiTheme) {
        uiTheme.gliaChatStartingHeadingTextColor?.let(itemView::getColorCompat)
            ?.also(chatStartingHeadingView::setTextColor)
    }

    private fun setStartingCaptionTextColor(uiTheme: UiTheme) {
        uiTheme.gliaChatStartingCaptionTextColor?.let(itemView::getColorCompat)
            ?.also(chatStartingCaptionView::setTextColor)
    }

    private fun setStartedHeadingTextColor(uiTheme: UiTheme) {
        uiTheme.gliaChatStartedHeadingTextColor?.let(itemView::getColorCompat)
            ?.also(chatStartedNameView::setTextColor)
    }

    private fun setStartedCaptionTextColor(uiTheme: UiTheme) {
        uiTheme.gliaChatStartedCaptionTextColor?.let(itemView::getColorCompat)
            ?.also(chatStartedCaptionView::setTextColor)
    }

    fun bind(item: OperatorStatusItem) {
        chatStartingHeadingView.text = item.companyName
        when (item.status) {
            OperatorStatusItem.Status.IN_QUEUE -> applyInQueueState(item.companyName)
            OperatorStatusItem.Status.OPERATOR_CONNECTED -> applyConnectedState(
                item.operatorName,
                item.profileImgUrl
            )
            OperatorStatusItem.Status.JOINED -> applyJoinedState(item.operatorName)
            OperatorStatusItem.Status.TRANSFERRING -> applyTransferringState()
        }
        statusPictureView.isVisible = isShowStatusPictureView(item.status)
        statusPictureView.setShowRippleAnimation(isShowStatusViewRippleAnimation(item))
    }

    private fun applyInQueueState(companyName: String) {
        statusPictureView.showPlaceholder()
        applyChatStartingViewsVisibility()
        applyChatStartedViewsVisibility(false)
        itemView.contentDescription =
            itemView.resources.getString(
                R.string.glia_chat_in_queue_message_content_description,
                companyName
            )

        engagementStatesTheme?.queue.also(::applyEngagementState)
    }


    private fun applyConnectedState(operatorName: String, profileImgUrl: String?) {
        profileImgUrl?.let { statusPictureView.showProfileImage(it) }
            ?: statusPictureView.showPlaceholder()

        applyChatStartingViewsVisibility(false)
        applyChatStartedViewsVisibility()

        chatStartedNameView.text = operatorName
        chatStartedCaptionView.text =
            itemView.resources.getString(R.string.glia_chat_operator_has_joined, operatorName)
        itemView.contentDescription = itemView.resources.getString(
            R.string.glia_chat_operator_has_joined_content_description,
            operatorName
        )

        engagementStatesTheme?.connected.also(::applyEngagementState)

    }

    private fun applyJoinedState(operatorName: String) {
        chatStartedNameView.text = operatorName
        chatStartedCaptionView.text =
            itemView.resources.getString(R.string.glia_chat_operator_has_joined, operatorName)
        itemView.contentDescription = itemView.resources.getString(
            R.string.glia_chat_operator_has_joined_content_description,
            operatorName
        )

        applyChatStartingViewsVisibility(false)
        applyChatStartedViewsVisibility()

        engagementStatesTheme?.connecting.also(::applyEngagementState)
    }

    private fun applyTransferringState() {
        statusPictureView.showPlaceholder()
        applyChatStartingViewsVisibility()
        chatStartedNameView.isVisible = true
        chatStartedCaptionView.isVisible = false

        chatStartedNameView.text =
            itemView.resources.getString(R.string.glia_chat_visitor_status_transferring)

        engagementStatesTheme?.transferring.also(::applyEngagementState)

    }

    private fun applyChatStartingViewsVisibility(visible: Boolean = true) {
        chatStartingCaptionView.isVisible = visible
        chatStartingHeadingView.isVisible = visible
    }

    private fun applyChatStartedViewsVisibility(visible: Boolean = true) {
        chatStartedNameView.isVisible = visible
        chatStartedCaptionView.isVisible = visible
    }

    private fun applyEngagementState(engagementStateTheme: EngagementStateTheme?) {
        engagementStateTheme?.description?.also {
            chatStartingCaptionView.applyTextTheme(it)
            chatStartedCaptionView.applyTextTheme(it)
        }
        engagementStateTheme?.title?.also {
            chatStartingHeadingView.applyTextTheme(it)
            chatStartedNameView.applyTextTheme(it)
        }
    }

    private fun isShowStatusPictureView(status: OperatorStatusItem.Status) =
        status != OperatorStatusItem.Status.JOINED

    private fun isShowStatusViewRippleAnimation(item: OperatorStatusItem) = item.status.let {
        it == OperatorStatusItem.Status.IN_QUEUE || it == OperatorStatusItem.Status.TRANSFERRING
    }
}