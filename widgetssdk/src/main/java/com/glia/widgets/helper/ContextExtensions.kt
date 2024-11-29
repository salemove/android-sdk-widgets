@file:JvmName("ContextExtensions")

package com.glia.widgets.helper

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.os.Build
import android.os.Parcelable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.widget.Toast
import androidx.annotation.AttrRes
import androidx.annotation.DimenRes
import androidx.annotation.IntRange
import androidx.annotation.StyleRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.content.withStyledAttributes
import androidx.core.util.TypedValueCompat
import com.glia.widgets.BuildConfig
import com.glia.widgets.R
import com.glia.widgets.UiTheme
import com.google.android.material.theme.overlay.MaterialThemeOverlay
import java.io.Serializable

internal fun Context.asActivity(): Activity? = (this as? ContextWrapper)?.let {
    it as? Activity ?: it.baseContext.asActivity()
}

internal fun Context.requireActivity(): Activity =
    asActivity() ?: throw IllegalStateException("Context $this is not an Activity.")

internal fun Context.pxToSp(pixels: Float): Float = TypedValueCompat.deriveDimension(TypedValue.COMPLEX_UNIT_SP, pixels, resources.displayMetrics)
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

internal fun Context.wrapWithTheme(@StyleRes themeResId: Int = R.style.Application_Glia_Activity_Style): Context =
    ContextThemeWrapper(this, themeResId)

internal fun Context.showToast(
    message: String,
    @IntRange(from = 0, to = 1) duration: Int = Toast.LENGTH_LONG
) {
    Toast.makeText(this, message, duration).show()
}

internal val Activity.rootView: View
    get() = findViewById(android.R.id.content) ?: window.decorView.findViewById(android.R.id.content)

internal fun Activity.withRuntimeTheme(callback: (themedContext: Context, uiTheme: UiTheme) -> Unit) {
    val themedContext = wrapWithMaterialThemeOverlay()
    themedContext.withStyledAttributes(R.style.Application_Glia_Chat, R.styleable.GliaView) {
        callback(themedContext, Utils.getThemeFromTypedArray(this, themedContext))
    }
}

internal val Activity.qualifiedName: String
    get() = this::class.qualifiedName!!

internal val Activity.isGlia: Boolean
    get() = qualifiedName.startsWith(BuildConfig.LIBRARY_PACKAGE_NAME + ".")

internal val AlertDialog.parentActivity: Activity? get() = context.asActivity()

internal fun Context.safeStartActivity(intent: Intent, onFailure: () -> Unit, onSuccess: () -> Unit = {}) {
    if (intent.resolveActivity(packageManager) != null) {
        startActivity(intent)
        onSuccess()
    } else {
        onFailure()
    }
}

internal fun Intent.setSafeFlags(context: Context): Intent = context.asActivity()?.let { this } ?: addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

internal inline fun <reified T : Serializable> Intent.getSerializableExtraCompat(key: String): T? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getSerializableExtra(key, T::class.java)
    } else {
        @Suppress("DEPRECATION")
        getSerializableExtra(key) as? T
    }

internal inline fun <reified T : Parcelable> Intent.getParcelableExtraCompat(key: String): T? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getParcelableExtra(key, T::class.java)
    } else {
        @Suppress("DEPRECATION")
        getParcelableExtra(key) as? T
    }

internal inline fun <reified T : Enum<T>> Intent.putEnumExtra(key: String, value: T?): Intent = putExtra(key, value?.ordinal)

internal inline fun <reified T : Enum<T>> Intent.getEnumExtra(key: String): T? = getIntExtra(key, -1).takeIf { it != -1 }
    ?.let { T::class.java.enumConstants?.getOrNull(it) }
