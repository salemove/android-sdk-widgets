package com.glia.widgets.chat.adapter.holder

import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.glia.widgets.R
import com.glia.widgets.UiTheme
import com.glia.widgets.chat.model.history.OperatorChatItem
import com.glia.widgets.databinding.ChatOperatorMessageLayoutBinding
import com.glia.widgets.di.Dependencies
import com.glia.widgets.view.unifiedui.theme.chat.MessageBalloonTheme

internal open class OperatorBaseViewHolder(
    private val operatorMessageBinding: ChatOperatorMessageLayoutBinding,
    private val uiTheme: UiTheme
) : RecyclerView.ViewHolder(operatorMessageBinding.root) {

    val operatorTheme: MessageBalloonTheme? by lazy {
        Dependencies.getGliaThemeManager().theme?.chatTheme?.operatorMessage
    }

    init {
        setupOperatorStatusView()
    }

    fun updateOperatorStatusView(item: OperatorChatItem) {
        operatorMessageBinding.chatHeadView.isVisible = item.showChatHead
        if (item.operatorProfileImgUrl != null) {
            operatorMessageBinding.chatHeadView.showProfileImage(item.operatorProfileImgUrl)
        } else {
            operatorMessageBinding.chatHeadView.showPlaceholder()
        }
    }

    fun updateItemContentDescription(operatorName: String?, message: String?) {
        when {
            operatorName.isNullOrEmpty() && message.isNullOrEmpty() -> {
                itemView.contentDescription = null
            }

            operatorName.isNullOrEmpty().not() -> {
                itemView.contentDescription = itemView.resources.getString(
                    R.string.glia_chat_operator_name_message_content_description,
                    operatorName,
                    message
                )
            }

            else -> {
                itemView.contentDescription = itemView.resources.getString(
                    R.string.glia_chat_operator_message_content_description,
                    message
                )
            }
        }
    }

    private fun setupOperatorStatusView() {
        operatorMessageBinding.chatHeadView.setTheme(uiTheme)
        operatorMessageBinding.chatHeadView.setShowRippleAnimation(false)
        operatorMessageBinding.chatHeadView.applyUserImageTheme(operatorTheme?.userImage)
    }
}
