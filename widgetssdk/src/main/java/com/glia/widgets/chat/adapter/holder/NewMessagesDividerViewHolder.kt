package com.glia.widgets.chat.adapter.holder

import androidx.recyclerview.widget.RecyclerView
import com.glia.widgets.R
import com.glia.widgets.databinding.ChatNewMessagesDividerLayoutBinding
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.gliaAttrFont
import com.glia.widgets.helper.setLocaleText
import com.glia.widgets.view.unifiedui.applyColorTheme
import com.glia.widgets.view.unifiedui.applyTextTheme
import com.glia.widgets.view.unifiedui.theme.chat.ChatTheme

internal class NewMessagesDividerViewHolder(
    binding: ChatNewMessagesDividerLayoutBinding
) :
    RecyclerView.ViewHolder(binding.root) {

    private val theme: ChatTheme? by lazy {
        Dependencies.gliaThemeManager.theme?.chatTheme
    }

    init {
        applyThemeFont(binding)
        applyUnifiedTheme(binding)
    }

    /**
     * The divider and its text take their colours from the layout's
     * `?attr/gliaNewMessagesDivider{,Text}Color`.
     */
    private fun applyThemeFont(binding: ChatNewMessagesDividerLayoutBinding) {
        itemView.context.gliaAttrFont()?.also { binding.newMessagesTv.typeface = it }
    }

    private fun applyUnifiedTheme(binding: ChatNewMessagesDividerLayoutBinding) {
        binding.newMessagesTv.setLocaleText(R.string.chat_unread_message_divider)
        theme?.apply {
            newMessagesDividerColorTheme.also {
                binding.newMessagesDividerLeft.applyColorTheme(it)
                binding.newMessagesDividerRight.applyColorTheme(it)
            }
            binding.newMessagesTv.applyTextTheme(newMessagesDividerTextTheme, withAlignment = false)
        }
    }
}
