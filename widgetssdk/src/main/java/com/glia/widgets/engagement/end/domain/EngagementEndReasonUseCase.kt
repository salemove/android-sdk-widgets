package com.glia.widgets.engagement.end.domain

import com.glia.widgets.engagement.end.EndEngagement
import com.glia.widgets.engagement.end.EngagementEndReasonDataSource
import io.reactivex.Flowable

internal class EngagementEndReasonUseCase(
    private val dataSource: EngagementEndReasonDataSource
) {
    operator fun invoke(): Flowable<EndEngagement.Reason> = dataSource.engagementEndReason
}
