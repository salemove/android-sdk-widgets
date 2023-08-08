package com.glia.widgets.chat.adapter.holder

import android.view.View
import com.glia.widgets.UiTheme
import com.glia.widgets.chat.adapter.ChatAdapter
import com.glia.widgets.chat.adapter.GvaButtonsAdapter
import com.glia.widgets.chat.model.GvaPersistentButtons
import com.glia.widgets.databinding.ChatGvaPersistentButtonsContentBinding
import com.glia.widgets.databinding.ChatOperatorMessageLayoutBinding
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.fromHtml
import com.glia.widgets.helper.getColorCompat
import com.glia.widgets.helper.getColorStateListCompat
import com.glia.widgets.helper.getFontCompat
import com.glia.widgets.view.unifiedui.applyLayerTheme
import com.glia.widgets.view.unifiedui.applyTextTheme
import com.glia.widgets.view.unifiedui.theme.gva.GvaPersistentButtonTheme
import kotlin.properties.Delegates

internal class GvaPersistentButtonsViewHolder(
    operatorMessageBinding: ChatOperatorMessageLayoutBinding,
    private val contentBinding: ChatGvaPersistentButtonsContentBinding,
    buttonsClickListener: ChatAdapter.OnGvaButtonsClickListener,
    private val uiTheme: UiTheme
) : OperatorBaseViewHolder(operatorMessageBinding.root, operatorMessageBinding.chatHeadView, uiTheme) {

    private var adapter: GvaButtonsAdapter by Delegates.notNull()

    private val persistentButtonTheme: GvaPersistentButtonTheme? by lazy {
        Dependencies.getGliaThemeManager().theme?.chatTheme?.gva?.persistentButtonTheme
    }

    init {
        adapter = GvaButtonsAdapter(buttonsClickListener, uiTheme, persistentButtonTheme?.button)
        contentBinding.buttonsRecyclerView.adapter = adapter
        contentBinding.root.apply {
            uiTheme.operatorMessageBackgroundColor?.let(::getColorStateListCompat)?.also {
                backgroundTintList = it
            }

            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO

            // Unified Ui
            applyLayerTheme(persistentButtonTheme?.background ?: operatorTheme?.background)
        }
        contentBinding.message.apply {
            uiTheme.operatorMessageTextColor?.let(::getColorCompat)?.also(::setTextColor)
            uiTheme.operatorMessageTextColor?.let(::getColorCompat)?.also(::setLinkTextColor)

            uiTheme.fontRes?.let(::getFontCompat)?.also(::setTypeface)

            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO

            // Unified Ui
            applyTextTheme(persistentButtonTheme?.title ?: operatorTheme?.text)
        }
    }

    fun bind(item: GvaPersistentButtons) {
        updateOperatorStatusView(item)
        updateItemContentDescription(item.operatorName, item.content)

        contentBinding.message.text = item.content.fromHtml()

        adapter.setOptions(item.options)
    }
}
