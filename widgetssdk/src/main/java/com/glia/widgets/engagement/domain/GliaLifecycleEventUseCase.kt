package com.glia.widgets.engagement.domain

import com.glia.widgets.engagement.MediaType
import com.glia.widgets.engagement.State
import com.glia.widgets.lifecycle.GliaEvent
import io.reactivex.rxjava3.core.Flowable

internal interface GliaLifecycleEventUseCase {
    operator fun invoke(): Flowable<GliaEvent>
}

internal class GliaLifecycleEventUseCaseImpl(
    private val engagementStateUseCase: EngagementStateUseCase,
    private val engagementTypeUseCase: EngagementTypeUseCase
) : GliaLifecycleEventUseCase {

    override fun invoke(): Flowable<GliaEvent> {
        val stateEvents = engagementStateUseCase().flatMap { state ->
            when (state) {
                is State.EngagementStarted -> Flowable.just(GliaEvent.EngagementStarted)

                is State.EngagementEnded -> Flowable.just(GliaEvent.EngagementEnded)

                else -> Flowable.empty()
            }
        }

        // UNKNOWN is EngagementTypeUseCase's fallback for "no chat/audio/video media" - which
        // includes having no ongoing engagement at all (e.g. right after SDK init, or after an
        // engagement ends). Filtered out so idle state doesn't get reported as "ongoing".
        val mediaTypeEvents = engagementTypeUseCase()
            .filter { it != MediaType.UNKNOWN }
            .distinctUntilChanged()
            .map<GliaEvent> { GliaEvent.EngagementOngoing(it) }

        return Flowable.merge(stateEvents, mediaTypeEvents)
    }
}
