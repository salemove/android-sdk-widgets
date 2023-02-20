package com.glia.widgets.core.engagement.domain

import com.glia.androidsdk.Engagement
import com.glia.androidsdk.Glia
import com.glia.androidsdk.omnibrowse.OmnibrowseEngagement


class IsCallVisualizerUseCase {
    fun execute(): Boolean {
        return Glia.getCurrentEngagement()
            .filter { engagement: Engagement -> engagement is OmnibrowseEngagement }
            .isPresent
    }
}
