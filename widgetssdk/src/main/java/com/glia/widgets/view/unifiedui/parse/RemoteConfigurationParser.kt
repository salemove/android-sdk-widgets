package com.glia.widgets.view.unifiedui.parse

import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.ResourceProvider
import com.glia.widgets.view.unifiedui.config.RemoteConfiguration
import com.glia.widgets.view.unifiedui.config.alert.AxisRemoteConfig
import com.glia.widgets.view.unifiedui.config.base.AlignmentTypeRemoteConfig
import com.glia.widgets.view.unifiedui.config.base.ColorLayerRemoteConfig
import com.glia.widgets.view.unifiedui.config.base.ColorRemoteConfig
import com.glia.widgets.view.unifiedui.config.base.SizeDpRemoteConfig
import com.glia.widgets.view.unifiedui.config.base.SizeSpRemoteConfig
import com.glia.widgets.view.unifiedui.config.base.TextStyleRemoteConfig
import com.glia.widgets.view.unifiedui.config.chat.AttachmentSourceTypeRemoteConfig
import com.google.gson.Gson
import com.google.gson.GsonBuilder

internal class RemoteConfigurationParser(
    resourceProvider: ResourceProvider = Dependencies.getResourceProvider()
) {
    /**
     * @return [Gson] instance with applied deserializers to parse remote config.
     */
    val defaultGson: Gson by lazy {
        GsonBuilder()
            .registerTypeAdapter(ColorRemoteConfig::class.java, ColorDeserializer())
            .registerTypeAdapter(ColorLayerRemoteConfig::class.java, ColorLayerDeserializer())
            .registerTypeAdapter(
                SizeDpRemoteConfig::class.java,
                DpDeserializer(resourceProvider)
            )
            .registerTypeAdapter(SizeSpRemoteConfig::class.java, SpDeserializer())
            .registerTypeAdapter(TextStyleRemoteConfig::class.java, TextStyleDeserializer())
            .registerTypeAdapter(AlignmentTypeRemoteConfig::class.java, AlignmentDeserializer())
            .registerTypeAdapter(AxisRemoteConfig::class.java, AxisDeserializer())
            .registerTypeAdapter(
                AttachmentSourceTypeRemoteConfig::class.java,
                AttachmentSourceTypeDeserializer()
            )
            .create()
    }

    inline fun <reified T> parse(json: String): T = defaultGson.fromJson(json, T::class.java)

    fun parseRemoteConfiguration(remoteConfiguration: String): RemoteConfiguration? =
        tryOrNull { parse(remoteConfiguration) }
}
