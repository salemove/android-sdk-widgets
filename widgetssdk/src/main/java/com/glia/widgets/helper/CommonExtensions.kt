package com.glia.widgets.helper

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.text.Html
import android.text.Spanned
import android.text.format.DateUtils
import androidx.annotation.ColorInt
import androidx.core.graphics.drawable.DrawableCompat
import com.glia.androidsdk.CoreConfiguration
import com.glia.androidsdk.Engagement
import com.glia.androidsdk.Engagement.ActionOnEnd
import com.glia.androidsdk.GliaException
import com.glia.androidsdk.Operator
import com.glia.androidsdk.chat.AttachmentFile
import com.glia.androidsdk.chat.ChatMessage
import com.glia.androidsdk.chat.MessageAttachment
import com.glia.androidsdk.chat.SingleChoiceAttachment
import com.glia.androidsdk.comms.MediaDirection
import com.glia.androidsdk.comms.MediaQuality
import com.glia.androidsdk.comms.MediaState
import com.glia.androidsdk.comms.MediaUpgradeOffer
import com.glia.androidsdk.omnibrowse.OmnibrowseEngagement
import com.glia.telemetry_lib.EventAttribute
import com.glia.telemetry_lib.GliaLogger
import com.glia.telemetry_lib.LogEvents
import com.glia.widgets.GliaWidgetsConfig
import com.glia.widgets.GliaWidgetsConfig.Regions
import com.glia.widgets.GliaWidgetsException
import com.glia.widgets.Region
import com.glia.widgets.UiTheme
import com.glia.widgets.engagement.MediaType
import com.glia.widgets.queue.Queue
import com.glia.widgets.toCoreType
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.processors.FlowableProcessor
import java.io.File
import java.util.UUID
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.io.path.createTempFile
import kotlin.jvm.optionals.getOrNull
import com.glia.androidsdk.Region as CoreRegion

internal fun Drawable.setTintCompat(@ColorInt color: Int) = DrawableCompat.setTint(this, color)

@ColorInt
internal fun ColorStateList?.colorForState(state: IntArray): Int? = this?.getColorForState(state, defaultColor)

// Common
internal fun String.separateStringWithSymbol(symbol: String): String = asSequence().joinToString(symbol)

internal fun Queue.supportedMediaTypes(): List<MediaType>? = when {
    status == Queue.Status.OPEN -> media.filterNot { it == MediaType.PHONE || it == MediaType.UNKNOWN }.takeIf { it.isNotEmpty() }
    status == Queue.Status.FULL && media.contains(MediaType.MESSAGING) -> listOf(MediaType.MESSAGING)
    status == Queue.Status.UNSTAFFED && media.contains(MediaType.MESSAGING) -> listOf(MediaType.MESSAGING)
    else -> null
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

internal fun UiTheme?.isAlertDialogButtonUseVerticalAlignment(): Boolean = this?.gliaAlertDialogButtonUseVerticalAlignment == true

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

internal val MediaQuality.isPoor: Boolean get() = this == MediaQuality.POOR

internal val GliaException.isQueueUnavailable: Boolean
    get() = cause == GliaException.Cause.QUEUE_CLOSED || cause == GliaException.Cause.QUEUE_FULL

internal val ActionOnEnd?.isRetain: Boolean get() = this == ActionOnEnd.RETAIN
internal val ActionOnEnd?.isSurvey: Boolean get() = this == ActionOnEnd.SHOW_SURVEY
internal val ActionOnEnd?.isShowEndDialog: Boolean get() = this == ActionOnEnd.END_NOTIFICATION
internal val ActionOnEnd?.isUnknown: Boolean get() = this == ActionOnEnd.UNKNOWN

internal val Engagement.isCallVisualizer: Boolean get() = this is OmnibrowseEngagement

internal fun <T : Any> FlowableProcessor<T>.asStateFlowable(): Flowable<T> = onBackpressureBuffer().observeOn(AndroidSchedulers.mainThread())
internal fun <T : Any> FlowableProcessor<T>.asOneTimeStateFlowable(): Flowable<OneTimeEvent<T>> =
    onBackpressureBuffer().map(::OneTimeEvent).observeOn(AndroidSchedulers.mainThread())

internal fun Uri.exists(context: Context): Boolean = context.contentResolver.query(this, null, null, null, null)?.use {
    it.moveToFirst()
} == true

internal fun createTempFileCompat(prefix: String, suffix: String? = null, directory: File? = null): File =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        createTempFile(directory?.toPath(), prefix, suffix).toFile()
    } else {
        File.createTempFile(prefix, suffix, directory)
    }

internal fun GliaLogger.logCallScreenButtonClicked(buttonName: String) = i(LogEvents.CALL_SCREEN_BUTTON_CLICKED) {
    put(EventAttribute.ButtonName, buttonName)
}

internal fun GliaLogger.logScWelcomeScreenButtonClicked(buttonName: String) = i(LogEvents.SC_WELCOME_SCREEN_BUTTON_CLICKED) {
    put(EventAttribute.ButtonName, buttonName)
}

internal fun GliaLogger.logScConfirmationScreenButtonClicked(buttonName: String) = i(LogEvents.SC_CONFIRMATION_SCREEN_BUTTON_CLICKED) {
    put(EventAttribute.ButtonName, buttonName)
}

private const val NOT_APPLICABLE = "N/A"

internal val String?.orNotApplicable: String
    get() = this ?: NOT_APPLICABLE

@Throws(GliaWidgetsException::class, GliaException::class)
internal fun GliaWidgetsConfig.toCoreType(): CoreConfiguration {
    val authorizationMethod = authorizationMethod.requireNotNull { "Authorization method is required" }
    val siteId = siteId.requireNotNull { "Site ID is required" }
    context.requireNotNull { "Context is required" }
    val region = requireRegion(region, regionString)

    return CoreConfiguration(
        authorizationMethod = authorizationMethod.toCoreType(),
        siteId = siteId,
        region = region.toCoreType(),
        applicationContext = context,
        manualLocaleOverride = manualLocaleOverride
    )
}

/**
 * Takes either region enum or region string and returns region enum.
 * Validates that only one of the parameters is provided, and if it is the String one, it is one of the known regions.
 * This is to support both new and deprecated way of setting region in the builder, but to deal only with enum internally.
 */
private fun requireRegion(region: Region?, regionString: String?): Region = when {
    // Both parameters are provided
    region != null && regionString != null -> throwGliaException(GliaWidgetsException.Cause.INVALID_INPUT) {
        "`setRegion(region: Region)` and `setRegion(region: String)` are mutually exclusive"
    }
    // Enum parameter is provided
    region != null -> region
    // None of the parameters is provided
    regionString == null -> throwGliaException(GliaWidgetsException.Cause.INVALID_INPUT) {
        "`setRegion(region: Region)` or `setRegion(region: String)` is required"
    }

    Regions.US.equals(regionString, ignoreCase = true) -> Region.US
    Regions.EU.equals(regionString, ignoreCase = true) -> Region.EU

    // Unknown region string provided
    else -> throwGliaException(GliaWidgetsException.Cause.INVALID_INPUT) { "Unknown region: $regionString" }
}

internal fun Region.toCoreType(): CoreRegion = when (this) {
    Region.US -> CoreRegion.US
    Region.EU -> CoreRegion.EU
    Region.Beta -> CoreRegion.Beta
    is Region.Custom -> CoreRegion.Custom(host)
}

// For logging only!!
internal val Region.stringValue: String
    get() = when (this) {
        Region.US -> Regions.US
        Region.EU -> Regions.EU
        Region.Beta -> "beta"
        is Region.Custom -> "region: custom, host: $host"
    }

@OptIn(ExperimentalContracts::class)
internal inline fun <T : Any> T?.requireNotNull(lazyMessage: () -> Any): T {
    contract {
        returns() implies (this@requireNotNull != null)
    }

    if (this == null) {
        throw GliaWidgetsException(lazyMessage().toString(), GliaWidgetsException.Cause.INVALID_INPUT)
    } else {
        return this
    }
}

internal fun throwGliaException(cause: GliaWidgetsException.Cause, lazyMessage: () -> Any): Nothing {
    throw GliaWidgetsException(lazyMessage().toString(), cause)
}


/**
 * Converts this String into a deterministic 64-bit stable ID.
 *
 * * **Primary Logic:** If the string is a valid UUID, it performs an "XOR Fold"
 * ([UUID.mostSignificantBits] XOR [UUID.leastSignificantBits]). This compresses
 * 128 bits of data into 64 bits, preserving significantly more entropy/uniqueness
 * than a standard 32-bit hash.
 *
 * * **Fallback Logic:** If the string is **not** a valid UUID, it safely catches the
 * exception and falls back to [String.hashCode], ensuring the app never crashes
 * due to malformed IDs.
 *
 * * **Purpose:** Ideal for [androidx.recyclerview.widget.RecyclerView.Adapter.getItemId]
 * where stable, unique 64-bit IDs are required for correct animations and state preservation.
 */
internal val String.asStableId: Long
    get() = runCatching {
        UUID.fromString(this).run { mostSignificantBits xor leastSignificantBits }
    }.getOrDefault(hashCode().toLong())
