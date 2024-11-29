package com.glia.widgets.entrywidget

import android.app.Activity
import com.glia.androidsdk.Engagement
import com.glia.androidsdk.queuing.Queue
import com.glia.widgets.chat.domain.IsAuthenticatedUseCase
import com.glia.widgets.core.queue.QueueRepository
import com.glia.widgets.core.queue.QueuesState
import com.glia.widgets.core.secureconversations.domain.HasOngoingSecureConversationUseCase
import com.glia.widgets.core.secureconversations.domain.ObserveUnreadMessagesCountUseCase
import com.glia.widgets.di.GliaCore
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.TAG
import com.glia.widgets.helper.mediaTypes
import com.glia.widgets.launcher.EngagementLauncher
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable

internal class EntryWidgetController @JvmOverloads constructor(
    private val queueRepository: QueueRepository,
    private val isAuthenticatedUseCase: IsAuthenticatedUseCase,
    private val observeUnreadMessagesCountUseCase: ObserveUnreadMessagesCountUseCase,
    private val hasOngoingSecureConversationUseCase: HasOngoingSecureConversationUseCase,
    private val core: GliaCore,
    private val engagementLauncher: EngagementLauncher,
    private val compositeDisposable: CompositeDisposable = CompositeDisposable()
) : EntryWidgetContract.Controller {
    private val queueStateObservable by lazy { queueRepository.queuesState.toObservable() }
    private val unreadMessagesCountObservable by lazy { observeUnreadMessagesCountUseCase() }
    private val hasOngoingSCObservable by lazy { hasOngoingSecureConversationUseCase() }

    private val loadingState: List<EntryWidgetContract.ItemType> by lazy {
        listOf(
            EntryWidgetContract.ItemType.LoadingState,
            EntryWidgetContract.ItemType.LoadingState,
            EntryWidgetContract.ItemType.LoadingState,
            EntryWidgetContract.ItemType.LoadingState
        )
    }

    private val emptyState: List<EntryWidgetContract.ItemType.EmptyState> by lazy {
        listOf(EntryWidgetContract.ItemType.EmptyState)
    }

    private val errorState: List<EntryWidgetContract.ItemType.EmptyState> by lazy {
        listOf(EntryWidgetContract.ItemType.EmptyState)
    }

    private val messagingChatObservableItemType: Observable<List<EntryWidgetContract.ItemType>>
        get() = queueStateObservable.map(::mapMessagingQueueState)

    private val entryWidgetObservableItemType: Observable<List<EntryWidgetContract.ItemType>>
        get() = Observable.combineLatest(queueStateObservable, unreadMessagesCountObservable, hasOngoingSCObservable, ::mapEntryWidgetQueueState)

    private lateinit var view: EntryWidgetContract.View

    override fun setView(view: EntryWidgetContract.View, type: EntryWidgetContract.ViewType) {
        this.view = view

        if (!core.isInitialized) {
            showSdkNotInitializedState(type == EntryWidgetContract.ViewType.MESSAGING_LIVE_SUPPORT)
        }

        itemsObservableBasedOnType(type)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(view::showItems)
            .let(compositeDisposable::add)
    }

    private fun itemsObservableBasedOnType(type: EntryWidgetContract.ViewType) = when (type) {
        EntryWidgetContract.ViewType.MESSAGING_LIVE_SUPPORT -> messagingChatObservableItemType
        else -> entryWidgetObservableItemType
    }

    private fun showSdkNotInitializedState(isMessaging: Boolean) {
        val items = if (view.whiteLabel || isMessaging) {
            listOf(EntryWidgetContract.ItemType.SdkNotInitializedState)
        } else {
            listOf(EntryWidgetContract.ItemType.SdkNotInitializedState, EntryWidgetContract.ItemType.PoweredBy)
        }
        view.showItems(items)
    }

    private fun mapEntryWidgetQueueState(
        queuesState: QueuesState,
        unreadMessagesCount: Int,
        hasOngoingSC: Boolean
    ): List<EntryWidgetContract.ItemType> {

        val messagingOrDefault: (default: List<EntryWidgetContract.ItemType>) -> List<EntryWidgetContract.ItemType> = { default ->
            if (isAuthenticatedUseCase() && hasOngoingSC)
                listOf(EntryWidgetContract.ItemType.Messaging(unreadMessagesCount))
            else
                default
        }

        val items = when (queuesState) {
            QueuesState.Empty -> messagingOrDefault(emptyState)
            QueuesState.Loading -> messagingOrDefault(loadingState)
            is QueuesState.Error -> messagingOrDefault(errorState)
            is QueuesState.Queues -> mapEntryWidgetQueues(queuesState.queues, unreadMessagesCount, hasOngoingSC)
        }.toMutableList()

        if (!view.whiteLabel) {
            items.add(EntryWidgetContract.ItemType.PoweredBy)
        }

        return items.apply { sort() }
    }

    private fun mapEntryWidgetQueues(queues: List<Queue>, unreadMessagesCount: Int, hasOngoingSC: Boolean): List<EntryWidgetContract.ItemType> {
        val messaging = EntryWidgetContract.ItemType.Messaging(unreadMessagesCount)

        val items = queues.mediaTypes.mapNotNull {
            when {
                it == Engagement.MediaType.VIDEO -> EntryWidgetContract.ItemType.VideoCall
                it == Engagement.MediaType.AUDIO -> EntryWidgetContract.ItemType.AudioCall
                it == Engagement.MediaType.TEXT -> EntryWidgetContract.ItemType.Chat
                it == Engagement.MediaType.MESSAGING && isAuthenticatedUseCase() -> messaging

                else -> null
            }
        }

        return when {
            isAuthenticatedUseCase() && hasOngoingSC && !items.contains(messaging) -> items + messaging
            items.isEmpty() -> emptyState
            else -> items
        }

    }

    private fun mapMessagingQueueState(queuesState: QueuesState): List<EntryWidgetContract.ItemType> = when (queuesState) {
        QueuesState.Empty -> emptyState
        QueuesState.Loading -> loadingState
        is QueuesState.Error -> errorState
        is QueuesState.Queues -> queuesState.queues.mediaTypes.mapNotNull { mediaType ->
            when (mediaType) {
                Engagement.MediaType.VIDEO -> EntryWidgetContract.ItemType.VideoCall
                Engagement.MediaType.AUDIO -> EntryWidgetContract.ItemType.AudioCall
                Engagement.MediaType.TEXT -> EntryWidgetContract.ItemType.Chat
                else -> null
            }
        }.takeIf { it.isNotEmpty() }?.sorted() ?: emptyState
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
        compositeDisposable.dispose()
    }
}
