package com.glia.widgets.helper

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.net.Uri
import android.text.Html
import android.text.Spanned
import android.text.format.DateUtils
import androidx.annotation.ColorInt
import androidx.core.graphics.drawable.DrawableCompat
import com.glia.androidsdk.Engagement.MediaType
import com.glia.androidsdk.GliaException
import com.glia.androidsdk.Operator
import com.glia.androidsdk.chat.AttachmentFile
import com.glia.androidsdk.chat.ChatMessage
import com.glia.androidsdk.chat.MessageAttachment
import com.glia.androidsdk.chat.SingleChoiceAttachment
import com.glia.androidsdk.comms.MediaDirection
import com.glia.androidsdk.comms.MediaState
import com.glia.androidsdk.comms.MediaUpgradeOffer
import com.glia.androidsdk.queuing.Queue
import com.glia.androidsdk.queuing.QueueState
import com.glia.widgets.UiTheme
import com.glia.widgets.view.unifiedui.merge
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.functions.Action
import io.reactivex.rxjava3.processors.FlowableProcessor
import kotlin.jvm.optionals.getOrNull

internal fun Drawable.setTintCompat(@ColorInt color: Int) = DrawableCompat.setTint(this, color)

@ColorInt
internal fun ColorStateList?.colorForState(state: IntArray): Int? = this?.getColorForState(state, defaultColor)

// Common
internal fun String.separateStringWithSymbol(symbol: String): String = asSequence().joinToString(symbol)

internal fun Queue.supportMessaging() = state.medias.contains(MediaType.MESSAGING)

internal fun Queue.supportedMediaTypes(): List<MediaType>? = state.run {
    when {
        status == QueueState.Status.OPEN -> medias.filterNot { it == MediaType.PHONE || it == MediaType.UNKNOWN }.takeIf { it.isNotEmpty() }
        status == QueueState.Status.FULL && medias.contains(MediaType.MESSAGING) -> listOf(MediaType.MESSAGING)
        status == QueueState.Status.UNSTAFFED && medias.contains(MediaType.MESSAGING) -> listOf(MediaType.MESSAGING)
        else -> null
    }
}

internal val List<Queue>.mediaTypes: List<MediaType>
    get() = this.mapNotNull { it.supportedMediaTypes() }.flatten().distinct()

internal fun MediaType.isAudioOrVideo() = this == MediaType.AUDIO || this == MediaType.VIDEO

internal fun formatElapsedTime(elapsedMilliseconds: Long) = DateUtils.formatElapsedTime(elapsedMilliseconds / DateUtils.SECOND_IN_MILLIS)

internal fun String.combineStringWith(string: String, separator: String): String {
    return this + separator + string
}

internal val Operator.formattedName: String get() = name.substringBefore(' ')

internal val Operator.imageUrl: String? get() = picture?.url?.getOrNull()

internal fun UiTheme?.isAlertDialogButtonUseVerticalAlignment(): Boolean = this?.gliaAlertDialogButtonUseVerticalAlignment ?: false

internal fun UiTheme?.getFullHybridTheme(newTheme: UiTheme?): UiTheme = merge(newTheme) ?: UiTheme.UiThemeBuilder().build()

/**
 * Returns styled text from the provided HTML string. Replaces \n to <br> regardless of the operating system where the string was created.
 */
internal fun String.fromHtml(flags: Int = Html.FROM_HTML_MODE_COMPACT): Spanned = Html.fromHtml(replace("(\r\n|\n)".toRegex(), "<br>"), flags)

internal val AttachmentFile.isImage: Boolean get() = contentType.startsWith("image")

internal fun MessageAttachment.asSingleChoice(): SingleChoiceAttachment? = this as? SingleChoiceAttachment

internal fun ChatMessage.isValid(): Boolean = content.isNotBlank() || attachment != null || metadata?.takeIf { it.length() > 0 } != null

internal val MediaState.hasAudio: Boolean get() = audio != null
internal val MediaState.hasVideo: Boolean get() = video != null
internal val MediaState.hasMedia: Boolean get() = hasAudio || hasVideo

internal val MediaUpgradeOffer.isAudio: Boolean get() = video == MediaDirection.NONE && audio == MediaDirection.TWO_WAY
internal val MediaUpgradeOffer.isTwoWayVideo: Boolean get() = video == MediaDirection.TWO_WAY
internal val MediaUpgradeOffer.isOneWayVideo: Boolean get() = video == MediaDirection.ONE_WAY

internal val GliaException.isQueueUnavailable: Boolean
    get() = cause == GliaException.Cause.QUEUE_CLOSED || cause == GliaException.Cause.QUEUE_FULL

@SuppressLint("CheckResult")
internal fun <T : Any> Flowable<out T>.unSafeSubscribe(onNextCallback: (T) -> Unit) {
    subscribe({ onNextCallback(it) }) { Logger.e("Observable<T>.unSafeSubscribe", "Unexpected local exception happened", it) }
}

@SuppressLint("CheckResult")
internal fun <T : Any> Single<out T>.unSafeSubscribe(onNextCallback: (T) -> Unit) {
    subscribe({ onNextCallback(it) }) { Logger.e("Single<T>.unSafeSubscribe", "Unexpected local exception happened", it) }
}

@SuppressLint("CheckResult")
internal fun Completable.unSafeSubscribe(onComplete: Action) {
    subscribe(onComplete) { Logger.e("Single<T>.unSafeSubscribe", "Unexpected local exception happened", it) }
}

internal fun <T : Any> FlowableProcessor<T>.asStateFlowable(): Flowable<T> = onBackpressureBuffer().observeOn(AndroidSchedulers.mainThread())
internal fun <T : Any> FlowableProcessor<T>.asOneTimeStateFlowable(): Flowable<OneTimeEvent<T>> =
    onBackpressureBuffer().map(::OneTimeEvent).observeOn(AndroidSchedulers.mainThread())

internal fun Uri.exists(context: Context): Boolean = context.contentResolver.query(this, null, null, null, null)?.use {
    it.moveToFirst()
} ?: false
