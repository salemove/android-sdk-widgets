package com.glia.widgets.view.unifiedui.exstensions

import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.TypedValue
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.RawRes
import com.glia.widgets.view.unifiedui.theme.base.ThemeButton
import com.glia.widgets.view.unifiedui.theme.base.ThemeColor
import com.glia.widgets.view.unifiedui.theme.base.ThemeLayer
import com.glia.widgets.view.unifiedui.theme.base.ThemeText


internal fun View.applyThemeLayer(layer: ThemeLayer?) {
    layer?.asDrawable()?.also {
        backgroundTintList = null
        background = it
    }
}

internal fun View.applyThemeColor(color: ThemeColor?) {
    background = color?.asDrawable() ?: return
}

internal fun View.applyShadow(@ColorInt color: Int?) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && color != null) {
        outlineSpotShadowColor = color
        outlineAmbientShadowColor = color
    }
}

internal fun TextView.applyTextThemeColor(color: ThemeColor?) {
    if (color == null) return

    if (color.isGradient) {
        paint.shader = LinearGradient(
            0f,
            0f,
            paint.measureText(text.toString()),
            textSize,
            color.valuesArray,
            null,
            Shader.TileMode.CLAMP
        )
    } else {
        setTextColor(color.primaryColor)
    }

}

internal fun TextView.applyThemeText(themeText: ThemeText?) {
    themeText?.apply {
        if (this@applyThemeText !is Button) {
            applyThemeColor(backgroundColor)
        }

        applyTextThemeColor(textColor)
        textSize?.also { setTextSize(TypedValue.COMPLEX_UNIT_SP, it) }
        textStyle?.also { typeface = Typeface.create(typeface, it) }
        textAlignment?.let { this@applyThemeText.textAlignment = it }

    }
}

internal fun Button.applyThemeButton(themeButton: ThemeButton?) {
    themeButton?.apply {
        background?.also { applyThemeLayer(it) }
        text?.also { applyThemeText(it) }
        elevation?.also { this@applyThemeButton.elevation = it }
        applyShadow(shadowColor)
    }
}

internal fun Resources.createNewDrawable(bitmap: Bitmap, themeColor: ThemeColor): Drawable {
    val newBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(newBitmap)
    canvas.drawBitmap(bitmap, 0f, 0f, null)

    val paint = Paint()
    val shader = LinearGradient(
        0f,
        0f,
        0f,
        bitmap.height.toFloat(),
        themeColor.valuesArray,
        null,
        Shader.TileMode.CLAMP
    )
    paint.shader = shader
    paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
    canvas.drawRect(0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat(), paint)

    return BitmapDrawable(this, newBitmap)
}
