package com.glia.widgets.call

import com.glia.androidsdk.Engagement
import com.glia.widgets.core.configuration.GliaSdkConfiguration
import com.glia.widgets.helper.Utils

internal class Configuration private constructor(builder: Builder) {
    @JvmField
    val sdkConfiguration: GliaSdkConfiguration?
    @JvmField
    val mediaType: Engagement.MediaType?
    @JvmField
    val isUpgradeToCall: Boolean

    init {
        sdkConfiguration = builder.widgetsConfiguration
        mediaType = builder.mediaType
        isUpgradeToCall = builder.isUpgradeToCall
    }

    class Builder {
        var widgetsConfiguration: GliaSdkConfiguration? = null
            private set
        var mediaType: Engagement.MediaType? = null
            private set
        var isUpgradeToCall = false
            private set

        fun setWidgetsConfiguration(configuration: GliaSdkConfiguration?) = apply {
            widgetsConfiguration = configuration
        }

        fun setMediaType(mediaType: Engagement.MediaType?) = apply {
            this.mediaType = mediaType
        }

        fun setMediaType(mediaType: String) = apply {
            this.mediaType = Utils.toMediaType(mediaType)
        }

        fun setIsUpgradeToCall(isUpgradeToCall: Boolean) = apply {
            this.isUpgradeToCall = isUpgradeToCall
        }


        fun build(): Configuration {
            return Configuration(this)
        }
    }
}
