package com.glia.widgets.entrywidget.adapter

import android.view.View
import androidx.core.view.isVisible
import com.glia.widgets.R
import com.glia.widgets.databinding.EntryWidgetErrorItemBinding
import com.glia.widgets.entrywidget.EntryWidgetContract
import com.glia.widgets.helper.setLocaleText

internal class EntryWidgetErrorStateViewHolder(
    private val binding: EntryWidgetErrorItemBinding
) : EntryWidgetAdapter.ViewHolder(binding.root) {

    override fun bind(itemType: EntryWidgetContract.ItemType, onClickListener: View.OnClickListener) {
        when (itemType) {
            EntryWidgetContract.ItemType.EMPTY_STATE -> {
                binding.title.setLocaleText(R.string.entry_widget_empty_state_title)
                binding.description.setLocaleText(R.string.entry_widget_empty_state_description)
                binding.button.isVisible = false
            }
            EntryWidgetContract.ItemType.ERROR_STATE -> {
                binding.title.setLocaleText(R.string.entry_widget_error_state_title)
                binding.description.setLocaleText(R.string.entry_widget_error_state_description)
                binding.button.setLocaleText(R.string.entry_widget_error_state_try_again_button_label)
                binding.button.setOnClickListener(onClickListener)
                binding.button.isVisible = true
            }
            else -> {
                // Do nothing
            }
        }
    }
}
