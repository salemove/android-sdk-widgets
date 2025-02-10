package com.glia.widgets.chat

import android.text.format.DateUtils
import androidx.annotation.VisibleForTesting
import com.glia.androidsdk.chat.FilesAttachment
import com.glia.androidsdk.chat.SendMessagePayload
import com.glia.androidsdk.chat.SingleChoiceAttachment
import com.glia.androidsdk.chat.VisitorMessage
import com.glia.widgets.chat.domain.AddNewMessagesDividerUseCase
import com.glia.widgets.chat.domain.AppendHistoryChatMessageUseCase
import com.glia.widgets.chat.domain.AppendNewChatMessageUseCase
import com.glia.widgets.chat.domain.GliaLoadHistoryUseCase
import com.glia.widgets.chat.domain.GliaOnMessageUseCase
import com.glia.widgets.chat.domain.HandleCustomCardClickUseCase
import com.glia.widgets.chat.domain.IsAuthenticatedUseCase
import com.glia.widgets.chat.domain.SendUnsentMessagesUseCase
import com.glia.widgets.chat.model.ChatItem
import com.glia.widgets.chat.model.CustomCardChatItem
import com.glia.widgets.chat.model.GvaButton
import com.glia.widgets.chat.model.GvaQuickReplies
import com.glia.widgets.chat.model.LocalAttachmentItem
import com.glia.widgets.chat.model.MediaUpgradeStartedTimerItem
import com.glia.widgets.chat.model.NewMessagesDividerItem
import com.glia.widgets.chat.model.OperatorChatItem
import com.glia.widgets.chat.model.OperatorMessageItem
import com.glia.widgets.chat.model.OperatorStatusItem
import com.glia.widgets.chat.model.RemoteAttachmentItem
import com.glia.widgets.chat.model.TapToRetryItem
import com.glia.widgets.chat.model.VisitorAttachmentItem
import com.glia.widgets.chat.model.VisitorChatItem
import com.glia.widgets.core.engagement.domain.model.ChatHistoryResponse
import com.glia.widgets.core.engagement.domain.model.ChatMessageInternal
import com.glia.widgets.core.secureconversations.domain.MarkMessagesReadWithDelayUseCase
import com.glia.widgets.core.secureconversations.domain.ShouldMarkMessagesReadUseCase
import com.glia.widgets.engagement.domain.IsQueueingOrLiveEngagementUseCase
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.TAG
import com.glia.widgets.helper.isValid
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.processors.BehaviorProcessor
import io.reactivex.rxjava3.schedulers.Schedulers

internal class ChatManager(
    private val onMessageUseCase: GliaOnMessageUseCase,
    private val loadHistoryUseCase: GliaLoadHistoryUseCase,
    private val addNewMessagesDividerUseCase: AddNewMessagesDividerUseCase,
    private val shouldMarkMessagesReadUseCase: ShouldMarkMessagesReadUseCase,
    private val markMessagesReadWithDelayUseCase: MarkMessagesReadWithDelayUseCase,
    private val appendHistoryChatMessageUseCase: AppendHistoryChatMessageUseCase,
    private val appendNewChatMessageUseCase: AppendNewChatMessageUseCase,
    private val sendUnsentMessagesUseCase: SendUnsentMessagesUseCase,
    private val handleCustomCardClickUseCase: HandleCustomCardClickUseCase,
    private val isAuthenticatedUseCase: IsAuthenticatedUseCase,
    private val isQueueingOrLiveEngagementUseCase: IsQueueingOrLiveEngagementUseCase,
    private val compositeDisposable: CompositeDisposable = CompositeDisposable(),
    private val markMessagesReadDisposable: CompositeDisposable = CompositeDisposable(),
    private val state: BehaviorProcessor<State> = BehaviorProcessor.createDefault(State()),
    private val quickReplies: BehaviorProcessor<List<GvaButton>> = BehaviorProcessor.create(),
    private val action: BehaviorProcessor<Action> = BehaviorProcessor.create(),
    private val historyLoaded: BehaviorProcessor<Boolean> = BehaviorProcessor.createDefault(false)
) {
    fun initialize(
        onHistoryLoaded: (hasHistory: Boolean) -> Unit,
        onQuickReplyReceived: (List<GvaButton>) -> Unit,
        onOperatorMessageReceived: (count: Int) -> Unit
    ): Flowable<List<ChatItem>> {
        compositeDisposable.clear()
        subscribe(onHistoryLoaded, onOperatorMessageReceived, onQuickReplyReceived)
        return state
            .doOnNext(::updateQuickReplies)
            .map(State::immutableChatItems)
            .onBackpressureLatest()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.computation())
            .share()
    }

    @VisibleForTesting
    fun subscribe(
        onHistoryLoaded: (hasHistory: Boolean) -> Unit,
        onOperatorMessageReceived: (count: Int) -> Unit,
        onQuickReplyReceived: (List<GvaButton>) -> Unit
    ) {
        subscribeToState(onHistoryLoaded, onOperatorMessageReceived).also(compositeDisposable::add)
        subscribeToQuickReplies(onQuickReplyReceived).also(compositeDisposable::add)
    }

    fun reset() {
        state.onNext(State())
        quickReplies.onNext(emptyList())
        compositeDisposable.clear()
        markMessagesReadDisposable.clear()
        action.onNext(Action.None)
        historyLoaded.onNext(false)
    }

    fun onChatAction(action: Action) {
        this.action.onNext(action)
    }

    @VisibleForTesting
    fun subscribeToState(onHistoryLoaded: (hasHistory: Boolean) -> Unit, onOperatorMessageReceived: (count: Int) -> Unit): Disposable = state.run {
        loadHistory(onHistoryLoaded)
            .concatWith(subscribeToMessages(onOperatorMessageReceived))
            .doOnError { it.printStackTrace() }
            .subscribe(::onNext, ::onError)
    }

    @VisibleForTesting
    fun loadHistory(onHistoryLoaded: (hasHistory: Boolean) -> Unit): Flowable<State> {
        val historyEvent = if (isAuthenticatedUseCase() || isQueueingOrLiveEngagementUseCase.hasOngoingLiveEngagement) {
            loadHistoryUseCase()
                .doOnSuccess { historyLoaded.onNext(true) }
                .zipWith(state.firstOrError(), ::mapChatHistory)
                .doOnSuccess { onHistoryLoaded(it.chatItems.isNotEmpty()) }
        } else {
            onHistoryLoaded(false)
            state.firstOrError()
        }

        return historyEvent.toFlowable()
    }

    @VisibleForTesting
    fun subscribeToMessages(onOperatorMessageReceived: (count: Int) -> Unit): Flowable<State> = Flowable.merge(onMessage(), onAction())
        .doOnNext { onOperatorMessageReceived(it.addedMessagesCount) }

    @VisibleForTesting
    fun updateQuickReplies(state: State) {
        state.run { chatItems.lastOrNull() as? GvaQuickReplies }
            ?.run { options }
            .orEmpty()
            .also(quickReplies::onNext)
    }

    @VisibleForTesting
    fun subscribeToQuickReplies(onQuickReplyReceived: (List<GvaButton>) -> Unit): Disposable = quickReplies
        .distinctUntilChanged()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { onQuickReplyReceived(it) }

    @VisibleForTesting
    fun onMessage(): Flowable<State> = onMessageUseCase().toFlowable(BackpressureStrategy.BUFFER).withLatestFrom(state, ::mapNewMessage)

    @VisibleForTesting
    fun onAction(): Flowable<State> = action.withLatestFrom(state, ::mapAction)

    @VisibleForTesting
    fun checkUnsentMessages(state: State) {
        val payload = state.preEngagementChatItemIds.takeIf { it.isNotEmpty() }
            ?.firstOrNull()
            ?.let { state.messagePreviews[it] } ?: return

        sendMessage(payload)
    }

    private fun sendMessage(payload: SendMessagePayload) {
        sendUnsentMessagesUseCase(payload, {
            onChatAction(Action.OnMessageSent(it))
        }, {
            onChatAction(Action.OnSendMessageError(payload.messageId))
        })
    }

    @VisibleForTesting
    fun mapChatHistory(historyResponse: ChatHistoryResponse, state: State): State {
        if (historyResponse.items.isEmpty()) return state
        val chatItems: MutableList<ChatItem> = mutableListOf()
        val rawItems = historyResponse.items

        for (index in rawItems.indices.reversed()) {
            val rawMessage = rawItems[index]
            if (state.isNew(rawMessage)) {
                appendHistoryChatMessageUseCase(chatItems, rawMessage, index == rawItems.lastIndex)
            }
        }

        chatItems.reverse()

        if (addNewMessagesDividerUseCase(chatItems, historyResponse.newMessagesCount)) {
            markMessagesReadWithDelay()
        }

        state.lastMessageWithVisibleOperatorImage = chatItems.lastOrNull() as? OperatorChatItem
        state.chatItems.addAll(chatItems)

        return state
    }

    @VisibleForTesting
    fun mapNewMessage(chatMessage: ChatMessageInternal, messagesState: State): State {
        check(chatMessage.chatMessage.isValid()) { "Invalid chat message passed -> ${chatMessage.chatMessage}" }

        if (messagesState.isNew(chatMessage)) {
            appendNewChatMessageUseCase(messagesState, chatMessage)
            if (chatMessage.chatMessage is VisitorMessage) {
                checkUnsentMessages(messagesState)
            } else if (shouldMarkMessagesReadUseCase()) {
                markMessagesReadWithDelay()
            }
        }

        return messagesState
    }

    @VisibleForTesting
    fun mapAction(action: Action, state: State): State {
        return when (action) {
            Action.QueuingStarted -> mapInQueue(state)
            is Action.OperatorConnected -> mapOperatorConnected(action, state)
            Action.Transferring -> mapTransferring(state)
            is Action.OperatorJoined -> mapOperatorJoined(action, state)
            is Action.ResponseCardClicked -> mapResponseCardClicked(action.responseCard, state)
            is Action.OnMediaUpgradeStarted -> mapMediaUpgrade(action.isVideo, state)
            Action.OnMediaUpgradeToVideo -> mapUpgradeMediaToVideo(state)
            Action.OnMediaUpgradeCanceled -> mapMediaUpgradeCanceled(state)
            is Action.OnMediaUpgradeTimerUpdated -> mapMediaUpgradeTimerUpdated(action.formattedValue, state)
            is Action.CustomCardClicked -> mapCustomCardClicked(action, state)
            Action.ChatRestored, Action.None -> state
            is Action.AttachmentPreviewAdded -> mapAttachmentPreviewAdded(action.attachments, action.payload, state)
            is Action.MessagePreviewAdded -> mapMessagePreviewAdded(action.visitorChatItem, action.payload, state)
            is Action.OnMessageSent -> mapNewMessage(ChatMessageInternal(action.message), state)
            is Action.OnSendMessageError -> mapSendMessageFailed(action.messageId, state)
            is Action.OnRetryClicked -> mapRetryClicked(action.messageId, state)
            is Action.OnSendMessageOperatorOffline -> mapSendMessageOperatorOffline(action.messageId, state)
            is Action.OnFileDownloadFailed -> mapFileDownloadFailed(action.attachmentId, state)
            is Action.OnFileDownloadStarted -> mapFileDownloadStarted(action.attachmentId, state)
            is Action.OnFileDownloadSucceeded -> mapFileDownloadSucceeded(action.attachmentId, state)
        }
    }

    @VisibleForTesting
    fun mapFileDownloadSucceeded(attachmentId: String, state: State): State =
        updateRemoteAttachmentState(state, attachmentId, isFileExists = true, isDownloading = false)

    @VisibleForTesting
    fun mapFileDownloadStarted(attachmentId: String, state: State): State =
        updateRemoteAttachmentState(state, attachmentId, isFileExists = false, isDownloading = true)

    @VisibleForTesting
    fun mapFileDownloadFailed(attachmentId: String, state: State): State =
        updateRemoteAttachmentState(state, attachmentId, isFileExists = false, isDownloading = false)

    @VisibleForTesting
    fun updateRemoteAttachmentState(
        state: State,
        attachmentId: String,
        isFileExists: Boolean,
        isDownloading: Boolean
    ): State = state.apply {
        val index = chatItems.indexOfLast { it.id == attachmentId }.takeIf { it != -1 } ?: return@apply

        val item = chatItems[index] as? RemoteAttachmentItem ?: return@apply
        state.chatItems[index] = item.updateWith(isFileExists, isDownloading)
    }

    private fun mapSendMessageOperatorOffline(messageId: String, state: State): State = state.apply {
        preEngagementChatItemIds.add(messageId)
    }

    @VisibleForTesting
    fun mapRetryClicked(messageId: String, state: State): State = state.apply {
        val payload = messagePreviews[messageId] ?: return@apply

        sendMessage(payload)

        chatItems.indexOfLast { (it as? TapToRetryItem)?.messageId == payload.messageId }
            .takeIf { it != -1 }
            ?.also { chatItems.removeAt(it) }

        val files = (payload.attachment as? FilesAttachment)?.files

        val messageIndex = chatItems.indexOfLast { it.id == payload.messageId }

        if (messageIndex != -1) {
            chatItems[messageIndex] = (chatItems[messageIndex] as VisitorChatItem).copyWithError(false)
        }

        files?.forEach { attachment ->
            val index = chatItems.indexOfLast { it.id == attachment.id }

            chatItems[index] = (chatItems[index] as VisitorChatItem).copyWithError(false)
        }
    }

    @VisibleForTesting
    fun mapSendMessageFailed(messageId: String, state: State): State = state.apply {
        val payload = messagePreviews[messageId] ?: return@apply

        val files = (payload.attachment as? FilesAttachment)?.files.orEmpty()

        val messageIndex = chatItems.indexOfLast { it.id == payload.messageId }

        if (messageIndex != -1) {
            chatItems[messageIndex] = (chatItems[messageIndex] as VisitorChatItem).copyWithError(true)
        }

        files.forEach { attachment ->
            val index = chatItems.indexOfLast { it.id == attachment.id }

            chatItems[index] = (chatItems[index] as VisitorChatItem).copyWithError(true)
        }

        val lastItemIndex = if (files.isNotEmpty()) {
            chatItems.indexOfLast { (it as? LocalAttachmentItem)?.messageId == payload.messageId }
        } else {
            messageIndex
        }

        chatItems.add(lastItemIndex + 1, TapToRetryItem(messageId = payload.messageId))
    }

    @VisibleForTesting
    fun mapMessagePreviewAdded(visitorChatItem: VisitorChatItem, payload: SendMessagePayload, state: State): State = state.apply {
        val index = indexForMessageItem(chatItems)
        chatItems.add(index, visitorChatItem)
        messagePreviews[payload.messageId] = payload
    }

    @VisibleForTesting
    fun mapAttachmentPreviewAdded(attachments: List<VisitorAttachmentItem>, payload: SendMessagePayload?, state: State): State = state.apply {
        payload?.also { messagePreviews[it.messageId] = it }
        chatItems.addAll(attachments)
    }

    @VisibleForTesting
    fun mapCustomCardClicked(action: Action.CustomCardClicked, state: State): State = action.run {
        handleCustomCardClickUseCase(customCard, attachment, state)
    }

    @VisibleForTesting
    fun mapMediaUpgradeTimerUpdated(formattedValue: String, state: State): State = state.apply {
        val oldItem = state.mediaUpgradeTimerItem ?: return@apply

        if (oldItem.time == formattedValue) return@apply

        val newItem = oldItem.updateTime(formattedValue)

        mediaUpgradeTimerItem = newItem

        val index = chatItems.indexOf(oldItem)

        if (index == -1) {
            chatItems += newItem
        } else {
            chatItems[index] = newItem
        }
    }

    @VisibleForTesting
    fun mapMediaUpgradeCanceled(state: State): State = state.apply {
        val oldItem = mediaUpgradeTimerItem
        mediaUpgradeTimerItem = null
        chatItems -= oldItem ?: return@apply
    }

    @VisibleForTesting
    fun mapUpgradeMediaToVideo(state: State): State = state.apply {
        val oldItem = this.mediaUpgradeTimerItem
        val newItem = MediaUpgradeStartedTimerItem.Video(oldItem?.time ?: DateUtils.formatElapsedTime(0))
        mediaUpgradeTimerItem = newItem
        chatItems += newItem
        chatItems -= oldItem ?: return@apply
    }

    @VisibleForTesting
    fun mapMediaUpgrade(video: Boolean, state: State): State = state.apply {
        val mediaUpgradeTimerItem = if (video) MediaUpgradeStartedTimerItem.Video() else MediaUpgradeStartedTimerItem.Audio()
        this.mediaUpgradeTimerItem = mediaUpgradeTimerItem
        chatItems += mediaUpgradeTimerItem
    }

    @VisibleForTesting
    fun mapOperatorJoined(action: Action.OperatorJoined, state: State): State = state.apply {
        chatItems -= OperatorStatusItem.Transferring
        chatItems += action.run {
            OperatorStatusItem.Joined(operatorFormattedName, operatorImageUrl)
        }
    }

    @VisibleForTesting
    fun mapResponseCardClicked(responseCard: OperatorMessageItem.ResponseCard, state: State): State = state.apply {
        val index = chatItems.indexOf(responseCard)
        chatItems[index] = responseCard.asPlainText()
    }

    /**
     * Returns the index where the new message should be placed.
     * If engagement hasn't started yet, the message should be placed before the operator status item.
     * */
    private fun indexForMessageItem(chatItems: List<ChatItem>) =
        if (chatItems.lastOrNull() is OperatorStatusItem.InQueue) chatItems.lastIndex else chatItems.lastIndex + 1

    @VisibleForTesting
    fun mapOperatorConnected(action: Action.OperatorConnected, state: State): State {
        val operatorStatusItem = action.run { OperatorStatusItem.Connected(operatorFormattedName, operatorImageUrl) }
        val oldOperatorStatusItem: OperatorStatusItem? = state.operatorStatusItem
        state.operatorStatusItem = operatorStatusItem

        checkUnsentMessages(state)

        if (oldOperatorStatusItem != null) {
            val index = state.chatItems.indexOf(oldOperatorStatusItem)

            if (index != -1) {
                state.chatItems[index] = operatorStatusItem
                return state
            }
        }

        state.chatItems += operatorStatusItem
        state.resetOperator()

        return state
    }

    @VisibleForTesting
    fun mapTransferring(state: State): State = state.apply {
        operatorStatusItem?.also { chatItems -= it }
        operatorStatusItem = OperatorStatusItem.Transferring.also {
            chatItems += it
        }
    }

    @VisibleForTesting
    fun mapInQueue(state: State): State = state.apply {
        OperatorStatusItem.InQueue.also {
            operatorStatusItem = it
            val isQueueingItemAlreadyDisplayed = chatItems.size > 0 && chatItems[chatItems.lastIndex] == it
            if (!isQueueingItemAlreadyDisplayed) chatItems += it
        }
    }

    @VisibleForTesting
    fun markMessagesReadWithDelay() {
        markMessagesReadDisposable.clear()
        val disposable = markMessagesReadWithDelayUseCase()
            .andThen(state.firstOrError())
            .map(::removeNewMessagesDivider)
            .subscribe(state::onNext, Throwable::printStackTrace)
        markMessagesReadDisposable.add(disposable)
    }

    @VisibleForTesting
    fun removeNewMessagesDivider(messagesState: State) = messagesState.apply {
        chatItems.remove(NewMessagesDividerItem)
    }

    fun reloadHistoryIfNeeded() {
        val loadHistory = historyLoaded.firstElement()
            .filter { !it }
            .flatMap { loadHistoryUseCase().toMaybe() }
            .zipWith(state.firstElement(), ::mapChatHistory)
            .subscribe(state::onNext, { Logger.e(TAG, "Chat reload failed", it) }, { Logger.i(TAG, "Chat history is already loaded") })

        compositeDisposable.add(loadHistory)
    }

    internal data class State(
        val chatItems: MutableList<ChatItem> = mutableListOf(),
        val chatItemIds: MutableSet<String> = mutableSetOf(),
        val preEngagementChatItemIds: LinkedHashSet<String> = linkedSetOf(),
        val messagePreviews: LinkedHashMap<String, SendMessagePayload> = LinkedHashMap(),
        var lastMessageWithVisibleOperatorImage: OperatorChatItem? = null,
        var operatorStatusItem: OperatorStatusItem? = null,
        var mediaUpgradeTimerItem: MediaUpgradeStartedTimerItem? = null,
        var addedMessagesCount: Int = 0
    ) {
        val immutableChatItems: List<ChatItem> get() = chatItems.toList()

        fun isNew(chatMessageInternal: ChatMessageInternal): Boolean = chatItemIds.add(chatMessageInternal.chatMessage.id)

        fun isOperatorChanged(operatorChatItem: OperatorChatItem): Boolean = lastMessageWithVisibleOperatorImage.let {
            lastMessageWithVisibleOperatorImage = operatorChatItem
            it?.operatorId != operatorChatItem.operatorId
        }

        fun resetOperator() {
            lastMessageWithVisibleOperatorImage = null
        }
    }

    internal sealed interface Action {
        data object QueuingStarted : Action
        data class OperatorConnected(val operatorFormattedName: String, val operatorImageUrl: String?) : Action
        data object Transferring : Action
        data class OperatorJoined(val operatorFormattedName: String, val operatorImageUrl: String?) : Action
        data class ResponseCardClicked(val responseCard: OperatorMessageItem.ResponseCard) : Action
        data class OnMediaUpgradeStarted(val isVideo: Boolean) : Action
        data class OnMediaUpgradeTimerUpdated(val formattedValue: String) : Action
        data object OnMediaUpgradeToVideo : Action
        data object OnMediaUpgradeCanceled : Action
        data class CustomCardClicked(val customCard: CustomCardChatItem, val attachment: SingleChoiceAttachment) : Action
        data object ChatRestored : Action
        data object None : Action
        data class MessagePreviewAdded(val visitorChatItem: VisitorChatItem, val payload: SendMessagePayload) : Action
        data class AttachmentPreviewAdded(val attachments: List<VisitorAttachmentItem>, val payload: SendMessagePayload?) : Action
        data class OnMessageSent(val message: VisitorMessage) : Action
        data class OnSendMessageError(val messageId: String) : Action
        data class OnRetryClicked(val messageId: String) : Action
        data class OnSendMessageOperatorOffline(val messageId: String) : Action
        data class OnFileDownloadStarted(val attachmentId: String) : Action
        data class OnFileDownloadSucceeded(val attachmentId: String) : Action
        data class OnFileDownloadFailed(val attachmentId: String) : Action
    }
}
