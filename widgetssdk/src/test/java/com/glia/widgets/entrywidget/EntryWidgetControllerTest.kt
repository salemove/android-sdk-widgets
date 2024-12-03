package com.glia.widgets.entrywidget

import android.COMMON_EXTENSIONS_CLASS_PATH
import android.app.Activity
import com.glia.androidsdk.Engagement
import com.glia.androidsdk.queuing.Queue
import com.glia.widgets.chat.domain.IsAuthenticatedUseCase
import com.glia.widgets.core.queue.QueueRepository
import com.glia.widgets.core.queue.QueuesState
import com.glia.widgets.core.secureconversations.SecureConversationsRepository
import com.glia.widgets.core.secureconversations.domain.HasOngoingSecureConversationUseCase
import com.glia.widgets.di.GliaCore
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.mediaTypes
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
import org.junit.Before
import org.junit.Test

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
        listOf(EntryWidgetContract.ItemType.EmptyState)
    }

    private val sdkNotInitializedState by lazy {
        listOf(EntryWidgetContract.ItemType.SdkNotInitializedState)
    }

    private val poweredByItem by lazy { EntryWidgetContract.ItemType.PoweredBy }

    private lateinit var queueRepository: QueueRepository
    private lateinit var isAuthenticatedUseCase: IsAuthenticatedUseCase
    private lateinit var secureConversationsRepository: SecureConversationsRepository
    private lateinit var hasOngoingSecureConversationUseCase: HasOngoingSecureConversationUseCase
    private lateinit var core: GliaCore
    private lateinit var controller: EntryWidgetController
    private lateinit var view: EntryWidgetContract.View
    private lateinit var activity: Activity
    private lateinit var engagementLauncher: EngagementLauncher
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
        core = mockk()
        activity = mockk()
        view = mockk(relaxUnitFun = true)
        engagementLauncher = mockk(relaxUnitFun = true)

        controller = EntryWidgetController(
            queueRepository,
            isAuthenticatedUseCase,
            secureConversationsRepository,
            hasOngoingSecureConversationUseCase,
            core,
            engagementLauncher,
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
        whiteLabel: Boolean = false
    ) {
        every { core.isInitialized } returns coreInitialized
        every { queueRepository.queuesState } returns (queuesState?.let { Flowable.just(it) } ?: Flowable.never())
        every { secureConversationsRepository.unreadMessagesCountObservable } returns (unreadMessagesCount?.let { Flowable.just(it) }
            ?: Flowable.never())
        every { hasOngoingSecureConversationUseCase() } returns (hasOngoingSc?.let { Flowable.just(it) } ?: Flowable.never())
        every { isAuthenticatedUseCase() } returns isAuthenticated
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
        every { any<List<Queue>>().mediaTypes } returns listOf(Engagement.MediaType.MESSAGING)

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
            Engagement.MediaType.TEXT,
            Engagement.MediaType.MESSAGING,
            Engagement.MediaType.AUDIO,
            Engagement.MediaType.VIDEO
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
    fun `showItems is called with Messaging item when state is Empty but has ongoing SC and view type is not Messaging Live Support`() {
        val unreadMessagesCount = 2

        mockInputData(
            coreInitialized = true,
            queuesState = QueuesState.Empty,
            unreadMessagesCount = unreadMessagesCount,
            hasOngoingSc = true,
            isAuthenticated = true
        )

        controller.setView(view, EntryWidgetContract.ViewType.EMBEDDED_VIEW)

        val messaging = listOf(
            EntryWidgetContract.ItemType.Messaging(unreadMessagesCount),
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
    fun `showItems is called with Messaging item when state is Loading but has ongoing SC and view type is not Messaging Live Support`() {
        val unreadMessagesCount = 2

        mockInputData(
            coreInitialized = true,
            queuesState = QueuesState.Loading,
            unreadMessagesCount = unreadMessagesCount,
            hasOngoingSc = true,
            isAuthenticated = true
        )

        controller.setView(view, EntryWidgetContract.ViewType.EMBEDDED_VIEW)

        val messaging = listOf(
            EntryWidgetContract.ItemType.Messaging(unreadMessagesCount),
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

        verify { view.showItems(emptyState) }
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

        verify { view.showItems(emptyState + poweredByItem) }
        verify { queueRepository.queuesState }
        verify { secureConversationsRepository.unreadMessagesCountObservable }
        verify { hasOngoingSecureConversationUseCase.invoke() }
    }

    @Test
    fun `showItems is called with Messaging item when state is Error but has ongoing SC and view type is not Messaging Live Support`() {
        val unreadMessagesCount = 2

        mockInputData(
            coreInitialized = true,
            queuesState = QueuesState.Error(mockk()),
            unreadMessagesCount = unreadMessagesCount,
            hasOngoingSc = true,
            isAuthenticated = true
        )

        controller.setView(view, EntryWidgetContract.ViewType.EMBEDDED_VIEW)

        val messaging = listOf(
            EntryWidgetContract.ItemType.Messaging(unreadMessagesCount),
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
            Engagement.MediaType.TEXT,
            Engagement.MediaType.MESSAGING,
            Engagement.MediaType.AUDIO,
            Engagement.MediaType.VIDEO
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
            Engagement.MediaType.TEXT,
            Engagement.MediaType.AUDIO,
            Engagement.MediaType.VIDEO
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
            Engagement.MediaType.TEXT,
            Engagement.MediaType.AUDIO,
            Engagement.MediaType.MESSAGING,
            Engagement.MediaType.VIDEO
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
