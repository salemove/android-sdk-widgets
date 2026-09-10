package com.glia.widgets.chat.adapter.holder

import android.view.View
import com.glia.widgets.chat.adapter.ChatAdapter
import com.glia.widgets.chat.adapter.GvaButtonsAdapter
import com.glia.widgets.chat.model.GvaPersistentButtons
import com.glia.widgets.databinding.ChatGvaPersistentButtonsContentBinding
import com.glia.widgets.databinding.ChatOperatorMessageLayoutBinding
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.applyGliaThemeFont
import com.glia.widgets.helper.fromHtml
import com.glia.widgets.view.unifiedui.applyLayerTheme
import com.glia.widgets.view.unifiedui.applyTextTheme
import com.glia.widgets.view.unifiedui.theme.UnifiedTheme
import com.glia.widgets.view.unifiedui.theme.gva.GvaPersistentButtonTheme
import kotlin.properties.Delegates

internal class GvaPersistentButtonsViewHolder(
    operatorMessageBinding: ChatOperatorMessageLayoutBinding,
    private val contentBinding: ChatGvaPersistentButtonsContentBinding,
    buttonsClickListener: ChatAdapter.OnGvaButtonsClickListener,
    unifiedTheme: UnifiedTheme? = Dependencies.gliaThemeManager.theme
) : OperatorBaseViewHolder(operatorMessageBinding.root, operatorMessageBinding.chatHeadView, unifiedTheme) {

    private var adapter: GvaButtonsAdapter by Delegates.notNull()

    private val persistentButtonTheme: GvaPersistentButtonTheme? by lazy {
        unifiedTheme?.chatTheme?.gva?.persistentButtonTheme
    }

    // Both the bubble and its text take their colours from the layout's `?attr/gliaOperatorMessage*`.
    init {
        adapter = GvaButtonsAdapter(buttonsClickListener, persistentButtonTheme?.button)
        contentBinding.buttonsRecyclerView.adapter = adapter
        contentBinding.root.apply {
            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO

            // Unified Ui
            applyLayerTheme(persistentButtonTheme?.background ?: operatorTheme?.background)
        }
        contentBinding.message.apply {
            context.applyGliaThemeFont(this)

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
