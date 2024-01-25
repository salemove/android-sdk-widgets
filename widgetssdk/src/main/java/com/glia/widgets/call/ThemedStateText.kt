package com.glia.widgets.call

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.glia.widgets.helper.pxToSp
import com.glia.widgets.view.unifiedui.applyTextTheme
import com.glia.widgets.view.unifiedui.theme.base.ColorTheme
import com.glia.widgets.view.unifiedui.theme.base.TextTheme

internal open class ThemedStateText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : AppCompatTextView(context, attrs) {

    private var defaultTheme: TextTheme? = null

    init {
        defaultTheme = TextTheme(
            textColor = ColorTheme(values = listOf(currentTextColor)),
            backgroundColor = null,
            textSize = context.pxToSp(textSize),
            textStyle = typeface.style,
            textAlignment = textAlignment
        )
    }

    override fun setTypeface(tf: Typeface?) {
        tf?.apply { defaultTheme = defaultTheme?.copy(textStyle = style) }
        super.setTypeface(tf)
    }

    fun restoreDefaultTheme() {
        applyTextTheme(defaultTheme)
    }

    internal fun applyThemeAsDefault(theme: TextTheme?) {
        theme.also(::applyTextTheme)?.also { defaultTheme = it }
    }

    internal fun applyThemeOrDefault(theme: TextTheme?) {
        theme.also(::applyTextTheme) ?: restoreDefaultTheme()
    }
}
