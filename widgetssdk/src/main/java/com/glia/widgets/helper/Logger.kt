package com.glia.widgets.helper

import android.util.Log
import com.glia.androidsdk.BuildConfig
import com.glia.androidsdk.LogLevel
import com.glia.androidsdk.LoggingAdapter
import java.util.function.Consumer

internal object Logger {

    const val SITE_ID_KEY = "site_id"
    private const val DEPRECATED_METHOD_CALL_LOG_MESSAGE = "Deprecated method usage: "
    private const val DEPRECATED_CLASS_CALL_LOG_MESSAGE = "Deprecated class usage: "

    private val loggingAdapters = mutableListOf<LoggingAdapter>()
    private val globalMetadata = mutableMapOf<String, String>()

    @Suppress("unused")
    @JvmStatic
    fun addAdapter(loggingAdapter: LoggingAdapter) {
        loggingAdapters.add(loggingAdapter)
    }

    @JvmStatic
    fun d(tag: String, message: String) {
        if (loggingAdapters.isEmpty() && BuildConfig.DEBUG) {
            // Log to console even if there were no any logging adapter set for debug build
            Log.d(tag, message)
        }

        loggingAdapters.forEach(Consumer { loggingAdapter: LoggingAdapter ->
            loggingAdapter.log(LogLevel.DEBUG, tag, message, concatGlobalMeta(null))
        })
    }

    @JvmStatic
    fun d(tag: String, message: String, metadata: Map<String, String>? = null) {
        if (loggingAdapters.isEmpty() && BuildConfig.DEBUG) {
            // Log to console even if there were no any logging adapter set for debug build
            Log.d(tag, message)
        }

        loggingAdapters.forEach(Consumer { loggingAdapter: LoggingAdapter ->
            loggingAdapter.log(LogLevel.DEBUG, tag, message, concatGlobalMeta(metadata))
        })
    }

    @JvmStatic
    fun i(tag: String, message: String) {
        if (loggingAdapters.isEmpty() && BuildConfig.DEBUG) {
            // Log to console even if there were no any logging adapter set for debug build
            Log.i(tag, message)
        }

        loggingAdapters.forEach(Consumer { loggingAdapter: LoggingAdapter ->
            loggingAdapter.log(LogLevel.INFO, tag, message, concatGlobalMeta(null))
        })
    }

    @JvmStatic
    fun i(tag: String, message: String, metadata: Map<String, String>? = null) {
        if (loggingAdapters.isEmpty() && BuildConfig.DEBUG) {
            // Log to console even if there were no any logging adapter set for debug build
            Log.i(tag, message)
        }

        loggingAdapters.forEach(Consumer { loggingAdapter: LoggingAdapter ->
            loggingAdapter.log(LogLevel.INFO, tag, message, concatGlobalMeta(metadata))
        })
    }

    @JvmStatic
    fun w(tag: String, message: String) {
        if (loggingAdapters.isEmpty() && BuildConfig.DEBUG) {
            // Log to console even if there were no any logging adapter set for debug build
            Log.w(tag, message)
        }

        loggingAdapters.forEach(Consumer { loggingAdapter: LoggingAdapter ->
            loggingAdapter.log(LogLevel.WARN, tag, message, concatGlobalMeta(null))
        })
    }

    @JvmStatic
    fun w(tag: String, message: String, metadata: Map<String, String>? = null) {
        if (loggingAdapters.isEmpty() && BuildConfig.DEBUG) {
            // Log to console even if there were no any logging adapter set for debug build
            Log.w(tag, message)
        }

        loggingAdapters.forEach(Consumer { loggingAdapter: LoggingAdapter ->
            loggingAdapter.log(LogLevel.WARN, tag, message, concatGlobalMeta(metadata))
        })
    }

    @JvmStatic
    fun e(tag: String, message: String) {
        if (loggingAdapters.isEmpty() && BuildConfig.DEBUG) {
            // Log to console even if there were no any logging adapter set for debug build
            Log.e(tag, message)
        }

        loggingAdapters.forEach(Consumer { loggingAdapter: LoggingAdapter ->
            loggingAdapter.error(tag, message, null, concatGlobalMeta(null))
        })
    }

    @JvmStatic
    fun e(tag: String, message: String, throwable: Throwable?) {
        if (loggingAdapters.isEmpty() && BuildConfig.DEBUG) {
            // Log to console even if there were no any logging adapter set for debug build
            Log.e(tag, message, throwable)
        }

        loggingAdapters.forEach(Consumer { loggingAdapter: LoggingAdapter ->
            loggingAdapter.error(tag, message, throwable, concatGlobalMeta(null))
        })
    }

    @JvmStatic
    fun e(tag: String, message: String, throwable: Throwable?, metadata: Map<String, String>? = null) {
        if (loggingAdapters.isEmpty() && BuildConfig.DEBUG) {
            // Log to console even if there were no any logging adapter set for debug build
            Log.e(tag, message, throwable)
        }

        loggingAdapters.forEach(Consumer { loggingAdapter: LoggingAdapter ->
            loggingAdapter.error(tag, message, throwable, concatGlobalMeta(metadata))
        })
    }

    @JvmStatic
    fun logDeprecatedMethodUse(tag: String, methodName: String) {
        val logMessage = DEPRECATED_METHOD_CALL_LOG_MESSAGE + methodName

        if (loggingAdapters.isEmpty() && BuildConfig.DEBUG) {
            // Log to console even if there were no any logging adapter set for debug build
            Log.d(tag, logMessage)
        }

        loggingAdapters.forEach(Consumer { loggingAdapter: LoggingAdapter ->
            loggingAdapter.log(LogLevel.INFO, tag, logMessage, concatGlobalMeta(null))
        })
    }

    @JvmStatic
    fun logDeprecatedClassUse(tag: String) {
        val logMessage = DEPRECATED_CLASS_CALL_LOG_MESSAGE + tag

        if (loggingAdapters.isEmpty() && BuildConfig.DEBUG) {
            // Log to console even if there were no any logging adapter set for debug build
            Log.d(tag, logMessage)
        }

        loggingAdapters.forEach(Consumer { loggingAdapter: LoggingAdapter ->
            loggingAdapter.log(LogLevel.INFO, tag, logMessage, concatGlobalMeta(null))
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
