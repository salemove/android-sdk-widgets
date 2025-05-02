package com.glia.widgets.entrywidget

import android.COMMON_EXTENSIONS_CLASS_PATH
import android.app.Activity
import com.glia.androidsdk.Engagement
import com.glia.androidsdk.engagement.EngagementState
import com.glia.widgets.chat.domain.IsAuthenticatedUseCase
import com.glia.widgets.engagement.MediaType
import com.glia.widgets.queue.Queue
import com.glia.widgets.core.queue.QueueRepository
import com.glia.widgets.core.queue.QueuesState
import com.glia.widgets.core.secureconversations.SecureConversationsRepository
import com.glia.widgets.core.secureconversations.domain.HasOngoingSecureConversationUseCase
import com.glia.widgets.di.GliaCore
import com.glia.widgets.engagement.EndedBy
import com.glia.widgets.engagement.EngagementUpdateState
import com.glia.widgets.engagement.State
import com.glia.widgets.engagement.domain.EngagementStateUseCase
import com.glia.widgets.engagement.domain.EngagementTypeUseCase
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.mediaTypes
import com.glia.widgets.launcher.ActivityLauncher
import com.glia.widgets.launcher.EngagementLauncher
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.spyk
import io.mockk.unmockkStatic
import io.mockk.verify
import io.reactivex.rxjava3.android.plugins.RxAndroidPlugins
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock

class EntryWidgetControllerTest {

    private val loadingState by lazy {
        listOf(
            EntryWidgetContract.ItemType.LoadingState,
            EntryWidgetContract.ItemType.LoadingState,
            EntryWidgetContract.ItemType.LoadingState,
            EntryWidgetContract.ItemType.LoadingState
        )
    }

    private val emptyState by lazy {
        listOf(EntryWidgetContract.ItemType.EmptyState)
    }

    private val errorState by lazy {
        listOf(EntryWidgetContract.ItemType.ErrorState)
    }

    private val sdkNotInitializedState by lazy {
        listOf(EntryWidgetContract.ItemType.SdkNotInitializedState)
    }

    private val poweredByItem by lazy { EntryWidgetContract.ItemType.PoweredBy }

    private lateinit var queueRepository: QueueRepository
    private lateinit var isAuthenticatedUseCase: IsAuthenticatedUseCase
    private lateinit var secureConversationsRepository: SecureConversationsRepository
    private lateinit var hasOngoingSecureConversationUseCase: HasOngoingSecureConversationUseCase
    private lateinit var engagementStateUseCase: EngagementStateUseCase
    private lateinit var engagementTypeUseCase: EngagementTypeUseCase
    private lateinit var core: GliaCore
    private lateinit var controller: EntryWidgetController
    private lateinit var view: EntryWidgetContract.View
    private lateinit var activity: Activity
    private lateinit var engagementLauncher: EngagementLauncher
    private lateinit var activityLauncher: ActivityLauncher
    private lateinit var disposable: CompositeDisposable

    @Before
    fun setUp() {
        mockkStatic(COMMON_EXTENSIONS_CLASS_PATH)
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }

        Logger.setIsDebug(false)

        disposable = spyk(CompositeDisposable())

        queueRepository = mockk(relaxUnitFun = true)
        isAuthenticatedUseCase = mockk()
        secureConversationsRepository = mockk()
        hasOngoingSecureConversationUseCase = mockk()
        engagementStateUseCase = mockk()
        engagementTypeUseCase = mockk()
        core = mockk()
        activity = mockk()
        view = mockk(relaxUnitFun = true)
        engagementLauncher = mockk(relaxUnitFun = true)
        activityLauncher = mockk(relaxUnitFun = true)

        controller = EntryWidgetController(
            queueRepository,
            isAuthenticatedUseCase,
            secureConversationsRepository,
            hasOngoingSecureConversationUseCase,
            engagementStateUseCase,
            engagementTypeUseCase,
            core,
            engagementLauncher,
            activityLauncher,
            disposable
        )
    }

    @After
    fun tearDown() {
        controller.onDestroy()
        verify { disposable.dispose() }
        RxAndroidPlugins.reset()
        unmockkStatic(COMMON_EXTENSIONS_CLASS_PATH)
    }

    private fun mockInputData(
        coreInitialized: Boolean = false,
        queuesState: QueuesState? = null,
        unreadMessagesCount: Int? = null,
        hasOngoingSc: Boolean? = null,
        isAuthenticated: Boolean = false,
        whiteLabel: Boolean = false,
        engagementState: State = State.NoEngagement,
        engagementType: MediaType = MediaType.UNKNOWN
    ) {
        every { core.isInitialized } returns coreInitialized
        every { queueRepository.queuesState } returns (queuesState?.let { Flowable.just(it) } ?: Flowable.never())
        every { secureConversationsRepository.unreadMessagesCountObservable } returns (unreadMessagesCount?.let { Flowable.just(it) }
            ?: Flowable.never())
        every { hasOngoingSecureConversationUseCase() } returns (hasOngoingSc?.let { Flowable.just(it) } ?: Flowable.never())
        every { isAuthenticatedUseCase() } returns isAuthenticated
        every { engagementStateUseCase() } returns Flowable.just(engagementState)
        every { engagementTypeUseCase() } returns Flowable.just(engagementType)
        every { view.whiteLabel } returns whiteLabel
    }

    @Test
    fun `showItems is called with ERROR_STATE if SDK is not initialized and view type is not Messaging Live Support`() {
        mockInputData()

        controller.setView(view, EntryWidgetContract.ViewType.BOTTOM_SHEET)

        verify { view.showItems(sdkNotInitializedState + poweredByItem) }
        verify { queueRepository.queuesState }
        verify { secureConversationsRepository.unreadMessagesCountObservable }
        verify { hasOngoingSecureConversationUseCase.invoke() }
    }

    @Test
    fun `showItems is called with ERROR_STATE if SDK is not initialized, whiteLabel and view type is not Messaging Live Support`() {
        mockInputData(whiteLabel = true)

        controller.setView(view, EntryWidgetContract.ViewType.BOTTOM_SHEET)

        verify { view.showItems(sdkNotInitializedState) }
        verify { queueRepository.queuesState }
        verify { secureConversationsRepository.unreadMessagesCountObservable }
        verify { hasOngoingSecureConversationUseCase.invoke() }
    }

    @Test
    fun `showItems is called with ERROR_STATE if SDK is not initialized and view type is Messaging Live Support`() {
        mockInputData()

        controller.setView(view, EntryWidgetContract.ViewType.MESSAGING_LIVE_SUPPORT)

        verify { view.showItems(sdkNotInitializedState) }
        verify { queueRepository.queuesState }
        verify(exactly = 0) { secureConversationsRepository.unreadMessagesCountObservable }
        verify(exactly = 0) { hasOngoingSecureConversationUseCase.invoke() }
    }

    @Test
    fun `showItems is called with Empty item when state is Empty and view type is Messaging Live Support`() {
        mockInputData(coreInitialized = true, queuesState = QueuesState.Empty, unreadMessagesCount = 0, hasOngoingSc = false, isAuthenticated = true)

        controller.setView(view, EntryWidgetContract.ViewType.MESSAGING_LIVE_SUPPORT)

        verify { view.showItems(emptyState) }
        verify { queueRepository.queuesState }
        verify(exactly = 0) { secureConversationsRepository.unreadMessagesCountObservable }
        verify(exactly = 0) { hasOngoingSecureConversationUseCase.invoke() }
    }

    @Test
    fun `showItems is called with Loading items when state is Loading and view type is Messaging Live Support`() {
        mockInputData(
            coreInitialized = true,
            queuesState = QueuesState.Loading,
            unreadMessagesCount = 0,
            hasOngoingSc = false,
            isAuthenticated = true
        )

        controller.setView(view, EntryWidgetContract.ViewType.MESSAGING_LIVE_SUPPORT)

        verify { view.showItems(loadingState) }
        verify { queueRepository.queuesState }
        verify(exactly = 0) { secureConversationsRepository.unreadMessagesCountObservable }
        verify(exactly = 0) { hasOngoingSecureConversationUseCase.invoke() }
    }

    @Test
    fun `showItems is called with Error item when state is Error and view type is Messaging Live Support`() {
        mockInputData(
            coreInitialized = true,
            queuesState = QueuesState.Error(mockk()),
            unreadMessagesCount = 0,
            hasOngoingSc = false,
            isAuthenticated = true
        )

        controller.setView(view, EntryWidgetContract.ViewType.MESSAGING_LIVE_SUPPORT)

        verify { view.showItems(errorState) }
        verify { queueRepository.queuesState }
        verify(exactly = 0) { secureConversationsRepository.unreadMessagesCountObservable }
        verify(exactly = 0) { hasOngoingSecureConversationUseCase.invoke() }
    }

    @Test
    fun `showItems is called with empty item when media types are unwanted and view type is Messaging Live Support`() {
        every { any<List<Queue>>().mediaTypes } returns listOf(MediaType.MESSAGING)

        mockInputData(
            coreInitialized = true,
            queuesState = QueuesState.Queues(mockk()),
            unreadMessagesCount = 0,
            hasOngoingSc = false,
            isAuthenticated = true
        )

        controller.setView(view, EntryWidgetContract.ViewType.MESSAGING_LIVE_SUPPORT)

        verify { view.showItems(emptyState) }
        verify { queueRepository.queuesState }
        verify(exactly = 0) { secureConversationsRepository.unreadMessagesCountObservable }
        verify(exactly = 0) { hasOngoingSecureConversationUseCase.invoke() }
    }

    @Test
    fun `showItems is called with properly ordered items when view type is Messaging Live Support`() {
        every { any<List<Queue>>().mediaTypes } returns listOf(
            MediaType.TEXT,
            MediaType.MESSAGING,
            MediaType.AUDIO,
            MediaType.VIDEO
        )

        mockInputData(
            coreInitialized = true,
            queuesState = QueuesState.Queues(mockk()),
            unreadMessagesCount = 0,
            hasOngoingSc = false,
            isAuthenticated = true
        )

        controller.setView(view, EntryWidgetContract.ViewType.MESSAGING_LIVE_SUPPORT)

        val items = listOf(
            EntryWidgetContract.ItemType.VideoCall,
            EntryWidgetContract.ItemType.AudioCall,
            EntryWidgetContract.ItemType.Chat
        )
        verify { view.showItems(items) }
        verify { queueRepository.queuesState }
        verify(exactly = 0) { secureConversationsRepository.unreadMessagesCountObservable }
        verify(exactly = 0) { hasOngoingSecureConversationUseCase.invoke() }
    }

    @Test
    fun `showItems is called with Empty item when state is Empty and view type is not Messaging Live Support`() {
        mockInputData(coreInitialized = true, queuesState = QueuesState.Empty, unreadMessagesCount = 0, hasOngoingSc = false, isAuthenticated = true)

        controller.setView(view, EntryWidgetContract.ViewType.EMBEDDED_VIEW)

        verify { view.showItems(emptyState + poweredByItem) }
        verify { queueRepository.queuesState }
        verify { secureConversationsRepository.unreadMessagesCountObservable }
        verify { hasOngoingSecureConversationUseCase.invoke() }
    }

    @Test
    fun `showItems is called with Empty item when state is Empty, whiteLabel and view type is not Messaging Live Support`() {
        mockInputData(
            coreInitialized = true,
            queuesState = QueuesState.Empty,
            unreadMessagesCount = 0,
            hasOngoingSc = false,
            isAuthenticated = true,
            whiteLabel = true
        )

        controller.setView(view, EntryWidgetContract.ViewType.EMBEDDED_VIEW)

        verify { view.showItems(emptyState) }
        verify { queueRepository.queuesState }
        verify { secureConversationsRepository.unreadMessagesCountObservable }
        verify { hasOngoingSecureConversationUseCase.invoke() }
    }

    @Test
    fun `showItems is called with MessagingOngoing item when state is Empty but has ongoing SC and view type is not Messaging Live Support`() {
        val unreadMessagesCount = 2
        val engagementState = mock<EngagementState>()
        val engagementUpdateState = EngagementUpdateState.Ongoing(mockk())

        mockInputData(
            coreInitialized = true,
            queuesState = QueuesState.Empty,
            unreadMessagesCount = unreadMessagesCount,
            hasOngoingSc = true,
            isAuthenticated = true,
            engagementState = State.Update(engagementState, engagementUpdateState),
            engagementType = MediaType.MESSAGING,
        )

        every { engagementTypeUseCase.isCallVisualizer } returns false

        controller.setView(view, EntryWidgetContract.ViewType.EMBEDDED_VIEW)

        val messaging = listOf(
            EntryWidgetContract.ItemType.MessagingOngoing(unreadMessagesCount),
            EntryWidgetContract.ItemType.PoweredBy
        )

        verify { view.showItems(messaging) }
        verify { queueRepository.queuesState }
        verify { secureConversationsRepository.unreadMessagesCountObservable }
        verify { hasOngoingSecureConversationUseCase.invoke() }
    }

    @Test
    fun `showItems is called with Loading item when state is Loading, whitelabel and view type is not Messaging Live Support`() {
        mockInputData(
            coreInitialized = true,
            queuesState = QueuesState.Loading,
            unreadMessagesCount = 0,
            hasOngoingSc = false,
            isAuthenticated = true,
            whiteLabel = true
        )

        controller.setView(view, EntryWidgetContract.ViewType.EMBEDDED_VIEW)

        verify { view.showItems(loadingState) }
        verify { queueRepository.queuesState }
        verify { secureConversationsRepository.unreadMessagesCountObservable }
        verify { hasOngoingSecureConversationUseCase.invoke() }
    }

    @Test
    fun `showItems is called with Loading item when state is Loading and view type is not Messaging Live Support`() {
        mockInputData(
            coreInitialized = true,
            queuesState = QueuesState.Loading,
            unreadMessagesCount = 0,
            hasOngoingSc = false,
            isAuthenticated = true
        )

        controller.setView(view, EntryWidgetContract.ViewType.EMBEDDED_VIEW)

        verify { view.showItems(loadingState + poweredByItem) }
        verify { queueRepository.queuesState }
        verify { secureConversationsRepository.unreadMessagesCountObservable }
        verify { hasOngoingSecureConversationUseCase.invoke() }
    }

    @Test
    fun `showItems is called with MessagingOngoing item when state is Loading but has ongoing SC and view type is not Messaging Live Support`() {
        val unreadMessagesCount = 2

        mockInputData(
            coreInitialized = true,
            queuesState = QueuesState.Loading,
            unreadMessagesCount = unreadMessagesCount,
            hasOngoingSc = true,
            isAuthenticated = true,
            engagementType = MediaType.MESSAGING
        )

        controller.setView(view, EntryWidgetContract.ViewType.EMBEDDED_VIEW)

        val messaging = listOf(
            EntryWidgetContract.ItemType.MessagingOngoing(unreadMessagesCount),
            EntryWidgetContract.ItemType.PoweredBy
        )

        verify { view.showItems(messaging) }
        verify { queueRepository.queuesState }
        verify { secureConversationsRepository.unreadMessagesCountObservable }
        verify { hasOngoingSecureConversationUseCase.invoke() }
    }

    @Test
    fun `showItems is called with Error item when state is Error, whiteLabel and view type is not Messaging Live Support`() {
        mockInputData(
            coreInitialized = true,
            queuesState = QueuesState.Error(mockk()),
            unreadMessagesCount = 0,
            hasOngoingSc = false,
            isAuthenticated = true,
            whiteLabel = true
        )

        controller.setView(view, EntryWidgetContract.ViewType.EMBEDDED_VIEW)

        verify { view.showItems(errorState) }
        verify { queueRepository.queuesState }
        verify { secureConversationsRepository.unreadMessagesCountObservable }
        verify { hasOngoingSecureConversationUseCase.invoke() }
    }

    @Test
    fun `showItems is called with Error item when state is Error and view type is not Messaging Live Support`() {
        mockInputData(
            coreInitialized = true,
            queuesState = QueuesState.Error(mockk()),
            unreadMessagesCount = 0,
            hasOngoingSc = false,
            isAuthenticated = true
        )

        controller.setView(view, EntryWidgetContract.ViewType.EMBEDDED_VIEW)

        verify { view.showItems(errorState + poweredByItem) }
        verify { queueRepository.queuesState }
        verify { secureConversationsRepository.unreadMessagesCountObservable }
        verify { hasOngoingSecureConversationUseCase.invoke() }
    }

    @Test
    fun `showItems is called with MessagingOngoing item when state is Error but has ongoing SC and view type is not Messaging Live Support`() {
        val unreadMessagesCount = 2

        mockInputData(
            coreInitialized = true,
            queuesState = QueuesState.Error(mockk()),
            unreadMessagesCount = unreadMessagesCount,
            hasOngoingSc = true,
            isAuthenticated = true,
            engagementType = MediaType.MESSAGING
        )

        controller.setView(view, EntryWidgetContract.ViewType.EMBEDDED_VIEW)

        val messaging = listOf(
            EntryWidgetContract.ItemType.MessagingOngoing(unreadMessagesCount),
            EntryWidgetContract.ItemType.PoweredBy
        )

        verify { view.showItems(messaging) }
        verify { queueRepository.queuesState }
        verify { secureConversationsRepository.unreadMessagesCountObservable }
        verify { hasOngoingSecureConversationUseCase.invoke() }
    }

    @Test
    fun `showItems is called with empty item when media types are empty and view type is not Messaging Live Support`() {
        every { any<List<Queue>>().mediaTypes } returns emptyList()

        mockInputData(
            coreInitialized = true,
            queuesState = QueuesState.Queues(mockk()),
            unreadMessagesCount = 0,
            hasOngoingSc = false,
            isAuthenticated = true
        )

        controller.setView(view, EntryWidgetContract.ViewType.EMBEDDED_VIEW)

        verify { view.showItems(emptyState + poweredByItem) }
        verify { queueRepository.queuesState }
        verify { secureConversationsRepository.unreadMessagesCountObservable }
        verify { hasOngoingSecureConversationUseCase.invoke() }
    }

    @Test
    fun `showItems is called with empty item without poweredBy when media types are empty and view type is not Messaging Live Support`() {
        every { any<List<Queue>>().mediaTypes } returns emptyList()

        mockInputData(
            coreInitialized = true,
            queuesState = QueuesState.Queues(mockk()),
            unreadMessagesCount = 0,
            hasOngoingSc = false,
            isAuthenticated = true,
            whiteLabel = true
        )

        controller.setView(view, EntryWidgetContract.ViewType.EMBEDDED_VIEW)

        verify { view.showItems(emptyState) }
        verify { queueRepository.queuesState }
        verify { secureConversationsRepository.unreadMessagesCountObservable }
        verify { hasOngoingSecureConversationUseCase.invoke() }
    }

    @Test
    fun `showItems is called without messaging when not authenticated and view type is not Messaging Live Support`() {
        every { any<List<Queue>>().mediaTypes } returns listOf(
            MediaType.TEXT,
            MediaType.MESSAGING,
            MediaType.AUDIO,
            MediaType.VIDEO
        )

        mockInputData(
            coreInitialized = true,
            queuesState = QueuesState.Queues(mockk()),
            unreadMessagesCount = 0,
            hasOngoingSc = true,
            isAuthenticated = false
        )

        controller.setView(view, EntryWidgetContract.ViewType.EMBEDDED_VIEW)

        val items = listOf(
            EntryWidgetContract.ItemType.VideoCall,
            EntryWidgetContract.ItemType.AudioCall,
            EntryWidgetContract.ItemType.Chat,
            EntryWidgetContract.ItemType.PoweredBy
        )
        verify { view.showItems(items) }
        verify { queueRepository.queuesState }
        verify { secureConversationsRepository.unreadMessagesCountObservable }
        verify { hasOngoingSecureConversationUseCase.invoke() }
    }

    @Test
    fun `showItems is called with messaging when authenticated, hasOngoingSC and view type is not Messaging Live Support`() {
        every { any<List<Queue>>().mediaTypes } returns listOf(
            MediaType.TEXT,
            MediaType.AUDIO,
            MediaType.VIDEO
        )

        val unreadMessagesCount = 2
        mockInputData(
            coreInitialized = true,
            queuesState = QueuesState.Queues(mockk()),
            unreadMessagesCount = unreadMessagesCount,
            hasOngoingSc = true,
            isAuthenticated = true
        )

        controller.setView(view, EntryWidgetContract.ViewType.EMBEDDED_VIEW)

        val items = listOf(
            EntryWidgetContract.ItemType.VideoCall,
            EntryWidgetContract.ItemType.AudioCall,
            EntryWidgetContract.ItemType.Chat,
            EntryWidgetContract.ItemType.Messaging(unreadMessagesCount),
            EntryWidgetContract.ItemType.PoweredBy
        )
        verify { view.showItems(items) }
        verify { queueRepository.queuesState }
        verify { secureConversationsRepository.unreadMessagesCountObservable }
        verify { hasOngoingSecureConversationUseCase.invoke() }
    }

    @Test
    fun `showItems is called with proper order when view type is not Messaging Live Support`() {
        every { any<List<Queue>>().mediaTypes } returns listOf(
            MediaType.TEXT,
            MediaType.AUDIO,
            MediaType.MESSAGING,
            MediaType.VIDEO
        )

        val unreadMessagesCount = 2
        mockInputData(
            coreInitialized = true,
            queuesState = QueuesState.Queues(mockk()),
            unreadMessagesCount = unreadMessagesCount,
            hasOngoingSc = false,
            isAuthenticated = true,
            whiteLabel = true
        )

        controller.setView(view, EntryWidgetContract.ViewType.EMBEDDED_VIEW)

        val items = listOf(
            EntryWidgetContract.ItemType.VideoCall,
            EntryWidgetContract.ItemType.AudioCall,
            EntryWidgetContract.ItemType.Chat,
            EntryWidgetContract.ItemType.Messaging(unreadMessagesCount)
        )
        verify { view.showItems(items) }
        verify { queueRepository.queuesState }
        verify { secureConversationsRepository.unreadMessagesCountObservable }
        verify { hasOngoingSecureConversationUseCase.invoke() }
    }

    @Test
    fun `prepareItemsBasedOnOngoingEngagement is called when State Update`() {
        every { any<List<Queue>>().mediaTypes } returns listOf(
            MediaType.TEXT,
            MediaType.AUDIO,
            MediaType.MESSAGING,
            MediaType.VIDEO
        )

        val unreadMessagesCount = 2
        val hasOngoingSc = false
        val engagementType = MediaType.TEXT
        mockInputData(
            coreInitialized = true,
            queuesState = QueuesState.Queues(mockk()),
            unreadMessagesCount = unreadMessagesCount,
            hasOngoingSc = hasOngoingSc,
            isAuthenticated = false,
            whiteLabel = true,
            engagementState = State.Update(mockk(), mockk()),
            engagementType = engagementType
        )
        every { engagementTypeUseCase.isCallVisualizer } returns false

        controller.setView(view, EntryWidgetContract.ViewType.EMBEDDED_VIEW)

        verify { controller.prepareItemsBasedOnOngoingEngagement(engagementType, unreadMessagesCount, hasOngoingSc) }
    }

    @Test
    fun `prepareItemsBasedOnOngoingEngagement is called when State Queueing`() {
        val spyController = spyk(controller)
        every { any<List<Queue>>().mediaTypes } returns listOf(
            MediaType.TEXT,
            MediaType.AUDIO,
            MediaType.MESSAGING,
            MediaType.VIDEO
        )

        val unreadMessagesCount = 2
        val hasOngoingSc = false
        val engagementType = MediaType.TEXT
        mockInputData(
            coreInitialized = true,
            queuesState = QueuesState.Queues(mockk()),
            unreadMessagesCount = unreadMessagesCount,
            hasOngoingSc = hasOngoingSc,
            isAuthenticated = false,
            whiteLabel = true,
            engagementState = State.Queuing("ticket_id", engagementType),
            engagementType = engagementType
        )
        every { engagementTypeUseCase.isCallVisualizer } returns false

        spyController.setView(view, EntryWidgetContract.ViewType.EMBEDDED_VIEW)

        verify { spyController.prepareItemsBasedOnOngoingEngagement(engagementType, unreadMessagesCount, hasOngoingSc) }
    }

    @Test
    fun `prepareItemsBasedOnQueues is called when State is not Update, Queuing or PreQueuing`() {
        testPrepareItemsBasedOnQueuesCalledForEngagementState(State.NoEngagement)
        testPrepareItemsBasedOnQueuesCalledForEngagementState(State.QueueUnstaffed)
        testPrepareItemsBasedOnQueuesCalledForEngagementState(State.UnexpectedErrorHappened)
        testPrepareItemsBasedOnQueuesCalledForEngagementState(State.QueueingCanceled)
        testPrepareItemsBasedOnQueuesCalledForEngagementState(State.EngagementStarted(false))
        testPrepareItemsBasedOnQueuesCalledForEngagementState(State.EngagementStarted(true))
        testPrepareItemsBasedOnQueuesCalledForEngagementState(State.EngagementEnded(false, EndedBy.VISITOR, Engagement.ActionOnEnd.UNKNOWN, mockk()))
        testPrepareItemsBasedOnQueuesCalledForEngagementState(State.EngagementEnded(true, EndedBy.OPERATOR, Engagement.ActionOnEnd.UNKNOWN, mockk()))
        testPrepareItemsBasedOnQueuesCalledForEngagementState(State.TransferredToSecureConversation)
    }

    private fun testPrepareItemsBasedOnQueuesCalledForEngagementState(engagementState: State) {
        val engagementType = MediaType.TEXT
        val queuesState = QueuesState.Queues(mockk())
        val unreadMessagesCount = 2
        val hasOngoingSc = false
        val isViewWhiteLabel = true

        val entryWidgetController = mockk<EntryWidgetController>(relaxed = true)
        every { entryWidgetController.prepareItemsBasedOnQueues(mockk(), mockk(), mockk()) } returns mock()
        every { entryWidgetController.mapToEntryWidgetItems(engagementState, engagementType, queuesState, unreadMessagesCount, hasOngoingSc, isViewWhiteLabel) } answers {
            callOriginal()
        }

        entryWidgetController.mapToEntryWidgetItems(engagementState, engagementType, queuesState, unreadMessagesCount, hasOngoingSc, isViewWhiteLabel)

        verify { entryWidgetController.prepareItemsBasedOnQueues(queuesState, unreadMessagesCount, hasOngoingSc) }
    }

    @Test
    fun `prepareItemsBasedOnOngoingEngagement returns ChatOngoing when ongoing engagement media type is TEXT`() {
        val mediaType = MediaType.TEXT
        val unreadMessagesCount = 2
        val hasOngoingSC = false

        every { engagementTypeUseCase.isCallVisualizer } returns false
        every { isAuthenticatedUseCase() } returns false
        val expectedItem = listOf(EntryWidgetContract.ItemType.ChatOngoing)

        val actualItem = controller.prepareItemsBasedOnOngoingEngagement(mediaType, unreadMessagesCount, hasOngoingSC)

        assertEquals(expectedItem, actualItem)
    }

    @Test
    fun `prepareItemsBasedOnOngoingEngagement returns AudioCallOngoing when ongoing engagement media type is AUDIO`() {
        val mediaType = MediaType.AUDIO
        val unreadMessagesCount = 2
        val hasOngoingSC = false

        every { engagementTypeUseCase.isCallVisualizer } returns false
        every { isAuthenticatedUseCase() } returns false
        val expectedItem = listOf(EntryWidgetContract.ItemType.AudioCallOngoing)

        val actualItem = controller.prepareItemsBasedOnOngoingEngagement(mediaType, unreadMessagesCount, hasOngoingSC)

        assertEquals(expectedItem, actualItem)
    }

    @Test
    fun `prepareItemsBasedOnOngoingEngagement returns VideoCallOngoing when ongoing engagement media type is VIDEO`() {
        val mediaType = MediaType.VIDEO
        val unreadMessagesCount = 2
        val hasOngoingSC = false

        every { engagementTypeUseCase.isCallVisualizer } returns false
        every { isAuthenticatedUseCase() } returns false
        val expectedItem = listOf(EntryWidgetContract.ItemType.VideoCallOngoing)

        val actualItem = controller.prepareItemsBasedOnOngoingEngagement(mediaType, unreadMessagesCount, hasOngoingSC)

        assertEquals(expectedItem, actualItem)
    }

    @Test
    fun `prepareItemsBasedOnOngoingEngagement returns MessagingOngoing when ongoing engagement media type is MESSAGING`() {
        val mediaType = MediaType.MESSAGING
        val unreadMessagesCount = 2
        val hasOngoingSC = true

        every { engagementTypeUseCase.isCallVisualizer } returns false
        every { isAuthenticatedUseCase() } returns false
        val expectedItem = listOf(EntryWidgetContract.ItemType.MessagingOngoing(unreadMessagesCount))

        val actualItem = controller.prepareItemsBasedOnOngoingEngagement(mediaType, unreadMessagesCount, hasOngoingSC)

        assertEquals(expectedItem, actualItem)
    }

    @Test
    fun `prepareItemsBasedOnOngoingEngagement returns ChatOngoing when queueing media type is TEXT`() {
        val mediaType = MediaType.TEXT
        val unreadMessagesCount = 2
        val hasOngoingSC = false

        every { engagementTypeUseCase.isCallVisualizer } returns false
        every { isAuthenticatedUseCase() } returns false
        val expectedItem = listOf(EntryWidgetContract.ItemType.ChatOngoing)

        val actualItem = controller.prepareItemsBasedOnOngoingEngagement(mediaType, unreadMessagesCount, hasOngoingSC)

        assertEquals(expectedItem, actualItem)
    }

    @Test
    fun `prepareItemsBasedOnOngoingEngagement returns AudioCallOngoing when queueing media type is AUDIO`() {
        val mediaType = MediaType.AUDIO
        val unreadMessagesCount = 2
        val hasOngoingSC = false

        every { engagementTypeUseCase.isCallVisualizer } returns false
        every { isAuthenticatedUseCase() } returns false
        val expectedItem = listOf(EntryWidgetContract.ItemType.AudioCallOngoing)

        val actualItem = controller.prepareItemsBasedOnOngoingEngagement(mediaType, unreadMessagesCount, hasOngoingSC)

        assertEquals(expectedItem, actualItem)
    }

    @Test
    fun `prepareItemsBasedOnOngoingEngagement returns VideoCallOngoing when queueing media type is VIDEO`() {
        val mediaType = MediaType.VIDEO
        val unreadMessagesCount = 2
        val hasOngoingSC = false

        every { engagementTypeUseCase.isCallVisualizer } returns false
        every { isAuthenticatedUseCase() } returns false
        val expectedItem = listOf(EntryWidgetContract.ItemType.VideoCallOngoing)

        val actualItem = controller.prepareItemsBasedOnOngoingEngagement(mediaType, unreadMessagesCount, hasOngoingSC)

        assertEquals(expectedItem, actualItem)
    }

    @Test
    fun `prepareItemsBasedOnOngoingEngagement returns MessagingOngoing when queueing media type is MESSAGING`() {
        val mediaType = MediaType.MESSAGING
        val unreadMessagesCount = 2
        val hasOngoingSC = true

        every { engagementTypeUseCase.isCallVisualizer } returns false
        every { isAuthenticatedUseCase() } returns true
        val expectedItem = listOf(EntryWidgetContract.ItemType.MessagingOngoing(unreadMessagesCount))

        val actualItem = controller.prepareItemsBasedOnOngoingEngagement(mediaType, unreadMessagesCount, hasOngoingSC)

        assertEquals(expectedItem, actualItem)
    }

    @Test
    fun `onItemClicked calls dismiss when CHAT item clicked`() {
        mockInputData()

        controller.setView(view, EntryWidgetContract.ViewType.EMBEDDED_VIEW)

        controller.onItemClicked(EntryWidgetContract.ItemType.Chat, activity)
        verify { engagementLauncher.startChat(activity) }
        verify { view.dismiss() }
    }

    @Test
    fun `onItemClicked calls dismiss when AUDIO_CALL item clicked`() {
        mockInputData()

        controller.setView(view, EntryWidgetContract.ViewType.EMBEDDED_VIEW)

        controller.onItemClicked(EntryWidgetContract.ItemType.AudioCall, activity)
        verify { engagementLauncher.startAudioCall(activity) }
        verify { view.dismiss() }
    }

    @Test
    fun `onItemClicked calls dismiss when VIDEO_CALL item clicked`() {
        mockInputData()

        controller.setView(view, EntryWidgetContract.ViewType.EMBEDDED_VIEW)

        controller.onItemClicked(EntryWidgetContract.ItemType.VideoCall, activity)
        verify { engagementLauncher.startVideoCall(activity) }
        verify { view.dismiss() }
    }

    @Test
    fun `onItemClicked calls dismiss when SECURE_MESSAGE item clicked`() {
        mockInputData()

        controller.setView(view, EntryWidgetContract.ViewType.EMBEDDED_VIEW)

        controller.onItemClicked(EntryWidgetContract.ItemType.Messaging(0), activity)
        verify { engagementLauncher.startSecureMessaging(activity) }
        verify { view.dismiss() }
    }

    @Test
    fun `onItemClicked does not call dismiss when ERROR_STATE item clicked`() {
        controller.onItemClicked(EntryWidgetContract.ItemType.ErrorState, activity)
        verify(exactly = 0) { view.dismiss() }
        verify { queueRepository.fetchQueues() }
    }
}
