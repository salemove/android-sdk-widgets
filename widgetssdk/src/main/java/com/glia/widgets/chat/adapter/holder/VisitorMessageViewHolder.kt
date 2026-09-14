package com.glia.widgets.chat.adapter.holder

import androidx.recyclerview.widget.RecyclerView
import com.glia.widgets.R
import com.glia.widgets.chat.adapter.ChatAdapter
import com.glia.widgets.chat.model.VisitorMessageItem
import com.glia.widgets.databinding.ChatVisitorMessageLayoutBinding
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.addClickActionAccessibilityLabel
import com.glia.widgets.helper.gliaAttrFont
import com.glia.widgets.helper.removeAccessibilityClickAction
import com.glia.widgets.helper.setLocaleContentDescription
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
    unifiedTheme: UnifiedTheme? = Dependencies.gliaThemeManager.theme,
    private val localeProvider: LocaleProvider = Dependencies.localeProvider
) : RecyclerView.ViewHolder(binding.root) {

    private val visitorTheme: MessageBalloonTheme? by lazy { unifiedTheme?.chatTheme?.visitorMessage }

    private lateinit var id: String

    init {
        applyThemeFont()
        applyUnifiedTheme()
    }

    private fun applyUnifiedTheme() {
        binding.content.applyLayerTheme(visitorTheme?.background)
        binding.content.applyTextTheme(visitorTheme?.text)
    }

    /** The bubble's background and text colours come from the layout's `?attr/gliaVisitorMessage*`. */
    private fun applyThemeFont() {
        itemView.context.gliaAttrFont()?.also { binding.content.typeface = it }
    }

    fun bind(item: VisitorMessageItem) {
        this.id = item.id

        updateStatus(item.isError, item.message)
    }

    fun updateStatus(isError: Boolean, message: String) {
        binding.content.text = message

        val contentDescriptionRes = if (isError) {
            R.string.android_chat_visitor_message_not_delivered_accessibility
        } else {
            R.string.android_chat_visitor_message_accessibility
        }

        itemView.setLocaleContentDescription(contentDescriptionRes, StringKeyPair(StringKey.MESSAGE, binding.content.text.toString()))

        if (isError) {
            itemView.addClickActionAccessibilityLabel(localeProvider.getString(R.string.general_retry))
            binding.content.addClickActionAccessibilityLabel(localeProvider.getString(R.string.general_retry))
            binding.content.setOnClickListener { onRetryClickListener.onRetryClicked(id) }
            binding.content.setTextIsSelectable(false)
            itemView.setOnClickListener { onRetryClickListener.onRetryClicked(id) }
        } else {
            itemView.removeAccessibilityClickAction()
            binding.content.removeAccessibilityClickAction()
            binding.content.setTextIsSelectable(true)
            itemView.setOnClickListener(null)
            binding.content.setOnClickListener(null)
        }
    }

}
