package com.glia.widgets.chat

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.ViewGroup
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.transition.TransitionManager
import com.glia.widgets.UiTheme
import com.glia.widgets.chat.model.GvaButton
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.applyShadow
import com.glia.widgets.helper.getColorCompat
import com.glia.widgets.helper.wrapWithMaterialThemeOverlay
import com.glia.widgets.view.unifiedui.applyTextTheme
import com.glia.widgets.view.unifiedui.theme.base.ButtonTheme
import com.glia.widgets.view.unifiedui.theme.base.LayerTheme
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.transition.MaterialFadeThrough

internal class GvaChip @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = com.google.android.material.R.attr.chipStyle
) : Chip(context.wrapWithMaterialThemeOverlay(attrs, defStyleAttr), attrs, defStyleAttr) {

    private val quickReplyTheme: ButtonTheme? by lazy {
        Dependencies.gliaThemeManager.theme?.chatTheme?.gva?.quickReplyTheme
    }

    init {
        applyQuickReplyTheme()
    }

    private fun applyQuickReplyTheme() {
        quickReplyTheme?.also { applyButtonTheme(it) }
    }

    private fun applyButtonTheme(buttonTheme: ButtonTheme) {
        applyTextTheme(buttonTheme.text, withAlignment = false)
        applyBackgroundTheme(buttonTheme.background)
        buttonTheme.elevation?.also { elevation = it }
        buttonTheme.shadowColor?.also(::applyShadow)
    }

    private fun applyBackgroundTheme(background: LayerTheme?) {
        background?.apply {
            fill?.also { chipBackgroundColor = it.primaryColorStateList }
            stroke?.also { chipStrokeColor = ColorStateList.valueOf(it) }
            borderWidth?.also { chipStrokeWidth = it }
            cornerRadius?.also { shapeAppearanceModel = shapeAppearanceModel.withCornerSize(it) }
        }
    }

    internal fun applyUiTheme(uiTheme: UiTheme?) {
        with(uiTheme ?: return) {
            gvaQuickReplyBackgroundColor?.let { setChipBackgroundColorResource(it) }
            gvaQuickReplyStrokeColor?.let { setChipStrokeColorResource(it) }
            gvaQuickReplyTextColor?.let { getColorCompat(it) }?.let { setTextColor(it) }
            applyQuickReplyTheme()
        }
    }
}

internal class GvaChipGroup @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = com.google.android.material.R.attr.chipGroupStyle
) : ChipGroup(context.wrapWithMaterialThemeOverlay(attrs, defStyleAttr), attrs, defStyleAttr) {

    internal var onItemClickedListener: OnItemClickedListener? = null
    private var theme: UiTheme? = null

    init {
        isSelectionRequired = false
        isSingleLine = false
        isSingleSelection = false

        if (isInEditMode) {
            // Add dummy content to show on Studio layout previews
            setButtons(
                listOf(
                    GvaButton(text = "GVA Option 1"),
                    GvaButton(text = "GVA Option 2"),
                    GvaButton(text = "GVA Option 3")
                )
            )
        }
    }

    internal fun updateTheme(theme: UiTheme?) {
        this.theme = theme

        children.forEach { (it as? GvaChip)?.applyUiTheme(theme) }
    }

    internal fun setButtons(buttons: List<GvaButton>) {
        val hasItems = buttons.isNotEmpty()

        if (hasItems) {
            removeAllViews()
            buttons.forEach { addButton(it, theme) }

            if (isVisible) return

            TransitionManager.beginDelayedTransition(parent as ViewGroup, MaterialFadeThrough())
        }

        isVisible = hasItems
    }

    private fun addButton(gvaButton: GvaButton, uiTheme: UiTheme?) {
        GvaChip(context).apply {
            applyUiTheme(uiTheme)
            text = gvaButton.text
            setOnClickListener {
                onItemClickedListener?.onItemClicked(gvaButton)
                this@GvaChipGroup.isVisible = false
            }

            addView(this)
        }
    }

    internal fun interface OnItemClickedListener {
        fun onItemClicked(gvaButton: GvaButton)
    }
}
