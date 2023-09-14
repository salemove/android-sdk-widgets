package com.glia.widgets.chat

import com.glia.widgets.StringProvider
import com.glia.widgets.helper.ResourceProvider

class SnapshotStringProvider(private val resourceProvider: ResourceProvider): StringProvider {

    override fun getRemoteString(stringKey: Int, vararg values: String?): String {
        return resourceProvider.getResourceKey(stringKey)
    }
}
