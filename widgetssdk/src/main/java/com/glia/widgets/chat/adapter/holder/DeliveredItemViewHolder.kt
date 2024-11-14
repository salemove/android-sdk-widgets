package com.glia.widgets.chat.adapter.holder

import androidx.recyclerview.widget.RecyclerView
import com.glia.widgets.R
import com.glia.widgets.UiTheme
import com.glia.widgets.databinding.ChatDeliveredItemLayoutBinding
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.getColorCompat
import com.glia.widgets.helper.getFontCompat
import com.glia.widgets.helper.setLocaleContentDescription
import com.glia.widgets.helper.setLocaleText
import com.glia.widgets.view.unifiedui.applyTextTheme
import com.glia.widgets.view.unifiedui.theme.UnifiedTheme
import com.glia.widgets.view.unifiedui.theme.chat.MessageBalloonTheme

internal class DeliveredItemViewHolder(
    private val binding: ChatDeliveredItemLayoutBinding,
    uiTheme: UiTheme,
    unifiedTheme: UnifiedTheme? = Dependencies.gliaThemeManager.theme
) : RecyclerView.ViewHolder(binding.root) {
    private val visitorTheme: MessageBalloonTheme? by lazy { unifiedTheme?.chatTheme?.visitorMessage }

    init {
        applyUiTheme(uiTheme)
        applyUnifiedTheme()
        itemView.setLocaleContentDescription(R.string.chat_message_delivered)
    }

    private fun applyUnifiedTheme() {
        binding.deliveredView.applyTextTheme(visitorTheme?.status)
        binding.deliveredView.setLocaleText(R.string.chat_message_delivered)
    }

    private fun applyUiTheme(uiTheme: UiTheme) {
        if (uiTheme.fontRes != null) {
            val fontFamily = itemView.getFontCompat(uiTheme.fontRes)
            binding.deliveredView.typeface = fontFamily
        }
        uiTheme.baseNormalColor?.let(itemView::getColorCompat)?.also(binding.deliveredView::setTextColor)
    }
}
