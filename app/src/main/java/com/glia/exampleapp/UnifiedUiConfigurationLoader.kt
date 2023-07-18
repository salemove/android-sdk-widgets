package com.glia.exampleapp

import android.content.Context
import android.util.Log
import androidx.annotation.RawRes
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.MessageLengthLimitingLogger
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.runBlocking
import java.io.BufferedReader

private fun Context.rawRes(@RawRes resId: Int): String = resources.openRawResource(resId).use {
    BufferedReader(it.reader()).readText()
}

object UnifiedUiConfigurationLoader {

    private const val TAG = "Ktor:Client"

    private val androidLogger: Logger = object : Logger {
        override fun log(message: String) {
            Log.d(TAG, message)
        }
    }

    private val defaultUrl: String by lazy {
        "https://raw.githubusercontent.com/DavDo/json_config_test/main/colors.json"
    }

    private val ktorClient: HttpClient by lazy {
        HttpClient(Android) {
            Logging {
                logger = MessageLengthLimitingLogger(delegate = androidLogger)
                level = LogLevel.ALL
            }
        }
    }

    // No error handling mechanisms because this is only for testing purposes
    @JvmOverloads
    @JvmStatic
    fun fetchRemoteConfiguration(url: String? = null): String = runBlocking {
        ktorClient.get(url ?: defaultUrl).bodyAsText()
    }

    @JvmStatic
    fun fetchLocalConfiguration(context: Context, @RawRes resId: Int): String =
        context.rawRes(resId)

    @JvmStatic
    fun fetchLocalGlobalColors(context: Context): String = fetchLocalConfiguration(context, R.raw.global_colors_unified_config)

    @JvmStatic
    fun fetchLocalConfigSample(context: Context): String = fetchLocalConfiguration(context, R.raw.sample_unified_config)
}
