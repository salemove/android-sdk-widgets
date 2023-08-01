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
import com.glia.androidsdk.chat.MessageAttachment
import com.glia.androidsdk.chat.SingleChoiceAttachment
import com.glia.androidsdk.queuing.Queue
import com.glia.widgets.UiTheme
import com.glia.widgets.core.engagement.data.LocalOperator
import com.glia.widgets.view.unifiedui.deepMerge
import kotlin.jvm.optionals.getOrNull

internal fun Drawable.setTintCompat(@ColorInt color: Int) = DrawableCompat.setTint(this, color)

@ColorInt
internal fun ColorStateList?.colorForState(state: IntArray): Int? = this?.getColorForState(state, defaultColor)

// Common
internal fun String.separateStringWithSymbol(symbol: String): String = asSequence().joinToString(symbol)

internal fun Queue.supportMessaging() = state.medias.contains(Engagement.MediaType.MESSAGING)

internal fun formatElapsedTime(elapsedMilliseconds: Long) = DateUtils.formatElapsedTime(elapsedMilliseconds / DateUtils.SECOND_IN_MILLIS)

internal val Operator.formattedName: String get() = name.substringBefore(' ')

internal val Operator.imageUrl: String? get() = picture?.url?.getOrNull()

internal fun Operator.toLocal(): LocalOperator = LocalOperator(id, name, imageUrl)

internal fun UiTheme?.isAlertDialogButtonUseVerticalAlignment(): Boolean = this?.gliaAlertDialogButtonUseVerticalAlignment ?: false

internal fun UiTheme?.getFullHybridTheme(newTheme: UiTheme?): UiTheme = deepMerge(newTheme) ?: UiTheme.UiThemeBuilder().build()

/**
 * Returns styled text from the provided HTML string.
 */
internal fun String.fromHtml(flags: Int = Html.FROM_HTML_MODE_COMPACT): Spanned = Html.fromHtml(this, flags)

internal val AttachmentFile.isImage: Boolean get() = contentType.startsWith("image")

internal fun MessageAttachment.asSingleChoice(): SingleChoiceAttachment? = this as? SingleChoiceAttachment
