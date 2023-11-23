package com.glia.widgets.engagement.end.domain

import com.glia.androidsdk.engagement.EngagementState
import com.glia.widgets.engagement.EngagementDataSource
import io.reactivex.Flowable

internal class EngagementStateUseCase(
    private val dataSource: EngagementDataSource,
    private val newEngagementUseCase: NewEngagementUseCase
) {
    operator fun invoke(): Flowable<EngagementState> = newEngagementUseCase().switchMap(dataSource::subscribeToEngagementState)
}
