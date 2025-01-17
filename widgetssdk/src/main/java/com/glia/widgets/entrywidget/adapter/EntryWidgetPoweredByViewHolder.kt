package com.glia.widgets.entrywidget.adapter

import com.glia.widgets.R
import com.glia.widgets.databinding.EntryWidgetPoweredByItemBinding
import com.glia.widgets.helper.setLocaleText

internal class EntryWidgetPoweredByViewHolder(binding: EntryWidgetPoweredByItemBinding) : EntryWidgetAdapter.ViewHolder(binding.root) {

    init {
        binding.title.setLocaleText(R.string.general_powered)
    }
}
