package com.glia.widgets.entrywidget

import android.annotation.SuppressLint
import com.glia.androidsdk.Engagement
import com.glia.androidsdk.queuing.Queue
import com.glia.androidsdk.queuing.QueueState
import com.glia.widgets.chat.domain.IsAuthenticatedUseCase
import com.glia.widgets.core.queue.QueueRepository
import com.glia.widgets.core.queue.QueuesState
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.TAG
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers

internal class EntryWidgetController(
    private val queueRepository: QueueRepository,
    private val isAuthenticatedUseCase: IsAuthenticatedUseCase
) : EntryWidgetContract.Controller {
    private lateinit var view: EntryWidgetContract.View

    @SuppressLint("CheckResult")
    override fun setView(view: EntryWidgetContract.View) {
        this.view = view
        queueRepository.queuesState
            .map(::mapState)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(view::showItems)
    }

    private fun mapState(state: QueuesState): List<EntryWidgetContract.ItemType> = when (state) {
        QueuesState.Loading -> mapLoadingState()
        QueuesState.Empty -> listOf(EntryWidgetContract.ItemType.EMPTY_STATE)
        is QueuesState.Error -> listOf(EntryWidgetContract.ItemType.ERROR_STATE)
        is QueuesState.Queues -> mapMediaTypes(state.queues)
    }

    private fun mapLoadingState() = listOf(
        EntryWidgetContract.ItemType.LOADING_STATE,
        EntryWidgetContract.ItemType.LOADING_STATE,
        EntryWidgetContract.ItemType.LOADING_STATE,
        EntryWidgetContract.ItemType.LOADING_STATE,
        EntryWidgetContract.ItemType.PROVIDED_BY
    )

    private fun mapMediaTypes(queues: List<Queue>): List<EntryWidgetContract.ItemType> =
        queues.map { it.state }
            .filter { it.status == QueueState.Status.OPEN }
            .flatMap { it.medias.toList() }
            .distinct()
            .let { allMedias ->
                return buildList {
                    if (allMedias.contains(Engagement.MediaType.TEXT)) add(EntryWidgetContract.ItemType.CHAT)
                    if (allMedias.contains(Engagement.MediaType.AUDIO)) add(EntryWidgetContract.ItemType.AUDIO_CALL)
                    if (allMedias.contains(Engagement.MediaType.VIDEO)) add(EntryWidgetContract.ItemType.VIDEO_CALL)
                    if (allMedias.contains(Engagement.MediaType.MESSAGING) && isAuthenticatedUseCase()) add(EntryWidgetContract.ItemType.SECURE_MESSAGE)

                    if (allMedias.isEmpty()) {
                        add(EntryWidgetContract.ItemType.EMPTY_STATE)
                    } else {
                        add(EntryWidgetContract.ItemType.PROVIDED_BY)
                    }
                }
            }

    override fun onItemClicked(itemType: EntryWidgetContract.ItemType) {
        // TODO: Handle item click

        Logger.d(TAG, "Item clicked: $itemType")
        // Dismiss the widget
        view.dismiss()
    }

    override fun onDestroy() {
        // Clean up
    }
}
