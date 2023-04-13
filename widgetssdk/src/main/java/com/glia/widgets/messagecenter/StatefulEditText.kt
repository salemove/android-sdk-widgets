package com.glia.widgets.messagecenter

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.ColorInt
import androidx.appcompat.widget.AppCompatEditText
import com.glia.widgets.R
import com.glia.widgets.view.unifiedui.extensions.*
import com.glia.widgets.view.unifiedui.theme.base.ColorTheme
import com.glia.widgets.view.unifiedui.theme.base.LayerTheme
import com.glia.widgets.view.unifiedui.theme.base.TextInputTheme
import com.glia.widgets.view.unifiedui.theme.base.TextTheme

class StatefulEditText @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    AppCompatEditText(context, attrs) {
    private val adapterCallback: StatefulWidgetAdapterCallback<State, TextInputTheme> =
        StatefulWidgetAdapterCallback { updateTheme(it) }

    private val statefulWidgetAdapter: StatefulWidgetAdapter<State, TextInputTheme> by lazy {
        SimpleStatefulWidgetAdapter(State.ENABLED, adapterCallback)
    }

    @get:ColorInt
    private val gliaBaseShadeColor: Int by lazy {
        getColorCompat(getAttr(R.attr.gliaBaseShadeColor, R.color.glia_base_shade_color))
    }

    @get:ColorInt
    private val gliaBaseDarkColor: Int by lazy {
        getColorCompat(getAttr(R.attr.gliaBaseDarkColor, R.color.glia_base_dark_color))
    }

    @get:ColorInt
    private val gliaBrandPrimaryColor: Int by lazy {
        getColorCompat(getAttr(R.attr.gliaBrandPrimaryColor, R.color.glia_brand_primary_color))
    }

    @get:ColorInt
    private val gliaDisabledColor: Int by lazy { getColorCompat(R.color.glia_disable_button_bg) }

    @get:ColorInt
    private val gliaNegativeColor: Int by lazy {
        getColorCompat(getAttr(R.attr.gliaSystemNegativeColor, R.color.glia_system_negative_color))
    }

    enum class State {
        ENABLED,
        FOCUSED,
        DISABLED,
        ERROR
    }

    init {
        applyDefaultTheme()
        observeFocusChanges()
    }

    private fun applyDefaultTheme() {
        val defaultLayer = LayerTheme(
            fill = ColorTheme(),
            stroke = gliaBaseShadeColor,
            borderWidth = context.getDimenRes(R.dimen.glia_px),
            cornerRadius = context.getDimenRes(R.dimen.glia_small)
        )
        val defaultText = TextTheme(
            textColor = ColorTheme(gliaBaseDarkColor),
            backgroundColor = ColorTheme(),
            textSize = textSize / resources.displayMetrics.scaledDensity,
            textStyle = typeface.style,
            textAlignment = textAlignment
        )

        val defaultTheme = TextInputTheme(textTheme = defaultText, backgroundTheme = defaultLayer)

        updateStatefulTheme(
            mapOf(
                State.ENABLED to defaultTheme,
                State.FOCUSED to defaultTheme.copy(
                    backgroundTheme = defaultLayer.copy(stroke = gliaBrandPrimaryColor)
                ),
                State.DISABLED to defaultTheme.copy(
                    backgroundTheme = defaultLayer.copy(fill = ColorTheme(gliaDisabledColor))
                ),
                State.ERROR to defaultTheme.copy(
                    backgroundTheme = defaultLayer.copy(stroke = gliaNegativeColor)
                )
            )
        )
    }

    private fun updateTheme(theme: TextInputTheme) {
        applyTextTheme(theme.textTheme)
        applyLayerTheme(theme.backgroundTheme)
    }

    internal fun updateHintTheme(theme: TextTheme?) {
        theme?.textColor?.primaryColor?.also { setHintTextColor(it) }
    }

    internal fun updateStatefulTheme(newStatefulTheme: Map<State, TextInputTheme?>) {
        statefulWidgetAdapter.updateStatefulTheme(newStatefulTheme)
    }

    private fun observeFocusChanges() {
        setOnFocusChangeListener { _, hasFocus ->
            if (isEnabled) onFocusChanged(hasFocus)
        }
    }

    private fun onFocusChanged(hasFocus: Boolean) {
        statefulWidgetAdapter.updateState(if (hasFocus) State.FOCUSED else State.ENABLED)
    }

    override fun setEnabled(enabled: Boolean) {
        if (hasFocus() && enabled) return

        super.setEnabled(enabled)

        statefulWidgetAdapter.updateState(if (enabled) State.ENABLED else State.DISABLED)
    }

    fun setError(error: Boolean) {
        if (!isEnabled) return

        val state = when {
            error -> State.ERROR
            hasFocus() -> State.FOCUSED
            else -> State.ENABLED
        }

        statefulWidgetAdapter.updateState(state)
    }

}