package com.glia.widgets.helper

import android.util.Log
import androidx.annotation.VisibleForTesting
import com.glia.androidsdk.LogLevel
import com.glia.androidsdk.LoggingAdapter
import com.glia.widgets.BuildConfig
import java.util.function.Consumer

internal object Logger {

    const val SITE_ID_KEY = "site_id"
    private const val DEPRECATED_METHOD_CALL_LOG_MESSAGE = "Deprecated method usage: "
    private const val DEPRECATED_CLASS_CALL_LOG_MESSAGE = "Deprecated class usage: "
    internal const val TAG_PREFIX = "Glia Widgets: "

    private val loggingAdapters = mutableListOf<LoggingAdapter>()
    private val globalMetadata = mutableMapOf<String, String>()
    private var isDebug = BuildConfig.DEBUG

    private val setOfUsedDeprecatedFunctions = mutableSetOf<String>()
    private val setOfUsedDeprecatedClasses = mutableSetOf<String>()

    @Suppress("unused")
    @JvmStatic
    fun addAdapter(loggingAdapter: LoggingAdapter) {
        loggingAdapters.add(loggingAdapter)
    }

    @VisibleForTesting
    fun setIsDebug(isDebug: Boolean) {
        this.isDebug = isDebug
    }

    @JvmStatic
    fun d(tag: String, message: String) {
        if (isDebug) {
            Log.d(TAG_PREFIX + tag, message)
            // Normally, Widgets DEBUG build uses Core SDK with RELEASE build variant.
            // Hence, Core will add ClientLoggerAdapter to loggingAdapters.
            // Return to avoid sending logs to ClientLoggerAdapter for Widgets DEBUG build.
            return
        }

        loggingAdapters.forEach(Consumer { loggingAdapter: LoggingAdapter ->
            loggingAdapter.log(LogLevel.DEBUG, TAG_PREFIX + tag, message, concatGlobalMeta(null))
        })
    }

    @JvmStatic
    fun d(tag: String, message: String, metadata: Map<String, String>? = null) {
        if (isDebug) {
            Log.d(TAG_PREFIX + tag, message)
            // Normally, Widgets DEBUG build uses Core SDK with RELEASE build variant.
            // Hence, Core will add ClientLoggerAdapter to loggingAdapters.
            // Return to avoid sending logs to ClientLoggerAdapter for Widgets DEBUG build.
            return
        }

        loggingAdapters.forEach(Consumer { loggingAdapter: LoggingAdapter ->
            loggingAdapter.log(LogLevel.DEBUG, TAG_PREFIX + tag, message, concatGlobalMeta(metadata))
        })
    }

    @JvmStatic
    fun i(tag: String, message: String) {
        if (isDebug) {
            Log.i(TAG_PREFIX + tag, message)
            // Normally, Widgets DEBUG build uses Core SDK with RELEASE build variant.
            // Hence, Core will add ClientLoggerAdapter to loggingAdapters.
            // Return to avoid sending logs to ClientLoggerAdapter for Widgets DEBUG build.
            return
        }

        loggingAdapters.forEach(Consumer { loggingAdapter: LoggingAdapter ->
            loggingAdapter.log(LogLevel.INFO, TAG_PREFIX + tag, message, concatGlobalMeta(null))
        })
    }

    @JvmStatic
    fun i(tag: String, message: String, metadata: Map<String, String>? = null) {
        if (isDebug) {
            Log.i(TAG_PREFIX + tag, message)
            // Normally, Widgets DEBUG build uses Core SDK with RELEASE build variant.
            // Hence, Core will add ClientLoggerAdapter to loggingAdapters.
            // Return to avoid sending logs to ClientLoggerAdapter for Widgets DEBUG build.
            return
        }

        loggingAdapters.forEach(Consumer { loggingAdapter: LoggingAdapter ->
            loggingAdapter.log(LogLevel.INFO, TAG_PREFIX + tag, message, concatGlobalMeta(metadata))
        })
    }

    @JvmStatic
    fun w(tag: String, message: String) {
        if (isDebug) {
            Log.w(TAG_PREFIX + tag, message)
            // Normally, Widgets DEBUG build uses Core SDK with RELEASE build variant.
            // Hence, Core will add ClientLoggerAdapter to loggingAdapters.
            // Return to avoid sending logs to ClientLoggerAdapter for Widgets DEBUG build.
            return
        }

        loggingAdapters.forEach(Consumer { loggingAdapter: LoggingAdapter ->
            loggingAdapter.log(LogLevel.WARN, TAG_PREFIX + tag, message, concatGlobalMeta(null))
        })
    }

    @JvmStatic
    fun w(tag: String, message: String, metadata: Map<String, String>? = null) {
        if (isDebug) {
            Log.w(TAG_PREFIX + tag, message)
            // Normally, Widgets DEBUG build uses Core SDK with RELEASE build variant.
            // Hence, Core will add ClientLoggerAdapter to loggingAdapters.
            // Return to avoid sending logs to ClientLoggerAdapter for Widgets DEBUG build.
            return
        }

        loggingAdapters.forEach(Consumer { loggingAdapter: LoggingAdapter ->
            loggingAdapter.log(LogLevel.WARN, TAG_PREFIX + tag, message, concatGlobalMeta(metadata))
        })
    }

    @JvmStatic
    fun e(tag: String, message: String) {
        if (isDebug) {
            Log.e(TAG_PREFIX + tag, message)
            // Normally, Widgets DEBUG build uses Core SDK with RELEASE build variant.
            // Hence, Core will add ClientLoggerAdapter to loggingAdapters.
            // Return to avoid sending logs to ClientLoggerAdapter for Widgets DEBUG build.
            return
        }

        loggingAdapters.forEach(Consumer { loggingAdapter: LoggingAdapter ->
            loggingAdapter.error(TAG_PREFIX + tag, message, null, concatGlobalMeta(null))
        })
    }

    @JvmStatic
    fun e(tag: String, message: String, throwable: Throwable?) {
        if (isDebug) {
            Log.e(TAG_PREFIX + tag, message, throwable)
            // Normally, Widgets DEBUG build uses Core SDK with RELEASE build variant.
            // Hence, Core will add ClientLoggerAdapter to loggingAdapters.
            // Return to avoid sending logs to ClientLoggerAdapter for Widgets DEBUG build.
            return
        }

        loggingAdapters.forEach(Consumer { loggingAdapter: LoggingAdapter ->
            loggingAdapter.error(TAG_PREFIX + tag, message, throwable, concatGlobalMeta(null))
        })
    }

    @JvmStatic
    fun e(tag: String, message: String, throwable: Throwable?, metadata: Map<String, String>? = null) {
        if (isDebug) {
            Log.e(TAG_PREFIX + tag, message, throwable)
            // Normally, Widgets DEBUG build uses Core SDK with RELEASE build variant.
            // Hence, Core will add ClientLoggerAdapter to loggingAdapters.
            // Return to avoid sending logs to ClientLoggerAdapter for Widgets DEBUG build.
            return
        }

        loggingAdapters.forEach(Consumer { loggingAdapter: LoggingAdapter ->
            loggingAdapter.error(TAG_PREFIX + tag, message, throwable, concatGlobalMeta(metadata))
        })
    }

    @JvmStatic
    fun logDeprecatedMethodUse(tag: String, methodName: String) {
        val logMessage = DEPRECATED_METHOD_CALL_LOG_MESSAGE + methodName

        if (!setOfUsedDeprecatedFunctions.add(logMessage)) return // Log only once per session

        if (isDebug) {
            // Do not log this event to console for debug builds because we see them in IDE
            return
        }

        loggingAdapters.forEach(Consumer { loggingAdapter: LoggingAdapter ->
            loggingAdapter.log(LogLevel.INFO, TAG_PREFIX + tag, logMessage, concatGlobalMeta(null))
        })
    }

    @JvmStatic
    fun logDeprecatedClassUse(tag: String) {
        val logMessage = DEPRECATED_CLASS_CALL_LOG_MESSAGE + tag

        if (!setOfUsedDeprecatedClasses.add(logMessage)) return // Log only once per session

        if (isDebug) {
            // Do not log this event to console for debug builds because we see them in IDE
            return
        }

        loggingAdapters.forEach(Consumer { loggingAdapter: LoggingAdapter ->
            loggingAdapter.log(LogLevel.INFO, TAG_PREFIX + tag, logMessage, concatGlobalMeta(null))
        })
    }

    @JvmStatic
    @Synchronized
    fun addGlobalMetadata(metadata: Map<String, String>) {
        globalMetadata.putAll(metadata)
    }

    @Synchronized
    fun removeGlobalMetadata(metaKey: String) {
        globalMetadata.remove(metaKey)
    }

    private fun concatGlobalMeta(metadata: Map<String, String>?): Map<String, String> {
        val combinedMap: MutableMap<String, String> = HashMap()
        if (globalMetadata.isNotEmpty()) combinedMap.putAll(globalMetadata)
        if (!metadata.isNullOrEmpty()) combinedMap.putAll(metadata)
        return combinedMap
    }
}

/**
 * This extension allows us to use TAG in any Kotlin class
 *
 * P.S: Java classes still need TAG to be defined manually
 */
internal val Any.TAG: String
    get() {
        return if (javaClass.isAnonymousClass) {
            javaClass.name
        } else {
            javaClass.simpleName // returns "" if the underlying class is anonymous
        }
    }
