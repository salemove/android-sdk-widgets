package com.glia.widgets.call

import com.glia.androidsdk.Engagement
import com.glia.widgets.core.configuration.EngagementConfiguration

internal data class CallConfiguration @JvmOverloads constructor(
    @JvmField
    val engagementConfiguration: EngagementConfiguration?,
    @JvmField
    val mediaType: Engagement.MediaType? = null,
    @JvmField
    val isUpgradeToCall: Boolean = false
)
