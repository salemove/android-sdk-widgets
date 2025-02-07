package com.glia.widgets.core.queue

import com.glia.androidsdk.Engagement
import com.glia.androidsdk.queuing.QueueState
import com.glia.widgets.view.unifiedui.Mergeable
import com.glia.widgets.view.unifiedui.merge
import com.glia.androidsdk.queuing.Queue as CoreSdkQueue

internal data class Queue(
    val id: String,
    val name: String,
    val isDefault: Boolean?,
    val lastUpdatedMillis: Long,
    val medias: List<Engagement.MediaType>,
    val status: QueueState.Status
) : Mergeable<Queue> {
    override fun merge(other: Queue): Queue = Queue(
        id = id merge other.id,
        name = name merge other.name,
        isDefault = isDefault merge other.isDefault,
        lastUpdatedMillis = lastUpdatedMillis merge other.lastUpdatedMillis,
        medias = medias merge other.medias,
        status = status merge other.status
    )
}

internal fun CoreSdkQueue.asLocalQueue(): Queue = Queue(
    id = id,
    name = name,
    isDefault = isDefault,
    lastUpdatedMillis = lastUpdatedMillis,
    medias = state.medias.asList(),
    status = state.status
)
