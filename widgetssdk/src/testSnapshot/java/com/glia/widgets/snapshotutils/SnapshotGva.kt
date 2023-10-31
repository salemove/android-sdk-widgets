package com.glia.widgets.snapshotutils

import com.glia.widgets.R
import com.glia.widgets.view.unifiedui.theme.UnifiedTheme
import com.google.gson.JsonObject

internal interface SnapshotGva : SnapshotTheme {

    fun gvaLongTitle() = "\uD83D\uDE80 Lorem ipsum dolor sit amet, consectetur adipiscing elit. " +
        "Etiam at interdum nisi. Nullam sem urna, vehicula eget metus vel."

    fun gvaLongSubtitle() = "<b>Quisque ut sollicitudin augue \uD83D\uDC40</b><br>" +
        "Ut lobortis sit amet neque nec <i>gravida</i>:" +
        "<ul><li>Sed risus</li><li>Maecenas</li></ul>" +
        "Nam commodo ligula non justo semper pellentesque. \uD83D\uDE4C"

    fun unifiedThemeWithoutGva(): UnifiedTheme = unifiedTheme(R.raw.test_unified_config) { unifiedTheme ->
        unifiedTheme.add(
            "chatScreen",
            (unifiedTheme.remove("chatScreen") as JsonObject).also {
                it.remove("gva")
            }
        )
    }
}
