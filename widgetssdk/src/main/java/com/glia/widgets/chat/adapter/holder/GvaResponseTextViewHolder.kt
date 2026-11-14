package com.glia.widgets.chat.adapter.holder

import android.view.View
import com.glia.widgets.chat.model.GvaResponseText
import com.glia.widgets.databinding.ChatOperatorMessageLayoutBinding
import com.glia.widgets.databinding.ChatReceiveMessageContentBinding
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.fromHtml
import com.glia.widgets.helper.gliaAttrFont
import com.glia.widgets.view.unifiedui.applyLayerTheme
import com.glia.widgets.view.unifiedui.applyTextTheme
import com.glia.widgets.view.unifiedui.theme.UnifiedTheme

internal class GvaResponseTextViewHolder(
    operatorMessageBinding: ChatOperatorMessageLayoutBinding,
    private val messageContentBinding: ChatReceiveMessageContentBinding,
    unifiedTheme: UnifiedTheme? = Dependencies.gliaThemeManager.theme
) : OperatorBaseViewHolder(operatorMessageBinding.root, operatorMessageBinding.chatHeadView, unifiedTheme) {

    init {
        setupMessageContentView()
    }

    /** The bubble's colours come from the layout's `?attr/gliaOperatorMessage*`. */
    private fun setupMessageContentView() {
        messageContentBinding.root.apply {
            context.gliaAttrFont()?.also(::setTypeface)

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
