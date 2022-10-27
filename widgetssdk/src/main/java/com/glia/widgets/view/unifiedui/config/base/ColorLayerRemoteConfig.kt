package com.glia.widgets.view.unifiedui.config.base

import android.os.Parcelable
import androidx.annotation.ColorInt
import com.glia.widgets.view.unifiedui.parse.ColorLayerDeserializer
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

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
@Parcelize
internal data class ColorLayerRemoteConfig(
    @SerializedName(ColorLayerDeserializer.TYPE_KEY)
    val type: ColorTypeRemoteConfig,

    @SerializedName(ColorLayerDeserializer.VALUE_KEY)
    val values: List<ColorRemoteConfig>
) : Parcelable {

    val isGradient: Boolean
        get() = type != ColorTypeRemoteConfig.FILL

    @get:ColorInt
    val primaryColor: Int
        get() = values.first().color

    val valuesExpanded: List<Int>
        get() = values.map(ColorRemoteConfig::color)

}