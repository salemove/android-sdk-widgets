package com.glia.exampleapp.data.model

import com.glia.widgets.Region

enum class EnvironmentSelection(val displayName: String) {
    BETA("Beta"),
    US("US"),
    EU("EU"),
    CUSTOM("Custom");

    fun toRegion(customUrl: String? = null): Region = when (this) {
        BETA -> Region.Beta
        US -> Region.US
        EU -> Region.EU
        CUSTOM -> Region.Custom(customUrl ?: "")
    }

    companion object {
        fun fromRegion(region: Region): EnvironmentSelection = when (region) {
            is Region.Beta -> BETA
            is Region.US -> US
            is Region.EU -> EU
            is Region.Custom -> CUSTOM
        }

        fun fromString(value: String): EnvironmentSelection = when (value.lowercase()) {
            "beta" -> BETA
            "us" -> US
            "eu" -> EU
            "custom" -> CUSTOM
            else -> BETA
        }
    }
}
