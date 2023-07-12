package com.glia.widgets.chat.adapter.holder

import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.glia.widgets.R
import com.glia.widgets.UiTheme
import com.glia.widgets.chat.model.history.GvaResponseText
import com.glia.widgets.databinding.ChatOperatorMessageLayoutBinding
import com.glia.widgets.databinding.ChatReceiveMessageContentBinding
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.fromHtml
import com.glia.widgets.helper.getColorCompat
import com.glia.widgets.helper.getColorStateListCompat
import com.glia.widgets.helper.getFontCompat
import com.glia.widgets.view.unifiedui.applyLayerTheme
import com.glia.widgets.view.unifiedui.applyTextTheme
import com.glia.widgets.view.unifiedui.theme.chat.MessageBalloonTheme

internal class GvaResponseTextViewHolder(
    private val operatorMessageBinding: ChatOperatorMessageLayoutBinding,
    private val messageContentBinding: ChatReceiveMessageContentBinding,
    private val uiTheme: UiTheme
) : RecyclerView.ViewHolder(operatorMessageBinding.root)  {

    private val operatorTheme: MessageBalloonTheme? by lazy {
        Dependencies.getGliaThemeManager().theme?.chatTheme?.operatorMessage
    }

    init {
        setupOperatorStatusView()
        setupMessageContentView()
    }

    private fun setupOperatorStatusView() {
        operatorMessageBinding.chatHeadView.setTheme(uiTheme)
        operatorMessageBinding.chatHeadView.setShowRippleAnimation(false)
        operatorMessageBinding.chatHeadView.applyUserImageTheme(operatorTheme?.userImage)
    }

    private fun setupMessageContentView() {
        messageContentBinding.root.apply {
            uiTheme.operatorMessageBackgroundColor?.let(::getColorStateListCompat)?.also {
                backgroundTintList = it
            }
            uiTheme.operatorMessageTextColor?.let(::getColorCompat)?.also(::setTextColor)
            uiTheme.operatorMessageTextColor?.let(::getColorCompat)?.also(::setLinkTextColor)

            uiTheme.fontRes?.let(::getFontCompat)?.also(::setTypeface)

            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO

            // Unified Ui
            applyLayerTheme(operatorTheme?.background)
            applyTextTheme(operatorTheme?.text)
        }
    }

    fun bind(item: GvaResponseText) {
        updateOperatorStatusView(item)
        updateMessageContentView(item)
    }

    private fun updateOperatorStatusView(item: GvaResponseText) {
        operatorMessageBinding.chatHeadView.isVisible = item.showChatHead
        if (item.operatorProfileImgUrl != null) {
            operatorMessageBinding.chatHeadView.showProfileImage(item.operatorProfileImgUrl)
        } else {
            operatorMessageBinding.chatHeadView.showPlaceholder()
        }
    }

    private fun updateMessageContentView(item: GvaResponseText) {
        messageContentBinding.root.text = item.content.fromHtml()

        if (item.operatorName?.isNotEmpty() == true) {
            itemView.contentDescription = itemView.resources.getString(
                R.string.glia_chat_operator_name_message_content_description,
                item.operatorName,
                item.content
            )
        } else {
            itemView.contentDescription = itemView.resources.getString(
                R.string.glia_chat_operator_message_content_description,
                item.content
            )
        }
    }
}
