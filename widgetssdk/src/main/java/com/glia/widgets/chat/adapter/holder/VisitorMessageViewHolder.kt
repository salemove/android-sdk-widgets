package com.glia.widgets.chat.adapter.holder

import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.glia.widgets.R
import com.glia.widgets.UiTheme
import com.glia.widgets.chat.adapter.ChatAdapter
import com.glia.widgets.chat.model.VisitorItemStatus
import com.glia.widgets.chat.model.VisitorMessageItem
import com.glia.widgets.databinding.ChatVisitorMessageLayoutBinding
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.addClickActionAccessibilityLabel
import com.glia.widgets.helper.getColorCompat
import com.glia.widgets.helper.getColorStateListCompat
import com.glia.widgets.helper.getFontCompat
import com.glia.widgets.helper.removeAccessibilityClickAction
import com.glia.widgets.helper.setLocaleContentDescription
import com.glia.widgets.helper.setLocaleText
import com.glia.widgets.locale.LocaleProvider
import com.glia.widgets.locale.StringKey
import com.glia.widgets.locale.StringKeyPair
import com.glia.widgets.view.unifiedui.applyLayerTheme
import com.glia.widgets.view.unifiedui.applyTextTheme
import com.glia.widgets.view.unifiedui.theme.UnifiedTheme
import com.glia.widgets.view.unifiedui.theme.chat.MessageBalloonTheme

internal class VisitorMessageViewHolder(
    private val binding: ChatVisitorMessageLayoutBinding,
    private val onRetryClickListener: ChatAdapter.OnRetryClickListener,
    uiTheme: UiTheme,
    unifiedTheme: UnifiedTheme? = Dependencies.gliaThemeManager.theme,
    private val localeProvider: LocaleProvider = Dependencies.localeProvider
) : RecyclerView.ViewHolder(binding.root) {

    private val visitorTheme: MessageBalloonTheme? by lazy { unifiedTheme?.chatTheme?.visitorMessage }

    private lateinit var id: String

    init {
        applyUiTheme(uiTheme)
        applyUnifiedTheme()
    }

    private fun applyUnifiedTheme() {
        binding.content.applyLayerTheme(visitorTheme?.background)
        binding.content.applyTextTheme(visitorTheme?.text)
        binding.deliveredView.applyTextTheme(visitorTheme?.status)
        binding.errorView.applyTextTheme(visitorTheme?.error)
        binding.deliveredView.setLocaleText(R.string.chat_message_delivered)
        binding.errorView.setLocaleText(R.string.chat_message_failed_to_deliver_retry)
    }

    private fun applyUiTheme(uiTheme: UiTheme) {
        uiTheme.visitorMessageBackgroundColor?.let(itemView::getColorStateListCompat)?.also(binding.content::setBackgroundTintList)

        uiTheme.visitorMessageTextColor?.let(itemView::getColorCompat)?.also(binding.content::setTextColor)

        if (uiTheme.fontRes != null) {
            val fontFamily = itemView.getFontCompat(uiTheme.fontRes)
            binding.content.typeface = fontFamily
            binding.deliveredView.typeface = fontFamily
            binding.errorView.typeface = fontFamily
        }
        uiTheme.baseNormalColor?.let(itemView::getColorCompat)?.also(binding.deliveredView::setTextColor)

        uiTheme.systemNegativeColor?.let(itemView::getColorCompat)?.also(binding.errorView::setTextColor)
    }

    fun bind(item: VisitorMessageItem) {
        this.id = item.id

        updateStatus(item.status, item.message)
    }

    fun updateStatus(status: VisitorItemStatus, message: String) {
        binding.content.text = message
        binding.deliveredView.isVisible = status == VisitorItemStatus.DELIVERED
        binding.errorView.isVisible = status == VisitorItemStatus.ERROR_INDICATOR

        val contentDescriptionRes = when (status) {
            VisitorItemStatus.ERROR, VisitorItemStatus.ERROR_INDICATOR -> R.string.android_chat_visitor_message_not_delivered_accessibility
            VisitorItemStatus.DELIVERED -> R.string.android_chat_visitor_message_delivered_accessibility
            else -> R.string.android_chat_visitor_message_accessibility
        }

        if (status.isError) {
            itemView.addClickActionAccessibilityLabel(localeProvider.getString(R.string.general_retry))
            itemView.setOnClickListener { onRetryClickListener.onRetryClicked(id) }
            binding.content.setOnClickListener { onRetryClickListener.onRetryClicked(id) }
        } else {
            itemView.removeAccessibilityClickAction()
            itemView.setOnClickListener(null)
            binding.content.setOnClickListener(null)
        }

        itemView.setLocaleContentDescription(
            contentDescriptionRes,
            StringKeyPair(StringKey.MESSAGE, binding.content.text.toString())
        )
    }

}
