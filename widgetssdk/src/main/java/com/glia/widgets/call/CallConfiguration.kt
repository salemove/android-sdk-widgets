package com.glia.widgets.call

import com.glia.androidsdk.Engagement
import com.glia.widgets.core.configuration.EngagementConfiguration
import com.glia.widgets.helper.Utils

internal class CallConfiguration private constructor(builder: Builder) {
    @JvmField
    val engagementConfiguration: EngagementConfiguration?
    @JvmField
    val mediaType: Engagement.MediaType?
    @JvmField
    val isUpgradeToCall: Boolean

    init {
        engagementConfiguration = builder.engagementConfiguration
        mediaType = builder.mediaType
        isUpgradeToCall = builder.isUpgradeToCall
    }

    class Builder {
        var engagementConfiguration: EngagementConfiguration? = null
            private set
        var mediaType: Engagement.MediaType? = null
            private set
        var isUpgradeToCall = false
            private set

        fun setEngagementConfiguration(engagementConfiguration: EngagementConfiguration?) = apply {
            this.engagementConfiguration = engagementConfiguration
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


        fun build(): CallConfiguration {
            return CallConfiguration(this)
        }
    }
}
