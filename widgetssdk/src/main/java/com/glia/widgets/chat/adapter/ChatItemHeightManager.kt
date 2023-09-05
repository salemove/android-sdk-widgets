package com.glia.widgets.chat.adapter

import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import androidx.collection.ArrayMap
import com.glia.widgets.R
import com.glia.widgets.UiTheme
import com.glia.widgets.chat.adapter.holder.GvaGalleryItemViewHolder
import com.glia.widgets.chat.model.ChatItem
import com.glia.widgets.chat.model.GvaGalleryCard
import com.glia.widgets.chat.model.GvaGalleryCards
import com.glia.widgets.databinding.ChatGvaGalleryItemBinding
import com.glia.widgets.di.Dependencies
import com.glia.widgets.view.unifiedui.theme.UnifiedTheme

internal class ChatItemHeightManager(
    private val uiTheme: UiTheme,
    private val layoutInflater: LayoutInflater,
    private val resources: Resources,
    private val unifiedTheme: UnifiedTheme? = Dependencies.getGliaThemeManager().theme
) {
    private val measuredHeightsMap = ArrayMap<ChatItem, Int>()

    private val gvaGalleryItemViewHolder: GvaGalleryItemViewHolder by lazy {
        GvaGalleryItemViewHolder(ChatGvaGalleryItemBinding.inflate(layoutInflater), {}, uiTheme, unifiedTheme)
    }

    private val gvaGalleryCardWidth: Int by lazy {
        resources.getDimensionPixelOffset(R.dimen.glia_chat_gva_gallery_card_width)
    }

    fun getMeasuredHeight(chatItem: ChatItem): Int? {
        return measuredHeightsMap[chatItem]
    }

    fun measureHeight(chatItems: List<ChatItem>?) {
        chatItems
            ?.filterIsInstance<GvaGalleryCards>() // Currently the HeightManager works only for GvaGalleryCards
            ?.forEach { chatItem ->
            if (!measuredHeightsMap.contains(chatItem)) {
                measuredHeightsMap[chatItem] = measureHeight(chatItem)
            }
        }
    }

    private fun measureHeight(gvaGalleryCards: GvaGalleryCards): Int {
        return gvaGalleryCards.galleryCards.maxOf(::measureHeight)
    }

    private fun measureHeight(gvaGalleryCard: GvaGalleryCard): Int {
        gvaGalleryItemViewHolder.bindForMeasure(gvaGalleryCard)
        gvaGalleryItemViewHolder.itemView.measure(
            View.MeasureSpec.makeMeasureSpec(gvaGalleryCardWidth, View.MeasureSpec.AT_MOST),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        return gvaGalleryItemViewHolder.itemView.measuredHeight
    }
}
