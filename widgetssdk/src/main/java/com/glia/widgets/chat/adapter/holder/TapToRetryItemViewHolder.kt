package com.glia.widgets.chat.adapter.holder

import androidx.recyclerview.widget.RecyclerView
import com.glia.widgets.R
import com.glia.widgets.UiTheme
import com.glia.widgets.chat.adapter.ChatAdapter
import com.glia.widgets.chat.model.TapToRetryItem
import com.glia.widgets.databinding.ChatTapToRetryItemLayoutBinding
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.getColorCompat
import com.glia.widgets.helper.getFontCompat
import com.glia.widgets.helper.setLocaleText
import com.glia.widgets.view.unifiedui.applyTextTheme
import com.glia.widgets.view.unifiedui.theme.UnifiedTheme
import com.glia.widgets.view.unifiedui.theme.chat.MessageBalloonTheme

internal class TapToRetryItemViewHolder(
    private val binding: ChatTapToRetryItemLayoutBinding,
    uiTheme: UiTheme,
    private val onRetryClickListener: ChatAdapter.OnRetryClickListener,
    unifiedTheme: UnifiedTheme? = Dependencies.gliaThemeManager.theme
) : RecyclerView.ViewHolder(binding.root) {
    private val visitorTheme: MessageBalloonTheme? by lazy { unifiedTheme?.chatTheme?.visitorMessage }

    init {
        applyUiTheme(uiTheme)
        applyUnifiedTheme()
    }

    fun bind(item: TapToRetryItem) {
        itemView.setOnClickListener { onRetryClickListener.onRetryClicked(item.messageId) }
    }

    private fun applyUnifiedTheme() {
        binding.errorView.applyTextTheme(visitorTheme?.error)
        binding.errorView.setLocaleText(R.string.chat_message_failed_to_deliver_retry)
    }

    private fun applyUiTheme(uiTheme: UiTheme) {
        if (uiTheme.fontRes != null) {
            val fontFamily = itemView.getFontCompat(uiTheme.fontRes)
            binding.errorView.typeface = fontFamily
        }
        uiTheme.systemNegativeColor?.let(itemView::getColorCompat)?.also(binding.errorView::setTextColor)
    }
}
