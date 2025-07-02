package com.glia.widgets.entrywidget.adapter

import android.view.View
import androidx.core.view.isVisible
import com.glia.widgets.R
import com.glia.widgets.databinding.EntryWidgetErrorItemBinding
import com.glia.widgets.entrywidget.EntryWidgetContract
import com.glia.widgets.helper.setLocaleText
import com.glia.widgets.view.unifiedui.applyButtonTheme
import com.glia.widgets.view.unifiedui.applyTextTheme
import com.glia.widgets.view.unifiedui.theme.base.ButtonTheme
import com.glia.widgets.view.unifiedui.theme.base.TextTheme

internal class EntryWidgetErrorStateViewHolder(
    private val binding: EntryWidgetErrorItemBinding,
    private val viewType: EntryWidgetContract.ViewType,
    errorTitleTheme: TextTheme? = null,
    errorMessageTheme: TextTheme? = null,
    errorButtonTheme: ButtonTheme? = null
) : EntryWidgetAdapter.ViewHolder(binding.root) {

    init {
        errorTitleTheme?.let { binding.title.applyTextTheme(it) }
        errorMessageTheme?.let { binding.description.applyTextTheme(it) }
        errorButtonTheme?.let { binding.button.applyButtonTheme(it) }
    }

    override fun bind(itemType: EntryWidgetContract.ItemType, onClickListener: View.OnClickListener) {
        when (itemType) {
            EntryWidgetContract.ItemType.EmptyState -> {
                binding.title.setLocaleText(R.string.entry_widget_empty_state_title)
                binding.description.setLocaleText(R.string.entry_widget_empty_state_description)
                binding.button.isVisible = false
            }

            EntryWidgetContract.ItemType.ErrorState -> {
                binding.title.setLocaleText(R.string.entry_widget_error_state_title)
                binding.description.setLocaleText(R.string.entry_widget_error_state_description)
                binding.button.setLocaleText(R.string.entry_widget_error_state_try_again_button_label)
                binding.button.setOnClickListener(onClickListener)
                binding.button.isVisible = true
            }

            EntryWidgetContract.ItemType.SdkNotInitializedState -> {
                binding.title.setLocaleText(R.string.entry_widget_error_state_title)
                binding.description.setLocaleText(R.string.entry_widget_error_state_description)
                binding.button.setLocaleText(R.string.entry_widget_error_state_try_again_button_label)
                binding.button.setOnClickListener(onClickListener)
                binding.button.isVisible = false
            }

            else -> {
                // Do nothing
            }
        }

        if (viewType == EntryWidgetContract.ViewType.BOTTOM_SHEET) {
            binding.root.minHeight = binding.root.resources.getDimensionPixelSize(R.dimen.entry_widget_error_item_min_height)
        }
    }
}
