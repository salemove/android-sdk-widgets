package com.glia.widgets.view.unifiedui.config.base

import androidx.annotation.ColorInt
import com.glia.widgets.view.unifiedui.parse.ColorLayerDeserializer
import com.glia.widgets.view.unifiedui.theme.base.ColorTheme
import com.google.gson.annotations.SerializedName

/**
 * Represents Color from remote config
 * `
 * {
 * "type": "gradient",
 * "value": [
 * "#FF4433DD",
 * "#AA4433DD"
 * ]
 * }
` *
 *
 *
 * Guarantees that at least 1 [ColorRemoteConfig] will present in [.values]
 *
 * @see ColorLayerDeserializer
 */
internal data class ColorLayerRemoteConfig(
    @SerializedName(ColorLayerDeserializer.TYPE_KEY)
    val type: ColorTypeRemoteConfig,

    @SerializedName(ColorLayerDeserializer.VALUE_KEY)
    val values: List<ColorRemoteConfig>
) {

    private val isGradient: Boolean
        get() = type != ColorTypeRemoteConfig.FILL

    private val valuesExpanded: List<Int>
        get() = values.map(ColorRemoteConfig::color)

    @get:ColorInt
    val primaryColor: Int
        get() = values.first().color

    fun toColorTheme(): ColorTheme = ColorTheme(isGradient = isGradient, values = valuesExpanded)
}
