package com.glia.widgets

import android.content.Context
import java.lang.Exception

class AndroidTestStringProvider(private val context: Context) : StringProvider {

    override fun getRemoteString(stringKey: Int, vararg values: StringKeyPair?): String {
        val vals = values.map { pair -> pair?.value }.toTypedArray()
        return context.getString(stringKey, *vals)
    }

    override fun reportImproperInitialisation(exception: Exception) {
        // no-op
    }
}
