package com.glia.widgets.chat.adapter.holder

import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
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
import com.glia.widgets.helper.getColorCompat
import com.glia.widgets.helper.getColorStateListCompat
import com.glia.widgets.helper.getFontCompat
import com.glia.widgets.helper.load
import com.glia.widgets.view.unifiedui.applyButtonTheme
import com.glia.widgets.view.unifiedui.applyLayerTheme
import com.glia.widgets.view.unifiedui.applyTextTheme
import com.glia.widgets.view.unifiedui.theme.base.ButtonTheme
import com.glia.widgets.view.unifiedui.theme.chat.MessageBalloonTheme
import com.glia.widgets.view.unifiedui.theme.gva.GvaGalleryCardTheme
import com.google.android.material.button.MaterialButton
import kotlin.properties.Delegates

internal class GvaGalleryItemViewHolder(
    private val binding: ChatGvaGalleryItemBinding,
    buttonsClickListener: ChatAdapter.OnGvaButtonsClickListener,
    private val uiTheme: UiTheme
) : ViewHolder(binding.root) {

    private var adapter: ButtonsAdapter by Delegates.notNull()

    private val operatorTheme: MessageBalloonTheme? by lazy {
        Dependencies.getGliaThemeManager().theme?.chatTheme?.operatorMessage
    }

    private val galleryCardTheme: GvaGalleryCardTheme? by lazy {
        Dependencies.getGliaThemeManager().theme?.chatTheme?.gva?.galleryCardTheme
    }

    init {
        adapter = ButtonsAdapter(buttonsClickListener, uiTheme, galleryCardTheme?.button)
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

    fun bind(card: GvaGalleryCard) {
        binding.title.text = card.title.fromHtml()

        card.subtitle?.let {
            binding.subtitle.text = it.fromHtml()
            binding.subtitle.isVisible = true
        } ?: run {
            binding.subtitle.isVisible = false
        }

        adapter.setOptions(card.options)
        binding.buttonsRecyclerView.isVisible = card.options.isEmpty().not()
    }

    fun bindImage(card: GvaGalleryCard) {
        card.imageUrl?.let {
            binding.image.load(it)
            binding.image.isVisible = true
        } ?: run {
            binding.image.isVisible = false
        }
    }

    private class ButtonsAdapter(
        private val buttonsClickListener: ChatAdapter.OnGvaButtonsClickListener,
        private val uiTheme: UiTheme,
        private val buttonTheme: ButtonTheme?
    ) : RecyclerView.Adapter<ButtonsAdapter.ButtonViewHolder>() {

        private var options: List<GvaButton>? = null

        fun setOptions(options: List<GvaButton>) {
            this.options = options
            notifyDataSetChanged()
        }

        class ButtonViewHolder(
            private val buttonView: MaterialButton
        ) : ViewHolder(buttonView) {
            fun bind(button: GvaButton, buttonsClickListener: ChatAdapter.OnGvaButtonsClickListener) {
                buttonView.text = button.text
                buttonView.setOnClickListener {
                    buttonsClickListener.onGvaButtonClicked(button)
                }
            }
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ButtonViewHolder {
            val styleResId = Utils.getAttrResourceId(parent.context, R.attr.gvaOptionButtonStyle)
            val button = MaterialButton(ContextThemeWrapper(parent.context, styleResId), null, 0).also {
                it.id = View.generateViewId()

                uiTheme.fontRes?.let(parent::getFontCompat)?.also(it::setTypeface)

                buttonTheme?.also(it::applyButtonTheme)
            }
            button.layoutParams = LinearLayoutCompat.LayoutParams(LinearLayoutCompat.LayoutParams.MATCH_PARENT, LinearLayoutCompat.LayoutParams.WRAP_CONTENT)
            return ButtonViewHolder(button)
        }

        override fun getItemCount(): Int = options?.size ?: 0

        override fun onBindViewHolder(holder: ButtonViewHolder, position: Int) {
            options?.get(position)?.let { holder.bind(it, buttonsClickListener) }
        }

    }
}
