package com.glia.widgets.chat.adapter.holder

import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.core.view.AccessibilityDelegateCompat
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.glia.widgets.R
import com.glia.widgets.UiTheme
import com.glia.widgets.chat.adapter.ChatAdapter
import com.glia.widgets.chat.adapter.GvaButtonsAdapter
import com.glia.widgets.chat.model.GvaGalleryCard
import com.glia.widgets.databinding.ChatGvaGalleryItemBinding
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.fromHtml
import com.glia.widgets.helper.getColorCompat
import com.glia.widgets.helper.getColorStateListCompat
import com.glia.widgets.helper.getFontCompat
import com.glia.widgets.helper.load
import com.glia.widgets.view.unifiedui.applyLayerTheme
import com.glia.widgets.view.unifiedui.applyTextTheme
import com.glia.widgets.view.unifiedui.theme.chat.MessageBalloonTheme
import com.glia.widgets.view.unifiedui.theme.gva.GvaGalleryCardTheme
import kotlin.properties.Delegates

internal class GvaGalleryItemViewHolder(
    private val binding: ChatGvaGalleryItemBinding,
    buttonsClickListener: ChatAdapter.OnGvaButtonsClickListener,
    private val uiTheme: UiTheme
) : ViewHolder(binding.root) {

    private var adapter: GvaButtonsAdapter by Delegates.notNull()

    private val operatorTheme: MessageBalloonTheme? by lazy {
        Dependencies.getGliaThemeManager().theme?.chatTheme?.operatorMessage
    }

    private val galleryCardTheme: GvaGalleryCardTheme? by lazy {
        Dependencies.getGliaThemeManager().theme?.chatTheme?.gva?.galleryCardTheme
    }

    init {
        ViewCompat.setAccessibilityDelegate(
            binding.root,
            object : AccessibilityDelegateCompat() {
                override fun performAccessibilityAction(host: View, action: Int, args: Bundle?): Boolean {
                    if (action == AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS) {
                        // Sends an accessibility event of accessibility focus type.
                        host.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED)
                    }
                    return super.performAccessibilityAction(host, action, args)
                }
            }
        )
        adapter = GvaButtonsAdapter(buttonsClickListener, uiTheme, galleryCardTheme?.button)
        binding.buttonsRecyclerView.adapter = adapter
        binding.item.apply {
            uiTheme.operatorMessageBackgroundColor?.let(::getColorStateListCompat)?.also {
                backgroundTintList = it
            }

            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO

            // Unified Ui
            applyLayerTheme(galleryCardTheme?.background ?: operatorTheme?.background)
        }
        binding.title.apply {
            uiTheme.operatorMessageTextColor?.let(::getColorCompat)?.also(::setTextColor)
            uiTheme.operatorMessageTextColor?.let(::getColorCompat)?.also(::setLinkTextColor)

            uiTheme.fontRes?.let(::getFontCompat)?.also(::setTypeface)

            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO

            // Unified Ui
            applyTextTheme(galleryCardTheme?.title ?: operatorTheme?.text)
        }
        binding.subtitle.apply {
            uiTheme.operatorMessageTextColor?.let(::getColorCompat)?.also(::setTextColor)
            uiTheme.operatorMessageTextColor?.let(::getColorCompat)?.also(::setLinkTextColor)

            uiTheme.fontRes?.let(::getFontCompat)?.also(::setTypeface)

            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO

            // Unified Ui
            applyTextTheme(galleryCardTheme?.subtitle ?: operatorTheme?.text)
        }
        galleryCardTheme?.image?.also(binding.image::applyLayerTheme)
    }

    fun bindForMeasure(card: GvaGalleryCard) {
        bindTexts(card)
        bindButtons(card)
    }

    fun bind(card: GvaGalleryCard, position: Int, size: Int) {
        bindTexts(card)
        bindButtons(card)
        bindImage(card)
        updateContendDescription(card, position, size)
    }

    private fun bindTexts(card: GvaGalleryCard) {
        binding.title.text = card.title.fromHtml()

        card.subtitle?.let {
            binding.subtitle.text = it.fromHtml()
            binding.subtitle.isVisible = true
        } ?: run {
            binding.subtitle.isVisible = false
        }
    }

    private fun bindButtons(card: GvaGalleryCard) {
        adapter.setOptions(card.options)
        binding.buttonsRecyclerView.isVisible = card.options.isEmpty().not()
    }

    private fun bindImage(card: GvaGalleryCard) {
        card.imageUrl?.let {
            binding.image.load(it)
            binding.image.isVisible = true
        } ?: run {
            binding.image.isVisible = false
        }
    }

    private fun updateContendDescription(card: GvaGalleryCard, position: Int, size: Int) {
        val cardContentDescription = listOf(card.title, card.subtitle)
            .filter { it?.isNotEmpty() ?: false }
            .joinToString(separator = ". ")

        itemView.contentDescription = itemView.resources.getString(
            R.string.gva_gallery_card_message_content_description,
            cardContentDescription,
            position + 1,
            size
        )
    }
}
