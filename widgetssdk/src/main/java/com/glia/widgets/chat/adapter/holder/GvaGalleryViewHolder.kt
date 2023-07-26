package com.glia.widgets.chat.adapter.holder

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
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

    private val holder: GvaGalleryItemViewHolder by lazy {
        adapter.createViewHolder(contentBinding.cardRecyclerView, 0)
    }

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
        setupRecyclerViewHeight(galleryCards) {
            adapter.setGalleryCards(galleryCards)
        }
    }

    private fun setupRecyclerViewHeight(galleryCards: List<GvaGalleryCard>, onComplete: () -> Unit) {
        val root = contentBinding.root
        val holderView = holder.itemView
        holderView.visibility = View.INVISIBLE
        root.addView(holderView)

        var position = 0
        var maxHeight = 0

        fun nextHeight() {
            holder.bind(galleryCards[position])

            holderView.post {
                val height = holderView.height
                if (maxHeight < height) {
                    maxHeight = height
                }

                position += 1
                if (position < galleryCards.size) {
                    nextHeight()
                } else {
                    root.removeView(holderView)
                    contentBinding.cardRecyclerView.layoutParams.height = maxHeight
                    contentBinding.cardRecyclerView.requestLayout()
                    onComplete()
                }
            }
        }

        nextHeight()
    }

}
