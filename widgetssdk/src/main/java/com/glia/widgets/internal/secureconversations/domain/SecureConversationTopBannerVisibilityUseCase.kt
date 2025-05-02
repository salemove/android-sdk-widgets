package com.glia.widgets.internal.secureconversations.domain

import com.glia.widgets.engagement.MediaType
import com.glia.widgets.queue.Queue
import com.glia.widgets.internal.queue.QueueRepository
import com.glia.widgets.internal.queue.QueuesState
import com.glia.widgets.helper.mediaTypes
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Flowable

/**
 * Should show top banner in secure conversations chat only if both conditions are met
 * - There is an open queue with text, audio or video media
 * - Has pending secure conversations
 */
internal class SecureConversationTopBannerVisibilityUseCase(
    private val queueRepository: QueueRepository,
    private val manageSecureMessagingStatusUseCase: ManageSecureMessagingStatusUseCase
) {
    operator fun invoke(): Flowable<Boolean> = queueRepository.queuesState
        .map(QueuesState::queuesOrEmpty)
        .map(::hasOpenQueueWithLiveMedia)
        .map { it && manageSecureMessagingStatusUseCase.shouldBehaveAsSecureMessaging }
        .observeOn(AndroidSchedulers.mainThread())
        .distinctUntilChanged()

    private fun hasOpenQueueWithLiveMedia(queues: List<Queue>): Boolean = queues.mediaTypes.hasLiveMediaType()

    private fun List<MediaType>.hasLiveMediaType() = any { it == MediaType.TEXT || it == MediaType.AUDIO || it == MediaType.VIDEO }
}
