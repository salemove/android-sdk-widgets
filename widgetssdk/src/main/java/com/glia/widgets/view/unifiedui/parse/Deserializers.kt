package com.glia.widgets.view.unifiedui.parse

import android.graphics.Typeface
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.ResourceProvider
import com.glia.widgets.view.unifiedui.config.alert.AxisRemoteConfig
import com.glia.widgets.view.unifiedui.config.base.AlignmentTypeRemoteConfig
import com.glia.widgets.view.unifiedui.config.base.ColorLayerRemoteConfig
import com.glia.widgets.view.unifiedui.config.base.ColorRemoteConfig
import com.glia.widgets.view.unifiedui.config.base.ColorTypeRemoteConfig
import com.glia.widgets.view.unifiedui.config.base.SizeDpRemoteConfig
import com.glia.widgets.view.unifiedui.config.base.SizeSpRemoteConfig
import com.glia.widgets.view.unifiedui.config.base.TextStyleRemoteConfig
import com.glia.widgets.view.unifiedui.config.chat.AttachmentSourceTypeRemoteConfig
import com.glia.widgets.view.unifiedui.parse.TextStyleDeserializer.Companion.BOLD
import com.glia.widgets.view.unifiedui.parse.TextStyleDeserializer.Companion.BOLD_ITALIC
import com.glia.widgets.view.unifiedui.parse.TextStyleDeserializer.Companion.ITALIC
import com.glia.widgets.view.unifiedui.parse.TextStyleDeserializer.Companion.REGULAR
import com.google.gson.JsonArray
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type
import android.graphics.Color as SystemColor

private const val TAG = "UnifiedUi:Deserializers"

internal fun <T> tryOrNull(
    onError: ((Exception) -> Unit)? = { Logger.e(TAG, "Skipping", it) },
    block: () -> T?
): T? = try {
    block()
} catch (ex: Exception) {
    onError?.invoke(ex)
    null
}

/**
 * Deserializes textStyle property from remote config
 * returns null if property differs from [AlignmentTypeRemoteConfig.type]
 */
internal class AlignmentDeserializer : JsonDeserializer<AlignmentTypeRemoteConfig?> {

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): AlignmentTypeRemoteConfig? = tryOrNull {
        AlignmentTypeRemoteConfig.values().firstOrNull { it.type == json.asString }
    }
}

/**
 * Json deserializer for ARGB color, will return `null` if color is not valid and [ColorRemoteConfig] for other cases
 */
internal class ColorDeserializer : JsonDeserializer<ColorRemoteConfig?> {

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): ColorRemoteConfig? = tryOrNull(onError = {
        Logger.w(TAG, "ColorDeserializer IllegalArgumentException(${it.message}: -> $json)")
    }) {
        ColorRemoteConfig(SystemColor.parseColor(json.asString))
    }
}

/**
 * Deserializes Color from remote config
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
 * to [ColorLayerRemoteConfig]
 *
 * will return `null` if "value" property is missing or empty
 * will change [ColorLayerRemoteConfig.type] to [ColorTypeRemoteConfig.FILL] if "value" property contains single color.
 *
 * @see ColorLayerDeserializer
 */
internal class ColorLayerDeserializer : JsonDeserializer<ColorLayerRemoteConfig?> {
    private val colorDeserializer = ColorDeserializer()

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): ColorLayerRemoteConfig? = tryOrNull {
        val root = json.asJsonObject

        val colors =
            parseColors(root[VALUE_KEY]?.asJsonArray, typeOfT, context) ?: return@tryOrNull null

        val type = if (colors.size == 1) {
            ColorTypeRemoteConfig.FILL
        } else {
            parseType(root[TYPE_KEY])
        }

        return@tryOrNull ColorLayerRemoteConfig(type, colors)
    }

    private fun parseColors(
        valuesArray: JsonArray?,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): List<ColorRemoteConfig>? = valuesArray?.mapNotNull {
        colorDeserializer.deserialize(it, typeOfT, context)
    }?.takeIf {
        it.isNotEmpty()
    }

    private fun parseType(element: JsonElement): ColorTypeRemoteConfig = tryOrNull {
        ColorTypeRemoteConfig.values().firstOrNull { it.type == element.asString }
    } ?: ColorTypeRemoteConfig.FILL

    companion object {
        const val TYPE_KEY = "type"
        const val VALUE_KEY = "value"
    }
}

/**
 * Deserializes textStyle property from remote config
 * returns null if property differs from [REGULAR], [BOLD], [ITALIC], [BOLD_ITALIC]
 */
internal class TextStyleDeserializer : JsonDeserializer<TextStyleRemoteConfig?> {

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): TextStyleRemoteConfig? = tryOrNull {
        val typeface = when (json.asString) {
            BOLD -> Typeface.BOLD
            ITALIC -> Typeface.ITALIC
            BOLD_ITALIC -> Typeface.BOLD_ITALIC
            REGULAR -> Typeface.NORMAL
            else -> return@tryOrNull null
        }

        TextStyleRemoteConfig(typeface)
    }

    companion object {
        private const val REGULAR = "regular"
        private const val BOLD = "bold"
        private const val ITALIC = "italic"
        private const val BOLD_ITALIC = "bold_italic"
    }
}

internal class DpDeserializer(private val resourceProvider: ResourceProvider) :
    JsonDeserializer<SizeDpRemoteConfig?> {

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): SizeDpRemoteConfig? = tryOrNull {
        val sizePx = json.asFloat.takeIf { it >= 0 }?.run(resourceProvider::convertDpToPixel)
            ?: return@tryOrNull null

        SizeDpRemoteConfig(sizePx)
    }
}

internal class SpDeserializer : JsonDeserializer<SizeSpRemoteConfig?> {

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): SizeSpRemoteConfig? = tryOrNull {
        val sizeSp = json.asFloat.takeIf { it >= 0 } ?: return@tryOrNull null

        SizeSpRemoteConfig(sizeSp)
    }
}

/**
 * Deserializes AttachmentSourceType property from remote config
 * returns null if property differs from [com.glia.widgets.view.unifiedui.config.chat.AttachmentSourceTypeRemoteConfig.value]
 */
internal class AttachmentSourceTypeDeserializer :
    JsonDeserializer<AttachmentSourceTypeRemoteConfig?> {

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): AttachmentSourceTypeRemoteConfig? = tryOrNull {
        AttachmentSourceTypeRemoteConfig.values().firstOrNull { it.value == json.asString }
    }
}

/**
 * Deserializes Axis property from remote config
 * returns null if property differs from [com.glia.widgets.view.unifiedui.config.alert.AxisRemoteConfig.value]
 */
internal class AxisDeserializer : JsonDeserializer<AxisRemoteConfig?> {

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): AxisRemoteConfig? = tryOrNull {
        AxisRemoteConfig.values().firstOrNull { it.value == json.asString }
    }
}
