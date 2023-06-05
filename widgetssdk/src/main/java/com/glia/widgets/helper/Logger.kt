package com.glia.widgets.helper

import android.util.Log
import com.glia.widgets.BuildConfig

object Logger {
    @JvmStatic
    fun d(tag: String, message: String?) {
        if (BuildConfig.DEBUG) {
            // No need to log an empty message
            Log.d(tag, message ?: return)
        }
    }

    @JvmStatic
    fun e(tag: String, message: String?) {
        if (BuildConfig.DEBUG) {
            // //No need to log an empty message
            Log.e(tag, message ?: return)
        }
    }

    @JvmStatic
    fun e(tag: String, message: String?, tr: Throwable?) {
        if (BuildConfig.DEBUG) {
            // No problem with lint in here
            if (message.isNullOrBlank() && tr == null) return
            Log.e(tag, message, tr)
        }
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
