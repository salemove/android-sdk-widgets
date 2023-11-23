package com.glia.widgets.engagement.end.domain

import com.glia.widgets.engagement.end.EndEngagement
import com.glia.widgets.engagement.end.EngagementEndReasonDataSource

internal class LocallyEndEngagementSilentlyUseCase(
    private val dataSource: EngagementEndReasonDataSource,
    private val endEngagementUseCase: EndEngagementUseCase,
    private val destroyControllersUseCase: DestroyControllersUseCase
) {
    operator fun invoke() {
        dataSource endBy EndEngagement.Reason.VISITOR_SILENTLY
        destroyControllersUseCase()
        endEngagementUseCase()
    }
}
