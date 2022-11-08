package com.glia.widgets.view.unifiedui.parse

import com.glia.widgets.di.Dependencies
import com.glia.widgets.view.unifiedui.config.RemoteConfiguration
import com.glia.widgets.view.unifiedui.config.alert.AxisRemoteConfig
import com.glia.widgets.view.unifiedui.config.base.*
import com.glia.widgets.view.unifiedui.config.chat.AttachmentSourceTypeRemoteConfig
import com.google.gson.Gson
import com.google.gson.GsonBuilder

internal object RemoteConfigurationParser {
    /**
     * @return [Gson] instance with applied deserializers to parse remote config.
     */
    val defaultGson: Gson by lazy {
        GsonBuilder()
            .registerTypeAdapter(ColorLayerRemoteConfig::class.java, ColorLayerDeserializer())
            .registerTypeAdapter(
                SizeDpRemoteConfig::class.java, DpDeserializer(Dependencies.getResourceProvider())
            )
            .registerTypeAdapter(SizeSpRemoteConfig::class.java, SpDeserializer())
            .registerTypeAdapter(TextStyleRemoteConfig::class.java, TextStyleDeserializer())
            .registerTypeAdapter(AlignmentTypeRemoteConfig::class.java, AlignmentDeserializer())
            .registerTypeAdapter(AxisRemoteConfig::class.java, AxisDeserializer())
            .registerTypeAdapter(
                AttachmentSourceTypeRemoteConfig::class.java, AttachmentSourceTypeDeserializer()
            )
            .create()
    }

    @JvmStatic
    inline fun <reified T> parse(json: String): T = defaultGson.fromJson(json, T::class.java)

    @JvmStatic
    fun parseRemoteConfiguration(remoteConfiguration: String): RemoteConfiguration? =
        parse(remoteConfiguration)
}