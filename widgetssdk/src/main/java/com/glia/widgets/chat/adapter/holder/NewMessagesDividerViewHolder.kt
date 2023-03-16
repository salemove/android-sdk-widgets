package com.glia.widgets.chat.adapter.holder

import androidx.recyclerview.widget.RecyclerView
import com.glia.widgets.UiTheme
import com.glia.widgets.databinding.ChatNewMessagesDividerLayoutBinding
import com.glia.widgets.di.Dependencies
import com.glia.widgets.view.unifiedui.extensions.applyColorTheme
import com.glia.widgets.view.unifiedui.extensions.applyTextTheme
import com.glia.widgets.view.unifiedui.extensions.getColorCompat
import com.glia.widgets.view.unifiedui.extensions.getFontCompat
import com.glia.widgets.view.unifiedui.theme.chat.ChatTheme

class NewMessagesDividerViewHolder(binding: ChatNewMessagesDividerLayoutBinding, uiTheme: UiTheme) :
    RecyclerView.ViewHolder(binding.root) {

    private val theme: ChatTheme? by lazy {
        Dependencies.getGliaThemeManager().theme?.chatTheme
    }

    init {
        setupUiTheme(uiTheme, binding)
        setupUnifiedUiTheme(binding)
    }

    private fun setupUiTheme(
        uiTheme: UiTheme,
        binding: ChatNewMessagesDividerLayoutBinding
    ) {

        uiTheme.newMessageDividerColor?.also {
            binding.newMessagesDividerLeft.setBackgroundResource(it)
            binding.newMessagesDividerRight.setBackgroundResource(it)
        }
        uiTheme.newMessageDividerTextColor?.also {
            binding.newMessagesTv.setTextColor(itemView.getColorCompat(it))
        }

        if (uiTheme.fontRes != null) {
            val fontFamily = itemView.getFontCompat(uiTheme.fontRes)
            binding.newMessagesTv.typeface = fontFamily
        }

        uiTheme.fontRes?.let {
            itemView.getFontCompat(it)
        }?.also {
            binding.newMessagesTv.typeface = it
        }
    }

    private fun setupUnifiedUiTheme(binding: ChatNewMessagesDividerLayoutBinding) {
        theme?.apply {
            newMessagesDividerColorTheme.also {
                binding.newMessagesDividerLeft.applyColorTheme(it)
                binding.newMessagesDividerRight.applyColorTheme(it)
            }
            binding.newMessagesTv.applyTextTheme(newMessagesDividerTextTheme, withAlignment = false)
        }
    }

}
