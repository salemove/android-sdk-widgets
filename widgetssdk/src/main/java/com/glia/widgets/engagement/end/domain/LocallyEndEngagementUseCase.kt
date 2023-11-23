package com.glia.widgets.engagement.end.domain

import com.glia.widgets.engagement.end.EndEngagement
import com.glia.widgets.engagement.end.EngagementEndReasonDataSource

internal class LocallyEndEngagementUseCase(
    private val dataSource: EngagementEndReasonDataSource,
    private val endEngagementUseCase: EndEngagementUseCase,
    private val destroyControllersUseCase: DestroyControllersUseCase
) {
    operator fun invoke() {
        dataSource endBy EndEngagement.Reason.VISITOR
        destroyControllersUseCase()
        endEngagementUseCase()
    }
}
