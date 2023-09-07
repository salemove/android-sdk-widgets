package com.glia.widgets.chat

import com.glia.androidsdk.chat.ChatMessage
import com.glia.androidsdk.chat.OperatorMessage
import com.glia.androidsdk.chat.SystemMessage
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
import com.glia.widgets.chat.model.GvaButton
import com.glia.widgets.chat.model.GvaQuickReplies
import com.glia.widgets.chat.model.MediaUpgradeStartedTimerItem
import com.glia.widgets.chat.model.NewMessagesDividerItem
import com.glia.widgets.chat.model.OperatorMessageItem
import com.glia.widgets.chat.model.OperatorStatusItem
import com.glia.widgets.chat.model.Unsent
import com.glia.widgets.core.engagement.domain.model.ChatHistoryResponse
import com.glia.widgets.core.engagement.domain.model.ChatMessageInternal
import com.glia.widgets.core.secureconversations.domain.MarkMessagesReadWithDelayUseCase
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.processors.BehaviorProcessor
import io.reactivex.processors.PublishProcessor
import io.reactivex.schedulers.Schedulers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.atLeastOnce
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.isNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.spy
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import java.util.UUID


@RunWith(RobolectricTestRunner::class)
class ChatManagerTest {
    private lateinit var onMessageUseCase: GliaOnMessageUseCase
    private lateinit var loadHistoryUseCase: GliaLoadHistoryUseCase
    private lateinit var addNewMessagesDividerUseCase: AddNewMessagesDividerUseCase
    private lateinit var markMessagesReadWithDelayUseCase: MarkMessagesReadWithDelayUseCase
    private lateinit var appendHistoryChatMessageUseCase: AppendHistoryChatMessageUseCase
    private lateinit var appendNewChatMessageUseCase: AppendNewChatMessageUseCase
    private lateinit var sendUnsentMessagesUseCase: SendUnsentMessagesUseCase
    private lateinit var handleCustomCardClickUseCase: HandleCustomCardClickUseCase
    private lateinit var isAuthenticatedUseCase: IsAuthenticatedUseCase
    private lateinit var subjectUnderTest: ChatManager
    private lateinit var state: ChatManager.State
    private lateinit var compositeDisposable: CompositeDisposable
    private lateinit var stateProcessor: BehaviorProcessor<ChatManager.State>
    private lateinit var quickReplies: BehaviorProcessor<List<GvaButton>>
    private lateinit var action: PublishProcessor<ChatManager.Action>

    @Before
    fun setUp() {
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }
        onMessageUseCase = mock()
        loadHistoryUseCase = mock()
        addNewMessagesDividerUseCase = mock()
        markMessagesReadWithDelayUseCase = mock()
        appendHistoryChatMessageUseCase = mock()
        appendNewChatMessageUseCase = mock()
        sendUnsentMessagesUseCase = mock()
        handleCustomCardClickUseCase = mock()
        isAuthenticatedUseCase = mock()
        compositeDisposable = spy()
        stateProcessor = spy(BehaviorProcessor.create())
        quickReplies = BehaviorProcessor.create()
        action = PublishProcessor.create()

        subjectUnderTest = spy(
            ChatManager(
                onMessageUseCase,
                loadHistoryUseCase,
                addNewMessagesDividerUseCase,
                markMessagesReadWithDelayUseCase,
                appendHistoryChatMessageUseCase,
                appendNewChatMessageUseCase,
                sendUnsentMessagesUseCase,
                handleCustomCardClickUseCase,
                isAuthenticatedUseCase,
                compositeDisposable,
                stateProcessor,
                quickReplies,
                action
            )
        )

        state = spy(ChatManager.State())
    }

    @Test
    fun `removeNewMessagesDivider removes NewMessagesDivider when it contains in chat items`() {
        state.chatItems.add(NewMessagesDividerItem)
        subjectUnderTest.removeNewMessagesDivider(state)
        assertTrue(state.chatItems.isEmpty())
    }

    @Test
    fun `markMessagesReadWithDelay removes NewMessagesDivider when it contains in chat items`() {
        state.chatItems.apply {
            add(mock())
            add(NewMessagesDividerItem)
            add(mock())
        }
        stateProcessor.onNext(state)
        whenever(markMessagesReadWithDelayUseCase()) doReturn Completable.complete()
        assertTrue(stateProcessor.value!!.chatItems.contains(NewMessagesDividerItem))

        subjectUnderTest.markMessagesReadWithDelay()

        verify(subjectUnderTest).removeNewMessagesDivider(any())
        assertFalse(stateProcessor.value!!.chatItems.contains(NewMessagesDividerItem))
        verify(compositeDisposable).add(any())
        verify(stateProcessor, times(2)).onNext(any())
    }

    @Test
    fun `mapInQueue adds OperatorStatusItem_InQueue to chatItems and updates operatorStatusItem`() {
        val companyName = "company Name"

        val newState = subjectUnderTest.mapInQueue(companyName, state)
        val lastItem = newState.chatItems.last() as OperatorStatusItem.InQueue

        assertEquals(companyName, lastItem.companyName)
        assertEquals(companyName, newState.operatorStatusItem!!.companyName)
        assertEquals(lastItem, newState.operatorStatusItem)
    }

    @Test
    fun `mapTransferring removes old OperatorStatusItem when it exists and adds new one`() {
        subjectUnderTest.mapTransferring(subjectUnderTest.mapInQueue("name", state)).apply {
            assertTrue(chatItems.count() == 1)
            assertTrue(chatItems.contains(OperatorStatusItem.Transferring))
        }
    }

    @Test
    fun `mapOperatorConnected adds OperatorStatusItem_Connected when the old is null`() {
        val action = ChatManager.Action.OperatorConnected("c_name", "o_name", "o_image")
        subjectUnderTest.mapOperatorConnected(action, state)

        verify(subjectUnderTest).checkUnsentMessages(any())
        val newItem = state.chatItems.last() as OperatorStatusItem.Connected
        assertEquals(newItem.operatorName, action.operatorFormattedName)
        assertEquals(newItem.companyName, action.companyName)
        assertEquals(newItem.profileImgUrl, action.operatorImageUrl)
    }

    @Test
    fun `mapOperatorConnected replaces old OperatorStatusItem when the old is exists`() {
        val action = ChatManager.Action.OperatorConnected("c_name", "o_name", "o_image")

        state.apply {
            operatorStatusItem = OperatorStatusItem.Transferring
            chatItems.add(mock())
            chatItems.add(mock())
            chatItems.add(OperatorStatusItem.Transferring)
        }

        val newState = subjectUnderTest.mapOperatorConnected(action, state)

        verify(subjectUnderTest).checkUnsentMessages(any())
        val newItem = newState.chatItems.last() as OperatorStatusItem.Connected
        assertEquals(newItem.operatorName, action.operatorFormattedName)
        assertEquals(newItem.companyName, action.companyName)
        assertEquals(newItem.profileImgUrl, action.operatorImageUrl)
        assertFalse(newState.chatItems.contains(OperatorStatusItem.Transferring))
    }

    @Test
    fun `addUnsentMessage adds Unsent message before OperatorStatusItem when chatItems contain OperatorStatusItem_InQueue`() {
        val message: Unsent.Message = Unsent.Message(message = "message")

        val inQueue = OperatorStatusItem.InQueue("company_name")


        assertTrue(state.unsentItems.isEmpty())
        assertTrue(state.chatItems.isEmpty())

        val newState = subjectUnderTest.addUnsentMessage(message, state)

        assertTrue(newState.unsentItems.count() == 1)
        assertTrue(newState.chatItems.count() == 1)
        assertEquals(newState.unsentItems.last().chatMessage, newState.chatItems.last())

        newState.chatItems.add(inQueue)

        subjectUnderTest.addUnsentMessage(message, state).apply {
            assertTrue(unsentItems.count() == 2)
            assertTrue(chatItems.count() == 3)
            assertEquals(unsentItems.last().chatMessage, chatItems[1])
            assertEquals(chatItems.last(), inQueue)
        }
    }

    @Test
    fun `mapResponseCardClicked converts ResponseCard to PlainText when it clicked`() {
        val responseCard: OperatorMessageItem.ResponseCard = mock {
            on { asPlainText() } doReturn mock()
        }
        state.chatItems.add(responseCard)

        subjectUnderTest.mapResponseCardClicked(responseCard, state).apply {
            assertTrue(chatItems.count() == 1)
            assertTrue(chatItems.last() is OperatorMessageItem.PlainText)
        }
    }

    @Test
    fun `mapOperatorJoined adds OperatorStatusItem_Joined to chatItems when called`() {
        val action: ChatManager.Action.OperatorJoined = ChatManager.Action.OperatorJoined("", "", "")

        subjectUnderTest.mapOperatorJoined(action, state).apply {
            assertTrue(chatItems.count() == 1)
            assertTrue(chatItems.last() is OperatorStatusItem.Joined)
        }
    }

    @Test
    fun `mapMediaUpgrade adds MediaUpgradeStartedTimerItem_Video to chatItems when video is true`() {
        subjectUnderTest.mapMediaUpgrade(true, state).apply {
            assertTrue(chatItems.count() == 1)
            assertTrue(chatItems.last() is MediaUpgradeStartedTimerItem.Video)
            assertNotNull(mediaUpgradeTimerItem)
        }
    }

    @Test
    fun `mapMediaUpgrade adds MediaUpgradeStartedTimerItem_Audio to chatItems when video is false`() {
        subjectUnderTest.mapMediaUpgrade(false, state).apply {
            assertTrue(chatItems.count() == 1)
            assertTrue(chatItems.last() is MediaUpgradeStartedTimerItem.Audio)
            assertNotNull(mediaUpgradeTimerItem)
        }
    }

    @Test
    fun `mapUpgradeMediaToVideo removes old MediaUpgradeItem when it exists`() {
        val time = "10"
        val oldItem: MediaUpgradeStartedTimerItem.Audio = MediaUpgradeStartedTimerItem.Audio(time)
        state.mediaUpgradeTimerItem = oldItem
        state.chatItems.add(oldItem)

        subjectUnderTest.mapUpgradeMediaToVideo(state).apply {
            assertTrue(chatItems.count() == 1)
            val newItem = chatItems.last() as MediaUpgradeStartedTimerItem.Video
            assertEquals(time, newItem.time)
            assertEquals(newItem, mediaUpgradeTimerItem)
        }
    }

    @Test
    fun `mapMediaUpgradeCanceled removes old MediaUpgradeItem when it exists`() {
        val time = "10"
        val oldItem: MediaUpgradeStartedTimerItem.Audio = MediaUpgradeStartedTimerItem.Audio(time)
        state.mediaUpgradeTimerItem = oldItem
        state.chatItems.add(oldItem)

        subjectUnderTest.mapMediaUpgradeCanceled(state).apply {
            assertTrue(chatItems.isEmpty())
            assertNull(mediaUpgradeTimerItem)
        }
    }

    @Test
    fun `mapMediaUpgradeTimerUpdated updates old MediaUpgradeItem when it called with new time`() {
        val oldItem: MediaUpgradeStartedTimerItem.Audio = MediaUpgradeStartedTimerItem.Audio("10")
        state.mediaUpgradeTimerItem = oldItem
        state.chatItems.add(oldItem)

        subjectUnderTest.mapMediaUpgradeTimerUpdated("11", state).apply {
            assertTrue(chatItems.isNotEmpty())
            assertEquals(mediaUpgradeTimerItem!!.time, "11")
            assertEquals((chatItems.last() as MediaUpgradeStartedTimerItem.Audio).time, "11")
        }
    }

    @Test
    fun `mapMessageSent adds new Message`() {
        val action: ChatManager.Action.MessageSent = mock {
            on { message } doReturn mock()
        }

        subjectUnderTest.mapMessageSent(action.message, state)
        verify(subjectUnderTest).mapNewMessage(any(), eq(state))
    }

    @Test
    fun `mapCustomCardClicked updates Custom Card`() {
        val action: ChatManager.Action.CustomCardClicked = mock {
            on { customCard } doReturn mock()
            on { attachment } doReturn mock()
        }

        subjectUnderTest.mapCustomCardClicked(action, state)
        verify(handleCustomCardClickUseCase).invoke(any(), any(), any())
    }

    @Test
    fun `mapAction calls mapInQueue when Action_QueuingStarted passed`() {
        val action: ChatManager.Action.QueuingStarted = mock {
            on { companyName } doReturn ""
        }

        subjectUnderTest.mapAction(action, state)
        verify(subjectUnderTest).mapInQueue(any(), any())
    }

    @Test
    fun `mapAction calls mapOperatorConnected when Action_OperatorConnected passed`() {
        val action: ChatManager.Action.OperatorConnected = mock {
            on { companyName } doReturn ""
            on { operatorFormattedName } doReturn ""
        }

        subjectUnderTest.mapAction(action, state)
        verify(subjectUnderTest).mapOperatorConnected(any(), any())
    }

    @Test
    fun `mapAction calls mapTransferring when Action_Transferring passed`() {
        val action: ChatManager.Action.Transferring = ChatManager.Action.Transferring

        subjectUnderTest.mapAction(action, state)
        verify(subjectUnderTest).mapTransferring(any())
    }

    @Test
    fun `mapAction calls mapOperatorJoined when Action_OperatorJoined passed`() {
        val action: ChatManager.Action.OperatorJoined = mock {
            on { companyName } doReturn ""
            on { operatorFormattedName } doReturn ""
        }

        subjectUnderTest.mapAction(action, state)
        verify(subjectUnderTest).mapOperatorJoined(any(), any())
    }

    @Test
    fun `mapAction calls addUnsentMessage when Action_UnsentMessageReceived passed`() {
        val action: ChatManager.Action.UnsentMessageReceived = mock {
            on { message } doReturn mock()
        }

        subjectUnderTest.mapAction(action, state)
        verify(subjectUnderTest).addUnsentMessage(any(), any())
    }

    @Test
    fun `mapAction calls mapResponseCardClicked when Action_ResponseCardClicked passed`() {
        val action: ChatManager.Action.ResponseCardClicked = mock {
            on { responseCard } doReturn mock()
        }
        state.chatItems.add(action.responseCard)

        subjectUnderTest.mapAction(action, state)
        verify(subjectUnderTest).mapResponseCardClicked(any(), any())
    }

    @Test
    fun `mapAction calls mapMediaUpgrade when Action_OnMediaUpgradeStarted passed`() {
        val action: ChatManager.Action.OnMediaUpgradeStarted = mock {
            on { isVideo } doReturn true
        }

        subjectUnderTest.mapAction(action, state)
        verify(subjectUnderTest).mapMediaUpgrade(any(), any())
    }

    @Test
    fun `mapAction calls mapUpgradeMediaToVideo when Action_OnMediaUpgradeToVideo passed`() {
        val action: ChatManager.Action.OnMediaUpgradeToVideo = ChatManager.Action.OnMediaUpgradeToVideo

        subjectUnderTest.mapAction(action, state)
        verify(subjectUnderTest).mapUpgradeMediaToVideo(any())
    }

    @Test
    fun `mapAction calls mapMediaUpgradeCanceled when Action_OnMediaUpgradeCanceled passed`() {
        val action: ChatManager.Action.OnMediaUpgradeCanceled = ChatManager.Action.OnMediaUpgradeCanceled

        subjectUnderTest.mapAction(action, state)
        verify(subjectUnderTest).mapMediaUpgradeCanceled(any())
    }

    @Test
    fun `mapAction calls mapMediaUpgradeTimerUpgraded when Action_OnMediaUpgradeTimerUpdated passed`() {
        val action: ChatManager.Action.OnMediaUpgradeTimerUpdated = mock {
            on { formattedValue } doReturn "10"
        }

        subjectUnderTest.mapAction(action, state)
        verify(subjectUnderTest).mapMediaUpgradeTimerUpdated(any(), any())
    }

    @Test
    fun `mapAction calls mapCustomCardClicked when Action_CustomCardClicked passed`() {
        val action: ChatManager.Action.CustomCardClicked = mock {
            on { customCard } doReturn mock()
            on { attachment } doReturn mock()
        }

        subjectUnderTest.mapAction(action, state)
        verify(subjectUnderTest).mapCustomCardClicked(any(), any())
    }

    @Test
    fun `mapAction returns same state when Action_ChatRestored passed`() {
        val action: ChatManager.Action.ChatRestored = ChatManager.Action.ChatRestored

        assertEquals(state, subjectUnderTest.mapAction(action, state))
    }

    @Test
    fun `mapNewMessage does nothing when message is not new`() {
        val chatMessageInternal = mockChatMessage<OperatorMessage>()
        doReturn(false).whenever(state).isNew(chatMessageInternal)

        subjectUnderTest.mapNewMessage(chatMessageInternal, state)

        verify(appendNewChatMessageUseCase, never()).invoke(any(), any())
        verify(subjectUnderTest, never()).checkUnsentMessages(any())
    }

    @Test
    fun `mapNewMessage calls appendNewChatMessageUseCase when message is new`() {
        val chatMessageInternal = mockChatMessage<OperatorMessage>()
        doReturn(true).whenever(state).isNew(chatMessageInternal)

        subjectUnderTest.mapNewMessage(chatMessageInternal, state)

        verify(appendNewChatMessageUseCase).invoke(any(), any())
        verify(subjectUnderTest, never()).checkUnsentMessages(any())
    }

    @Test
    fun `mapNewMessage calls checkUnsentMessage when message is new VisitorMessage`() {
        val chatMessageInternal = mockChatMessage<VisitorMessage>()
        doReturn(true).whenever(state).isNew(chatMessageInternal)

        subjectUnderTest.mapNewMessage(chatMessageInternal, state)

        verify(appendNewChatMessageUseCase).invoke(any(), any())
        verify(subjectUnderTest).checkUnsentMessages(any())
    }

    @Test
    fun `mapChatHistory calls appendHistoryChatMessageUseCase when response contains messages`() {
        val chatHistoryResponse: ChatHistoryResponse = mock()
        whenever(chatHistoryResponse.newMessagesCount) doReturn 1
        val visitorMessage = mockChatMessage<VisitorMessage>()
        val operatorMessage = mockChatMessage<OperatorMessage>()

        whenever(chatHistoryResponse.items) doReturn listOf(visitorMessage, operatorMessage)

        whenever(addNewMessagesDividerUseCase(any(), any())) doReturn true
        whenever(markMessagesReadWithDelayUseCase()) doReturn Completable.complete()

        val newState = subjectUnderTest.mapChatHistory(chatHistoryResponse)

        verify(appendHistoryChatMessageUseCase).invoke(any(), any(), eq(true))
        verify(appendHistoryChatMessageUseCase).invoke(any(), any(), eq(false))

        assertTrue(newState.chatItemIds.count() == 2)
        verify(subjectUnderTest).markMessagesReadWithDelay()
    }

    @Test
    fun `checkUnsentMessages calls sendUnsentMessagesUseCase when unsent messages list is not empty`() {
        val mockUnsentMessage: Unsent.Message = mock {
            on { message } doReturn "message"
        }

        whenever(sendUnsentMessagesUseCase(any(), any())).thenAnswer {
            (it.getArgument(1) as ((VisitorMessage) -> Unit)).invoke(mock())
        }

        state.unsentItems.add(mockUnsentMessage)

        subjectUnderTest.checkUnsentMessages(state)
        verify(sendUnsentMessagesUseCase).invoke(any(), any())
        verify(subjectUnderTest).onChatAction(any())
    }

    @Test
    fun `onAction triggers mapAction when new action received`() {
        stateProcessor.onNext(state)
        val onActionFlowable = subjectUnderTest.onAction().test()
        onActionFlowable.assertNoValues()

        action.onNext(ChatManager.Action.ChatRestored)
        onActionFlowable.assertValue(state)
        verify(subjectUnderTest).mapAction(any(), any())
    }

    @Test
    fun `mapNewMessage triggers mapNewMessage when new message received`() {
        stateProcessor.onNext(state)
        val messageProcessor: BehaviorProcessor<ChatMessageInternal> = BehaviorProcessor.create()
        whenever(onMessageUseCase()) doReturn messageProcessor.share().toObservable()

        val onMessageFlowable = subjectUnderTest.onMessage().test()
        onMessageFlowable.assertNoValues()

        messageProcessor.onNext(mockChatMessage<OperatorMessage>())
        verify(subjectUnderTest).mapNewMessage(any(), any())
    }

    @Test
    fun `updateQuickReplies triggers quickReplies onNext when the last chat item is GvaQuickReplies`() {
        val quickRepliesTest = quickReplies.test()
        val mockOptions: List<GvaButton> = listOf(mock())
        val quickReplies: GvaQuickReplies = mock {
            on { options } doReturn mockOptions
        }
        state.chatItems.add(quickReplies)

        subjectUnderTest.updateQuickReplies(state)
        quickRepliesTest.assertValue(mockOptions)
    }

    @Test
    fun `subscribeToQuickReplies subscribes to quickReplies`() {
        val testSubscriber = quickReplies.test()
        val callback: (List<GvaButton>) -> Unit = mock()
        subjectUnderTest.subscribeToQuickReplies(callback)
        testSubscriber.assertSubscribed()

        quickReplies.onNext(emptyList())
        verify(callback).invoke(any())
    }

    @Test
    fun `loadHistory triggers mapChatHistory when history response receives`() {
        whenever(loadHistoryUseCase()) doReturn Single.just(mock())
        val testFlowable = subjectUnderTest.loadHistory { }.test()
        verify(subjectUnderTest).mapChatHistory(any(), isNull())
        assertTrue(testFlowable.valueCount() == 1)
    }

    @Test
    fun `subscribeToState subscribes to history and messages`() {
        val chatMessageInternal = mockChatMessage<SystemMessage>()
        doReturn(true).whenever(state).isNew(chatMessageInternal)
        doReturn(10).whenever(state).addedMessagesCount
        stateProcessor.onNext(state)

        whenever(loadHistoryUseCase()) doReturn Single.just(mock())
        whenever(onMessageUseCase()) doReturn Observable.just(chatMessageInternal)

        subjectUnderTest.subscribeToState(mock(), mock())
        verify(subjectUnderTest).loadHistory(any())
        verify(subjectUnderTest).subscribeToMessages(any())
    }

    @Test
    fun `onChatAction emits action to observable`() {
        val test = action.test()
        subjectUnderTest.onChatAction(ChatManager.Action.Transferring)
        test.assertValue(ChatManager.Action.Transferring)
    }

    @Test
    fun `reset clears state`() {
        stateProcessor.onNext(ChatManager.State(addedMessagesCount = 10))
        quickReplies.onNext(listOf(mock()))

        subjectUnderTest.reset()

        verify(compositeDisposable).clear()
        assertEquals(stateProcessor.value, ChatManager.State())
        assertEquals(quickReplies.value, emptyList<GvaButton>())
    }

    @Test
    fun `subscribe subscribes to state and quick replies`() {
        whenever(loadHistoryUseCase()) doReturn Single.just(mock())
        whenever(onMessageUseCase()) doReturn Observable.just(mock())

        subjectUnderTest.apply {
            subscribe({ }, { }, { })
            verify(this).subscribeToState(any(), any())
            verify(this).subscribeToQuickReplies(any())
        }
    }

    @Test
    fun `initialize subscribes to state and quick replies`() {
        val chatMessageInternal = mockChatMessage<SystemMessage>()
        doReturn(true).whenever(state).isNew(chatMessageInternal)

        whenever(loadHistoryUseCase()) doReturn Single.just(mock())
        whenever(onMessageUseCase()) doReturn Observable.just(chatMessageInternal)

        val chatItems = mutableListOf<ChatItem>(mock())
        subjectUnderTest.apply {
            val onHistoryLoaded = mock<(hasHistory: Boolean) -> Unit>()
            val onQuickReplyReceived = mock<(List<GvaButton>) -> Unit>()
            val onOperatorMessageReceived = mock<(count: Int) -> Unit>()
            val itemsFlowable = initialize(onHistoryLoaded, onQuickReplyReceived, onOperatorMessageReceived)
            verify(this).subscribe(onHistoryLoaded, onOperatorMessageReceived, onQuickReplyReceived)

            val test = itemsFlowable.test()

            stateProcessor.onNext(ChatManager.State(chatItems = chatItems))

            verify(this, atLeastOnce()).updateQuickReplies(any())

            assertEquals(test.values().last().last(), chatItems.last())
        }
    }

    @Test
    fun `reloadHistoryIfNeeded does nothing when authenticated`() {
        whenever(isAuthenticatedUseCase()) doReturn true
        subjectUnderTest.reloadHistoryIfNeeded()
        verify(loadHistoryUseCase, never()).invoke()
    }

    @Test
    fun `reloadHistoryIfNeeded reloads history when not authenticated`() {
        whenever(isAuthenticatedUseCase()) doReturn false
        whenever(loadHistoryUseCase()) doReturn Single.just(mock())
        subjectUnderTest.reloadHistoryIfNeeded()
        verify(loadHistoryUseCase).invoke()
    }

    private inline fun <reified T : ChatMessage> mockChatMessage(): ChatMessageInternal {
        val chatMessageInternal: ChatMessageInternal = mock()
        val chatMessage = mock<T>()
        whenever(chatMessage.id) doReturn UUID.randomUUID().toString()
        whenever(chatMessage.content) doReturn UUID.randomUUID().toString()
        whenever(chatMessage.timestamp) doReturn 100
        whenever(chatMessageInternal.chatMessage) doReturn chatMessage
        return chatMessageInternal
    }

}
