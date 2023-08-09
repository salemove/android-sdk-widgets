package com.glia.widgets.helper

import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.text.Html
import android.text.Spanned
import android.text.format.DateUtils
import androidx.annotation.ColorInt
import androidx.core.graphics.drawable.DrawableCompat
import com.glia.androidsdk.Engagement
import com.glia.androidsdk.Operator
import com.glia.androidsdk.chat.AttachmentFile
import com.glia.androidsdk.queuing.Queue
import com.glia.widgets.UiTheme
import com.glia.widgets.view.unifiedui.deepMerge

internal fun Drawable.setTintCompat(@ColorInt color: Int) = DrawableCompat.setTint(this, color)

@ColorInt
internal fun ColorStateList?.colorForState(state: IntArray): Int? =
    this?.getColorForState(state, defaultColor)

// Common
internal fun String.separateStringWithSymbol(symbol: String): String =
    asSequence().joinToString(symbol)

internal fun Queue.supportsMessaging() = state.medias.contains(Engagement.MediaType.MESSAGING)

internal fun String.combineStringWith(string: String, separator: String) : String {
    return this + separator + string
}

internal val Operator.imageUrl: String?
    get() = picture?.url?.orElse(null)

internal fun formatElapsedTime(elapsedMilliseconds: Long) =
    DateUtils.formatElapsedTime(elapsedMilliseconds / DateUtils.SECOND_IN_MILLIS)

internal val Operator.formattedName: String
    get() = name.substringBefore(' ')

internal fun UiTheme?.isAlertDialogButtonUseVerticalAlignment(): Boolean =
    this?.gliaAlertDialogButtonUseVerticalAlignment ?: false

internal fun UiTheme?.getFullHybridTheme(newTheme: UiTheme?): UiTheme =
    deepMerge(newTheme) ?: UiTheme.UiThemeBuilder().build()

/**
 * Returns styled text from the provided HTML string.
 */
internal fun String.fromHtml(flags: Int = Html.FROM_HTML_MODE_COMPACT): Spanned = Html.fromHtml(this, flags)

internal val AttachmentFile.isImage: Boolean
    get() = contentType.startsWith("image")
