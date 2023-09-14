package com.glia.widgets.chat

import android.content.Context
import com.glia.widgets.StringProvider

class SnapshotStringProvider(private val context: Context): StringProvider {

    override fun getRemoteString(stringKey: Int, vararg values: String?): String {
        return context.resources.getResourceName(stringKey).split("/")[1]
    }
}
