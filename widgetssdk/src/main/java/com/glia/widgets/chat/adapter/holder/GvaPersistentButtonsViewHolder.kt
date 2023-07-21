package com.glia.widgets.chat.adapter.holder

import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.LinearLayoutCompat
import com.glia.widgets.R
import com.glia.widgets.UiTheme
import com.glia.widgets.chat.adapter.ChatAdapter
import com.glia.widgets.chat.model.GvaPersistentButtons
import com.glia.widgets.databinding.ChatGvaPersistentButtonsContentBinding
import com.glia.widgets.databinding.ChatOperatorMessageLayoutBinding
import com.glia.widgets.helper.Utils
import com.glia.widgets.helper.fromHtml
import com.glia.widgets.helper.getColorCompat
import com.glia.widgets.helper.getColorStateListCompat
import com.glia.widgets.helper.getFontCompat
import com.glia.widgets.view.unifiedui.applyLayerTheme
import com.glia.widgets.view.unifiedui.applyTextTheme
import com.google.android.material.button.MaterialButton

internal class GvaPersistentButtonsViewHolder(
    operatorMessageBinding: ChatOperatorMessageLayoutBinding,
    private val contentBinding: ChatGvaPersistentButtonsContentBinding,
    private val uiTheme: UiTheme
) : OperatorBaseViewHolder(operatorMessageBinding, uiTheme) {

    init {
        contentBinding.root.apply {
            uiTheme.operatorMessageBackgroundColor?.let(::getColorStateListCompat)?.also {
                backgroundTintList = it
            }

            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO

            // Unified Ui
            applyLayerTheme(operatorTheme?.background)
        }
        contentBinding.message.apply {
            uiTheme.operatorMessageTextColor?.let(::getColorCompat)?.also(::setTextColor)
            uiTheme.operatorMessageTextColor?.let(::getColorCompat)?.also(::setLinkTextColor)

            uiTheme.fontRes?.let(::getFontCompat)?.also(::setTypeface)

            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO

            // Unified Ui
            applyTextTheme(operatorTheme?.text)
        }
    }

    fun bind(item: GvaPersistentButtons, onGvaButtonsClickListener: ChatAdapter.OnGvaButtonsClickListener) {
        updateOperatorStatusView(item)
        updateItemContentDescription(item.operatorName, item.content)

        contentBinding.message.text = item.content.fromHtml()
        setupButtons(item, contentBinding.container, onGvaButtonsClickListener)
    }

    private fun setupButtons(
        item: GvaPersistentButtons,
        container: ViewGroup,
        onGvaButtonsClickListener: ChatAdapter.OnGvaButtonsClickListener
    ) {
        container.removeAllViews()
        val horizontalMargin = container.resources.getDimensionPixelOffset(R.dimen.glia_large)
        val verticalMargin = container.resources.getDimensionPixelOffset(R.dimen.glia_medium)
        for (option in item.options) {
            val onClickListener = onGvaButtonsClickListener.run {
                View.OnClickListener { onGvaButtonClicked(option) }
            }

            val button = composeButton(
                container = container,
                text = option.text,
                onClickListener = onClickListener
            )

            val params = LinearLayoutCompat.LayoutParams(LinearLayoutCompat.LayoutParams.MATCH_PARENT, LinearLayoutCompat.LayoutParams.WRAP_CONTENT)
            val topMargin = if (item.options.first() == option) verticalMargin else 0
            val bottomMargin = if (item.options.last() == option) verticalMargin else 0
            params.setMargins(horizontalMargin, topMargin, horizontalMargin, bottomMargin)
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

            // TODO: should be changed to persistent theme later - MOB 2373
            //gvaButtonTheme?.also(it::applyButtonTheme)
        }
    }
}
