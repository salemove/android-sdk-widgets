package com.glia.widgets.entrywidget.adapter

import android.view.View
import androidx.core.view.isVisible
import com.glia.widgets.R
import com.glia.widgets.databinding.EntryWidgetAuthenticatedContactBinding
import com.glia.widgets.entrywidget.EntryWidgetContract
import com.glia.widgets.helper.setLocaleHint
import com.glia.widgets.helper.setLocaleText
import com.glia.widgets.view.unifiedui.applyImageColorTheme
import com.glia.widgets.view.unifiedui.applyLayerTheme
import com.glia.widgets.view.unifiedui.applyTextTheme
import com.glia.widgets.view.unifiedui.theme.entrywidget.MediaTypeItemTheme
import java.util.Locale

internal class EntryWidgetAuthenticatedContactViewHolder(
    private val binding: EntryWidgetAuthenticatedContactBinding,
    itemTheme: MediaTypeItemTheme?
) : EntryWidgetAdapter.ViewHolder(binding.root) {

    init {
        itemTheme?.let {
            binding.root.applyLayerTheme(it.background)
            binding.title.applyTextTheme(it.title)
            binding.description.applyTextTheme(it.message)
            binding.icon.applyImageColorTheme(it.iconColor)
            it.loadingTintColor?.primaryColorStateList?.let { tintList ->
                binding.iconLoading.backgroundTintList = tintList
                binding.titleLoading.backgroundTintList = tintList
                binding.descriptionLoading.backgroundTintList = tintList
            }
        }
    }

    override fun bind(itemType: EntryWidgetContract.ItemType, onClickListener: View.OnClickListener) {
        binding.root.setOnClickListener(onClickListener)
        binding.root.contentDescription = null
        binding.icon.setImageResource(R.drawable.ic_secure_message)
        binding.title.setLocaleText(R.string.entry_widget_secure_messaging_button_label)
        binding.description.setLocaleText(R.string.entry_widget_secure_messaging_button_description)
        binding.description.setLocaleHint(R.string.entry_widget_secure_messaging_button_accessibility_hint)
        binding.unreadMessagesBadge.visibility = View.GONE
        binding.loadingGroup.isVisible = itemType == EntryWidgetContract.ItemType.LOADING_STATE
    }

    fun updateUnreadMessageCount(count: Int) {
        if (count > 0) {
            binding.unreadMessagesBadge.text = String.format(Locale.getDefault(), "%d", count)
            binding.unreadMessagesBadge.visibility = View.VISIBLE
        } else {
            binding.unreadMessagesBadge.text = ""
            binding.unreadMessagesBadge.visibility = View.GONE
        }
    }
}
