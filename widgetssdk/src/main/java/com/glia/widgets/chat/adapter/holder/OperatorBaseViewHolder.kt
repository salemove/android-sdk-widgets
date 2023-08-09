package com.glia.widgets.chat.adapter.holder

import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.glia.widgets.R
import com.glia.widgets.StringKey
import com.glia.widgets.StringKeyPair
import com.glia.widgets.UiTheme
import com.glia.widgets.chat.model.OperatorChatItem
import com.glia.widgets.di.Dependencies
import com.glia.widgets.view.OperatorStatusView
import com.glia.widgets.view.unifiedui.theme.UnifiedTheme
import com.glia.widgets.view.unifiedui.theme.chat.MessageBalloonTheme

internal open class OperatorBaseViewHolder(
    itemView: View,
    private val chatHeadView: OperatorStatusView,
    private val uiTheme: UiTheme,
    unifiedTheme: UnifiedTheme? = Dependencies.getGliaThemeManager().theme
) : RecyclerView.ViewHolder(itemView) {

    private val stringProvider = Dependencies.getStringProvider()
    val operatorTheme: MessageBalloonTheme? by lazy {
        unifiedTheme?.chatTheme?.operatorMessage
    }

    init {
        setupOperatorStatusView()
    }

    fun updateOperatorStatusView(item: OperatorChatItem) {
        chatHeadView.isVisible = item.showChatHead
        if (item.operatorProfileImgUrl != null) {
            chatHeadView.showProfileImage(item.operatorProfileImgUrl)
        } else {
            chatHeadView.showPlaceholder()
        }
    }

    fun updateItemContentDescription(operatorName: String?, message: String?) {
        when {
            operatorName.isNullOrEmpty() && message.isNullOrEmpty() -> {
                itemView.contentDescription = null
            }

            operatorName.isNullOrEmpty().not() -> {
                itemView.contentDescription = stringProvider.getRemoteString(
                    R.string.android_chat_operator_name_accessibility_message,
                    StringKeyPair(StringKey.OPERATOR_NAME, operatorName ?: ""),
                    StringKeyPair(StringKey.MESSAGE, message ?: "")
                )
            }

            else -> {
                itemView.contentDescription = stringProvider.getRemoteString(
                    R.string.android_chat_operator_message_accessibility,
                    StringKeyPair(StringKey.MESSAGE, message ?: "")
                )
            }
        }
    }

    private fun setupOperatorStatusView() {
        chatHeadView.setTheme(uiTheme)
        chatHeadView.setShowRippleAnimation(false)
        chatHeadView.applyUserImageTheme(operatorTheme?.userImage)
    }
}
