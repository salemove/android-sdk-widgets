package com.glia.widgets.core.queue

import com.glia.androidsdk.Engagement.MediaType as CoreSdkMediaType
import com.glia.androidsdk.queuing.QueueState.Status as CoreSdkQueueState
import com.glia.androidsdk.queuing.Queue as CoreSdkQueue
import com.glia.widgets.core.engagement.MediaType
import com.glia.widgets.view.unifiedui.Mergeable
import com.glia.widgets.view.unifiedui.merge

/**
 * Contains information about Queue.
 *
 * @see [com.glia.widgets.GliaWidgets.getQueues]
 */
data class Queue(
    val id: String,
    val name: String,
    val isDefault: Boolean?,
    val lastUpdatedMillis: Long,
    val medias: List<MediaType>,
    val status: Status
) : Mergeable<Queue> {
    /**
     * Defines possible Queue state types.
     */
    enum class Status {
        /**
         * Visitor can enqueue
         */
        OPEN,

        /**
         * Visitor cannot enqueue because the Queue is closed
         */
        CLOSED,

        /**
         * Visitor cannot enqueue because the Queue reached its max capacity
         */
        FULL,

        /**
         * Visitor cannot enqueue because the Queue is unstaffed
         */
        UNSTAFFED,

        /**
         * Visitor should not enqueue because the Queue state is not supported by current version of SDK
         */
        UNKNOWN
    }

    override fun merge(other: Queue): Queue = Queue(
        id = id merge other.id,
        name = name merge other.name,
        isDefault = isDefault merge other.isDefault,
        lastUpdatedMillis = lastUpdatedMillis merge other.lastUpdatedMillis,
        medias = medias merge other.medias,
        status = status merge other.status
    )
}

internal fun CoreSdkQueue.toWidgetsType(): Queue = Queue(
    id = id,
    name = name,
    isDefault = isDefault,
    lastUpdatedMillis = lastUpdatedMillis,
    medias = state.medias.map { it.toWidgetsType() },
    status = state.status.toWidgetsType()
)

internal fun Array<CoreSdkQueue>.toWidgetsType(): Collection<Queue> =
    map { it.toWidgetsType() }

internal fun CoreSdkMediaType.toWidgetsType(): MediaType =
    when (this) {
        CoreSdkMediaType.TEXT -> MediaType.TEXT
        CoreSdkMediaType.AUDIO -> MediaType.AUDIO
        CoreSdkMediaType.PHONE -> MediaType.PHONE
        CoreSdkMediaType.VIDEO -> MediaType.VIDEO
        CoreSdkMediaType.MESSAGING -> MediaType.MESSAGING
        else -> MediaType.UNKNOWN
    }

internal fun CoreSdkQueueState.toWidgetsType(): Queue.Status =
    when (this) {
        CoreSdkQueueState.OPEN -> Queue.Status.OPEN
        CoreSdkQueueState.CLOSED -> Queue.Status.CLOSED
        CoreSdkQueueState.FULL -> Queue.Status.FULL
        CoreSdkQueueState.UNSTAFFED -> Queue.Status.UNSTAFFED
        else -> Queue.Status.UNKNOWN
    }

internal fun MediaType.toCoreType(): CoreSdkMediaType =
    when (this) {
        MediaType.TEXT -> CoreSdkMediaType.TEXT
        MediaType.AUDIO -> CoreSdkMediaType.AUDIO
        MediaType.PHONE -> CoreSdkMediaType.PHONE
        MediaType.VIDEO -> CoreSdkMediaType.VIDEO
        MediaType.MESSAGING -> CoreSdkMediaType.MESSAGING
        else -> CoreSdkMediaType.UNKNOWN
    }
