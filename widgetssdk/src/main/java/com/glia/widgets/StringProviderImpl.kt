package com.glia.widgets

import com.glia.widgets.helper.ResourceProvider
import com.glia.androidsdk.Glia
import com.glia.androidsdk.GliaException
import com.glia.widgets.helper.Logger
import java.lang.Exception

internal class StringProviderImpl(private val resourceProvider: ResourceProvider) : StringProvider {

    private val regex = "(\\{[a-zA-Z\\d]*\\})".toRegex()
    private var shouldLog = true

    override fun getRemoteString(stringKey: Int, vararg values: StringKeyPair?): String {
        val key = resourceProvider.getResourceKey(stringKey)
        return try {
            Glia.getRemoteString(key)?.let {
                var returnValue = it
                values.forEach { pair ->
                    pair?.run {
                        returnValue = replaceInsertedValues(returnValue, this)
                    }
                }
                cleanupRemainingReferences(returnValue)
            } ?: kotlin.run {
                resourceProvider.getString(stringKey, *values.map { pair -> pair?.value }.toTypedArray())
            }
        } catch (e: GliaException) {
            reportImproperInitialisation(e)
            resourceProvider.getString(stringKey, *values.map { pair -> pair?.value }.toTypedArray())
        }
    }

    override fun reportImproperInitialisation(exception: Exception) {
        // Logging will be performed only once per app lifecycle
        if (shouldLog) {
            shouldLog = false
            Logger.e(
                "StringProvider",
                "**** ATTENTION **** \n " +
                    "An engagement view was opened immediately after Glia was initialized. \n " +
                    "It is strongly suggested to keep the initialization and actual engagement " +
                    "start separated by a little more time to allow custom locales feature to work properly.\n " +
                    "For further information See the  Custom Locales migration guide",
                exception
            )
        }
    }

    private fun replaceInsertedValues(string: String, pair: StringKeyPair): String {
        return if (string.contains(pair.key.value, false)) {
            string.replace("{${pair.key.value}}", pair.value)
        } else {
            string
        }
    }

    private fun cleanupRemainingReferences(string: String): String {
        return regex.replace(string, "")
    }
}
