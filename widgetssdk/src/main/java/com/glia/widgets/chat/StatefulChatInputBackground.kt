package com.glia.widgets.chat

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorInt
import com.glia.widgets.R
import com.glia.widgets.helper.getAttr
import com.glia.widgets.helper.getColorCompat
import com.glia.widgets.messagecenter.SimpleStatefulWidgetAdapter
import com.glia.widgets.messagecenter.StatefulWidgetAdapter
import com.glia.widgets.messagecenter.StatefulWidgetAdapterCallback
import com.glia.widgets.view.unifiedui.applyLayerTheme
import com.glia.widgets.view.unifiedui.theme.base.ColorTheme
import com.glia.widgets.view.unifiedui.theme.base.LayerTheme

/**
 * StatefulChatInputBackground is a custom view that provides a background for chat input with stateful theming.
 * It uses UnifiedUi for both default enabled and disabled states, so applying a new UnifiedUi theme will work smoothly,
 * even in case when only one state is provided.
 */
internal class StatefulChatInputBackground @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    private val adapterCallback: StatefulWidgetAdapterCallback<State, LayerTheme> =
        StatefulWidgetAdapterCallback { applyLayerTheme(it) }

    private val statefulWidgetAdapter: StatefulWidgetAdapter<State, LayerTheme> by lazy {
        SimpleStatefulWidgetAdapter(State.ENABLED, adapterCallback)
    }

    @ColorInt
    private val gliaSystemAgentBubbleColor: Int = getColorCompat(getAttr(R.attr.gliaSystemAgentBubbleColor, R.color.glia_neutral_color))

    @get:ColorInt
    private val disabledBackgroundColor: Int
        get() = gliaSystemAgentBubbleColor

    init {
        applyDefaultTheme()
    }

    private fun applyDefaultTheme() {
        val defaultTheme = LayerTheme(fill = ColorTheme(), stroke = Color.TRANSPARENT, borderWidth = 0f, cornerRadius = 0f)
        applyThemes(
            defaultTheme = defaultTheme,
            disabledTheme = defaultTheme.copy(fill = ColorTheme(disabledBackgroundColor))
        )
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        statefulWidgetAdapter.updateState(if (enabled) State.ENABLED else State.DISABLED)
    }

    fun applyThemes(defaultTheme: LayerTheme?, disabledTheme: LayerTheme?) {
        statefulWidgetAdapter.updateStatefulTheme(
            mapOf(
                State.ENABLED to defaultTheme,
                State.DISABLED to disabledTheme
            )
        )
    }

    enum class State {
        ENABLED,
        DISABLED
    }
}
