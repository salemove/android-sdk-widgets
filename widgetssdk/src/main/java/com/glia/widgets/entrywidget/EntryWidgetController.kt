package com.glia.widgets.entrywidget

import android.app.Activity
import androidx.annotation.VisibleForTesting
import com.glia.androidsdk.Engagement
import com.glia.androidsdk.queuing.Queue
import com.glia.androidsdk.queuing.QueueState
import com.glia.widgets.chat.domain.IsAuthenticatedUseCase
import com.glia.widgets.core.queue.QueueRepository
import com.glia.widgets.core.queue.QueuesState
import com.glia.widgets.di.Dependencies
import com.glia.widgets.di.GliaCore
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.TAG
import com.glia.widgets.helper.unSafeSubscribe
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers

internal class EntryWidgetController(
    private val queueRepository: QueueRepository,
    private val isAuthenticatedUseCase: IsAuthenticatedUseCase,
    private val core: GliaCore
) : EntryWidgetContract.Controller {
    private lateinit var view: EntryWidgetContract.View
    private val engagementLauncher = Dependencies.engagementLauncher

    override fun setView(view: EntryWidgetContract.View) {
        this.view = view

        if (core.isInitialized) {
            subscribeToQueueState()
        } else {
            showSdkNotInitializedState()
        }
    }

    private fun showSdkNotInitializedState() {
        view.showItems(listOf(EntryWidgetContract.ItemType.SDK_NOT_INITIALIZED_STATE))
    }

    private fun subscribeToQueueState() {
        queueRepository.queuesState
            .map(::mapState)
            .observeOn(AndroidSchedulers.mainThread())
            .unSafeSubscribe(view::showItems)
    }

    @VisibleForTesting
    fun mapState(state: QueuesState): List<EntryWidgetContract.ItemType> = when (state) {
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

                    if (allMedias.isEmpty() || allMedias.hasOnlyMessagingAndIsNotAuthenticated()) {
                        add(EntryWidgetContract.ItemType.EMPTY_STATE)
                    } else {
                        add(EntryWidgetContract.ItemType.PROVIDED_BY)
                    }
                }
            }

    private fun List<Engagement.MediaType>.hasOnlyMessagingAndIsNotAuthenticated(): Boolean =
        this.size == 1 && this.contains(Engagement.MediaType.MESSAGING) && !isAuthenticatedUseCase()

    override fun onItemClicked(itemType: EntryWidgetContract.ItemType, activity: Activity) {
        Logger.d(TAG, "Item clicked: $itemType")

        when (itemType) {
            EntryWidgetContract.ItemType.CHAT -> engagementLauncher.startChat(activity)
            EntryWidgetContract.ItemType.AUDIO_CALL -> engagementLauncher.startAudioCall(activity)
            EntryWidgetContract.ItemType.VIDEO_CALL -> engagementLauncher.startVideoCall(activity)
            EntryWidgetContract.ItemType.SECURE_MESSAGE -> engagementLauncher.startSecureMessaging(activity)
            EntryWidgetContract.ItemType.ERROR_STATE -> onRetryButtonClicked()
            else -> {}
        }

        if (itemType != EntryWidgetContract.ItemType.ERROR_STATE) {
            // Dismiss the widget only if the clicked item is not a retry button
            view.dismiss()
        }
    }

    private fun onRetryButtonClicked() {
        queueRepository.fetchQueues()
    }

    override fun onDestroy() {
        // Clean up
    }
}
