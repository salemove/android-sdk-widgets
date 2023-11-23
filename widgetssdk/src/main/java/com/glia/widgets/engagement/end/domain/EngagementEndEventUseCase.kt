package com.glia.widgets.engagement.end.domain

import com.glia.androidsdk.Engagement
import com.glia.widgets.engagement.EngagementDataSource
import io.reactivex.Flowable

internal class EngagementEndEventUseCase(
    private val dataSource: EngagementDataSource,
    private val newEngagementUseCase: NewEngagementUseCase
) {
    operator fun invoke(): Flowable<Engagement> = newEngagementUseCase().switchMapSingle(dataSource::subscribeToEngagementEnd)
}
