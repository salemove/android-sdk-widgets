package com.glia.widgets

import com.glia.widgets.helper.ResourceProvider

class StringProviderImpl(private val resourceProvider: ResourceProvider): StringProvider {

    override fun getRemoteString(stringKey: Int, vararg values: StringKeyPair?): String {
        // TODO IMPLEMENT NEW METHOD FROM CORE
        val key = resourceProvider.getResourceKey(stringKey)
        val vals = values.map { pair -> pair?.value }.toTypedArray()
        val fallback = resourceProvider.getString(stringKey, *vals)
        // TODO IMPLEMENT NEW METHOD FROM CORE
        return fallback
    }
}
