package com.glia.widgets.chat.adapter.holder

import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.glia.widgets.R
import com.glia.widgets.UiTheme
import com.glia.widgets.chat.model.VisitorMessageItem
import com.glia.widgets.databinding.ChatVisitorMessageLayoutBinding
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.getColorCompat
import com.glia.widgets.helper.getColorStateListCompat
import com.glia.widgets.helper.getFontCompat
import com.glia.widgets.view.unifiedui.applyLayerTheme
import com.glia.widgets.view.unifiedui.applyTextTheme
import com.glia.widgets.view.unifiedui.theme.chat.MessageBalloonTheme

internal class VisitorMessageViewHolder(
    private val binding: ChatVisitorMessageLayoutBinding,
    uiTheme: UiTheme
) : RecyclerView.ViewHolder(binding.root) {

    private val visitorTheme: MessageBalloonTheme? by lazy {
        Dependencies.getGliaThemeManager().theme?.chatTheme?.visitorMessage
    }

    init {
        uiTheme.visitorMessageBackgroundColor?.let(itemView::getColorStateListCompat)
            ?.also(binding.content::setBackgroundTintList)

        uiTheme.visitorMessageTextColor?.let(itemView::getColorCompat)
            ?.also(binding.content::setTextColor)

        if (uiTheme.fontRes != null) {
            val fontFamily = itemView.getFontCompat(uiTheme.fontRes)
            binding.content.typeface = fontFamily
            binding.deliveredView.typeface = fontFamily
        }
        uiTheme.baseNormalColor?.let(itemView::getColorCompat)
            ?.also(binding.deliveredView::setTextColor)

        // Unified Ui
        binding.content.applyLayerTheme(visitorTheme?.background)
        binding.content.applyTextTheme(visitorTheme?.text)
        binding.deliveredView.applyTextTheme(visitorTheme?.status)
    }

    fun bind(item: VisitorMessageItem) {
        binding.content.text = item.message
        binding.deliveredView.isVisible = item.showDelivered
        val contentDescription = itemView.resources.getString(
            if (item.showDelivered) {
                R.string.glia_chat_visitor_message_delivered_content_description
            } else {
                R.string.glia_chat_visitor_message_content_description
            },
            item.message
        )
        itemView.contentDescription = contentDescription
    }

    fun updateDelivered(delivered: Boolean) {
        binding.deliveredView.isVisible = delivered
    }
}
