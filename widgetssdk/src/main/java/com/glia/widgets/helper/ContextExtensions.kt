package com.glia.widgets.helper

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.AttributeSet
import androidx.annotation.AttrRes
import androidx.annotation.DimenRes
import androidx.annotation.StyleRes
import com.glia.widgets.R
import com.google.android.material.theme.overlay.MaterialThemeOverlay

internal fun Context.asActivity(): Activity? = (this as? ContextWrapper)?.let {
    it as? Activity ?: it.baseContext.asActivity()
}

internal fun Context.requireActivity(): Activity =
    asActivity() ?: throw IllegalStateException("Context $this is not an Activity.")

internal fun Context.pxToSp(pixels: Float): Float = pixels / resources.displayMetrics.scaledDensity
internal fun Context.getDimenRes(@DimenRes dimenId: Int): Float = resources.getDimension(dimenId)
internal fun Context.getDimenResPx(@DimenRes dimenId: Int): Int =
    resources.getDimensionPixelSize(dimenId)

internal fun Context.wrapWithMaterialThemeOverlay(
    attrs: AttributeSet? = null,
    @AttrRes defStyleAttr: Int = R.attr.gliaChatStyle,
    @StyleRes defStyleRes: Int = R.style.Application_Glia_Chat
): Context {
    return MaterialThemeOverlay.wrap(
        this,
        attrs,
        defStyleAttr,
        defStyleRes
    )
}