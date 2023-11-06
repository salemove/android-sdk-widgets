package com.glia.widgets.snapshotutils

import com.glia.widgets.R
import com.glia.widgets.view.unifiedui.theme.UnifiedTheme
import com.google.gson.JsonObject

internal interface SnapshotChatScreen : SnapshotTheme {
    fun unifiedThemeWithoutVisitorMessage(): UnifiedTheme = unifiedTheme(R.raw.test_unified_config) { unifiedTheme ->
        unifiedTheme.add(
            "chatScreen",
            (unifiedTheme.remove("chatScreen") as JsonObject).also {
                it.remove("visitorMessage")
            }
        )
    }
}
