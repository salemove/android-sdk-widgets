package com.glia.widgets.helper

import android.util.Log
import com.glia.widgets.BuildConfig

object Logger {
    @JvmStatic
    fun d(tag: String?, message: String?) {
        if (BuildConfig.DEBUG) {
            // For some reason lint doesn't like when `message` is null
            Log.d(tag, message ?: "")
        }
    }

    @JvmStatic
    fun e(tag: String?, message: String?) {
        if (BuildConfig.DEBUG) {
            // For some reason lint doesn't like when `message` is null
            Log.e(tag, message ?: "")
        }
    }

    @JvmStatic
    fun e(tag: String?, message: String?, tr: Throwable?) {
        if (BuildConfig.DEBUG) {
            // No problem with lint in here
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
            return javaClass.name
        } else {
            return javaClass.simpleName // returns "" if the underlying class is anonymo
        }
    }