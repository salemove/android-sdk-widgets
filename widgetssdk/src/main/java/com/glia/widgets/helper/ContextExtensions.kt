package com.glia.widgets.helper

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.AttributeSet
import android.view.View
import android.widget.Toast
import androidx.annotation.AttrRes
import androidx.annotation.DimenRes
import androidx.annotation.IntRange
import androidx.annotation.StyleRes
import androidx.core.content.withStyledAttributes
import com.glia.widgets.BuildConfig
import com.glia.widgets.GliaWidgets
import com.glia.widgets.R
import com.glia.widgets.UiTheme
import com.glia.widgets.di.Dependencies
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

internal fun Context.showToast(
    message: String,
    @IntRange(from = 0, to = 1) duration: Int = Toast.LENGTH_LONG
) {
    Toast.makeText(this, message, duration).show()
}

internal val Activity.rootView: View
    get() = findViewById(android.R.id.content) ?: window.decorView.findViewById(android.R.id.content)

internal val Activity.qualifiedName: String
    get() = this::class.qualifiedName!!

internal val Activity.isGlia: Boolean
    get() = qualifiedName.startsWith(BuildConfig.LIBRARY_PACKAGE_NAME)

internal fun Activity.withRuntimeTheme(callback: (themedContext: Context, uiTheme: UiTheme) -> Unit) {
    val themedContext = wrapWithMaterialThemeOverlay()

    intent.getParcelableExtra<UiTheme>(GliaWidgets.UI_THEME)?.also {
        callback(themedContext, it.withConfigurationTheme)
    } ?: themedContext.withStyledAttributes(R.style.Application_Glia_Chat, R.styleable.GliaView) {
        callback(themedContext, Utils.getThemeFromTypedArray(this, themedContext).withConfigurationTheme)
    }
}

internal val Activity.runtimeTheme: UiTheme
    get() {
        val themeFromIntent: UiTheme? = intent?.getParcelableExtra(GliaWidgets.UI_THEME)
        val themeFromGlobalSetting = Dependencies.getSdkConfigurationManager().uiTheme
        return themeFromGlobalSetting.getFullHybridTheme(themeFromIntent)
    }
