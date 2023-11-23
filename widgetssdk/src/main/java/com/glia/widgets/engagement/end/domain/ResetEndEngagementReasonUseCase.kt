package com.glia.widgets.engagement.end.domain

import com.glia.widgets.engagement.end.EndEngagement
import com.glia.widgets.engagement.end.EngagementEndReasonDataSource

internal class ResetEndEngagementReasonUseCase(
    private val dataSource: EngagementEndReasonDataSource
) {
    operator fun invoke() {
        dataSource endBy EndEngagement.Reason.OPERATOR
    }
}
