package com.glia.exampleapp.data.model

import androidx.compose.ui.graphics.Color
import org.json.JSONObject

/**
 * Predefined color palette matching iOS and current Android app
 */
enum class PredefinedColor(val displayName: String, val color: Color, val hexValue: String) {
    DEFAULT("Default", Color.Transparent, ""),
    BLUE("Blue", Color(0xFF4DD0E1), "#FF4DD0E1"),
    GREY("Grey", Color(0xFF607D8B), "#FF607D8B"),
    RED("Red", Color(0xFFFF1744), "#FFFF1744"),
    BLACK("Black", Color(0xFF000000), "#FF000000"),
    WHITE("White", Color(0xFFFFFFFF), "#FFFFFFFF"),
    DARK_GRAYISH_BLUE("Dark Grayish Blue", Color(0xFF6C7683), "#FF6C7683"),
    PURE_YELLOW("Pure Yellow", Color(0xFFFECD00), "#FFFFECD00"),
    DARK_CYAN("Dark Cyan", Color(0xFF00748B), "#FF00748B"),
    VERY_DARK_BLUE("Very Dark Blue", Color(0xFF1A1446), "#FF1A1446"),
    VERY_DARK_GRAYISH_BLUE("Very Dark Grayish Blue", Color(0xFF333741), "#FF333741"),
    VERY_LIGHT_GRAY("Very Light Gray", Color(0xFFE6E6E6), "#FFE6E6E6");

    companion object {
        fun fromName(name: String): PredefinedColor =
            entries.find { it.name == name } ?: DEFAULT
    }
}

data class ThemeColors(
    val primary: PredefinedColor = PredefinedColor.DEFAULT,
    val secondary: PredefinedColor = PredefinedColor.DEFAULT,
    val baseNormal: PredefinedColor = PredefinedColor.DEFAULT,
    val baseLight: PredefinedColor = PredefinedColor.DEFAULT,
    val baseDark: PredefinedColor = PredefinedColor.DEFAULT,
    val baseShade: PredefinedColor = PredefinedColor.DEFAULT,
    val background: PredefinedColor = PredefinedColor.DEFAULT,
    val systemNegative: PredefinedColor = PredefinedColor.DEFAULT
) {
    fun toJsonString(): String? {
        if (allDefault()) return null

        val globalColors = buildMap {
            if (primary != PredefinedColor.DEFAULT) put("primary", primary.hexValue)
            if (secondary != PredefinedColor.DEFAULT) put("secondary", secondary.hexValue)
            if (baseNormal != PredefinedColor.DEFAULT) put("baseNormal", baseNormal.hexValue)
            if (baseLight != PredefinedColor.DEFAULT) put("baseLight", baseLight.hexValue)
            if (baseDark != PredefinedColor.DEFAULT) put("baseDark", baseDark.hexValue)
            if (baseShade != PredefinedColor.DEFAULT) put("baseShade", baseShade.hexValue)
            if (background != PredefinedColor.DEFAULT) put("background", background.hexValue)
            if (systemNegative != PredefinedColor.DEFAULT) put("systemNegative", systemNegative.hexValue)
        }

        return JSONObject()
            .put("globalColors", JSONObject(globalColors))
            .toString()
    }

    private fun allDefault(): Boolean = listOf(
        primary, secondary, baseNormal, baseLight, baseDark, baseShade, background, systemNegative
    ).all { it == PredefinedColor.DEFAULT }
}
