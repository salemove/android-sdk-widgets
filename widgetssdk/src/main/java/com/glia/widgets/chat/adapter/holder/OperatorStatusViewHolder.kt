package com.glia.widgets.chat.adapter.holder

import android.graphics.Typeface
import android.view.View
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.glia.widgets.R
import com.glia.widgets.UiTheme
import com.glia.widgets.chat.model.OperatorStatusItem
import com.glia.widgets.databinding.ChatOperatorStatusLayoutBinding
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.getColorCompat
import com.glia.widgets.helper.getFontCompat
import com.glia.widgets.helper.setLocaleContentDescription
import com.glia.widgets.helper.setLocaleText
import com.glia.widgets.locale.StringKey
import com.glia.widgets.locale.StringKeyPair
import com.glia.widgets.view.OperatorStatusView
import com.glia.widgets.view.unifiedui.applyTextTheme
import com.glia.widgets.view.unifiedui.theme.chat.EngagementStateTheme
import com.glia.widgets.view.unifiedui.theme.chat.EngagementStatesTheme

internal class OperatorStatusViewHolder(
    binding: ChatOperatorStatusLayoutBinding,
    uiTheme: UiTheme
) : RecyclerView.ViewHolder(binding.root) {
    private val statusPictureView: OperatorStatusView by lazy { binding.statusPictureView }
    private val chatStartingHeadingView: TextView by lazy { binding.chatStartingHeadingView }
    private val chatStartingCaptionView: TextView by lazy { binding.chatStartingCaptionView }
    private val chatStartedNameView: TextView by lazy { binding.chatStartedNameView }
    private val chatStartedCaptionView: TextView by lazy { binding.chatStartedCaptionView }

    private val engagementStatesTheme: EngagementStatesTheme? by lazy {
        Dependencies.getGliaThemeManager().theme?.chatTheme?.connect
    }
    private val localeProvider = Dependencies.getLocaleProvider()

    init {
        applyBaseConfig(uiTheme)
        setBaseStrings()
    }

    private fun setBaseStrings() {
        chatStartingCaptionView.setLocaleText(R.string.engagement_connection_screen_message)
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
            chatStartedCaptionView.setLocaleText(R.string.engagement_connection_screen_message)
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
        chatStartingHeadingView.setLocaleText(R.string.general_company_name)
        when (item) {
            is OperatorStatusItem.Connected -> applyConnectedState(item.operatorName, item.profileImgUrl)
            is OperatorStatusItem.InQueue -> applyInQueueState(item.companyName)
            is OperatorStatusItem.Joined -> applyConnectedState(item.operatorName, item.profileImgUrl)
            is OperatorStatusItem.Transferring -> applyTransferringState()
        }
        statusPictureView.isVisible = true
        statusPictureView.setShowRippleAnimation(isShowStatusViewRippleAnimation(item))
    }

    private fun applyInQueueState(companyName: String?) {
        statusPictureView.showPlaceholder()
        applyChatStartingViewsVisibility()
        applyChatStartedViewsVisibility(false)
        itemView.setLocaleContentDescription(
            R.string.android_chat_queue_message_accessibility_label,
            StringKeyPair(StringKey.COMPANY_NAME, companyName ?: "")
        )

        engagementStatesTheme?.queue.also(::applyEngagementState)
    }

    private fun applyConnectedState(operatorName: String, profileImgUrl: String?) {
        profileImgUrl?.let { statusPictureView.showProfileImage(it) } ?: statusPictureView.showPlaceholder()

        applyChatStartingViewsVisibility(false)
        applyChatStartedViewsVisibility()

        chatStartedNameView.text = operatorName
        chatStartedCaptionView.setLocaleText(
            R.string.chat_operator_joined_system_message,
            StringKeyPair(StringKey.OPERATOR_NAME, operatorName)
        )
        itemView.setLocaleContentDescription(
            R.string.chat_operator_joined_system_message,
            StringKeyPair(StringKey.OPERATOR_NAME, operatorName)
        )

        engagementStatesTheme?.connected.also(::applyEngagementState)
    }

    private fun applyJoinedState(operatorName: String, profileImgUrl: String?) {
        profileImgUrl?.let { statusPictureView.showProfileImage(it) }
            ?: statusPictureView.showPlaceholder()
        chatStartedNameView.text = operatorName
        chatStartedCaptionView.setLocaleText(
            R.string.chat_operator_joined_system_message, StringKeyPair(StringKey.OPERATOR_NAME, operatorName)
        )
        itemView.setLocaleContentDescription(
            R.string.chat_operator_joined_system_message,
            StringKeyPair(StringKey.OPERATOR_NAME, operatorName)
        )

        applyChatStartingViewsVisibility(false)
        applyChatStartedViewsVisibility()

        engagementStatesTheme?.connecting.also(::applyEngagementState)
    }

    private fun applyTransferringState() {
        statusPictureView.showPlaceholder()
        chatStartingCaptionView.isVisible = true
        chatStartedNameView.isVisible = true
        chatStartedCaptionView.isVisible = false
        chatStartingHeadingView.visibility = View.INVISIBLE

        chatStartedNameView.setLocaleText(R.string.engagement_queue_transferring)

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

    private fun isShowStatusViewRippleAnimation(item: OperatorStatusItem): Boolean = when (item) {
        is OperatorStatusItem.InQueue, is OperatorStatusItem.Transferring -> true
        else -> false
    }
}
