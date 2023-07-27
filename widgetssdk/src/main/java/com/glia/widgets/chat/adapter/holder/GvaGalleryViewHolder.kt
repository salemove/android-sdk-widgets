package com.glia.widgets.chat.adapter.holder

import com.glia.widgets.UiTheme
import com.glia.widgets.chat.adapter.ChatAdapter
import com.glia.widgets.chat.adapter.GvaGalleryAdapter
import com.glia.widgets.chat.model.GvaGalleryCard
import com.glia.widgets.chat.model.GvaGalleryCards
import com.glia.widgets.databinding.ChatGvaGalleryItemBinding
import com.glia.widgets.databinding.ChatGvaGalleryLayoutBinding
import com.glia.widgets.helper.layoutInflater

internal class GvaGalleryViewHolder(
    private val contentBinding: ChatGvaGalleryLayoutBinding,
    private val buttonsClickListener: ChatAdapter.OnGvaButtonsClickListener,
    private val gvaGalleryAdapter: GvaGalleryAdapter,
    private val uiTheme: UiTheme
) : OperatorBaseViewHolder(contentBinding.root, contentBinding.chatHeadView, uiTheme) {

    fun bind(item: GvaGalleryCards) {
        updateOperatorStatusView(item)

        setupItems(item.galleryCards)
    }

    private fun setupItems(galleryCards: List<GvaGalleryCard>) {
        contentBinding.container.removeAllViews()
        val layoutInflater = contentBinding.root.layoutInflater
        galleryCards.forEach {

            val holder = GvaGalleryItemViewHolder(
                ChatGvaGalleryItemBinding.inflate(layoutInflater, contentBinding.container, false),
                buttonsClickListener,
                uiTheme
            )
//            val holder = gvaGalleryAdapter.items.removeFirst()
            holder.setupData(it)

            contentBinding.container.addView(holder.itemView)
        }
    }

}
