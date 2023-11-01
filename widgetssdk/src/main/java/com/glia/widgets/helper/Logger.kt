package com.glia.widgets.helper

import com.glia.androidsdk.LogLevel
import com.glia.androidsdk.LoggingAdapter
import java.util.function.Consumer

object Logger {

    const val SITE_ID_KEY = "site_id"

    private val loggingAdapters = mutableListOf<LoggingAdapter>()
    private val globalMetadata = mutableMapOf<String, String>()

    @JvmStatic
    fun addAdapter(loggingAdapter: LoggingAdapter) {
        loggingAdapters.add(loggingAdapter)
    }

    @JvmStatic
    fun d(tag: String, message: String?) {
        // No need to log an empty message
        if (message == null) return

        loggingAdapters.forEach(Consumer { loggingAdapter: LoggingAdapter ->
            loggingAdapter.log(LogLevel.DEBUG, tag, message, concatGlobalMeta(null))
        })
    }

    @JvmStatic
    fun d(tag: String, message: String?, metadata: Map<String, String>? = null) {
        // No need to log an empty message
        if (message == null) return

        loggingAdapters.forEach(Consumer { loggingAdapter: LoggingAdapter ->
            loggingAdapter.log(LogLevel.DEBUG, tag, message, concatGlobalMeta(metadata))
        })
    }

    @JvmStatic
    fun e(tag: String, message: String?) {
        if (message == null) return

        loggingAdapters.forEach(Consumer { loggingAdapter: LoggingAdapter ->
            loggingAdapter.error(tag, message, null, concatGlobalMeta(null))
        })
    }

    @JvmStatic
    fun e(tag: String, message: String?, throwable: Throwable?) {
        if (message == null) return

        loggingAdapters.forEach(Consumer { loggingAdapter: LoggingAdapter ->
            loggingAdapter.error(tag, message, throwable, concatGlobalMeta(null))
        })
    }

    @JvmStatic
    fun e(tag: String, message: String?, throwable: Throwable?, metadata: Map<String, String>? = null) {
        if (message == null) return

        loggingAdapters.forEach(Consumer { loggingAdapter: LoggingAdapter ->
            loggingAdapter.error(tag, message, throwable, concatGlobalMeta(metadata))
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

    @Synchronized
    fun clearGlobalMetadata() {
        globalMetadata.clear()
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
