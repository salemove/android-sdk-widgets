package com.glia.widgets.chat.adapter.holder

import android.view.View
import com.glia.widgets.UiTheme
import com.glia.widgets.chat.model.GvaResponseText
import com.glia.widgets.databinding.ChatOperatorMessageLayoutBinding
import com.glia.widgets.databinding.ChatReceiveMessageContentBinding
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.fromHtml
import com.glia.widgets.helper.getColorCompat
import com.glia.widgets.helper.getColorStateListCompat
import com.glia.widgets.helper.getFontCompat
import com.glia.widgets.view.unifiedui.applyLayerTheme
import com.glia.widgets.view.unifiedui.applyTextTheme
import com.glia.widgets.view.unifiedui.theme.UnifiedTheme

internal class GvaResponseTextViewHolder(
    operatorMessageBinding: ChatOperatorMessageLayoutBinding,
    private val messageContentBinding: ChatReceiveMessageContentBinding,
    private val uiTheme: UiTheme,
    unifiedTheme: UnifiedTheme? = Dependencies.gliaThemeManager.theme
) : OperatorBaseViewHolder(operatorMessageBinding.root, operatorMessageBinding.chatHeadView, uiTheme, unifiedTheme) {

    init {
        setupMessageContentView()
    }

    private fun setupMessageContentView() {
        messageContentBinding.root.apply {
            uiTheme.operatorMessageBackgroundColor?.let(::getColorStateListCompat)?.also {
                backgroundTintList = it
            }
            uiTheme.operatorMessageTextColor?.let(::getColorCompat)?.also(::setTextColor)
            uiTheme.operatorMessageTextColor?.let(::getColorCompat)?.also(::setLinkTextColor)

            uiTheme.fontRes?.let(::getFontCompat)?.also(::setTypeface)

            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO

            // Unified Ui
            applyLayerTheme(operatorTheme?.background)
            applyTextTheme(operatorTheme?.text)
        }
    }

    fun bind(item: GvaResponseText) {
        updateOperatorStatusView(item)
        updateMessageContentView(item)
        updateItemContentDescription(item.operatorName, item.content)
    }

    private fun updateMessageContentView(item: GvaResponseText) {
        messageContentBinding.root.text = item.content.fromHtml()
    }
}
