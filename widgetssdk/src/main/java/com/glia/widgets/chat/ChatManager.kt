package com.glia.widgets.chat

import android.text.format.DateUtils
import androidx.annotation.VisibleForTesting
import com.glia.androidsdk.chat.OperatorMessage
import com.glia.androidsdk.chat.SingleChoiceAttachment
import com.glia.androidsdk.chat.SystemMessage
import com.glia.androidsdk.chat.VisitorMessage
import com.glia.telemetry_lib.EventAttribute
import com.glia.telemetry_lib.GliaLogger
import com.glia.telemetry_lib.LogEvents
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
import com.glia.widgets.chat.model.MediaUpgradeStartedTimerItem
import com.glia.widgets.chat.model.NewMessagesDividerItem
import com.glia.widgets.chat.model.OperatorChatItem
import com.glia.widgets.chat.model.OperatorMessageItem
import com.glia.widgets.chat.model.OperatorStatusItem
import com.glia.widgets.chat.model.OutgoingMessage
import com.glia.widgets.chat.model.RemoteAttachmentItem
import com.glia.widgets.chat.model.TapToRetryItem
import com.glia.widgets.chat.model.VisitorAttachmentItem
import com.glia.widgets.chat.model.VisitorChatItem
import com.glia.widgets.engagement.domain.IsQueueingOrLiveEngagementUseCase
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.TAG
import com.glia.widgets.helper.isValid
import com.glia.widgets.internal.engagement.domain.model.ChatHistoryResponse
import com.glia.widgets.internal.engagement.domain.model.ChatMessageInternal
import com.glia.widgets.internal.secureconversations.domain.MarkMessagesReadWithDelayUseCase
import com.glia.widgets.internal.secureconversations.domain.ShouldMarkMessagesReadUseCase
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.processors.BehaviorProcessor
import io.reactivex.rxjava3.processors.FlowableProcessor
import io.reactivex.rxjava3.processors.UnicastProcessor
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
    private val historyLoaded: BehaviorProcessor<Boolean> = BehaviorProcessor.createDefault(false),
    // Opens once the transcript has been applied or given up on, which is not the same as
    // `historyLoaded` - that one stays false after a failure so the transcript is retried.
    private val historySettled: BehaviorProcessor<Boolean> = BehaviorProcessor.createDefault(false)
) {
    // Send callbacks invoke onChatAction from Core's background threads - BehaviorProcessor.onNext
    // is not thread-safe, so emissions must go through this serialized wrapper.
    private val serializedAction: FlowableProcessor<Action> = action.toSerialized()

    // The transcript can settle from the initial load or from a retry, on whichever Core thread
    // called back, while subscribeToState() opens it from the main thread - so writes go through
    // this serialized wrapper for the same reason `action` does.
    private val serializedHistorySettled: FlowableProcessor<Boolean> = historySettled.toSerialized()

    fun initialize(
        onHistoryLoaded: (hasHistory: Boolean) -> Unit,
        onHistoryLoadFailed: (Throwable) -> Unit,
        onQuickReplyReceived: (List<GvaButton>) -> Unit,
        onOperatorMessageReceived: (count: Int) -> Unit
    ): Flowable<List<ChatItem>> {
        compositeDisposable.clear()
        subscribe(onHistoryLoaded, onHistoryLoadFailed, onOperatorMessageReceived, onQuickReplyReceived)
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
        onHistoryLoadFailed: (Throwable) -> Unit,
        onOperatorMessageReceived: (count: Int) -> Unit,
        onQuickReplyReceived: (List<GvaButton>) -> Unit
    ) {
        subscribeToState(onHistoryLoaded, onHistoryLoadFailed, onOperatorMessageReceived).also(compositeDisposable::add)
        subscribeToQuickReplies(onQuickReplyReceived).also(compositeDisposable::add)
    }

    fun reset() {
        state.onNext(State())
        quickReplies.onNext(emptyList())
        compositeDisposable.clear()
        markMessagesReadDisposable.clear()
        action.onNext(Action.None)
        historyLoaded.onNext(false)
        serializedHistorySettled.onNext(false)
    }

    fun onChatAction(action: Action) {
        serializedAction.onNext(action)
    }

    @VisibleForTesting
    fun subscribeToState(
        onHistoryLoaded: (hasHistory: Boolean) -> Unit,
        onHistoryLoadFailed: (Throwable) -> Unit,
        onOperatorMessageReceived: (count: Int) -> Unit
    ): Disposable {
        serializedHistorySettled.onNext(false)

        // Core registers its CHAT_MESSAGE listener only when the message stream is subscribed, and
        // `action` retains no more than its latest value, so neither survives a late subscription.
        // Both are buffered from here and drained once the transcript has been applied, which keeps
        // history above the messages that followed it without losing anything that arrived while
        // the request was in flight.
        val messages: UnicastProcessor<ChatMessageInternal> = UnicastProcessor.create()
        val actions: UnicastProcessor<Action> = UnicastProcessor.create()

        // `state` is a BehaviorProcessor: terminating it drops every later emission, including a
        // successful retry from reloadHistoryIfNeeded(), and reset() cannot revive it. Failures
        // here end their own stream and leave the screen alive.
        return CompositeDisposable(
            onMessageUseCase().toFlowable(BackpressureStrategy.BUFFER).subscribe(messages::onNext, messages::onError),
            action.subscribe(actions::onNext, actions::onError),
            loadHistory(onHistoryLoaded, onHistoryLoadFailed).subscribe(state::onNext) { Logger.e(TAG, "Chat history stream failed", it) },
            subscribeToMessages(messages, actions, onOperatorMessageReceived)
                .delaySubscription(historySettled.filter { it })
                .subscribe(state::onNext) { Logger.e(TAG, "Chat message stream failed", it) }
        )
    }

    @VisibleForTesting
    fun loadHistory(onHistoryLoaded: (hasHistory: Boolean) -> Unit, onHistoryLoadFailed: (Throwable) -> Unit): Flowable<State> {
        val historyEvent = if (isAuthenticatedUseCase() || isQueueingOrLiveEngagementUseCase.hasOngoingLiveEngagement) {
            loadHistoryUseCase()
                .doOnSuccess { historyLoaded.onNext(true) }
                .zipWith(state.firstOrError(), ::mapChatHistory)
                // Core calls back on its own threads and the callbacks below reach the screen.
                .observeOn(AndroidSchedulers.mainThread())
                // A rejected transcript still has to initialize the screen - `onHistoryLoaded` is
                // what brings the chat input up - and reloadHistoryIfNeeded() retries it once the
                // engagement the visitor is actually in has started. The failure is reported to the
                // screen rather than terminating the stream, so that retry can still land.
                .onErrorResumeNext {
                    // Warn, not error: every mid-engagement authentication takes this path and
                    // recovers through the engagement-start reload, so nothing here needs
                    // investigating on its own - only a spike does, and error would forward every
                    // occurrence to Sentry. The variants that do lose the screen are logged at
                    // error by the consumer.
                    Logger.w(TAG, "Chat history load failed: ${it.message}")
                    onHistoryLoadFailed(it)
                    state.firstOrError()
                }
                .doOnSuccess { onHistoryLoaded(it.chatItems.isNotEmpty()) }
                .doAfterSuccess { serializedHistorySettled.onNext(true) }
        } else {
            onHistoryLoaded(false)
            serializedHistorySettled.onNext(true)
            state.firstOrError()
        }

        return historyEvent.toFlowable()
    }

    @VisibleForTesting
    fun subscribeToMessages(
        messages: Flowable<ChatMessageInternal>,
        actions: Flowable<Action>,
        onOperatorMessageReceived: (count: Int) -> Unit
    ): Flowable<State> = Flowable.merge(onMessage(messages), onAction(actions))
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

    // Core invokes send callbacks and emits CHAT_MESSAGE events on background threads, so the
    // send-success path and the message-stream echo can arrive concurrently. State is mutable and
    // not thread-safe - hop to the main thread before mapping so `isNew` reconciliation never
    // processes the same message twice (which duplicated delivered messages).
    @VisibleForTesting
    fun onMessage(messages: Flowable<ChatMessageInternal>): Flowable<State> = messages
        .observeOn(AndroidSchedulers.mainThread())
        .withLatestFrom(state, ::mapNewMessage)

    @VisibleForTesting
    fun onAction(actions: Flowable<Action>): Flowable<State> = actions
        .observeOn(AndroidSchedulers.mainThread())
        .withLatestFrom(state, ::mapAction)

    @VisibleForTesting
    fun checkUnsentMessages(state: State) {
        val payload = state.preEngagementChatItemIds.takeIf { it.isNotEmpty() }
            ?.firstOrNull()
            ?.let { state.messagePreviews[it] } ?: return

        sendMessage(payload)
    }

    private fun sendMessage(payload: OutgoingMessage) {
        sendUnsentMessagesUseCase(payload, {
            onChatAction(Action.OnMessageSent(payload.messageId))
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
            if (state.isNew(rawMessage.chatMessage.id)) {
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

    /**
     * Handles a send-API success confirmation: marks the optimistic item with [messageId] as
     * delivered and tries the next unsent message. Skipped when the message was already
     * reconciled through the incoming message stream.
     */
    @VisibleForTesting
    fun mapMessageSent(messageId: String, messagesState: State): State {
        if (messagesState.isNew(messageId)) {
            appendNewChatMessageUseCase.markMessageDelivered(messageId, messagesState)
            checkUnsentMessages(messagesState)
        }

        return messagesState
    }

    @VisibleForTesting
    fun mapNewMessage(chatMessage: ChatMessageInternal, messagesState: State): State {
        // Never interpolate the message itself - ChatMessage.toString() carries the message
        // content and the operator name, and this throwable is logged at error, which ships to
        // Kibana and Sentry in release builds.
        check(chatMessage.chatMessage.isValid()) {
            "Invalid chat message passed -> id=${chatMessage.chatMessage.id}, senderType=${chatMessage.chatMessage.senderType}"
        }

        if (messagesState.isNew(chatMessage.chatMessage.id)) {
            appendNewChatMessageUseCase(messagesState, chatMessage)
            if (chatMessage.chatMessage is VisitorMessage) {
                checkUnsentMessages(messagesState)
            } else if (shouldMarkMessagesReadUseCase()) {
                markMessagesReadWithDelay()
            }

            GliaLogger.i(LogEvents.CHAT_SCREEN_MESSAGE_SHOWN) {
                put(EventAttribute.MessageId, chatMessage.chatMessage.id)
                put(EventAttribute.MessageSender, chatMessage.chatMessage.senderType.toString())
                val messageType = when (chatMessage.chatMessage) {
                    is VisitorMessage -> "visitor"
                    is OperatorMessage -> "operator"
                    is SystemMessage -> "system"
                    else -> "unknown"
                }
                put(EventAttribute.MessageType, messageType)
            }
        } else {
            // The message was already reconciled through the send-success callback, which carries
            // no content - the echo is still the source of truth for the displayed text.
            appendNewChatMessageUseCase.updateVisitorMessageContent(chatMessage, messagesState)
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
            is Action.AttachmentPreviewAdded -> mapAttachmentPreviewAdded(action.attachment, action.outgoingMessage, state)
            is Action.MessagePreviewAdded -> mapMessagePreviewAdded(action.visitorChatItem, action.payload, state)
            is Action.OnMessageSent -> mapMessageSent(action.messageId, state)
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

        val messageIndex = chatItems.indexOfLast { it.id == payload.messageId }

        if (messageIndex != -1) {
            chatItems[messageIndex] = (chatItems[messageIndex] as VisitorChatItem).copyWithError(false)
        }
    }

    @VisibleForTesting
    fun mapSendMessageFailed(messageId: String, state: State): State = state.apply {
        val payload = messagePreviews[messageId] ?: return@apply

        val messageIndex = chatItems.indexOfLast { it.id == payload.messageId }

        if (messageIndex != -1) {
            chatItems[messageIndex] = (chatItems[messageIndex] as VisitorChatItem).copyWithError(true)
        }

        chatItems.add(messageIndex + 1, TapToRetryItem(messageId = payload.messageId))
    }

    @VisibleForTesting
    fun mapMessagePreviewAdded(visitorChatItem: VisitorChatItem, payload: OutgoingMessage, state: State): State = state.apply {
        val index = indexForMessageItem(chatItems)
        chatItems.add(index, visitorChatItem)
        messagePreviews[payload.messageId] = payload
    }

    @VisibleForTesting
    fun mapAttachmentPreviewAdded(attachment: VisitorAttachmentItem, outgoingMessage: OutgoingMessage, state: State): State = state.apply {
        messagePreviews[attachment.id] = outgoingMessage
        chatItems.add(attachment)
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
        val previous = operatorStatusItem
        if (previous is OperatorStatusItem.InQueue) {
            chatItems -= previous
        }
        operatorStatusItem = OperatorStatusItem.Transferring
        if (!chatItems.contains(OperatorStatusItem.Transferring)) {
            chatItems += OperatorStatusItem.Transferring
        }
    }

    @VisibleForTesting
    fun mapInQueue(state: State): State = state.apply {
        OperatorStatusItem.InQueue.also {
            operatorStatusItem = it
            val isQueueingItemAlreadyDisplayed = chatItems.isNotEmpty() && chatItems[chatItems.lastIndex] == it
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

    /**
     * A transcript request issued against an engagement being replaced can fail, or produce no
     * outcome at all. In the latter case this retry is the only thing that settles the history
     * stage and releases the buffered live messages for a screen opened during the replacement.
     *
     * A transcript that only arrives on the retry is appended below the live messages that
     * rendered in the meantime, because [mapChatHistory] always appends. The visitor keeps the
     * newer messages and gains the older ones under them.
     */
    fun reloadHistoryIfNeeded() {
        val loadHistory = historyLoaded.firstElement()
            .filter { !it }
            .flatMap { loadHistoryUseCase().toMaybe() }
            // By the time a retry runs, the message stream is writing the same mutable State from
            // the main thread. Applying the transcript there too keeps a single writer.
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSuccess { historyLoaded.onNext(true) }
            .zipWith(state.firstElement(), ::mapChatHistory)
            .doAfterSuccess { serializedHistorySettled.onNext(true) }
            .subscribe(state::onNext, { Logger.e(TAG, "Chat reload failed", it) }, { Logger.i(TAG, "Chat history is already loaded") })

        compositeDisposable.add(loadHistory)
    }

    internal data class State(
        val chatItems: MutableList<ChatItem> = mutableListOf(),
        val chatItemIds: MutableSet<String> = mutableSetOf(),
        val preEngagementChatItemIds: LinkedHashSet<String> = linkedSetOf(),
        val messagePreviews: LinkedHashMap<String, OutgoingMessage> = LinkedHashMap(),
        var lastMessageWithVisibleOperatorImage: OperatorChatItem? = null,
        var operatorStatusItem: OperatorStatusItem? = null,
        var mediaUpgradeTimerItem: MediaUpgradeStartedTimerItem? = null,
        var addedMessagesCount: Int = 0
    ) {
        val immutableChatItems: List<ChatItem> get() = chatItems.toList()

        fun isNew(messageId: String): Boolean = chatItemIds.add(messageId)

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
        data class MessagePreviewAdded(val visitorChatItem: VisitorChatItem, val payload: OutgoingMessage) : Action
        data class AttachmentPreviewAdded(val attachment: VisitorAttachmentItem, val outgoingMessage: OutgoingMessage) : Action
        data class OnMessageSent(val messageId: String) : Action
        data class OnSendMessageError(val messageId: String) : Action
        data class OnRetryClicked(val messageId: String) : Action
        data class OnSendMessageOperatorOffline(val messageId: String) : Action
        data class OnFileDownloadStarted(val attachmentId: String) : Action
        data class OnFileDownloadSucceeded(val attachmentId: String) : Action
        data class OnFileDownloadFailed(val attachmentId: String) : Action
    }
}
