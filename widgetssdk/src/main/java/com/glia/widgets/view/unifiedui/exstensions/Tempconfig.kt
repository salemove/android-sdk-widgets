package com.glia.widgets.view.unifiedui.exstensions

import android.content.Context
import androidx.annotation.RawRes

//TODO Temporary. Ticket - https://glia.atlassian.net/browse/MOB-1657
fun Context.readRaw(@RawRes resourceId: Int): String {
    return resources.openRawResource(resourceId).bufferedReader(Charsets.UTF_8)
        .use { it.readText() }
}