package com.glia.widgets.chat.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.glia.widgets.UiTheme
import com.glia.widgets.chat.adapter.holder.GvaGalleryItemViewHolder
import com.glia.widgets.chat.model.GvaGalleryCard
import com.glia.widgets.databinding.ChatGvaGalleryItemBinding
import com.glia.widgets.helper.layoutInflater

internal class GvaGalleryAdapter(
    private val buttonsClickListener: ChatAdapter.OnGvaButtonsClickListener,
    private val uiTheme: UiTheme
) : RecyclerView.Adapter<GvaGalleryItemViewHolder>() {
    private var galleryCards: List<GvaGalleryCard>? = null

    fun setGalleryCards(galleryCards: List<GvaGalleryCard>) {
        this.galleryCards = galleryCards
        notifyDataSetChanged()
    }

    override fun getItemCount() = galleryCards?.count() ?: 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = GvaGalleryItemViewHolder(
        ChatGvaGalleryItemBinding.inflate(parent.layoutInflater, parent, false),
        buttonsClickListener,
        uiTheme
    )

    override fun onBindViewHolder(holder: GvaGalleryItemViewHolder, position: Int) {
        galleryCards?.apply {
            getOrNull(position)?.let {
                holder.bind(it, position, size)
            }
        }
    }
}
