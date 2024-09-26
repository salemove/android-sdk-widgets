package com.glia.widgets.core.secureconversations.domain

import com.glia.androidsdk.GliaException
import com.glia.androidsdk.RequestCallback
import com.glia.androidsdk.queuing.Queue
import com.glia.widgets.core.engagement.GliaEngagementConfigRepository
import com.glia.widgets.core.queue.GliaQueueRepository
import com.glia.widgets.helper.Data.Value
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.TAG
import com.glia.widgets.helper.rx.Schedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import java.util.UUID

//TODO this Class must be removed after we have layer for up to date queue list monitoring
internal class GetAvailableQueueIdsForSecureMessagingUseCase(
    private val engagementConfigRepository: GliaEngagementConfigRepository,
    private val queueRepository: GliaQueueRepository,
    private val isMessagingAvailableUseCase: IsMessagingAvailableUseCase,
    private val schedulers: Schedulers
) {
    private val disposable = CompositeDisposable()

    operator fun invoke() = queueRepository.queues
        .map { queues ->
            val queueIds = engagementConfigRepository.queueIds
            validateIds(queueIds)

            val matchedQueues = matchQueues(queueIds, queues)

            if (matchedQueues.isEmpty()) {
                if (queueIds.isNotEmpty()) {
                    Logger.w(TAG, "Provided queue IDs do not match with any queue.")
                }

                val defaultQueues = queues
                    .filter { it.isDefault == true }

                return@map Value(
                    if (isMessagingAvailableUseCase(defaultQueues)) {
                        Logger.i(TAG, "Secure Messaging is available using queues that are set as **Default**.")
                        defaultQueues.map { it.id }
                    } else {
                        Logger.w(TAG, "No default queues that have status other than closed and support messaging were found.")
                        null
                    }
                )
            }

            Value(
                if (isMessagingAvailableUseCase(matchedQueues)) {
                    Logger.i(TAG, "Secure Messaging is available in queues with IDs: $queueIds.")
                    queueIds
                } else {
                    Logger.w(TAG, "Provided queue IDs do not match with queues that have status other than closed and support messaging.")
                    null
                }
            )
        }
        .subscribeOn(schedulers.computationScheduler)
        .observeOn(schedulers.mainScheduler)

    private fun validateIds(queueIds: List<String>) {
        val invalidIds = queueIds.filter { queueId ->
            try {
                UUID.fromString(queueId)
                false
            } catch (e: IllegalArgumentException) {
                true
            }
        }
        if (invalidIds.isNotEmpty()) {
            Logger.w(TAG, "Queue ID array for Secure Messaging contains invalid queue IDs: $invalidIds.")
        }
    }

    private fun matchQueues(queueIds: List<String>, queues: Array<Queue>) = queues
        .filter { queueIds.contains(it.id) }

    operator fun invoke(callback: RequestCallback<List<String>>) {
        disposable.add(
            invoke().subscribe(
                { callback.onResult(it.result, null) },
                { callback.onResult(null, GliaException.from(it)) }
            )
        )
    }

    fun dispose() {
        disposable.clear()
    }
}
