package com.glia.widgets.core.secureconversations.domain

import com.glia.widgets.engagement.EngagementRepository

/**
 * This use case is responsible for determining if the messages should be marked as read.
 * The messages should be marked as read if there is no ongoing live engagement or if the current
 * engagement action on end is retain.
 *
 * If result is true, [MarkMessagesReadWithDelayUseCase] should be used to mark the messages as read.
 * Additional logic is implemented in [MarkMessagesReadWithDelayUseCase] to delay the marking of
 * messages as read if the chat screen is open or the leave dialog is visible.
 */
internal class ShouldMarkMessagesReadUseCase(
    private val engagementRepository: EngagementRepository
) {

    operator fun invoke(): Boolean {
        return !engagementRepository.hasOngoingLiveEngagement
            || engagementRepository.isRetainAfterEnd
    }
}
