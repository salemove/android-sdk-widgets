package com.glia.widgets

import com.glia.widgets.helper.ResourceProvider

class StringProviderImpl(private val resourceProvider: ResourceProvider): StringProvider {

    override fun getRemoteString(stringKey: Int, vararg values: String): String {
        // TODO IMPLEMENT NEW METHOD FROM CORE
        val fallback = resourceProvider.getString(stringKey, values)
        val key = resourceProvider.getResourceKey(stringKey)
        // TODO IMPLEMENT NEW METHOD FROM CORE
        return key
    }
}
