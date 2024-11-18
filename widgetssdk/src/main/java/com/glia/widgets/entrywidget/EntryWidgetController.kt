package com.glia.widgets.entrywidget

import android.app.Activity
import androidx.annotation.VisibleForTesting
import com.glia.androidsdk.Engagement
import com.glia.androidsdk.queuing.Queue
import com.glia.androidsdk.queuing.QueueState
import com.glia.widgets.chat.domain.IsAuthenticatedUseCase
import com.glia.widgets.core.queue.QueueRepository
import com.glia.widgets.core.queue.QueuesState
import com.glia.widgets.core.secureconversations.domain.ObserveUnreadMessagesCountUseCase
import com.glia.widgets.di.GliaCore
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.TAG
import com.glia.widgets.launcher.EngagementLauncher
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable

internal class EntryWidgetController(
    private val queueRepository: QueueRepository,
    private val isAuthenticatedUseCase: IsAuthenticatedUseCase,
    private val observeUnreadMessagesCountUseCase: ObserveUnreadMessagesCountUseCase,
    private val core: GliaCore,
    private val engagementLauncher: EngagementLauncher
) : EntryWidgetContract.Controller {
    private lateinit var view: EntryWidgetContract.View
    private var disposable = CompositeDisposable()
    private lateinit var type: EntryWidgetContract.ViewType

    override fun setView(view: EntryWidgetContract.View, type: EntryWidgetContract.ViewType) {
        this.view = view
        this.type = type

        if (!core.isInitialized) {
            showSdkNotInitializedState()
        }
        subscribeToQueueState()
    }

    private fun showSdkNotInitializedState() {
        view.showItems(listOf(EntryWidgetContract.ItemType.SdkNotInitializedState))
    }

    private fun subscribeToQueueState() {
        val queueStateObservable = queueRepository.queuesState
            .toObservable()
            .observeOn(AndroidSchedulers.mainThread())

        val unreadMessagesCountObservable = observeUnreadMessagesCountUseCase()
            .observeOn(AndroidSchedulers.mainThread())

        Observable.combineLatest(
            queueStateObservable,
            unreadMessagesCountObservable
        ) { queueState: QueuesState, unreadCount: Int ->
            mapState(queueState, unreadCount)
        }
            .subscribe(view::showItems)
            .let(disposable::add)
    }

    @VisibleForTesting
    fun mapState(state: QueuesState, count: Int): List<EntryWidgetContract.ItemType> = when (state) {
        QueuesState.Loading -> mapLoadingState()
        QueuesState.Empty -> listOf(EntryWidgetContract.ItemType.EmptyState)
        is QueuesState.Error -> listOf(EntryWidgetContract.ItemType.ErrorState)
        is QueuesState.Queues -> mapMediaTypes(state.queues, count)
    }

    private fun mapLoadingState() = listOf(
        EntryWidgetContract.ItemType.LoadingState,
        EntryWidgetContract.ItemType.LoadingState,
        EntryWidgetContract.ItemType.LoadingState,
        EntryWidgetContract.ItemType.LoadingState,
        EntryWidgetContract.ItemType.ProvidedBy
    )

    private fun mapMediaTypes(queues: List<Queue>, count: Int): List<EntryWidgetContract.ItemType> =
        queues.asSequence()
            .map { it.state }
            .filter { it.status == QueueState.Status.OPEN }
            .flatMap { it.medias.toList() }
            .distinct()
            .filterNot { it == null || it == Engagement.MediaType.UNKNOWN || it == Engagement.MediaType.PHONE }
            .filterNot { it == Engagement.MediaType.MESSAGING && (!isAuthenticatedUseCase() || type == EntryWidgetContract.ViewType.MESSAGING_LIVE_SUPPORT) }
            .map {
                when (it) {
                    Engagement.MediaType.VIDEO -> EntryWidgetContract.ItemType.VideoCall
                    Engagement.MediaType.AUDIO -> EntryWidgetContract.ItemType.AudioCall
                    Engagement.MediaType.TEXT -> EntryWidgetContract.ItemType.Chat
                    Engagement.MediaType.MESSAGING -> EntryWidgetContract.ItemType.Messaging(count)
                    // Anything else should be impossible based on filtering few lines above.
                    // Added `else` to meet `when()` requirement to be exhaustive
                    else -> EntryWidgetContract.ItemType.ErrorState
                }
            }
            .toMutableList()
            .apply {
                if (isEmpty()) {
                    add(EntryWidgetContract.ItemType.EmptyState)
                } else if (type != EntryWidgetContract.ViewType.MESSAGING_LIVE_SUPPORT) {
                    add(EntryWidgetContract.ItemType.ProvidedBy)
                }
                sort()
            }

    override fun onItemClicked(itemType: EntryWidgetContract.ItemType, activity: Activity) {
        Logger.d(TAG, "Item clicked: $itemType")

        when (itemType) {
            EntryWidgetContract.ItemType.Chat -> engagementLauncher.startChat(activity)
            EntryWidgetContract.ItemType.AudioCall -> engagementLauncher.startAudioCall(activity)
            EntryWidgetContract.ItemType.VideoCall -> engagementLauncher.startVideoCall(activity)
            is EntryWidgetContract.ItemType.Messaging -> engagementLauncher.startSecureMessaging(activity)
            EntryWidgetContract.ItemType.ErrorState -> onRetryButtonClicked()
            else -> {}
        }

        if (itemType != EntryWidgetContract.ItemType.ErrorState) {
            // Dismiss the widget only if the clicked item is not a retry button
            view.dismiss()
        }
    }

    private fun onRetryButtonClicked() {
        queueRepository.fetchQueues()
    }

    override fun onDestroy() {
        disposable.dispose()
    }
}
