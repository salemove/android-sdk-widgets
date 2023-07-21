package com.glia.widgets.chat.adapter.holder

import androidx.recyclerview.widget.LinearLayoutManager
import com.glia.widgets.UiTheme
import com.glia.widgets.chat.adapter.ChatAdapter
import com.glia.widgets.chat.adapter.GvaGalleryAdapter
import com.glia.widgets.chat.model.GvaGalleryCard
import com.glia.widgets.chat.model.GvaGalleryCards
import com.glia.widgets.databinding.ChatGvaGalleryLayoutBinding
import com.glia.widgets.databinding.ChatOperatorMessageLayoutBinding

internal class GvaGalleryViewHolder(
    operatorMessageBinding: ChatOperatorMessageLayoutBinding,
    contentBinding: ChatGvaGalleryLayoutBinding,
    buttonsClickListener: ChatAdapter.OnGvaButtonsClickListener,
    private val uiTheme: UiTheme
) : OperatorBaseViewHolder(operatorMessageBinding, uiTheme) {
    private val adapter = GvaGalleryAdapter(buttonsClickListener, uiTheme)

    init {
        contentBinding.cardRecyclerView.adapter = adapter
        contentBinding.cardRecyclerView.layoutManager = LinearLayoutManager(
            contentBinding.root.context,
            LinearLayoutManager.HORIZONTAL,
            false
        )
    }

    fun bind(item: GvaGalleryCards) {
        updateOperatorStatusView(item)

        setupItems(item.galleryCards)
    }

    private fun setupItems(galleryCards: List<GvaGalleryCard>) {
        adapter.setGalleryCards(galleryCards)
    }

}
