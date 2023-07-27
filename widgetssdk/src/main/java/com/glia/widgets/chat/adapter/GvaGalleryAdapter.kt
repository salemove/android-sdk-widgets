package com.glia.widgets.chat.adapter

import android.view.LayoutInflater
import com.glia.widgets.UiTheme
import com.glia.widgets.chat.adapter.holder.GvaGalleryItemViewHolder
import com.glia.widgets.databinding.ChatGvaGalleryItemBinding

internal class GvaGalleryAdapter(
    private val layoutInflater: LayoutInflater,
    private val buttonsClickListener: ChatAdapter.OnGvaButtonsClickListener,
    private val uiTheme: UiTheme
) {
    val items = mutableListOf<GvaGalleryItemViewHolder>()

    init {
        repeat(100) { // TODO: finish the buffer logic
            items.add(GvaGalleryItemViewHolder(
                ChatGvaGalleryItemBinding.inflate(layoutInflater),
                buttonsClickListener,
                uiTheme
            ))
        }
    }
}
