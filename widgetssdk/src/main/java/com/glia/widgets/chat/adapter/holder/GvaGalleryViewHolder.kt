package com.glia.widgets.chat.adapter.holder

import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import com.glia.widgets.UiTheme
import com.glia.widgets.chat.adapter.ChatAdapter
import com.glia.widgets.chat.adapter.GvaGalleryAdapter
import com.glia.widgets.chat.model.GvaGalleryCard
import com.glia.widgets.chat.model.GvaGalleryCards
import com.glia.widgets.databinding.ChatGvaGalleryLayoutBinding

internal class GvaGalleryViewHolder(
    private val contentBinding: ChatGvaGalleryLayoutBinding,
    buttonsClickListener: ChatAdapter.OnGvaButtonsClickListener,
    uiTheme: UiTheme
) : OperatorBaseViewHolder(contentBinding.root, contentBinding.chatHeadView, uiTheme) {
    private val adapter = GvaGalleryAdapter(buttonsClickListener, uiTheme)

    init {
        contentBinding.cardRecyclerView.adapter = adapter
        contentBinding.cardRecyclerView.layoutManager = LinearLayoutManager(
            contentBinding.root.context,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        LinearSnapHelper().attachToRecyclerView(contentBinding.cardRecyclerView)
    }

    fun bind(item: GvaGalleryCards, measuredHeight: Int?) {
        updateOperatorStatusView(item)

        setupRecyclerViewHeight(measuredHeight)
        setupItems(item.galleryCards)
    }

    private fun setupItems(galleryCards: List<GvaGalleryCard>) {
        adapter.setGalleryCards(galleryCards)
        contentBinding.cardRecyclerView.scrollToPosition(0)
    }

    private fun setupRecyclerViewHeight(measuredHeight: Int?) {
        if (measuredHeight != null) {
            contentBinding.cardRecyclerView.layoutParams.height = measuredHeight
        } else {
            contentBinding.cardRecyclerView.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        }
    }

}
