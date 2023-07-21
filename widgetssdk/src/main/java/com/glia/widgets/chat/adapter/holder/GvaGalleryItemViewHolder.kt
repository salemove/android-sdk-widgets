package com.glia.widgets.chat.adapter.holder

import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.glia.widgets.R
import com.glia.widgets.UiTheme
import com.glia.widgets.chat.adapter.ChatAdapter
import com.glia.widgets.chat.model.GvaButton
import com.glia.widgets.chat.model.GvaGalleryCard
import com.glia.widgets.databinding.ChatGvaGalleryItemBinding
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.Utils
import com.glia.widgets.helper.fromHtml
import com.glia.widgets.helper.getColorStateListCompat
import com.glia.widgets.helper.getFontCompat
import com.glia.widgets.helper.load
import com.glia.widgets.view.unifiedui.applyButtonTheme
import com.glia.widgets.view.unifiedui.applyLayerTheme
import com.glia.widgets.view.unifiedui.theme.chat.MessageBalloonTheme
import com.glia.widgets.view.unifiedui.theme.gva.GvaPersistentButtonTheme
import com.google.android.material.button.MaterialButton

internal class GvaGalleryItemViewHolder(
    private val binding: ChatGvaGalleryItemBinding,
    private val buttonsClickListener: ChatAdapter.OnGvaButtonsClickListener,
    private val uiTheme: UiTheme
) : ViewHolder(binding.root) {

    private val operatorTheme: MessageBalloonTheme? by lazy {
        Dependencies.getGliaThemeManager().theme?.chatTheme?.operatorMessage
    }

    private val persistentButtonTheme: GvaPersistentButtonTheme? by lazy {
        Dependencies.getGliaThemeManager().theme?.chatTheme?.gva?.persistentButtonTheme
    }

    init {
        binding.root.apply {
            uiTheme.operatorMessageBackgroundColor?.let(::getColorStateListCompat)?.also {
                backgroundTintList = it
            }

            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO

            // Unified Ui
            applyLayerTheme(operatorTheme?.background)
        }
    }

    fun bind(card: GvaGalleryCard) {
        binding.title.text = card.title.fromHtml()

        card.subtitle?.let {
            binding.subtitle.text = it.fromHtml()
            binding.subtitle.isVisible = true
        } ?: run {
            binding.subtitle.isVisible = false
        }

        card.imageUrl?.let {
            binding.image.load(it)
            binding.image.isVisible = true
        } ?: run {
            binding.image.isVisible = false
        }

        if (card.options.isEmpty().not()) {
            setupButtons(card.options, binding.container)
            binding.container.isVisible = true
        } else {
            binding.container.isVisible = false
        }
    }

    private fun setupButtons(
        options: List<GvaButton>,
        container: ViewGroup
    ) {
        container.removeAllViews()
        for (option in options) {
            val onClickListener = buttonsClickListener.run {
                View.OnClickListener { onGvaButtonClicked(option) }
            }

            val button = composeButton(
                container = container,
                text = option.text,
                onClickListener = onClickListener
            )

            val params = LinearLayoutCompat.LayoutParams(LinearLayoutCompat.LayoutParams.MATCH_PARENT, LinearLayoutCompat.LayoutParams.WRAP_CONTENT)
            container.addView(button, params)
        }
    }

    private fun composeButton(
        container: ViewGroup,
        text: String,
        onClickListener: View.OnClickListener
    ): MaterialButton {
        val styleResId = Utils.getAttrResourceId(container.context, R.attr.gvaOptionButtonStyle)
        return MaterialButton(ContextThemeWrapper(container.context, styleResId), null, 0).also {
            it.id = View.generateViewId()
            it.text = text
            it.setOnClickListener(onClickListener)

            uiTheme.fontRes?.let(container::getFontCompat)?.also(it::setTypeface)

            persistentButtonTheme?.button?.also(it::applyButtonTheme)
        }
    }
}
