package com.glia.widgets.view.head

import android.content.Context
import android.graphics.Typeface
import android.text.InputType
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import com.glia.widgets.R
import com.glia.widgets.helper.getAttr
import com.glia.widgets.helper.getColorCompat
import com.glia.widgets.view.unifiedui.applyLayerTheme
import com.glia.widgets.view.unifiedui.applyTextColorTheme
import com.glia.widgets.view.unifiedui.theme.base.BadgeTheme
import com.google.android.material.color.MaterialColors
import com.google.android.material.textview.MaterialTextView
import kotlin.math.max
import com.google.android.material.R as Material_R

internal class BadgeTextView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    MaterialTextView(context, attrs) {

    init {
        val minSize = resources.getDimensionPixelSize(R.dimen.glia_chat_head_badge_size)

        inputType = InputType.TYPE_CLASS_NUMBER
        gravity = Gravity.CENTER
        minWidth = minSize
        minHeight = minSize

        setBackgroundResource(R.drawable.bg_badge)

        importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO

        setTextAppearance(
            getAttr(
                Material_R.attr.textAppearanceCaption,
                R.style.Application_Glia_Caption
            )
        )

        setTextColor(
            MaterialColors.getColor(
                context,
                R.attr.gliaBaseLightColor,
                getColorCompat(R.color.glia_light_color)
            )
        )
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val size = max(measuredHeight, measuredWidth) +
            resources.getDimensionPixelSize(R.dimen.glia_x_small)
        setMeasuredDimension(size, size)
    }

    internal fun applyBadgeTheme(theme: BadgeTheme?) {
        theme?.apply {
            background?.also(::applyLayerTheme)
            textColor.also(::applyTextColorTheme)
            textStyle?.also { typeface = Typeface.create(typeface, it) }
            textSize?.also { setTextSize(TypedValue.COMPLEX_UNIT_SP, it) }
        }
    }
}
