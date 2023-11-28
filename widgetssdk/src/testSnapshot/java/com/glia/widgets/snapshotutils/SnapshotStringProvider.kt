package com.glia.widgets.snapshotutils

import android.content.Context
import com.glia.widgets.StringKeyPair
import com.glia.widgets.StringProvider
import java.lang.Exception

class SnapshotStringProvider(private val context: Context) : StringProvider {

    override fun getRemoteString(stringKey: Int, vararg values: StringKeyPair?): String {
        return context.resources.getResourceName(stringKey).split("/")[1]
    }

    override fun reportImproperInitialisation(exception: Exception) {
        // no-op
    }
}
