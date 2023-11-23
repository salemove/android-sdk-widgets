package com.glia.widgets.engagement.end

import io.reactivex.Flowable
import io.reactivex.processors.BehaviorProcessor

internal interface EngagementEndReasonDataSource {
    val engagementEndReason: Flowable<EndEngagement.Reason>
    infix fun endBy(reason: EndEngagement.Reason)
}

internal class EngagementEndReasonDataSourceImpl(
    private val _engagementEndReason: BehaviorProcessor<EndEngagement.Reason> = BehaviorProcessor.createDefault(EndEngagement.Reason.OPERATOR)
) : EngagementEndReasonDataSource {
    override val engagementEndReason: Flowable<EndEngagement.Reason> get() = _engagementEndReason.share()
    override infix fun endBy(reason: EndEngagement.Reason) {
        _engagementEndReason.onNext(reason)
    }

}
