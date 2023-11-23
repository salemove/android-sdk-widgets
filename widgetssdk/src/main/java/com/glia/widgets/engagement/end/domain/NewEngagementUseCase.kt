package com.glia.widgets.engagement.end.domain

import com.glia.androidsdk.Engagement
import com.glia.widgets.engagement.EngagementDataSource
import io.reactivex.Flowable

internal class NewEngagementUseCase(private val engagementDataSource: EngagementDataSource) {
    operator fun invoke(): Flowable<Engagement> = engagementDataSource.subscribeToEngagementStart()
}
