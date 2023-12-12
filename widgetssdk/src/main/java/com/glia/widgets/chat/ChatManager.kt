package com.glia.widgets.chat

import android.text.format.DateUtils
import androidx.annotation.VisibleForTesting
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
import com.glia.widgets.chat.model.MediaUpgradeStartedTimerItem
import com.glia.widgets.chat.model.NewMessagesDividerItem
import com.glia.widgets.chat.model.OperatorChatItem
import com.glia.widgets.chat.model.OperatorMessageItem
import com.glia.widgets.chat.model.OperatorStatusItem
import com.glia.widgets.chat.model.Unsent
import com.glia.widgets.core.engagement.domain.IsOngoingEngagementUseCase
import com.glia.widgets.core.engagement.domain.model.ChatHistoryResponse
import com.glia.widgets.core.engagement.domain.model.ChatMessageInternal
import com.glia.widgets.core.secureconversations.domain.MarkMessagesReadWithDelayUseCase
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.TAG
import com.glia.widgets.helper.isValid
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.processors.BehaviorProcessor
import io.reactivex.schedulers.Schedulers

internal class ChatManager(
    private val onMessageUseCase: GliaOnMessageUseCase,
    private val loadHistoryUseCase: GliaLoadHistoryUseCase,
    private val addNewMessagesDividerUseCase: AddNewMessagesDividerUseCase,
    private val markMessagesReadWithDelayUseCase: MarkMessagesReadWithDelayUseCase,
    private val appendHistoryChatMessageUseCase: AppendHistoryChatMessageUseCase,
    private val appendNewChatMessageUseCase: AppendNewChatMessageUseCase,
    private val sendUnsentMessagesUseCase: SendUnsentMessagesUseCase,
    private val handleCustomCardClickUseCase: HandleCustomCardClickUseCase,
    private val isAuthenticatedUseCase: IsAuthenticatedUseCase,
    private val isOngoingEngagementUseCase: IsOngoingEngagementUseCase,
    private val compositeDisposable: CompositeDisposable = CompositeDisposable(),
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
        subscribe(onHistoryLoaded, onOperatorMessageReceived, onQuickReplyReceived)
        return state
            .doOnNext(::updateQuickReplies)
            .map(State::immutableChatItems)
            .onBackpressureLatest()
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
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.computation())
            .subscribe(::onNext, ::onError)
    }

    @VisibleForTesting
    fun loadHistory(onHistoryLoaded: (hasHistory: Boolean) -> Unit): Flowable<State> {
        val historyEvent = if (isAuthenticatedUseCase() || isOngoingEngagementUseCase()) {
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
        val unsent = state.unsentItems.firstOrNull { it.error == null } ?: return
        sendUnsentMessagesUseCase(unsent, {
            onChatAction(Action.MessageSent(it))
        }, {
            onChatAction(Action.RemoveMessage(unsent.messageId))
            onChatAction(Action.MessageSendError(unsent.copy(error = it)))
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
            }
        }

        return messagesState
    }

    @VisibleForTesting
    fun mapAction(action: Action, state: State): State {
        return when (action) {
            is Action.QueuingStarted -> mapInQueue(action.companyName, state)
            is Action.OperatorConnected -> mapOperatorConnected(action, state)
            Action.Transferring -> mapTransferring(state)
            is Action.OperatorJoined -> mapOperatorJoined(action, state)
            is Action.UnsentMessageReceived -> addUnsentMessage(action.message, state)
            is Action.ResponseCardClicked -> mapResponseCardClicked(action.responseCard, state)
            is Action.OnMediaUpgradeStarted -> mapMediaUpgrade(action.isVideo, state)
            Action.OnMediaUpgradeToVideo -> mapUpgradeMediaToVideo(state)
            Action.OnMediaUpgradeCanceled -> mapMediaUpgradeCanceled(state)
            is Action.OnMediaUpgradeTimerUpdated -> mapMediaUpgradeTimerUpdated(action.formattedValue, state)
            is Action.CustomCardClicked -> mapCustomCardClicked(action, state)
            Action.ChatRestored, Action.None -> state
            is Action.MessageClicked -> mapMessageClicked(action.messageId, state)
            is Action.MessageSent -> mapMessageSent(action.message, state)
            is Action.MessageSendError -> addErrorMessage(action.message, state)
            is Action.RemoveMessage -> removeMessage(action.messageId, state)
        }
    }

    @VisibleForTesting
    fun mapMessageSent(message: VisitorMessage, state: State): State = mapNewMessage(ChatMessageInternal(message), state)

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
        chatItems += action.run {
            OperatorStatusItem.Joined(companyName, operatorFormattedName, operatorImageUrl)
        }
    }

    @VisibleForTesting
    fun mapResponseCardClicked(responseCard: OperatorMessageItem.ResponseCard, state: State): State = state.apply {
        val index = chatItems.indexOf(responseCard)
        chatItems[index] = responseCard.asPlainText()
    }

    @VisibleForTesting
    fun mapMessageClicked(messageId: String, state: State): State {
        val unsent = state.unsentItems.firstOrNull { it.messageId == messageId }

        return if (unsent != null) {
            state.apply {
                onChatAction(Action.RemoveMessage(messageId))
                sendUnsentMessagesUseCase(unsent, {
                    onChatAction(Action.MessageSent(it))
                }, {
                    onChatAction(Action.MessageSendError(unsent))
                })
            }
        } else {
            state
        }
    }

    @VisibleForTesting
    fun addUnsentMessage(message: Unsent, state: State): State {
        state.unsentItems += message
        return state.apply {
            message.chatMessage?.let {
                val index = indexForMessageItem(chatItems)
                chatItems.add(index, it)
            }
        }
    }

    @VisibleForTesting
    fun addErrorMessage(message: Unsent, state: State): State {
        return state.apply {
            unsentItems += message

            message.chatMessage?.let {
                val index = indexForMessageItem(chatItems)
                chatItems.add(index, it)
            }
            message.attachmentItems?.let {
                chatItems += it
            }
        }
    }

    /**
     * Returns the index where the new message should be placed.
     * If engagement hasn't started yet, the message should placed before the operator status item.
     * */
    private fun indexForMessageItem(chatItems: List<ChatItem>) =
        if (chatItems.lastOrNull() is OperatorStatusItem.InQueue) chatItems.lastIndex else chatItems.lastIndex + 1

    @VisibleForTesting
    fun removeMessage(messageId: String, state: State): State {
        return state.apply {
            unsentItems.removeIf { it.messageId == messageId }
            chatItems.removeAll { it.id == messageId }

            checkUnsentMessages(state)
        }
    }

    @VisibleForTesting
    fun mapOperatorConnected(action: Action.OperatorConnected, state: State): State {
        val operatorStatusItem = action.run { OperatorStatusItem.Connected(companyName, operatorFormattedName, operatorImageUrl) }
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
    fun mapInQueue(companyName: String, state: State): State = state.apply {
        OperatorStatusItem.InQueue(companyName).also {
            operatorStatusItem = it
            chatItems += it
        }
    }

    @VisibleForTesting
    fun markMessagesReadWithDelay() {
        val disposable = markMessagesReadWithDelayUseCase()
            .andThen(state.firstOrError())
            .map(::removeNewMessagesDivider)
            .subscribe(state::onNext, Throwable::printStackTrace)
        compositeDisposable.add(disposable)
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
        val unsentItems: MutableList<Unsent> = mutableListOf(),
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
        data class QueuingStarted(val companyName: String) : Action
        data class OperatorConnected(val companyName: String, val operatorFormattedName: String, val operatorImageUrl: String?) : Action
        object Transferring : Action
        data class OperatorJoined(val companyName: String, val operatorFormattedName: String, val operatorImageUrl: String?) : Action
        data class UnsentMessageReceived(val message: Unsent) : Action
        data class ResponseCardClicked(val responseCard: OperatorMessageItem.ResponseCard) : Action
        data class OnMediaUpgradeStarted(val isVideo: Boolean) : Action
        data class OnMediaUpgradeTimerUpdated(val formattedValue: String) : Action
        object OnMediaUpgradeToVideo : Action
        object OnMediaUpgradeCanceled : Action
        data class CustomCardClicked(val customCard: CustomCardChatItem, val attachment: SingleChoiceAttachment) : Action
        object ChatRestored : Action
        object None : Action
        data class MessageClicked(val messageId: String) : Action
        data class MessageSent(val message: VisitorMessage) : Action
        data class MessageSendError(val message: Unsent) : Action
        data class RemoveMessage(val messageId: String) : Action
    }
}
