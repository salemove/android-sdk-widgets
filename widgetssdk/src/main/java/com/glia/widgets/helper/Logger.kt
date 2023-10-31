package com.glia.widgets.helper

import android.util.Log
import com.glia.androidsdk.Glia
import com.glia.widgets.BuildConfig
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

object Logger {

    private var coreSdkLogger: com.glia.androidsdk.internal.logger.Logger? = null
    private var coreSdkLogDFunction: Method? = null
    private var coreSDKLogEFunction: Method? = null

    @Suppress("unused")
    @JvmStatic
    fun setCoreSdkLogger(coreSdkLogger: com.glia.androidsdk.internal.logger.Logger) {
        d(TAG, "Core SDK logger set successfully")
        this.coreSdkLogger = coreSdkLogger
    }

    @JvmStatic
    fun getCoreSdkLogger() : com.glia.androidsdk.internal.logger.Logger? = coreSdkLogger

    @JvmStatic
    fun d(tag: String, message: String?) {
        if (BuildConfig.DEBUG) {
            // No need to log an empty message
            logDToCoreSdk(tag, message ?: return)
        }
    }

    @JvmStatic
    fun e(tag: String, message: String?) {
        if (BuildConfig.DEBUG) {
            //No need to log an empty message
            logEToCoreSdk(tag, message ?: return)
        }
    }

    private fun logDToCoreSdk(tag: String, message: String?) {
        if (!Glia.isInitialized()) return
        if (message == null) return

        if (coreSdkLogDFunction == null) {
            try {
                coreSdkLogDFunction = getCoreSdkLogger()?.javaClass?.getDeclaredMethod(
                    "d",
                    String::class.java,
                    String::class.java
                )
            } catch (e: IllegalAccessException) {
                Log.d(TAG, "Unable to access Core SDK logger function $e")
            } catch (e: InvocationTargetException) {
                Log.d(TAG, "Unable to invoke Core SDK logger function $e")
            } catch (e: NoSuchMethodException) {
                Log.d(TAG, "Unable to find Core SDK logger function $e")
            }
        }

        coreSdkLogDFunction?.invoke(null, tag, message)
    }

    private fun logEToCoreSdk(tag: String, message: String?, throwable: Throwable? = null) {
        if (!Glia.isInitialized()) return
        if (message == null) return

        if (coreSDKLogEFunction == null) {
            try {
                coreSDKLogEFunction = getCoreSdkLogger()?.javaClass?.getDeclaredMethod(
                    "e",
                    String::class.java,
                    String::class.java,
                    Throwable::class.java
                )
            } catch (e: IllegalAccessException) {
                Log.d(TAG, "Unable to log message using Core SDK logger $e")
            } catch (e: InvocationTargetException) {
                Log.d(TAG, "Unable to log message using Core SDK logger $e")
            } catch (e: NoSuchMethodException) {
                Log.d(TAG, "Unable to log message using Core SDK logger $e")
            }
        }

        coreSDKLogEFunction?.invoke(null, tag, message, throwable)
    }

    @JvmStatic
    fun e(tag: String, message: String?, tr: Throwable?) {
        if (BuildConfig.DEBUG) {
            // No problem with lint in here
            if (message.isNullOrBlank() && tr == null) return
            logEToCoreSdk(tag, message, tr)
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
