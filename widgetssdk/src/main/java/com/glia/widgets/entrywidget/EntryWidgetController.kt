package com.glia.widgets.entrywidget

import android.app.Activity
import androidx.annotation.VisibleForTesting
import com.glia.androidsdk.Engagement.MediaType
import com.glia.widgets.chat.Intention
import com.glia.widgets.chat.domain.IsAuthenticatedUseCase
import com.glia.widgets.core.queue.Queue
import com.glia.widgets.core.queue.QueueRepository
import com.glia.widgets.core.queue.QueuesState
import com.glia.widgets.core.secureconversations.SecureConversationsRepository
import com.glia.widgets.core.secureconversations.domain.HasOngoingSecureConversationUseCase
import com.glia.widgets.di.GliaCore
import com.glia.widgets.engagement.State
import com.glia.widgets.engagement.domain.EngagementStateUseCase
import com.glia.widgets.engagement.domain.EngagementTypeUseCase
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.TAG
import com.glia.widgets.helper.mediaTypes
import com.glia.widgets.launcher.ActivityLauncher
import com.glia.widgets.launcher.EngagementLauncher
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.disposables.CompositeDisposable

internal class EntryWidgetController @JvmOverloads constructor(
    private val queueRepository: QueueRepository,
    private val isAuthenticatedUseCase: IsAuthenticatedUseCase,
    private val secureConversationsRepository: SecureConversationsRepository,
    private val hasOngoingSecureConversationUseCase: HasOngoingSecureConversationUseCase,
    private val engagementStateUseCase: EngagementStateUseCase,
    private val engagementTypeUseCase: EngagementTypeUseCase,
    private val core: GliaCore,
    private val engagementLauncher: EngagementLauncher,
    private val activityLauncher: ActivityLauncher,
    private val compositeDisposable: CompositeDisposable = CompositeDisposable()
) : EntryWidgetContract.Controller {
    private val queueStateObservable by lazy { queueRepository.queuesState }
    private val unreadMessagesCountObservable by lazy { secureConversationsRepository.unreadMessagesCountObservable }
    private val hasOngoingSCObservable by lazy { hasOngoingSecureConversationUseCase() }
    private val engagementStateObservable by lazy { engagementStateUseCase() }
    private val mediaTypeObservable by lazy { engagementTypeUseCase() }

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

    private val messagingChatObservableItemType: Flowable<List<EntryWidgetContract.ItemType>>
        get() = queueStateObservable.map(::mapMessagingQueueState)

    private val entryWidgetObservableItemType: Flowable<List<EntryWidgetContract.ItemType>>
        get() = Flowable.combineLatest(
            engagementStateObservable,
            mediaTypeObservable,
            queueStateObservable,
            unreadMessagesCountObservable,
            hasOngoingSCObservable,
            ::mapToEntryWidgetItems
        )

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

    @VisibleForTesting
    fun mapToEntryWidgetItems(
        engagementState: State,
        mediaType: MediaType,
        queuesState: QueuesState,
        unreadMessagesCount: Int,
        hasOngoingSC: Boolean,
        isViewWhiteLabel: Boolean = view.whiteLabel
    ): List<EntryWidgetContract.ItemType> {
        val items = when (engagementState) {
            is State.PreQueuing -> prepareItemsBasedOnOngoingEngagement(engagementState.mediaType, unreadMessagesCount, hasOngoingSC)
            is State.Queuing -> prepareItemsBasedOnOngoingEngagement(engagementState.mediaType, unreadMessagesCount, hasOngoingSC)
            is State.Update -> prepareItemsBasedOnOngoingEngagement(mediaType, unreadMessagesCount, hasOngoingSC)
            else -> prepareItemsBasedOnQueues(queuesState, unreadMessagesCount, hasOngoingSC)
        }.toMutableList()
        if (isViewWhiteLabel.not()) {
            items.add(EntryWidgetContract.ItemType.PoweredBy)
        }
        return items.apply { sort() }
    }

    @VisibleForTesting
    fun prepareItemsBasedOnOngoingEngagement(
        mediaType: MediaType,
        unreadMessagesCount: Int,
        hasOngoingSC: Boolean
    ): List<EntryWidgetContract.ItemType> {
        Logger.i(TAG, "Preparing items based on ongoing engagement")
        return when {
            engagementTypeUseCase.isCallVisualizer -> listOf(EntryWidgetContract.ItemType.CallVisualizerOngoing)
            hasOngoingSC -> listOf(EntryWidgetContract.ItemType.MessagingOngoing(unreadMessagesCount))
            mediaType == MediaType.VIDEO -> listOf(EntryWidgetContract.ItemType.VideoCallOngoing)
            mediaType == MediaType.AUDIO -> listOf(EntryWidgetContract.ItemType.AudioCallOngoing)
            else -> listOf(EntryWidgetContract.ItemType.ChatOngoing)
        }
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

    @VisibleForTesting
    fun prepareItemsBasedOnQueues(
        queuesState: QueuesState,
        unreadMessagesCount: Int,
        hasOngoingSC: Boolean
    ): List<EntryWidgetContract.ItemType> {

        val messagingOrDefault: (default: List<EntryWidgetContract.ItemType>) -> List<EntryWidgetContract.ItemType> = { default ->
            /* This check may be unreliable due to the asynchronous nature of authentication.
            *  We lack a callback for authentication completion, making it difficult to track when authentication has finished.
            *  Sometimes(when we authenticate with opened Entry Widget embedded view),
            *  we receive updates for ongoing secure conversations before the authentication result is saved, causing this check to return false. */
            if (hasOngoingSC && isAuthenticatedUseCase())
                listOf(EntryWidgetContract.ItemType.MessagingOngoing(unreadMessagesCount))
            else
                default
        }

        return when (queuesState) {
            QueuesState.Empty -> messagingOrDefault(emptyState)
            QueuesState.Loading -> messagingOrDefault(loadingState)
            is QueuesState.Error -> messagingOrDefault(errorState)
            is QueuesState.Queues -> mapEntryWidgetQueues(queuesState.queues, unreadMessagesCount, hasOngoingSC)
        }
    }

    private fun mapEntryWidgetQueues(queues: List<Queue>, unreadMessagesCount: Int, hasOngoingSC: Boolean): List<EntryWidgetContract.ItemType> {
        val messaging = EntryWidgetContract.ItemType.Messaging(unreadMessagesCount)

        val items = queues.mediaTypes.mapNotNull {
            when {
                it == MediaType.VIDEO -> EntryWidgetContract.ItemType.VideoCall
                it == MediaType.AUDIO -> EntryWidgetContract.ItemType.AudioCall
                it == MediaType.TEXT -> EntryWidgetContract.ItemType.Chat
                it == MediaType.MESSAGING && isAuthenticatedUseCase() -> messaging

                else -> null
            }
        }

        return when {
            hasOngoingSC && !items.contains(messaging) && isAuthenticatedUseCase() -> items + messaging
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
                MediaType.VIDEO -> EntryWidgetContract.ItemType.VideoCall
                MediaType.AUDIO -> EntryWidgetContract.ItemType.AudioCall
                MediaType.TEXT -> EntryWidgetContract.ItemType.Chat
                else -> null
            }
        }.takeIf { it.isNotEmpty() }?.sorted() ?: emptyState
    }

    override fun onItemClicked(itemType: EntryWidgetContract.ItemType, activity: Activity) {
        Logger.d(TAG, "Item clicked: $itemType")

        when (itemType) {
            EntryWidgetContract.ItemType.VideoCall -> engagementLauncher.startVideoCall(activity)
            EntryWidgetContract.ItemType.VideoCallOngoing -> activityLauncher.launchCall(activity, null, false)
            EntryWidgetContract.ItemType.AudioCall -> engagementLauncher.startAudioCall(activity)
            EntryWidgetContract.ItemType.AudioCallOngoing -> activityLauncher.launchCall(activity, null, false)
            EntryWidgetContract.ItemType.Chat -> engagementLauncher.startChat(activity)
            EntryWidgetContract.ItemType.ChatOngoing -> activityLauncher.launchChat(activity, Intention.RETURN_TO_CHAT)
            is EntryWidgetContract.ItemType.Messaging,
            is EntryWidgetContract.ItemType.MessagingOngoing -> engagementLauncher.startSecureMessaging(activity)

            is EntryWidgetContract.ItemType.CallVisualizerOngoing -> {
                if (engagementTypeUseCase.isMediaEngagement) activityLauncher.launchCall(activity, null, false)
                else if (engagementTypeUseCase.isCallVisualizerScreenSharing) activityLauncher.launchEndScreenSharing(activity)
            }

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
