package com.glia.widgets.messagecenter

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.widget.Button
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.core.view.isVisible
import androidx.transition.TransitionManager
import com.glia.widgets.R
import com.glia.widgets.databinding.ProgressButtonBinding
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.ResourceProvider
import com.glia.widgets.helper.applyShadow
import com.glia.widgets.helper.getAttr
import com.glia.widgets.helper.getColorCompat
import com.glia.widgets.helper.getDimenRes
import com.glia.widgets.helper.getDrawableCompat
import com.glia.widgets.helper.layoutInflater
import com.glia.widgets.helper.setLocaleContentDescription
import com.glia.widgets.locale.LocaleProvider
import com.glia.widgets.view.unifiedui.applyIndicatorColorTheme
import com.glia.widgets.view.unifiedui.applyLayerTheme
import com.glia.widgets.view.unifiedui.applyTextTheme
import com.glia.widgets.view.unifiedui.theme.base.ButtonTheme
import com.glia.widgets.view.unifiedui.theme.base.ColorTheme
import com.glia.widgets.view.unifiedui.theme.base.LayerTheme
import com.glia.widgets.view.unifiedui.theme.base.TextTheme
import com.google.android.material.transition.MaterialFade

internal class ProgressButton @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    FrameLayout(context, attrs) {

    private val adapterCallback: StatefulWidgetAdapterCallback<State, ButtonTheme> =
        object : StatefulWidgetAdapterCallback<State, ButtonTheme> {
            override fun onNewTheme(theme: ButtonTheme) {
                updateTheme(theme)
            }

            override fun onNewState(newState: State) {
                showIndicator(newState == State.PROGRESS)
            }
        }

    private val statefulWidgetAdapter: StatefulWidgetAdapter<State, ButtonTheme> by lazy {
        SimpleStatefulWidgetAdapter(State.ENABLED, adapterCallback)
    }

    // Widgets + Binding
    private val binding: ProgressButtonBinding by lazy {
        ProgressButtonBinding.inflate(layoutInflater, this)
    }

    private val title get() = binding.titleBtn

    private val progressBar get() = binding.progressBar

    // End Widgets + Binding
    private val localeProvider get() = Dependencies.localeProvider

    @get:ColorInt
    private val gliaBrandPrimaryColor: Int by lazy { // btn bg
        getColorCompat(getAttr(R.attr.gliaBrandPrimaryColor, R.color.glia_brand_primary_color))
    }

    @get:ColorInt
    private val gliaBaseLightColor: Int by lazy { // text
        getColorCompat(getAttr(R.attr.gliaBaseLightColor, R.color.glia_base_light_color))
    }

    @get:ColorInt
    private val gliaDisabledBackgroundColor: Int by lazy { getColorCompat(R.color.glia_disable_button_bg) }

    @get:ColorInt
    private val gliaDisabledBorderColor: Int by lazy { getColorCompat(R.color.glia_disable_button_border) }

    @get:ColorInt
    private val gliaDisabledTextColor: Int by lazy { getColorCompat(R.color.glia_disable_button_text) }

    enum class State {
        ENABLED,
        DISABLED,
        PROGRESS
    }

    init {
        isFocusable = true
        isClickable = true
        accessibilityLiveRegion = ACCESSIBILITY_LIVE_REGION_POLITE
        contentDescription = localeProvider.getString(R.string.general_send)
        foreground = getDrawableCompat(getAttr(android.R.attr.selectableItemBackground, android.R.color.transparent))
        applyDefaultTheme()
    }

    override fun getAccessibilityClassName(): CharSequence {
        return Button::class.java.name
    }

    private fun updateTheme(buttonTheme: ButtonTheme) {
        applyLayerTheme(buttonTheme.background)
        buttonTheme.elevation?.also { elevation = it }
        buttonTheme.shadowColor?.also(::applyShadow)
        title.applyTextTheme(buttonTheme.text)
        title.text = localeProvider.getString(R.string.general_send)
    }

    internal fun updateProgressTheme(colorTheme: ColorTheme?) {
        progressBar.applyIndicatorColorTheme(colorTheme)
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)

        statefulWidgetAdapter.updateState(if (enabled) State.ENABLED else State.DISABLED)
    }

    fun setProgress(progress: Boolean) {
        val state = when {
            progress -> State.PROGRESS
            isEnabled -> State.ENABLED
            else -> State.DISABLED
        }
        statefulWidgetAdapter.updateState(state)
    }

    internal fun updateStatefulTheme(theme: Map<State, ButtonTheme?>) {
        statefulWidgetAdapter.updateStatefulTheme(theme)
    }

    private fun showIndicator(show: Boolean) {
        TransitionManager.beginDelayedTransition(this, MaterialFade())
        progressBar.isVisible = show
        progressBar.setLocaleContentDescription(R.string.general_sending)
    }

    private fun applyDefaultTheme() {
        val defaultLayer = LayerTheme(
            fill = ColorTheme(gliaBrandPrimaryColor),
            stroke = Color.TRANSPARENT,
            borderWidth = 0f,
            cornerRadius = context.getDimenRes(R.dimen.glia_small)
        )

        val defaultText = TextTheme(
            textColor = ColorTheme(gliaBaseLightColor),
            backgroundColor = ColorTheme(),
            textSize = title.textSize / resources.displayMetrics.scaledDensity,
            textStyle = title.typeface.style
        )

        val defaultTheme = ButtonTheme(
            text = defaultText,
            background = defaultLayer,
            elevation = context.getDimenRes(R.dimen.glia_px)
        )

        val disabledTheme = ButtonTheme(
            text = defaultText.copy(textColor = ColorTheme(gliaDisabledTextColor)),
            background = defaultLayer.copy(
                fill = ColorTheme(gliaDisabledBackgroundColor),
                stroke = gliaDisabledBorderColor,
                borderWidth = context.getDimenRes(R.dimen.glia_px),
            ),
            elevation = 0f
        )

        updateStatefulTheme(
            mapOf(
                State.ENABLED to defaultTheme,
                State.DISABLED to disabledTheme,
                State.PROGRESS to disabledTheme
            )
        )
    }
}
