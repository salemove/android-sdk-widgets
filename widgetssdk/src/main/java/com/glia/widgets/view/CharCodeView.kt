package com.glia.widgets.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.glia.widgets.R
import com.glia.widgets.UiTheme
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.applyTextTheme
import com.glia.widgets.helper.getColorCompat
import com.glia.widgets.helper.getDimenRes
import com.glia.widgets.helper.getDimenResPx
import com.glia.widgets.helper.getFontCompat
import com.glia.widgets.view.unifiedui.applyLayerTheme
import com.glia.widgets.view.unifiedui.applyTextTheme
import com.glia.widgets.view.unifiedui.theme.base.ColorTheme
import com.glia.widgets.view.unifiedui.theme.base.LayerTheme
import com.glia.widgets.view.unifiedui.theme.callvisulaizer.VisitorCodeTheme
import com.google.android.material.theme.overlay.MaterialThemeOverlay

private data class ViewDefaultProperties(
    val borderWidth: Float,
    val borderRadius: Float,
    val borderColor: Int,
    val backgroundColor: Int,
    val horizonPadding: Int,
    val verticalPadding: Int,
    val horizonMargin: Int,
    val verticalMargin: Int
)

class CharCodeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(MaterialThemeOverlay.wrap(context, attrs, defStyleAttr, R.style.Application_Glia_Chat), attrs, defStyleAttr) {


    private val charViewProps: ViewDefaultProperties
    private var runtimeTheme: UiTheme? = null
    private val remoteTheme: VisitorCodeTheme? by lazy {
        Dependencies.getGliaThemeManager().theme?.callVisualizerTheme?.visitorCodeTheme
    }

    init {
        charViewProps = ViewDefaultProperties(
            borderWidth = context.getDimenRes(R.dimen.glia_px),
            borderRadius = context.getDimenRes(R.dimen.glia_medium),
            borderColor = getColorCompat(R.color.glia_stroke_light),
            backgroundColor = getColorCompat(android.R.color.transparent),
            horizonPadding = context.getDimenResPx(R.dimen.glia_large),
            verticalPadding = context.getDimenResPx(R.dimen.glia_small),
            horizonMargin = context.getDimenResPx(R.dimen.glia_small),
            verticalMargin = 0,
        )

        if (isInEditMode) {
            // Dummy code for preview in Android Studio
            alpha = 1f
            setText("53009")
        } else {
            // Placeholder so that view correctly allocates space on the screen before text is set
            setText("     ")
            alpha = 0f
        }
    }

    fun applyRuntimeTheme(theme: UiTheme) {
        runtimeTheme = theme
    }

    fun setText(text: String) {
        removeAllViews()
        for (character in text) {
            addView(createCharSlotView(character, runtimeTheme, remoteTheme))
        }
    }

    private fun createCharSlotView(character: Char, runtimeTheme: UiTheme?, remoteTheme: VisitorCodeTheme?): TextView {

        val charView = TextView(context, null, R.attr.visitorCodeStyle, R.style.Application_Glia_VisitorCode)
        charView.isFocusable = false
        charView.text = character.toString()
        charView.textAlignment = TEXT_ALIGNMENT_CENTER
        charView.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES

        applyRuntimeTheme(runtimeTheme, charView)
        applyRemoteTheme(remoteTheme, charView)

        val charViewLayout = LayoutParams(
            WIDTH_NOT_SPECIFIED,
            LayoutParams.WRAP_CONTENT,
            USE_ALL_AVAILABLE_WIDTH
        )
        charViewLayout.setMargins(
            charViewProps.horizonMargin,
            charViewProps.verticalMargin,
            charViewProps.horizonMargin,
            charViewProps.verticalMargin
        )
        charView.layoutParams = charViewLayout

        charView.setPadding(0, charViewProps.verticalPadding, 0, charViewProps.verticalPadding
        )

        return charView
    }

    private fun applyRuntimeTheme(theme: UiTheme?, charView: TextView) {
        if (theme == null) {
            return
        }

        val fontFamily = theme.fontRes?.let { getFontCompat(it) }
        val backgroundColor = theme.visitorCodeBackgroundColor?.let { getColorCompat(it) }
            ?: charViewProps.backgroundColor
        val borderColor =
            theme.visitorCodeBorderColor?.let { getColorCompat(it) } ?: charViewProps.borderColor
        val textColor = theme.visitorCodeTextColor?.let { getColorCompat(it) }
            ?: theme.baseDarkColor?.let { getColorCompat(it) }

        val backgroundTheme = LayerTheme(
            fill = ColorTheme(backgroundColor),
            stroke = borderColor,
            borderWidth = charViewProps.borderWidth,
            cornerRadius = charViewProps.borderRadius
        )

        charView.applyTextTheme(
            textColor = textColor,
            textFont = fontFamily
        )

        charView.applyLayerTheme(backgroundTheme)
    }

    private fun applyRemoteTheme(theme: VisitorCodeTheme?, charView: TextView) {
        if (theme == null) {
            return
        }

        charView.applyLayerTheme(theme.numberSlotBackground)
        charView.applyTextTheme(theme.numberSlotText, withAlignment = false)
    }

    companion object {
        private const val WIDTH_NOT_SPECIFIED = 0
        private const val USE_ALL_AVAILABLE_WIDTH = 1f
    }

}
