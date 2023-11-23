package com.glia.widgets.engagement.end.domain

import com.glia.widgets.di.GliaCore
import com.glia.widgets.engagement.EngagementDataSource

internal class EndEngagementUseCase(
    private val dataSource: EngagementDataSource,
    private val core: GliaCore
) {
    operator fun invoke() = core.currentEngagement.ifPresent {
        dataSource end it
    }
}
