package com.glia.widgets.snapshotutils

import androidx.annotation.RawRes
import com.glia.widgets.R
import com.glia.widgets.helper.ResourceProvider
import com.glia.widgets.view.unifiedui.config.RemoteConfiguration
import com.glia.widgets.view.unifiedui.parse.RemoteConfigurationParser
import com.glia.widgets.view.unifiedui.theme.UnifiedTheme
import com.glia.widgets.view.unifiedui.theme.alert.AlertTheme
import com.google.gson.Gson
import com.google.gson.JsonObject

internal interface SnapshotTheme : SnapshotContent {

    fun unifiedTheme(json: String): UnifiedTheme {
        val resourceProvider = ResourceProvider(context)
        return RemoteConfigurationParser(resourceProvider).defaultGson
            .fromJson(json, RemoteConfiguration::class.java).toUnifiedTheme()!!
    }

    fun unifiedTheme(@RawRes resId: Int, modifier: ((JsonObject) -> Unit)? = null): UnifiedTheme {
        val jsonRaw = rawRes(resId)
        if (modifier != null) {
            val jsonObject = Gson().fromJson(rawRes(R.raw.test_unified_config), JsonObject::class.java)
            modifier(jsonObject)
            return unifiedTheme(jsonObject.toString())
        }
        return unifiedTheme(jsonRaw)
    }

    fun unifiedThemeWithGlobalColors(): UnifiedTheme = unifiedTheme(R.raw.global_colors_unified_config).run {
        copy(alertTheme = alertTheme?.copy(isVerticalAxis = true) ?: AlertTheme(isVerticalAxis = true))
    }

    fun unifiedTheme(): UnifiedTheme = unifiedTheme(R.raw.test_unified_config)
}
