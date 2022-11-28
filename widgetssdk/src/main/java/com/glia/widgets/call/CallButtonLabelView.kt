package com.glia.widgets.call

import android.content.Context
import android.util.AttributeSet
import com.glia.widgets.view.unifiedui.exstensions.applyTextTheme
import com.glia.widgets.view.unifiedui.theme.call.BarButtonStatesTheme

class CallButtonLabelView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    ThemedStateText(context, attrs) {

    private var barButtonStatesTheme: BarButtonStatesTheme? = null

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        applyNewState()
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

    private fun applyNewState() {
        when {
            !isEnabled -> applyDisabledTheme()
            isActivated -> applyActivatedTheme()
            else -> applyEnabledTheme()
        }
    }

    private fun applyEnabledTheme() {
        barButtonStatesTheme?.enabled?.title?.also(::applyTextTheme) ?: restoreDefaultTheme()
    }

    private fun applyDisabledTheme() {
        barButtonStatesTheme?.disabled?.title?.also(::applyTextTheme) ?: restoreDefaultTheme()
    }

    private fun applyActivatedTheme() {
        barButtonStatesTheme?.activated?.title?.also(::applyTextTheme) ?: restoreDefaultTheme()
    }

}