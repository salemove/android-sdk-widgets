package com.glia.widgets.view

import android.content.Context
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.glia.widgets.R
import com.glia.widgets.UiTheme
import com.glia.widgets.di.Dependencies
import com.glia.widgets.view.unifiedui.extensions.*
import com.glia.widgets.view.unifiedui.extensions.applyLayerTheme
import com.glia.widgets.view.unifiedui.extensions.getColorCompat
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

    public fun applyRuntimeTheme(theme: UiTheme) {
        runtimeTheme = theme
    }

    fun setText(text: String) {
        removeAllViews()
        for (character in text) {
            addView(createCharSlotView(character, runtimeTheme, remoteTheme))
        }
    }

    private fun createCharSlotView(character: Char, runtimeTheme: UiTheme?, remoteTheme: VisitorCodeTheme?): TextView {

        val charView = TextView(context, null, R.attr.visitorCodeStyle, R.style.Application_Glia_VisitorCode) // TODO: confirm this
        charView.isFocusable = false
        charView.text = character.toString()
        charView.gravity = TEXT_ALIGNMENT_CENTER
        charView.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES

        runtimeTheme?.let {
            applyRuntimeTheme(it, charView)
        }

        remoteTheme?.let {
            applyRemoteTheme(it, charView)
        }

        val charViewLayout = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        charViewLayout.setMargins(
            charViewProps.horizonMargin,
            charViewProps.verticalMargin,
            charViewProps.horizonMargin,
            charViewProps.verticalMargin
        )
        charView.layoutParams = charViewLayout

        // TODO: work a bit more on sizes and margins in layout XMLs
        return charView
    }

    private fun applyRuntimeTheme(theme: UiTheme, charView: TextView) {

        val backgroundColor = theme.visitorCodeBackgroundColor?.let { getColorCompat(it) } ?: charViewProps.backgroundColor
        val borderColor = theme.visitorCodeBorderColor?.let { getColorCompat(it) } ?: charViewProps.borderColor
        charView.background = createCharBackground(backgroundColor, borderColor, charViewProps.borderWidth, charViewProps.borderRadius)

        theme.baseDarkColor?.let {
            charView.setTextColor(getColorCompat(it))
        }
        theme.visitorCodeTextColor?.let {
            charView.setTextColor(getColorCompat(it))
        }
    }

    private fun applyRemoteTheme(theme: VisitorCodeTheme, charView: TextView) {
        val padding = Rect(
            charViewProps.horizonPadding,
            charViewProps.verticalPadding,
            charViewProps.horizonPadding,
            charViewProps.verticalPadding
        )
        charView.applyLayerTheme(theme.numberSlotBackground, padding)
        charView.applyTextTheme(theme.numberSlotText,
            withBackground = false, // background is applied from background directly
            withAlignment = false // no alignment value in JSON
        )
    }

    // More or less creates same shape as in bg_char_slot.xml but programmatically for runtime and remote themes
    private fun createCharBackground(backgroundColor: Int, borderColor: Int, borderWidth: Float, cornerRadius: Float): Drawable {
        return LayerDrawable(arrayOf(
            createCharBackgroundShape(backgroundColor, cornerRadius),
            createCharBorderShape(borderColor, borderWidth, cornerRadius)
        ))
    }

    private fun createCharBackgroundShape(backgroundColor: Int, cornerRadius: Float): Drawable {
        return ShapeDrawable().apply {
            shape = RoundRectShape(createBorderRadii(cornerRadius), null, null)
            paint.color = backgroundColor
            paint.style = Paint.Style.FILL_AND_STROKE
        }
    }

    private fun createCharBorderShape(borderColor: Int, borderWidth: Float, cornerRadius: Float): Drawable {
        return ShapeDrawable().apply {
            shape = RoundRectShape(createBorderRadii(cornerRadius), null, null)
            paint.color = borderColor
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = borderWidth
            setPadding(
                charViewProps.horizonPadding,
                charViewProps.verticalPadding,
                charViewProps.horizonPadding,
                charViewProps.verticalPadding
            )
        }
    }

    private fun createBorderRadii(borderRadius: Float): FloatArray {
        return floatArrayOf(borderRadius, borderRadius, borderRadius, borderRadius, borderRadius, borderRadius, borderRadius, borderRadius)
    }
}
