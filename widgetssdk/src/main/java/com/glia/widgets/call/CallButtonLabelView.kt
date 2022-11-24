package com.glia.widgets.call

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.glia.widgets.helper.Utils
import com.glia.widgets.view.unifiedui.exstensions.applyTextTheme
import com.glia.widgets.view.unifiedui.theme.base.ColorTheme
import com.glia.widgets.view.unifiedui.theme.base.TextTheme
import com.glia.widgets.view.unifiedui.theme.call.BarButtonStatesTheme

class CallButtonLabelView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    AppCompatTextView(context, attrs) {

    private var defaultLabelTheme: TextTheme? = null
    private var barButtonStatesTheme: BarButtonStatesTheme? = null

    init {
        defaultLabelTheme = TextTheme(
            textColor = ColorTheme(values = listOf(currentTextColor)),
            backgroundColor = null,
            textSize = Utils.pxToSp(context, textSize),
            textStyle = typeface.style,
            textAlignment = textAlignment
        )
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        applyNewState()
    }

    override fun setTypeface(tf: Typeface?) {
        tf?.apply { defaultLabelTheme = defaultLabelTheme?.copy(textStyle = style) }
        super.setTypeface(tf)
    }

    override fun setEnabled(enabled: Boolean) {
        if (isEnabled == enabled) return
        super.setEnabled(enabled)

        applyNewState()
    }

    override fun setActivated(activated: Boolean) {
        if (isActivated == activated) return
        super.setActivated(activated)

        applyNewState()
    }

    internal fun setBarButtonStatesTheme(barButtonStatesTheme: BarButtonStatesTheme?) {
        this.barButtonStatesTheme = barButtonStatesTheme
    }

    private fun applyDefaultTheme() {
        applyTextTheme(defaultLabelTheme)
    }

    private fun applyNewState() {
        when {
            !isEnabled -> applyDisabledTheme()
            isActivated -> applyActivatedTheme()
            else -> applyEnabledTheme()
        }
    }

    private fun applyEnabledTheme() {
        barButtonStatesTheme?.enabled?.title?.also(::applyTextTheme) ?: applyDefaultTheme()
    }

    private fun applyDisabledTheme() {
        barButtonStatesTheme?.disabled?.title?.also(::applyTextTheme) ?: applyDefaultTheme()
    }

    private fun applyActivatedTheme() {
        barButtonStatesTheme?.activated?.title?.also(::applyTextTheme) ?: applyDefaultTheme()
    }

}